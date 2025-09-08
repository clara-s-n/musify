package com.tfu.backend.auth;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
  private final AuthService authService;
  private final JwtService jwtService;

  public AuthController(AuthService a, JwtService j) {
    this.authService = a;
    this.jwtService = j;
  }

  @PostMapping("/login")
  public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest req) {
    var userId = authService.login(req.email(), req.password()); // rate limited
    var token = jwtService.createToken(userId);
    return ResponseEntity.ok(new TokenResponse(token));
  }
}

record TokenResponse(String accessToken) {
}
