
package business.marcinowski.stopchocolate.auth.exception;

import org.springframework.security.core.AuthenticationException;

public class KeycloakServiceException extends AuthenticationException {
    public KeycloakServiceException(String message) {
        super(message);
    }
}