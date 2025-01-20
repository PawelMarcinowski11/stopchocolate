package business.marcinowski.stopchocolate.auth;

import java.util.Collections;
import java.util.Map;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import business.marcinowski.stopchocolate.auth.dto.LoginRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RefreshRequestDto;
import business.marcinowski.stopchocolate.auth.dto.RegisterRequestDto;
import business.marcinowski.stopchocolate.auth.dto.TokenResponseDto;
import jakarta.ws.rs.core.Response;

@Service
public class AuthServiceImpl implements AuthService {

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
        credentials.setGrantType("password");
        credentials.setClientId(clientId);
        return executeTokenRequest(credentials);
    }

    @Override
    public TokenResponseDto refresh(RefreshRequestDto refreshRequest) {
        refreshRequest.setGrantType("refresh_token");
        refreshRequest.setClientId(clientId);
        return executeTokenRequest(refreshRequest);
    }

    @Override
    public void register(RegisterRequestDto registerRequest) {
        try {
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
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration failed");
            }
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration failed: " + e.getMessage());
        }
    }

    private <T> TokenResponseDto executeTokenRequest(T requestDto) {
        String tokenUrl = issuerUri + "/protocol/openid-connect/token";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        Map<String, Object> properties = new ObjectMapper().convertValue(requestDto,
                new TypeReference<Map<String, Object>>() {
                });
        properties.forEach((key, value) -> formData.add(key, value.toString()));

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(formData, headers);
        return restTemplate.postForObject(tokenUrl, request, TokenResponseDto.class);
    }
}
