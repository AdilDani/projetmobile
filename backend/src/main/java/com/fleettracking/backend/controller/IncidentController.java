package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Incident;
import com.fleettracking.backend.repository.IncidentRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentRepository repo;

    public IncidentController(IncidentRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Incident> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Incident> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Incident create(@RequestBody Incident i) { return repo.save(i); }

    @PutMapping("/{id}")
    public ResponseEntity<Incident> update(@PathVariable String id, @RequestBody Incident i) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        i.setId(id);
        return ResponseEntity.ok(repo.save(i));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
