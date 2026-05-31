package com.fleettracking.app.model;

public class Incident {
    public String id;
    public String vehiculeNom;
    public String immatriculation;
    public String type;
    public String description;
    public String date;
    public String statut;   // En cours / Résolu

    public Incident(String id, String vehiculeNom, String immatriculation, String type,
                    String description, String date, String statut) {
        this.id = id;
        this.vehiculeNom = vehiculeNom;
        this.immatriculation = immatriculation;
        this.type = type;
        this.description = description;
        this.date = date;
        this.statut = statut;
    }
}
