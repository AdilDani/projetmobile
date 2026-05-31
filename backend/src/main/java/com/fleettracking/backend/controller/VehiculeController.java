package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Vehicule;
import com.fleettracking.backend.repository.VehiculeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vehicules")
public class VehiculeController {

    private final VehiculeRepository repo;

    public VehiculeController(VehiculeRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Vehicule> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Vehicule> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Vehicule create(@RequestBody Vehicule v) { return repo.save(v); }

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
}
