package business.marcinowski.stopchocolate.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http.oauth2ResourceServer(server -> server
                                .jwt(Customizer.withDefaults()));
                http.sessionManagement(management -> management.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
                http.csrf(csrf -> csrf.disable());
                http.cors(cors -> cors.disable());
                http.sessionManagement(management -> management.disable());
                http.logout(logout -> logout.disable());
                http.authorizeHttpRequests(auth -> auth
                                .requestMatchers(new AntPathRequestMatcher("/auth/login", HttpMethod.POST.toString()))
                                .permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/auth/logout", HttpMethod.POST.toString()))
                                .permitAll()
                                .requestMatchers(
                                                new AntPathRequestMatcher("/auth/register", HttpMethod.POST.toString()))
                                .permitAll()
                                .anyRequest().permitAll());
                return http.build();
        }
}
