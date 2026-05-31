package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Chauffeur;
import com.fleettracking.backend.repository.ChauffeurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chauffeurs")
public class ChauffeurController {

    private final ChauffeurRepository repo;

    public ChauffeurController(ChauffeurRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Chauffeur> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Chauffeur> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Chauffeur create(@RequestBody Chauffeur c) { return repo.save(c); }

    @PutMapping("/{id}")
    public ResponseEntity<Chauffeur> update(@PathVariable String id, @RequestBody Chauffeur c) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        c.setId(id);
        return ResponseEntity.ok(repo.save(c));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
