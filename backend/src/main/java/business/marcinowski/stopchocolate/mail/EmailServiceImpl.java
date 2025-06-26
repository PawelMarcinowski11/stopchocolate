package business.marcinowski.stopchocolate.mail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Template;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.ws.rs.InternalServerErrorException;

@Component
public class EmailServiceImpl implements EmailService {

  @Value("${spring.mail.sender-address}")
  private String senderAddress;

  @Value("${spring.mail.sender-display-name}")
  private String senderDisplayName;

  @Value("${password-reset.token-expiry-minutes}")
  private Long tokenExpiryTime;

  @Autowired
  private JavaMailSender mailSender;

  @Override
  public void sendPasswordResetMail(String recipientAddress, String username, String token) {
    // TODO prepare activation link
    Map<String, String> parameters = new HashMap<>();
    parameters.put("username", username);
    parameters.put("expiryTime", tokenExpiryTime.toString());
    sendHtmlTemplateMail(recipientAddress, "templates/password-reset-email", parameters);
  }

  private void sendHtmlTemplateMail(String recipientAddress, String path, Object parameters) {
    try {
      Handlebars handlebars = new Handlebars();
      Template template = handlebars.compile(path);
      String templateString = template.apply(parameters);

      MimeMessage message = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

      helper.setFrom(senderAddress, senderDisplayName);
      helper.setTo(recipientAddress);
      helper.setSubject("subject");
      helper.setText(templateString, true);

      mailSender.send(message);
    } catch (IOException e) {
      throw new InternalServerErrorException("Failed to prepare e-mail message");
    } catch (MessagingException | MailException e) {
      throw new MailSendException("Failed to send e-mail message");
    }
  }
}