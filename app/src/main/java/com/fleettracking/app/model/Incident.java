package com.fleettracking.app.model;

import java.util.List;

public class Incident {
    public String id;
    public String vehiculeNom;
    public String immatriculation;
    public String type;
    public String description;
    public String date;
    public String statut;       // En cours / Résolu
    public String chauffeurNom;  // Nom du chauffeur qui a déclaré l'incident
    public List<String> photos;  // Liste d'URLs ou de chaînes Base64

    public Incident(String id, String vehiculeNom, String immatriculation, String type,
                    String description, String date, String statut, String chauffeurNom, List<String> photos) {
        this.id = id;
        this.vehiculeNom = vehiculeNom;
        this.immatriculation = immatriculation;
        this.type = type;
        this.description = description;
        this.date = date;
        this.statut = statut;
        this.chauffeurNom = chauffeurNom;
        this.photos = photos;
    }
}
