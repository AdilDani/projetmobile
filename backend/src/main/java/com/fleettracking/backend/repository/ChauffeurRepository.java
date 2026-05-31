package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.Chauffeur;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChauffeurRepository extends JpaRepository<Chauffeur, String> {
    Optional<Chauffeur> findByLogin(String login);
}
