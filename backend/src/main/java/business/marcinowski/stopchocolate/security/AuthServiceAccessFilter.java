package business.marcinowski.stopchocolate.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuthServiceAccessFilter extends OncePerRequestFilter {

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (AuthenticationServiceException e) {
            if (e.getCause() != null && e.getCause() instanceof JwtException) {
                ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                        org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE,
                        "Couldn't retrieve remote JWK set");
                problemDetail.setType(URI.create("https://api.stopchocolate.com/errors/auth-service-unavailable"));
                problemDetail.setTitle("Authentication Service Unavailable");
                problemDetail.setInstance(URI.create(request.getRequestURI()));

                response.setStatus(problemDetail.getStatus());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                objectMapper.writeValue(response.getOutputStream(), problemDetail);
                return;
            }
            throw e;
        }
    }
}
