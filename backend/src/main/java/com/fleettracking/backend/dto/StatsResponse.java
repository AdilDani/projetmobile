package com.fleettracking.backend.dto;

public class StatsResponse {
    private long vehicules;
    private long chauffeurs;
    private long incidents;
    private long entretiens;

    public StatsResponse(long vehicules, long chauffeurs, long incidents, long entretiens) {
        this.vehicules = vehicules;
        this.chauffeurs = chauffeurs;
        this.incidents = incidents;
        this.entretiens = entretiens;
    }

    public long getVehicules() { return vehicules; }
    public long getChauffeurs() { return chauffeurs; }
    public long getIncidents() { return incidents; }
    public long getEntretiens() { return entretiens; }
}
