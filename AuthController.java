package com.focusflow.controller;

import com.focusflow.model.User;
import com.focusflow.repository.UserRepository;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final UserRepository users;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserRepository users) {
        this.users = users;
    }

    // ============================================================
    // REGISTER
    // ============================================================

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {

        System.out.println("========== REGISTRATION REQUEST ==========");
        System.out.println("Received body: " + body);

        String name = body.get("name") == null
                ? ""
                : body.get("name").toString().trim();

        String email = body.get("email") == null
                ? ""
                : body.get("email").toString().trim().toLowerCase();

        String password = body.get("password") == null
                ? ""
                : body.get("password").toString();

        String interests = body.get("interests") == null
                ? ""
                : body.get("interests").toString().trim();

        System.out.println("Name     : " + name);
        System.out.println("Email    : " + email);
        System.out.println("Password : " +
                (password.isEmpty() ? "EMPTY" : "RECEIVED"));
        System.out.println("Interests: " + interests);

        // --------------------------------------------------------
        // VALIDATION
        // --------------------------------------------------------

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Name, email and password are required"
                    ));
        }

        if (!email.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Please enter a valid email address"
                    ));
        }

        if (password.length() < 6) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Password must contain at least 6 characters"
                    ));
        }

        // --------------------------------------------------------
        // DUPLICATE EMAIL
        // --------------------------------------------------------

        if (users.existsByEmail(email)) {

            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of(
                            "message",
                            "Email already registered"
                    ));
        }

        // --------------------------------------------------------
        // CREATE USER
        // --------------------------------------------------------

        User user = new User();

        user.setName(name);
        user.setEmail(email);
        user.setPassword(encoder.encode(password));
        user.setInterests(interests);

        User savedUser = users.save(user);

        System.out.println("USER CREATED SUCCESSFULLY");
        System.out.println("USER ID: " + savedUser.getId());
        System.out.println("==========================================");

        // --------------------------------------------------------
        // CREATE USER-SPECIFIC COOKIE
        // --------------------------------------------------------

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(
                        HttpHeaders.SET_COOKIE,
                        clientCookie(savedUser)
                )
                .body(safe(savedUser));
    }

    // ============================================================
    // LOGIN
    // ============================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @RequestBody Map<String, String> body) {
// Add this method inside AuthController (com.focusflow.controller), next to /login.
// Imports needed: org.springframework.http.ResponseCookie (already imported), ResponseEntity, Map.

@PostMapping("/logout")
public ResponseEntity<?> logout() {
    String expired = ResponseCookie.from("ff_client", "")
            .httpOnly(false).secure(false).path("/").maxAge(0).sameSite("Lax")
            .build().toString();
    return ResponseEntity.ok()
            .header("Set-Cookie", expired)
            .body(Map.of("message", "Signed out"));
}
        String email = body.getOrDefault("email", "")
                .trim()
                .toLowerCase();

        String password = body.getOrDefault("password", "");

        if (email.isEmpty() || password.isEmpty()) {

            return ResponseEntity
                    .badRequest()
                    .body(Map.of(
                            "message",
                            "Email and password are required"
                    ));
        }

        User user = users.findByEmail(email).orElse(null);

        if (user == null ||
                !encoder.matches(
                        password,
                        user.getPassword())) {

            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Invalid email or password"
                    ));
        }

        System.out.println("USER LOGIN SUCCESS");
        System.out.println("USER ID: " + user.getId());
        System.out.println("EMAIL  : " + user.getEmail());

        // --------------------------------------------------------
        // CREATE USER-SPECIFIC COOKIE ON LOGIN
        // --------------------------------------------------------

        return ResponseEntity
                .ok()
                .header(
                        HttpHeaders.SET_COOKIE,
                        clientCookie(user)
                )
                .body(safe(user));
    }

    // ============================================================
    // USER-SPECIFIC TRACKING COOKIE
    // ============================================================

    private String clientCookie(User user) {

        return ResponseCookie
                .from(
                        "ff_client",
                        "u:" + user.getId()
                )
                .httpOnly(false)
                .secure(false)
                .path("/")
                .maxAge(60L * 60 * 24 * 365)
                .sameSite("Lax")
                .build()
                .toString();
    }

    // ============================================================
    // SAFE USER RESPONSE
    // NEVER SEND PASSWORD TO FRONTEND
    // ============================================================

    private Map<String, Object> safe(User user) {

        Map<String, Object> result =
                new LinkedHashMap<>();

        result.put("id", user.getId());
        result.put("name", user.getName());
        result.put("email", user.getEmail());
        result.put("interests", user.getInterests());
        result.put("level", user.getLevel());
        result.put("xp", user.getXp());

        return result;
    }
}