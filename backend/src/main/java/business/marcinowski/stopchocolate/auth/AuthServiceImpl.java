package business.marcinowski.stopchocolate.auth;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.http.conn.HttpHostConnectException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

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
import business.marcinowski.stopchocolate.auth.entity.PasswordResetToken;
import business.marcinowski.stopchocolate.auth.exception.EmailAlreadyExistsException;
import business.marcinowski.stopchocolate.auth.exception.InternalServerErrorException;
import business.marcinowski.stopchocolate.auth.exception.InvalidCredentialsException;
import business.marcinowski.stopchocolate.auth.exception.InvalidRefreshTokenException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakServiceException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakUnavailableException;
import business.marcinowski.stopchocolate.auth.exception.UsernameAlreadyExistsException;
import business.marcinowski.stopchocolate.auth.repository.PasswordResetTokenRepository;
import business.marcinowski.stopchocolate.mail.EmailService;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.core.Response;

@Service
public class AuthServiceImpl implements AuthService {
    private static final String INVALID_GRANT_ERROR = "invalid_grant";

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${password-reset.token-expiry-minutes}")
    private Long tokenExpiryTime;

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    Keycloak keycloak;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    EmailService emailService;

    @Override
    public TokenResponseDto login(LoginRequestDto credentials) {
        try {
            return attemptLogin(credentials.getUsername(), credentials.getPassword());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                throw new InvalidCredentialsException("Invalid username or password");
            }
            throw new KeycloakServiceException("Authentication request failed");
        } catch (RestClientException e) {
            throw new KeycloakServiceException("Unable to connect to authentication service");
        }
    }

    @Override
    public TokenResponseDto refresh(RefreshRequestDto refreshRequest) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("refresh_token", refreshRequest.getRefreshToken());
            formData.add("grant_type", "refresh_token");
            formData.add("client_id", clientId);
            return executeTokenRequest(formData);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 400 && e.getMessage() != null
                    && e.getMessage().toLowerCase().contains(INVALID_GRANT_ERROR)) {
                throw new InvalidRefreshTokenException("The refresh token is invalid or has expired");
            }
            throw new KeycloakServiceException("Token refresh failed");
        } catch (RestClientException e) {
            throw new KeycloakServiceException("Unable to connect to authentication service");
        }
    }

    @Override
    public void register(RegisterRequestDto registerRequest) {
        try {
            if (!keycloak.realm(realm).users()
                    .searchByUsername(registerRequest.getUsername(), true).isEmpty()) {
                throw new UsernameAlreadyExistsException("A user with this username already exists");
            }

            if (!keycloak.realm(realm).users()
                    .searchByEmail(registerRequest.getEmail(), true).isEmpty()) {
                throw new EmailAlreadyExistsException("A user with this email address already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setEnabled(true);
            user.setUsername(registerRequest.getUsername());
            user.setEmail(registerRequest.getEmail());

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(registerRequest.getPassword());
            user.setCredentials(Collections.singletonList(credential));

            Response response = keycloak.realm(realm).users().create(user);

            if (response.getStatus() != 201) {
                throw new KeycloakServiceException("Registration failed");
            }
        } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException | KeycloakServiceException e) {
            throw e;
        } catch (Exception e) {
            if (e.getCause() instanceof HttpHostConnectException) {
                throw new KeycloakUnavailableException("Authentication service unavailable");
            }
            throw new KeycloakServiceException("Authentication service error");
        }
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequestDto forgotPasswordRequest) {
        try {
            String token = UUID.randomUUID().toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            String userId;
            String username;
            List<UserRepresentation> usersFound = keycloak.realm(realm).users()
                    .searchByEmail(forgotPasswordRequest.getEmail(), true);

            if (usersFound.isEmpty()) {
                // Exit silently without informing about wrong email address
                // to prevent data harvesting / enumeration
                return;
            } else {
                userId = usersFound.getFirst().getId();
                username = usersFound.getFirst().getUsername();
            }

            PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                    .expiryDate(Instant.now().plusSeconds(tokenExpiryTime * 60))
                    .hashedToken(hash)
                    .userId(userId)
                    .build();

            emailService.sendPasswordResetMail(forgotPasswordRequest.getEmail(), username, token);
            passwordResetTokenRepository.save(passwordResetToken);
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Failed to execute forgot password flow");
        }
    }

    @Override
    @Transactional
    public ValidateResetTokenResponseDto validateResetToken(ValidateResetTokenRequestDto validateResetTokenRequest) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest
                    .digest(validateResetTokenRequest.getPasswordResetToken().getBytes(StandardCharsets.UTF_8));
            ValidateResetTokenResponseDto response = new ValidateResetTokenResponseDto();
            response.setValid(
                    passwordResetTokenRepository.findByHashedTokenAndExpiryDateAfter(hash, Instant.now()).isPresent());
            return response;
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Failed to validate password reset token");
        }
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequestDto resetPasswordRequest) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(resetPasswordRequest.getPasswordResetToken().getBytes(StandardCharsets.UTF_8));
            PasswordResetToken passwordResetToken = passwordResetTokenRepository
                    .findByHashedTokenAndExpiryDateAfter(hash, Instant.now()).orElse(null);
            if (passwordResetToken != null) {
                CredentialRepresentation passwordCredentials = new CredentialRepresentation();
                passwordCredentials.setTemporary(false);
                passwordCredentials.setType(CredentialRepresentation.PASSWORD);
                passwordCredentials.setValue(resetPasswordRequest.getPassword());

                keycloak.realm(realm).users().get(passwordResetToken.getUserId())
                        .resetPassword(passwordCredentials);
                passwordResetTokenRepository.delete(passwordResetToken);
            }
        } catch (NoSuchAlgorithmException e) {
            throw new InternalServerErrorException("Failed to reset password");
        }
    }

    @Override
    public void updatePassword(UpdatePasswordRequestDto updatePasswordRequestDto) {
        String userId = getUserIdFromSecurityContextHolder();
        String username = keycloak.realm(realm).users().get(userId).toRepresentation().getUsername();

        try {
            attemptLogin(username, updatePasswordRequestDto.getCurrentPassword());
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 401) {
                throw new InvalidCredentialsException("Current password is incorrect");
            }
            throw new KeycloakServiceException("Unable to verify the current password");
        } catch (RestClientException e) {
            throw new KeycloakServiceException("Unable to connect to authentication service");
        }

        CredentialRepresentation passwordCredentials = new CredentialRepresentation();
        passwordCredentials.setTemporary(false);
        passwordCredentials.setType(CredentialRepresentation.PASSWORD);
        passwordCredentials.setValue(updatePasswordRequestDto.getNewPassword());

        keycloak.realm(realm).users().get(userId).resetPassword(passwordCredentials);
    }

    @Override
    public void updateUsername(UpdateUsernameRequestDto updateUsernameRequestDto) {
        String userId = getUserIdFromSecurityContextHolder();
        List<UserRepresentation> matchingUsers = keycloak.realm(realm).users()
                .searchByUsername(updateUsernameRequestDto.getUsername(), true);

        if (!matchingUsers.isEmpty()
                && (matchingUsers.size() > 1 || !matchingUsers.getFirst().getId().equals(userId))) {
            throw new UsernameAlreadyExistsException("Username already taken");
        }

        UserRepresentation userRepresentation = keycloak.realm(realm).users().get(userId).toRepresentation();
        userRepresentation.setUsername(updateUsernameRequestDto.getUsername());
        keycloak.realm(realm).users().get(userId).update(userRepresentation);
    }

    @Override
    public void updateEmail(UpdateEmailRequestDto updateEmailRequestDto) {
        String userId = getUserIdFromSecurityContextHolder();
        List<UserRepresentation> matchingUsers = keycloak.realm(realm).users()
                .searchByEmail(updateEmailRequestDto.getEmail(), true);

        if (!matchingUsers.isEmpty()
                && (matchingUsers.size() > 1 || !matchingUsers.getFirst().getId().equals(userId))) {
            throw new EmailAlreadyExistsException("Email already taken");
        }

        UserRepresentation userRepresentation = keycloak.realm(realm).users().get(userId).toRepresentation();
        userRepresentation.setEmail(updateEmailRequestDto.getEmail());
        keycloak.realm(realm).users().get(userId).update(userRepresentation);
    }

    private String getUserIdFromSecurityContextHolder() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        String userId = authentication.getToken().getSubject();
        return userId;
    }

    private TokenResponseDto attemptLogin(String username, String password) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("username", username);
        formData.add("password", password);
        formData.add("grant_type", "password");
        formData.add("client_id", clientId);
        return executeTokenRequest(formData);
    }

    private TokenResponseDto executeTokenRequest(MultiValueMap<String, String> formData) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        return restTemplate.postForObject(tokenUrl, request, TokenResponseDto.class);
    }
}
