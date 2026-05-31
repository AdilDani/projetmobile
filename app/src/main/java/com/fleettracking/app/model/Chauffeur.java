package com.fleettracking.app.model;

public class Chauffeur {
    public String id;
    public String nom;
    public String telephone;
    public String email;
    public String vehiculeAffecte;  // display name e.g. "Renault Master"
    public String permis;
    public String login;
    public String password;
    public String statut;

    public Chauffeur(String id, String nom, String telephone, String email,
                     String vehiculeAffecte, String permis, String login,
                     String password, String statut) {
        this.id = id;
        this.nom = nom;
        this.telephone = telephone;
        this.email = email;
        this.vehiculeAffecte = vehiculeAffecte;
        this.permis = permis;
        this.login = login;
        this.password = password;
        this.statut = statut;
    }
}
