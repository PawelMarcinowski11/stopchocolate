package business.marcinowski.stopchocolate.auth.dto;

import business.marcinowski.stopchocolate.auth.validator.ValidPassword;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdatePasswordRequestDto {
    @NotBlank(message = "Current password is required")
    @ValidPassword
    private String currentPassword;

    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;
}
