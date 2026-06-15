package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, String> {
    long countByStatut(String statut);
}
