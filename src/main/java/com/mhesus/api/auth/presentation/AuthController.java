package com.mhesus.api.auth.presentation;

import com.mhesus.api.shared.dto.ErrorResponse;
import com.mhesus.api.auth.application.LoginRequest;
import com.mhesus.api.auth.application.LoginResponse;
import com.mhesus.api.auth.application.UsuarioDto;
import com.mhesus.api.auth.application.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        return authService.login(req.usuario(), req.password())
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(new LoginResponse(r.token(), UsuarioDto.de(r.usuario()))))
                .orElseGet(() -> ResponseEntity.status(401).body(new ErrorResponse("Usuario o contraseña incorrectos.")));
    }
}
