
package business.marcinowski.stopchocolate.auth.exception;

public class KeycloakServiceException extends RuntimeException {
    public KeycloakServiceException(String message) {
        super(message);
    }
}