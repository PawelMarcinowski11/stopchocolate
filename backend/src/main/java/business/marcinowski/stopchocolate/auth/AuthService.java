package business.marcinowski.stopchocolate.auth;

import org.springframework.web.bind.annotation.RequestBody;

import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;
import jakarta.validation.Valid;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto credentials);

    TokenResponseDto refresh(RefreshRequestDto refreshRequest);

    void register(@Valid @RequestBody RegisterRequestDto registerRequest);
}
