package business.marcinowski.stopchocolate.auth;

import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto credentials);

    TokenResponseDto refresh(RefreshRequestDto refreshRequest);

    void logout(String accessToken);

    void register(RegisterRequestDto registerRequest);
}
