package business.marcinowski.stopchocolate.config;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${keycloak.admin-username}")
    private String adminUsername;

    @Value("${keycloak.admin-password}")
    private String adminPassword;

    @Value("${keycloak.realm}")
    private String realm;

    @Bean
    public Keycloak keycloak() {
        String serverUrl = issuerUri.replace("/realms/" + realm, "");
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .grantType("password")
                .clientId("admin-cli")
                .username(adminUsername)
                .password(adminPassword)
                .build();
    }
}
