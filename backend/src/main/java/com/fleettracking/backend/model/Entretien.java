package com.fleettracking.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "entretien")
public class Entretien {

    @Id
    private String id;
    private String type;
    private String vehiculeNom;
    private String immatriculation;
    @Column(name = "entretien_date")
    private String date;
    private String echeance;
    private boolean aVenir;

    public Entretien() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "e-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getVehiculeNom() { return vehiculeNom; }
    public void setVehiculeNom(String vehiculeNom) { this.vehiculeNom = vehiculeNom; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getEcheance() { return echeance; }
    public void setEcheance(String echeance) { this.echeance = echeance; }
    public boolean isAVenir() { return aVenir; }
    public void setAVenir(boolean aVenir) { this.aVenir = aVenir; }
}
