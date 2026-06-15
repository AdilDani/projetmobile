package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Entretien;
import com.fleettracking.backend.model.Vehicule;
import com.fleettracking.backend.repository.EntretienRepository;
import com.fleettracking.backend.repository.VehiculeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/entretiens")
public class EntretienController {

    private final EntretienRepository repo;
    private final VehiculeRepository vehiculeRepo;

    public EntretienController(EntretienRepository repo, VehiculeRepository vehiculeRepo) {
        this.repo = repo;
        this.vehiculeRepo = vehiculeRepo;
    }

    @GetMapping
    public List<Entretien> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Entretien> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Entretien create(@RequestBody Entretien e) {
        e.setStatut("aVenir");
        return repo.save(e);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Entretien> update(@PathVariable String id, @RequestBody Entretien e) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        e.setId(id);
        return ResponseEntity.ok(repo.save(e));
    }

    @PutMapping("/{id}/done")
    public ResponseEntity<Entretien> markDone(@PathVariable String id) {
        Optional<Entretien> opt = repo.findById(id);
        if (!opt.isPresent()) return ResponseEntity.notFound().build();

        Entretien e = opt.get();
        LocalDate today = LocalDate.now();

        // Compute overdue days (date-based only)
        if (!e.isEstKmBase() && e.getCibleDate() != null && !e.getCibleDate().isEmpty()) {
            LocalDate cible = LocalDate.parse(e.getCibleDate());
            int retard = (int) ChronoUnit.DAYS.between(cible, today);
            e.setRetardJours(Math.max(0, retard));
        }

        e.setStatut("effectue");
        e.setDateEffectuee(today.toString());
        repo.save(e);

        // Schedule next occurrence if recurring
        if (e.getIntervalKm() > 0 || e.getIntervalJours() > 0) {
            Entretien next = new Entretien();
            next.setVehiculeId(e.getVehiculeId());
            next.setVehiculeNom(e.getVehiculeNom());
            next.setImmatriculation(e.getImmatriculation());
            next.setType(e.getType());
            next.setEstKmBase(e.isEstKmBase());
            next.setIntervalKm(e.getIntervalKm());
            next.setIntervalJours(e.getIntervalJours());
            next.setStatut("aVenir");

            if (e.getIntervalKm() > 0) {
                int currentKm = vehiculeRepo.findById(e.getVehiculeId())
                        .map(Vehicule::getKilometrage).orElse(e.getCibleKm());
                next.setCibleKm(currentKm + e.getIntervalKm());
            }
            if (e.getIntervalJours() > 0) {
                next.setCibleDate(today.plusDays(e.getIntervalJours()).toString());
            }
            repo.save(next);
        }

        return ResponseEntity.ok(e);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
