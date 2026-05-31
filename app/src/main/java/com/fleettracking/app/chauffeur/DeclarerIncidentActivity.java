package com.fleettracking.app.chauffeur;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fleettracking.app.R;
import com.fleettracking.app.data.ApiClient;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.ImageUtils;
import com.fleettracking.app.util.NotificationHelper;
import com.fleettracking.app.util.Prefs;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

public class DeclarerIncidentActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_NOTIF_PERMISSION = 103;

    private LinearLayout photosContainer;
    private Spinner spinnerType;
    private EditText inputDescription;
    private final List<String> imagesBase64 = new ArrayList<>();

    private final ActivityResultLauncher<String> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    String base64 = ImageUtils.encodeFromUri(this, uri);
                    if (base64 != null) addPhoto(base64);
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declarer_incident);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.declare_incident_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        photosContainer = findViewById(R.id.photos_container);
        spinnerType = findViewById(R.id.spinner_type);
        inputDescription = findViewById(R.id.input_description);

        findViewById(R.id.btn_add_photo).setOnClickListener(v -> showImageSourceDialog());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_NOTIF_PERMISSION);
        }

        findViewById(R.id.btn_send).setOnClickListener(v -> {
            String type = spinnerType.getSelectedItem() != null
                    ? spinnerType.getSelectedItem().toString() : "";
            String desc = inputDescription.getText().toString().trim();
            String chauffeurId = new Prefs(this).getUserId();
            new SendIncidentTask(this, chauffeurId, type, desc, new ArrayList<>(imagesBase64)).execute();
        });
    }

    private void showImageSourceDialog() {
        String[] options = {"Appareil photo", "Galerie"};
        new AlertDialog.Builder(this)
                .setTitle("Ajouter une photo")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) requestCamera();
                    else galleryLauncher.launch("image/*");
                }).show();
    }

    private void requestCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        } else {
            launchCamera();
        }
    }

    private void launchCamera() {
        Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePicture.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePicture, REQUEST_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "Caméra non disponible", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bundle extras = data.getExtras();
            if (extras != null && extras.get("data") instanceof Bitmap) {
                String base64 = ImageUtils.encode((Bitmap) extras.get("data"));
                if (base64 != null) addPhoto(base64);
            }
        }
    }

    private void addPhoto(String base64) {
        imagesBase64.add(base64);
        
        View v = getLayoutInflater().inflate(R.layout.view_incident_photo_item, photosContainer, false);
        ImageView img = v.findViewById(R.id.img_thumbnail);
        ImageUtils.bind(img, base64, R.drawable.ic_camera);
        
        v.findViewById(R.id.btn_remove).setOnClickListener(x -> {
            int idx = photosContainer.indexOfChild(v) - 1; // -1 because btn_add_photo is first
            if (idx >= 0) {
                imagesBase64.remove(idx);
                photosContainer.removeView(v);
            }
        });
        
        photosContainer.addView(v);
    }

    private static class SendIncidentTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<DeclarerIncidentActivity> ref;
        private final String chauffeurId;
        private final String type;
        private final String description;
        private final List<String> images;

        SendIncidentTask(DeclarerIncidentActivity a, String chauffeurId, String type, 
                         String description, List<String> images) {
            this.ref = new WeakReference<>(a);
            this.chauffeurId = chauffeurId;
            this.type = type;
            this.description = description;
            this.images = images;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                String vehId = "";
                String vehName = "";
                String plate = "";
                String chauffeurNom = "";

                // Get chauffeur info
                Response<List<Chauffeur>> cr = ApiClient.get().getChauffeurs().execute();
                if (cr.isSuccessful() && cr.body() != null) {
                    for (Chauffeur c : cr.body()) {
                        if (chauffeurId.equals(c.id)) {
                            chauffeurNom = c.nom;
                            break;
                        }
                    }
                }

                // Find vehicle
                Response<List<Vehicule>> vr = ApiClient.get().getVehicules().execute();
                if (vr.isSuccessful() && vr.body() != null) {
                    for (Vehicule v : vr.body()) {
                        if (chauffeurId.equals(v.conducteurId)) {
                            vehId = v.id;
                            vehName = v.getNomComplet();
                            plate = v.immatriculation;
                            break;
                        }
                    }
                }
                
                String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                
                Incident incident = new Incident();
                incident.chauffeurId = chauffeurId;
                incident.chauffeurNom = chauffeurNom;
                incident.vehiculeId = vehId;
                incident.vehiculeNom = vehName;
                incident.immatriculation = plate;
                incident.type = type;
                incident.description = description;
                incident.date = today;
                incident.statut = "En cours";
                incident.images = images;

                Response<Incident> r = ApiClient.get().createIncident(incident).execute();
                return r.isSuccessful();
            } catch (Exception e) {
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean ok) {
            DeclarerIncidentActivity a = ref.get();
            if (a == null || a.isFinishing()) return;
            if (ok) {
                Toast.makeText(a, R.string.incident_sent_toast, Toast.LENGTH_SHORT).show();
                NotificationHelper.notifyIncidentSent(a);
                a.finish();
            } else {
                Toast.makeText(a, R.string.error_server, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
