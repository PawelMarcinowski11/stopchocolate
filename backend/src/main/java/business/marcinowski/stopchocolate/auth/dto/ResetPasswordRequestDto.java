package business.marcinowski.stopchocolate.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import business.marcinowski.stopchocolate.auth.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequestDto {
    @NotBlank(message = "New password is required")
    @ValidPassword
    private String password;

    @NotBlank(message = "Password reset token is required")
    @JsonProperty("password_reset_token")
    private String passwordResetToken;
}
