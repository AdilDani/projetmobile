package com.fleettracking.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    private String id;
    private String vehiculeNom;
    private String immatriculation;
    private String type;
    @Column(length = 1000)
    private String description;
    @Column(name = "event_date")
    private String date;
    private String statut;

    public Incident() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "i-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVehiculeNom() { return vehiculeNom; }
    public void setVehiculeNom(String vehiculeNom) { this.vehiculeNom = vehiculeNom; }
    public String getImmatriculation() { return immatriculation; }
    public void setImmatriculation(String immatriculation) { this.immatriculation = immatriculation; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }
}
