package app;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationEntryPoint entryPoint,
            AccessDeniedHandler accessDeniedHandler
    ) throws Exception {
        return http
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/api/earth-meter/v1/takeout").permitAll()
                            .requestMatchers("/api/earth-meter/v1/walk-history/{user}").permitAll()
                            .requestMatchers("/api/earth-meter/v1/events").permitAll())
                .oauth2ResourceServer((oauth2) ->
                        oauth2.opaqueToken(Customizer.withDefaults())
                                .authenticationEntryPoint(entryPoint))
                .exceptionHandling(h -> {
                    h.authenticationEntryPoint(entryPoint);
                    h.accessDeniedHandler(accessDeniedHandler);
                })
                .csrf(AbstractHttpConfigurer::disable)
                .build();
    }
}