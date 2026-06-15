package com.fleettracking.app.model;

public class Vehicule {
    public String id;
    public String marque;
    public String modele;
    public String immatriculation;
    public int annee;
    public int kilometrage;
    public String statut;
    public int carburantPct;
    public double consommation;
    public String photo;
    public int vidangeCibleKm;
    public String controleTechniqueDate; // yyyy-MM-dd
    public String conducteurId;
    public double lat;
    public double lng;
    public int vitesse;

    public Vehicule() {}

    public String getNomComplet() {
        String m = marque != null ? marque : "";
        String mo = modele != null ? modele : "";
        return (m + " " + mo).trim();
    }
}
