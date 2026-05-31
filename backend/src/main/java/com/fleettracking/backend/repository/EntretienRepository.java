package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.Entretien;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntretienRepository extends JpaRepository<Entretien, String> {
}
