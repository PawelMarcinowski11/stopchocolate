package business.marcinowski.stopchocolate.auth;

import java.util.Collections;

import org.apache.http.conn.HttpHostConnectException;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;
import business.marcinowski.stopchocolate.auth.exception.EmailAlreadyExistsException;
import business.marcinowski.stopchocolate.auth.exception.InvalidCredentialsException;
import business.marcinowski.stopchocolate.auth.exception.InvalidRefreshTokenException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakServiceException;
import business.marcinowski.stopchocolate.auth.exception.KeycloakUnavailableException;
import business.marcinowski.stopchocolate.auth.exception.UsernameAlreadyExistsException;
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

    private final RestTemplate restTemplate = new RestTemplate();

    @Autowired
    Keycloak keycloak;

    @Override
    public TokenResponseDto login(LoginRequestDto credentials) {
        try {
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            formData.add("username", credentials.getUsername());
            formData.add("password", credentials.getPassword());
            formData.add("grant_type", "password");
            formData.add("client_id", clientId);
            return executeTokenRequest(formData);
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

    private TokenResponseDto executeTokenRequest(MultiValueMap<String, String> formData) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        return restTemplate.postForObject(tokenUrl, request, TokenResponseDto.class);
    }
}
