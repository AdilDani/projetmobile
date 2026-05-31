package com.fleettracking.app.chauffeur;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.fleettracking.app.R;
import com.fleettracking.app.data.ApiClient;
import com.fleettracking.app.model.Incident;
import com.fleettracking.app.model.Vehicule;
import com.fleettracking.app.util.NotificationHelper;
import com.fleettracking.app.util.Prefs;

import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Response;

/**
 * Declares an incident. Demonstrates an implicit Intent (camera capture),
 * runtime permissions and an AsyncTask that performs the Retrofit upload of
 * the incident to the backend on a background thread.
 */
public class DeclarerIncidentActivity extends AppCompatActivity {

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_CAMERA_PERMISSION = 102;
    private static final int REQUEST_NOTIF_PERMISSION = 103;

    private ImageView imagePhoto;
    private Spinner spinnerType;
    private EditText inputDescription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_declarer_incident);

        ((TextView) findViewById(R.id.toolbar_title)).setText(R.string.declare_incident_title);
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        imagePhoto = findViewById(R.id.image_photo);
        spinnerType = findViewById(R.id.spinner_type);
        inputDescription = findViewById(R.id.input_description);

        findViewById(R.id.btn_add_photo).setOnClickListener(v -> requestCamera());
        imagePhoto.setOnClickListener(v -> requestCamera());

        // Android 13+ requires runtime consent before posting notifications.
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
            new SendIncidentTask(this, chauffeurId, type, desc).execute();
        });
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
            Toast.makeText(this, R.string.cd_camera, Toast.LENGTH_SHORT).show();
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
                imagePhoto.setImageBitmap((Bitmap) extras.get("data"));
                imagePhoto.clearColorFilter();
            }
        }
    }

    /** Uploads the incident to the backend (Retrofit, executed on a background thread). */
    private static class SendIncidentTask extends AsyncTask<Void, Void, Boolean> {
        private final WeakReference<DeclarerIncidentActivity> ref;
        private final String chauffeurId;
        private final String type;
        private final String description;

        SendIncidentTask(DeclarerIncidentActivity a, String chauffeurId, String type, String description) {
            this.ref = new WeakReference<>(a);
            this.chauffeurId = chauffeurId;
            this.type = type;
            this.description = description;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                // Find the chauffeur's vehicule so the incident is attached to it.
                String vehName = "";
                String plate = "";
                Response<List<Vehicule>> vr = ApiClient.get().getVehicules().execute();
                if (vr.isSuccessful() && vr.body() != null) {
                    for (Vehicule v : vr.body()) {
                        if (chauffeurId != null && chauffeurId.equals(v.conducteurId)) {
                            vehName = v.getNomComplet();
                            plate = v.immatriculation;
                            break;
                        }
                    }
                }
                String today = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());
                Incident incident = new Incident(null, vehName, plate, type, description, today, "En cours");
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
