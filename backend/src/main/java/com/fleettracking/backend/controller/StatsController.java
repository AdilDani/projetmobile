package com.fleettracking.backend.controller;

import com.fleettracking.backend.dto.StatsResponse;
import com.fleettracking.backend.repository.ChauffeurRepository;
import com.fleettracking.backend.repository.EntretienRepository;
import com.fleettracking.backend.repository.IncidentRepository;
import com.fleettracking.backend.repository.VehiculeRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final VehiculeRepository vehicules;
    private final ChauffeurRepository chauffeurs;
    private final IncidentRepository incidents;
    private final EntretienRepository entretiens;

    public StatsController(VehiculeRepository vehicules, ChauffeurRepository chauffeurs,
                           IncidentRepository incidents, EntretienRepository entretiens) {
        this.vehicules = vehicules;
        this.chauffeurs = chauffeurs;
        this.incidents = incidents;
        this.entretiens = entretiens;
    }

    @GetMapping
    public StatsResponse stats() {
        return new StatsResponse(
                vehicules.count(),
                chauffeurs.count(),
                incidents.countByStatut("En cours"),   // unresolved only
                entretiens.count());
    }
}
