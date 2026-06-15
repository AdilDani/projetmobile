package com.fleettracking.backend.controller;

import com.fleettracking.backend.model.AdminUser;
import com.fleettracking.backend.repository.AdminUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admins")
public class AdminUserController {

    private final AdminUserRepository repo;

    public AdminUserController(AdminUserRepository repo) { this.repo = repo; }

    @GetMapping("/{id}")
    public ResponseEntity<AdminUser> one(@PathVariable String id) {
        return repo.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<AdminUser> update(@PathVariable String id, @RequestBody AdminUser a) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        a.setId(id);
        return ResponseEntity.ok(repo.save(a));
    }
}
