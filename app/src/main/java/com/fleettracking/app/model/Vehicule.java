package com.fleettracking.app.model;

public class Vehicule {
    public String id;
    public String marque;
    public String modele;
    public String immatriculation;
    public int annee;
    public int kilometrage;
    public String statut;          // Disponible / Indisponible / En mission
    public int carburantPct;
    public double consommation;    // L/100km
    public String photo;           // base64-encoded image, may be null
    public String prochaineVidange;
    public String controleTechnique;
    public String conducteurId;    // Chauffeur id, set only when "En mission"
    public double lat;
    public double lng;
    public int vitesse;            // km/h

    public Vehicule() {}

    public Vehicule(String id, String marque, String modele, String immatriculation,
                    int annee, int kilometrage, String statut, int carburantPct,
                    String prochaineVidange, String controleTechnique,
                    String conducteurId, double lat, double lng, int vitesse) {
        this.id = id;
        this.marque = marque;
        this.modele = modele;
        this.immatriculation = immatriculation;
        this.annee = annee;
        this.kilometrage = kilometrage;
        this.statut = statut;
        this.carburantPct = carburantPct;
        this.prochaineVidange = prochaineVidange;
        this.controleTechnique = controleTechnique;
        this.conducteurId = conducteurId;
        this.lat = lat;
        this.lng = lng;
        this.vitesse = vitesse;
    }

    public String getNomComplet() {
        String m = marque != null ? marque : "";
        String mo = modele != null ? modele : "";
        return (m + " " + mo).trim();
    }
}
