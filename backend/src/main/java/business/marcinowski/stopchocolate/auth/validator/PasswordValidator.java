package business.marcinowski.stopchocolate.auth.validator;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class PasswordValidator implements ConstraintValidator<ValidPassword, String> {

    @Value("${MIN_PASSWORD_LENGTH}")
    private int minLength;

    @Value("${MAX_PASSWORD_LENGTH}")
    private int maxLength;

    @Value("${MIN_DIGITS}")
    private int minDigits;

    @Value("${MIN_UPPERCASE}")
    private int minUppercase;

    @Value("${MIN_LOWERCASE}")
    private int minLowercase;

    @Value("${MIN_SPECIAL_CHARS}")
    private int minSpecialChars;

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.trim().isEmpty()) {
            return true;
        }

        context.disableDefaultConstraintViolation();

        long digitCount = password.chars().filter(Character::isDigit).count();
        long upperCount = password.chars().filter(Character::isUpperCase).count();
        long lowerCount = password.chars().filter(Character::isLowerCase).count();
        long specialCount = password.chars().filter(ch -> !Character.isLetterOrDigit(ch)).count();

        if (password.length() < minLength || password.length() > maxLength) {
            context.buildConstraintViolationWithTemplate(
                    "Password must be between " + minLength + " and " + maxLength + " characters")
                    .addConstraintViolation();
            return false;
        }

        if (digitCount < minDigits) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least " + minDigits + " digit(s)")
                    .addConstraintViolation();
            return false;
        }

        if (upperCount < minUppercase) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least " + minUppercase + " uppercase letter(s)")
                    .addConstraintViolation();
            return false;
        }

        if (lowerCount < minLowercase) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least " + minLowercase + " lowercase letter(s)")
                    .addConstraintViolation();
            return false;
        }

        if (specialCount < minSpecialChars) {
            context.buildConstraintViolationWithTemplate(
                    "Password must contain at least " + minSpecialChars + " special character(s)")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
