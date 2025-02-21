package business.marcinowski.stopchocolate.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        ProblemDetail problemDetail;

        if (authException instanceof InvalidBearerTokenException) {
            problemDetail = ProblemDetail.forStatusAndDetail(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "The access token is invalid or has expired");
            problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/invalid-access-token"));
            problemDetail.setTitle("Invalid Access Token");
        } else if (authException instanceof InsufficientAuthenticationException) {
            problemDetail = ProblemDetail.forStatusAndDetail(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "No authentication token provided");
            problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/unauthenticated"));
            problemDetail.setTitle("Authentication Required");
        } else {
            problemDetail = ProblemDetail.forStatusAndDetail(
                    org.springframework.http.HttpStatus.UNAUTHORIZED,
                    "Authentication failed");
            problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/unauthenticated"));
            problemDetail.setTitle("Authentication Failed");
        }

        problemDetail.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(problemDetail.getStatus());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problemDetail);
    }
}
