package com.fleettracking.backend.controller;

import com.fleettracking.backend.dto.LoginRequest;
import com.fleettracking.backend.dto.LoginResponse;
import com.fleettracking.backend.model.AdminUser;
import com.fleettracking.backend.model.Chauffeur;
import com.fleettracking.backend.repository.AdminUserRepository;
import com.fleettracking.backend.repository.ChauffeurRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AdminUserRepository admins;
    private final ChauffeurRepository chauffeurs;

    public AuthController(AdminUserRepository admins, ChauffeurRepository chauffeurs) {
        this.admins = admins;
        this.chauffeurs = chauffeurs;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        String login = req.getLogin() == null ? "" : req.getLogin().trim();
        String pwd = req.getPassword() == null ? "" : req.getPassword();

        Optional<AdminUser> admin = admins.findByLogin(login);
        if (admin.isPresent() && admin.get().getPassword().equals(pwd)) {
            AdminUser a = admin.get();
            return ResponseEntity.ok(new LoginResponse(true, "admin", a.getId(), a.getNom()));
        }

        Optional<Chauffeur> ch = chauffeurs.findByLogin(login);
        if (ch.isPresent() && ch.get().getPassword().equals(pwd)) {
            Chauffeur c = ch.get();
            return ResponseEntity.ok(new LoginResponse(true, "chauffeur", c.getId(), c.getNom()));
        }

        return ResponseEntity.status(401).body(new LoginResponse(false, null, null, null));
    }
}
