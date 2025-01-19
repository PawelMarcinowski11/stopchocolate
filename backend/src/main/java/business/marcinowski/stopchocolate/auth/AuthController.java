package business.marcinowski.stopchocolate.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody LoginRequestDto credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refresh(@RequestBody RefreshRequestDto refreshRequest) {
        return ResponseEntity.ok(authService.refresh(refreshRequest));
    }

    @PostMapping("/register")
    public ResponseEntity<Void> register(@RequestBody RegisterRequestDto registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.ok().build();
    }
}
