package business.marcinowski.stopchocolate.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ValidateResetTokenRequestDto {
    @NotBlank(message = "Password reset token is required")
    @JsonProperty("password_reset_token")
    private String passwordResetToken;
}
