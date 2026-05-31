package com.fleettracking.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "vehicule")
public class Vehicule {

    @Id
    private String id;
    private String marque;
    private String modele;
    private String immatriculation;
    private int annee;
    private int kilometrage;
    private String statut;
    private int carburantPct;
    private String prochaineVidange;
    private String controleTechnique;
    private String conducteurId;
    private double lat;
    private double lng;
    private int vitesse;

    public Vehicule() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "v-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getMarque() { return marque; }
    public void setMarque(String marque) { this.marque = marque; }
    public String getModele() { return modele; }
    public void setModele(String modele) { this.modele = modele; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public int getAnnee() { return annee; }
    public void setAnnee(int annee) { this.annee = annee; }
    public int getKilometrage() { return kilometrage; }
    public void setKilometrage(int kilometrage) { this.kilometrage = kilometrage; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
    public int getCarburantPct() { return carburantPct; }
    public void setCarburantPct(int carburantPct) { this.carburantPct = carburantPct; }
    public String getProchaineVidange() { return prochaineVidange; }
    public void setProchaineVidange(String prochaineVidange) { this.prochaineVidange = prochaineVidange; }
    public String getControleTechnique() { return controleTechnique; }
    public void setControleTechnique(String controleTechnique) { this.controleTechnique = controleTechnique; }
    public String getConducteurId() { return conducteurId; }
    public void setConducteurId(String conducteurId) { this.conducteurId = conducteurId; }
    public double getLat() { return lat; }
    public void setLat(double lat) { this.lat = lat; }
    public double getLng() { return lng; }
    public void setLng(double lng) { this.lng = lng; }
    public int getVitesse() { return vitesse; }
    public void setVitesse(int vitesse) { this.vitesse = vitesse; }
}
