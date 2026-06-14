package com.fleettracking.app.admin.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.admin.ChauffeurDetailsActivity;
import com.fleettracking.app.admin.ProfilActivity;
import com.fleettracking.app.adapters.ChauffeurAdapter;
import com.fleettracking.app.data.RepoCallback;
import com.fleettracking.app.data.Repository;
import com.fleettracking.app.model.Chauffeur;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChauffeursFragment extends Fragment implements ChauffeurAdapter.OnChauffeurAction {

    private final List<Chauffeur> all = new ArrayList<>();
    private final List<Chauffeur> filtered = new ArrayList<>();
    private ChauffeurAdapter adapter;
    private EditText search;
    private Repository repo;

    // Refresh the list only when a chauffeur edit actually completed (RESULT_OK),
    // not on every back navigation.
    private final ActivityResultLauncher<Intent> detailsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    load();
                }
            }
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_admin_chauffeurs, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);
        repo = new Repository(requireContext());

        ((TextView) v.findViewById(R.id.toolbar_title)).setText(R.string.nav_chauffeurs);
        v.findViewById(R.id.btn_profile).setOnClickListener(x ->
                startActivity(new Intent(getContext(), ProfilActivity.class)));

        adapter = new ChauffeurAdapter(filtered, this);
        RecyclerView rv = v.findViewById(R.id.recycler_chauffeurs);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));
        rv.setAdapter(adapter);

        search = v.findViewById(R.id.input_search);
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { filter(s.toString()); }
            @Override public void afterTextChanged(Editable s) {}
        });

        load();
    }

    private void load() {
        repo.getChauffeurs(new RepoCallback<List<Chauffeur>>() {
            @Override public void onResult(List<Chauffeur> list) {
                all.clear();
                all.addAll(list);
                filter(search.getText().toString());
            }
            @Override public void onError(String message) { /* keep current list */ }
        });
    }

    private void filter(String query) {
        String q = query.toLowerCase(Locale.getDefault()).trim();
        filtered.clear();
        for (Chauffeur c : all) {
            if (c.nom.toLowerCase(Locale.getDefault()).contains(q)
                    || c.telephone.toLowerCase(Locale.getDefault()).contains(q)) {
                filtered.add(c);
            }
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(Chauffeur c) {
        Intent i = new Intent(getContext(), ChauffeurDetailsActivity.class);
        i.putExtra(ChauffeurDetailsActivity.EXTRA_CHAUFFEUR_ID, c.id);
        detailsLauncher.launch(i);
    }

    @Override
    public void onCall(Chauffeur c) {
        Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + c.telephone));
        startActivity(i);
    }

    @Override
    public void onSms(Chauffeur c) {
        Intent i = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + c.telephone));
        startActivity(i);
    }
}
