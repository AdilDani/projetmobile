package com.fleettracking.app.model;

public class Trajet {
    public String id;
    public String date;
    public String chauffeurId;
    public String vehiculeId;
    public String vehiculeNom;
    public String depart;
    public String arrivee;
    public String heureDepart;
    public String heureArrivee;
    public double distanceKm;
    public String duree;
    public int vitesseMoyenne;
    public double consommation;   // L/100km
    public boolean enCours;
    public double departLat;
    public double departLng;
    public double arriveeLat;
    public double arriveeLng;
    public String waypoints; // JSON: [[lat,lng], ...]

    public Trajet() {}
}
