package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Entretien;
import com.fleettracking.backend.model.Vehicule;
import com.fleettracking.backend.repository.EntretienRepository;
import com.fleettracking.backend.repository.VehiculeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeRepository repo;
    private final EntretienRepository entretienRepo;

    public VehiculeController(VehiculeRepository repo, EntretienRepository entretienRepo) {
        this.repo = repo;
        this.entretienRepo = entretienRepo;
    }

    @GetMapping
    public List<Vehicule> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Vehicule create(@RequestBody Vehicule v) {
        Vehicule saved = repo.save(v);
        createDefaultEntretiens(saved);
        return saved;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Vehicule> update(@PathVariable String id, @RequestBody Vehicule v) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        v.setId(id);
        return ResponseEntity.ok(repo.save(v));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void createDefaultEntretiens(Vehicule v) {
        String nom = (v.getMarque() != null ? v.getMarque() : "") + " " +
                     (v.getModele() != null ? v.getModele() : "");
        nom = nom.trim();

        Entretien vidange = new Entretien();
        vidange.setVehiculeId(v.getId());
        vidange.setVehiculeNom(nom);
        vidange.setImmatriculation(v.getImmatriculation() != null ? v.getImmatriculation() : "");
        vidange.setType("Vidange");
        vidange.setEstKmBase(true);
        vidange.setCibleKm(v.getVidangeCibleKm() > 0
                ? v.getVidangeCibleKm()
                : v.getKilometrage() + 15000);
        vidange.setIntervalKm(15000);
        vidange.setStatut("aVenir");
        entretienRepo.save(vidange);

        Entretien ct = new Entretien();
        ct.setVehiculeId(v.getId());
        ct.setVehiculeNom(nom);
        ct.setImmatriculation(v.getImmatriculation() != null ? v.getImmatriculation() : "");
        ct.setType("Contrôle Technique");
        ct.setEstKmBase(false);
        ct.setCibleDate(v.getControleTechniqueDate() != null && !v.getControleTechniqueDate().isEmpty()
                ? v.getControleTechniqueDate()
                : LocalDate.now().plusYears(1).toString());
        ct.setIntervalJours(365);
        ct.setStatut("aVenir");
        entretienRepo.save(ct);
    }
}
