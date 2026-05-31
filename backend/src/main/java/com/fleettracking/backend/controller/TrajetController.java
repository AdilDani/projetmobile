package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Trajet;
import com.fleettracking.backend.repository.TrajetRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trajets")
public class TrajetController {

    private final TrajetRepository repo;

    public TrajetController(TrajetRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Trajet> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Trajet> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Trajet create(@RequestBody Trajet t) { return repo.save(t); }

    @PutMapping("/{id}")
    public ResponseEntity<Trajet> update(@PathVariable String id, @RequestBody Trajet t) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        t.setId(id);
        return ResponseEntity.ok(repo.save(t));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
