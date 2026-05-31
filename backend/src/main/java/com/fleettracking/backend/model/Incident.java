package com.fleettracking.backend.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "incident")
public class Incident {

    @Id
    private String id;
    private String chauffeurId;
    private String chauffeurNom;
    private String vehiculeId;
    private String vehiculeNom;
    private String immatriculation;
    private String type;
    @Column(length = 1000)
    private String description;
    @Column(name = "event_date")
    private String date;
    private String statut;

    // Multiple base64-encoded images stored in a child table.
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "incident_image", joinColumns = @JoinColumn(name = "incident_id"))
    @Lob
    @Column(name = "image", columnDefinition = "text")
    private List<String> images = new ArrayList<>();

    public Incident() {}

    @PrePersist
    public void ensureId() {
        if (id == null || id.isEmpty()) id = "i-" + UUID.randomUUID();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getChauffeurId() { return chauffeurId; }
    public void setChauffeurId(String chauffeurId) { this.chauffeurId = chauffeurId; }
    public String getChauffeurNom() { return chauffeurNom; }
    public void setChauffeurNom(String chauffeurNom) { this.chauffeurNom = chauffeurNom; }
    public String getVehiculeId() { return vehiculeId; }
    public void setVehiculeId(String vehiculeId) { this.vehiculeId = vehiculeId; }
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
    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }
}
