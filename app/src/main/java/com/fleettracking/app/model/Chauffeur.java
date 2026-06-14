package com.fleettracking.app.model;

public class Chauffeur {
    public String id;
    public String nom;
    public String telephone;
    public String email;
    public String permis;
    public String login;
    public String password;
    public String statut;
    public String vehiculeAffecte;  // display name of assigned vehicle, may be null
    public String photo;            // base64-encoded image, may be null

    public Chauffeur() {}

    public Chauffeur(String id, String nom, String telephone, String email,
                     String permis, String login, String password, String statut) {
        this.id = id;
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        this.permis = permis;
        this.login = login;
        this.password = password;
        this.statut = statut;
    }
}
