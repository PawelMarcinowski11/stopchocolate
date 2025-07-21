package business.marcinowski.stopchocolate.auth;

import business.marcinowski.stopchocolate.auth.dto.ForgotPasswordRequestDto;
import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.ResetPasswordRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;
import business.marcinowski.stopchocolate.auth.dto.UpdateEmailRequestDto;
import business.marcinowski.stopchocolate.auth.dto.UpdatePasswordRequestDto;
import business.marcinowski.stopchocolate.auth.dto.UpdateUsernameRequestDto;
import business.marcinowski.stopchocolate.auth.dto.ValidateResetTokenRequestDto;
import business.marcinowski.stopchocolate.auth.dto.ValidateResetTokenResponseDto;

public interface AuthService {
    TokenResponseDto login(LoginRequestDto credentials);

    TokenResponseDto refresh(RefreshRequestDto refreshRequest);

    void register(RegisterRequestDto registerRequest);

    void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequest);

    ValidateResetTokenResponseDto validateResetToken(ValidateResetTokenRequestDto validateResetTokenRequest);

    void resetPassword(ResetPasswordRequestDto resetPasswordRequest);

    void updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto);

    void updateUsername(UpdateUsernameRequestDto updateUsernameRequestDto);

    void updateEmail(UpdateEmailRequestDto updateEmailRequestDto);
}
