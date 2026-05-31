package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.Vehicule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehiculeRepository extends JpaRepository<Vehicule, String> {
}
