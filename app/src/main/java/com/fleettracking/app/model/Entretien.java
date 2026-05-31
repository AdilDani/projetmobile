package com.fleettracking.app.model;

public class Entretien {
    public String id;
    public String type;
    public String vehiculeNom;
    public String immatriculation;
    public String date;
    public String echeance;     // e.g. "Dans 6 jours"
    public boolean aVenir;      // true = à venir, false = historique

    public Entretien(String id, String type, String vehiculeNom, String immatriculation,
                     String date, String echeance, boolean aVenir) {
        this.id = id;
        this.type = type;
        this.vehiculeNom = vehiculeNom;
        this.immatriculation = immatriculation;
        this.date = date;
        this.echeance = echeance;
        this.aVenir = aVenir;
    }
}
