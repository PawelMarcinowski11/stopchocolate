package business.marcinowski.stopchocolate.mail;

public interface EmailService {
    public void sendPasswordResetMail(String recipientAddress, String username, String token);
}
