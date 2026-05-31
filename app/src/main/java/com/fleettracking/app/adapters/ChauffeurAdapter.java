package com.fleettracking.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fleettracking.app.R;
import com.fleettracking.app.model.Chauffeur;
import com.fleettracking.app.util.ImageUtils;

import java.util.List;

public class ChauffeurAdapter extends RecyclerView.Adapter<ChauffeurAdapter.VH> {

    public interface OnChauffeurAction {
        void onClick(Chauffeur c);
        void onCall(Chauffeur c);
        void onSms(Chauffeur c);
    }

    private final List<Chauffeur> items;
    private final OnChauffeurAction listener;

    public ChauffeurAdapter(List<Chauffeur> items, OnChauffeurAction listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chauffeur, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Chauffeur c = items.get(position);
        h.name.setText(c.nom);
        h.phone.setText(c.telephone);
        ImageUtils.bind(h.photo, c.photo, R.drawable.ic_person);
        
        h.itemView.setOnClickListener(x -> { if (listener != null) listener.onClick(c); });
        h.call.setOnClickListener(x -> { if (listener != null) listener.onCall(c); });
        h.sms.setOnClickListener(x -> { if (listener != null) listener.onSms(c); });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView name, phone;
        ImageView photo, call, sms;
        VH(@NonNull View v) {
            super(v);
            name = v.findViewById(R.id.chauffeur_name);
            phone = v.findViewById(R.id.chauffeur_phone);
            photo = v.findViewById(R.id.img_chauffeur);
            call = v.findViewById(R.id.btn_call);
            sms = v.findViewById(R.id.btn_sms);
        }
    }
}
