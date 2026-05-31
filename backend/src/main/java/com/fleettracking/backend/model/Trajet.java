package com.fleettracking.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "trajet")
public class Trajet {

    @Id
    private String id;
    @Column(name = "trajet_date")
    private String date;
    private String vehiculeNom;
    private String depart;
    private String arrivee;
    private String heureDepart;
    private String heureArrivee;
    private int distanceKm;
    private String duree;
    private int vitesseMoyenne;
    private double consommation;

    public Trajet() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "t-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getVehiculeNom() { return vehiculeNom; }
    public void setVehiculeNom(String vehiculeNom) { this.vehiculeNom = vehiculeNom; }
    public String getDepart() { return depart; }
    public void setDepart(String depart) { this.depart = depart; }
    public String getArrivee() { return arrivee; }
    public void setArrivee(String arrivee) { this.arrivee = arrivee; }
    public String getHeureDepart() { return heureDepart; }
    public void setHeureDepart(String heureDepart) { this.heureDepart = heureDepart; }
    public String getHeureArrivee() { return heureArrivee; }
    public void setHeureArrivee(String heureArrivee) { this.heureArrivee = heureArrivee; }
    public int getDistanceKm() { return distanceKm; }
    public void setDistanceKm(int distanceKm) { this.distanceKm = distanceKm; }
    public String getDuree() { return duree; }
    public void setDuree(String duree) { this.duree = duree; }
    public int getVitesseMoyenne() { return vitesseMoyenne; }
    public void setVitesseMoyenne(int vitesseMoyenne) { this.vitesseMoyenne = vitesseMoyenne; }
    public double getConsommation() { return consommation; }
    public void setConsommation(double consommation) { this.consommation = consommation; }
}
