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
    private String vehiculeId;
    private String vehiculeNom;
    private String immatriculation;
    private String type;
    private boolean estKmBase;
    private int cibleKm;
    @Column(name = "cible_date")
    private String cibleDate;       // yyyy-MM-dd, for date-based entretiens
    private int intervalKm;         // 0 if non-recurring
    private int intervalJours;      // 0 if non-recurring
    private String statut;          // "aVenir" or "effectue"
    private String dateEffectuee;   // yyyy-MM-dd when marked done
    private int retardJours;        // days overdue when marked done

    public Entretien() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "e-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(String vehiculeId) { this.vehiculeId = vehiculeId; }
    public String getVehiculeNom() { return vehiculeNom; }
    public void setVehiculeNom(String vehiculeNom) { this.vehiculeNom = vehiculeNom; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public boolean isEstKmBase() { return estKmBase; }
    public void setEstKmBase(boolean estKmBase) { this.estKmBase = estKmBase; }
    public int getCibleKm() { return cibleKm; }
    public void setCibleKm(int cibleKm) { this.cibleKm = cibleKm; }
    public String getCibleDate() { return cibleDate; }
    public void setCibleDate(String cibleDate) { this.cibleDate = cibleDate; }
    public int getIntervalKm() { return intervalKm; }
    public void setIntervalKm(int intervalKm) { this.intervalKm = intervalKm; }
    public int getIntervalJours() { return intervalJours; }
    public void setIntervalJours(int intervalJours) { this.intervalJours = intervalJours; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public String getDateEffectuee() { return dateEffectuee; }
    public void setDateEffectuee(String dateEffectuee) { this.dateEffectuee = dateEffectuee; }
    public int getRetardJours() { return retardJours; }
    public void setRetardJours(int retardJours) { this.retardJours = retardJours; }
}
