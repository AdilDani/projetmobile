package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.Entretien;
import com.fleettracking.backend.repository.EntretienRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/entretiens")
public class EntretienController {

    private final EntretienRepository repo;

    public EntretienController(EntretienRepository repo) { this.repo = repo; }

    @GetMapping
    public List<Entretien> all() { return repo.findAll(); }

    @GetMapping("/{id}")
    public ResponseEntity<Entretien> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Entretien create(@RequestBody Entretien e) { return repo.save(e); }

    @PutMapping("/{id}")
    public ResponseEntity<Entretien> update(@PathVariable String id, @RequestBody Entretien e) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        e.setId(id);
        return ResponseEntity.ok(repo.save(e));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
