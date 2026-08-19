package com.mhesus.api.auth.presentation;

import com.mhesus.api.auth.application.LoginErrorResponse;
import com.mhesus.api.auth.application.LoginRequest;
import com.mhesus.api.auth.application.LoginResponse;
import com.mhesus.api.auth.application.UsuarioDto;
import com.mhesus.api.auth.application.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        var res = authService.login(req.usuario(), req.password());
        if (res.ok()) {
            return ResponseEntity.ok(new LoginResponse(res.token(), UsuarioDto.de(res.usuario())));
        }
        return ResponseEntity.status(401).body(new LoginErrorResponse(res.error(), res.bloqueadoHastaMillis()));
    }

    /** Consulta si un usuario está bloqueado sin intentar el login — usado por el frontend mientras se escribe el nombre de usuario. */
    @GetMapping("/estado-bloqueo")
    public Map<String, Long> estadoBloqueo(@RequestParam String usuario) {
        Long hasta = authService.estadoBloqueo(usuario);
        return java.util.Collections.singletonMap("bloqueadoHasta", hasta);
    }
}
