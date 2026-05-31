package com.fleettracking.app.model;

public class Trajet {
    public String id;
    public String date;
    public String vehiculeNom;
    public String depart;
    public String arrivee;
    public String heureDepart;
    public String heureArrivee;
    public int distanceKm;
    public String duree;
    public int vitesseMoyenne;
    public double consommation;   // L/100km

    public Trajet(String id, String date, String vehiculeNom, String depart, String arrivee,
                  String heureDepart, String heureArrivee, int distanceKm, String duree,
                  int vitesseMoyenne, double consommation) {
        this.id = id;
        this.date = date;
        this.vehiculeNom = vehiculeNom;
        this.depart = depart;
        this.arrivee = arrivee;
        this.heureDepart = heureDepart;
        this.heureArrivee = heureArrivee;
        this.distanceKm = distanceKm;
        this.duree = duree;
        this.vitesseMoyenne = vitesseMoyenne;
        this.consommation = consommation;
    }
}
