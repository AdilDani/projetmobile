package com.fleettracking.app.model;

public class Entretien {
    public String id;
    public String vehiculeId;
    public String vehiculeNom;
    public String immatriculation;
    public String type;
    public boolean estKmBase;
    public int cibleKm;
    public String cibleDate;       // yyyy-MM-dd
    public int intervalKm;
    public int intervalJours;
    public String statut;          // "aVenir" or "effectue"
    public String dateEffectuee;   // yyyy-MM-dd
    public int retardJours;

    public Entretien() {}
}
