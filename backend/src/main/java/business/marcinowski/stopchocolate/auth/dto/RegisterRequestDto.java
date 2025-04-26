package business.marcinowski.stopchocolate.auth.dto;

import business.marcinowski.stopchocolate.auth.validator.ValidPassword;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDto {
    @NotBlank(message = "Username is required")
    private String username;
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address format")
    private String email;
    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;
}
