package com.fleettracking.app.model;

import java.util.ArrayList;
import java.util.List;

public class Incident {
    public String id;
    public String chauffeurId;
    public String chauffeurNom;
    public String vehiculeId;
    public String vehiculeNom;
    public String immatriculation;
    public String type;
    public String description;
    public String date;
    public String statut;   // En cours / Résolu
    public List<String> images = new ArrayList<>();   // base64-encoded images

    public Incident() {}
}
