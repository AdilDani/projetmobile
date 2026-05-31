package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrajetRepository extends JpaRepository<Trajet, String> {
}
