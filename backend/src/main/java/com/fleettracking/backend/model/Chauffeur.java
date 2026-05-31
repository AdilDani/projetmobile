package com.fleettracking.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "chauffeur")
public class Chauffeur {

    @Id
    private String id;
    private String nom;
    private String telephone;
    private String email;
    private String vehiculeAffecte;
    private String permis;
    private String login;
    private String password;
    private String statut;

    public Chauffeur() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "c-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getTelephone() { return telephone; }
    public void setTelephone(String telephone) { this.telephone = telephone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getVehiculeAffecte() { return vehiculeAffecte; }
    public void setVehiculeAffecte(String vehiculeAffecte) { this.vehiculeAffecte = vehiculeAffecte; }
    public String getPermis() { return permis; }
    public void setPermis(String permis) { this.permis = permis; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
