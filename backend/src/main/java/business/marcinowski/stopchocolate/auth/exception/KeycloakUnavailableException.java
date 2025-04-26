
package business.marcinowski.stopchocolate.auth.exception;

public class KeycloakUnavailableException extends RuntimeException {
    public KeycloakUnavailableException(String message) {
        super(message);
    }
}