package business.marcinowski.stopchocolate.auth;

import java.net.URI;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.mail.MailException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import business.marcinowski.stopchocolate.auth.exception.EmailAlreadyExistsException;
import business.marcinowski.stopchocolate.auth.exception.InternalServerErrorException;
import business.marcinowski.stopchocolate.auth.exception.InvalidAccessTokenException;
import business.marcinowski.stopchocolate.auth.exception.InvalidCredentialsException;
import business.marcinowski.stopchocolate.auth.exception.InvalidRefreshTokenException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakServiceException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakUnavailableException;
import business.marcinowski.stopchocolate.auth.exception.UsernameAlreadyExistsException;

@RestControllerAdvice("business.marcinowski.stopchocolate.auth")
public class AuthExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleInvalidCredentials(InvalidCredentialsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/invalid-credentials"));
        problemDetail.setTitle("Invalid Credentials");
        return problemDetail;
    }

    @ExceptionHandler(InvalidAccessTokenException.class)
    public ProblemDetail handleInvalidAccessToken(InvalidAccessTokenException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/invalid-access-token"));
        problemDetail.setTitle("Invalid Access Token");
        return problemDetail;
    }

    @ExceptionHandler(InvalidRefreshTokenException.class)
    public ProblemDetail handleInvalidRefreshToken(InvalidRefreshTokenException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/invalid-refresh-token"));
        problemDetail.setTitle("Invalid Refresh Token");
        return problemDetail;
    }

    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ProblemDetail handleUsernameExists(UsernameAlreadyExistsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/username-exists"));
        problemDetail.setTitle("Username Already Exists");
        return problemDetail;
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailExists(EmailAlreadyExistsException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/email-exists"));
        problemDetail.setTitle("Email Already Exists");
        return problemDetail;
    }

    @ExceptionHandler(KeycloakServiceException.class)
    public ProblemDetail handleKeycloakService(KeycloakServiceException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/auth-service-error"));
        problemDetail.setTitle("Authentication Service Error");
        return problemDetail;
    }

    @ExceptionHandler(KeycloakUnavailableException.class)
    public ProblemDetail handleKeycloakUnavailable(KeycloakUnavailableException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/auth-service-unavailable"));
        problemDetail.setTitle("Authentication Service Unavailable");
        return problemDetail;
    }

    @ExceptionHandler(MailException.class)
    public ProblemDetail handleMailException(MailException e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                e.getMessage());
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/mail-service-unavailable"));
        problemDetail.setTitle("Mail Service Unavailable");
        return problemDetail;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequestData(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                message);
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/invalid-request-data"));
        problemDetail.setTitle("Invalid Request Data");
        return problemDetail;
    }

    @ExceptionHandler({ InternalServerErrorException.class, Exception.class })
    public ProblemDetail handleInternalServerError(Exception e) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred");
        problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/server-error"));
        problemDetail.setTitle("Internal Server Error");
        return problemDetail;
    }
}
