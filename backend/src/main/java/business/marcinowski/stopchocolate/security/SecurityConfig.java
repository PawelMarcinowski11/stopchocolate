package business.marcinowski.stopchocolate.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private CustomAccessDeniedHandler accessDeniedHandler;

        @Autowired
        private CustomAuthenticationEntryPoint authenticationEntryPoint;

        @Autowired
        private AuthServiceAccessFilter keycloakAccessFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.addFilterBefore(keycloakAccessFilter, BearerTokenAuthenticationFilter.class);
                http.oauth2ResourceServer(server -> server
                                .jwt(Customizer.withDefaults())
                                .accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint));
                http.exceptionHandling(x -> x.accessDeniedHandler(accessDeniedHandler)
                                .authenticationEntryPoint(authenticationEntryPoint));
                http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                http.csrf(csrf -> csrf.disable());
                http.cors(cors -> cors.disable());
                http.sessionManagement(management -> management.disable());
                http.logout(logout -> logout.disable());
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers(new AntPathRequestMatcher("/auth/login", HttpMethod.POST.toString()))
                                .permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/auth/refresh", HttpMethod.POST.toString()))
                                .permitAll()
                                .requestMatchers(
                                                new AntPathRequestMatcher("/auth/register", HttpMethod.POST.toString()))
                                .permitAll()
                                .anyRequest().denyAll());
                return http.build();
        }
}
