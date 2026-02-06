package gr.aueb.cf.cookingfactory.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationProvider authenticationProvider) throws Exception {
        http
                .cors(httpSecurityCorsConfigurer -> httpSecurityCorsConfigurer.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(req -> req//
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()   // CORS preflight – must succeed before auth
                                .requestMatchers("/api/auth/authenticate").permitAll()
                                .requestMatchers(
                                        "/swagger-ui.html",        // The old Swagger UI HTML (if used)
                                        "/swagger-ui/**",          // All Swagger UI resources (JS, CSS, etc.)
                                        "/v3/api-docs/**",         // The API JSON docs
                                        "/v3/api-docs.yaml",       // YAML version of the docs
                                        "/swagger-resources/**",   // Swagger resource descriptors
                                        "/configuration/**"        // Swagger configuration endpoints
                                ).permitAll()

                                // Write operations (add, update) - ADMIN only
                                .requestMatchers(HttpMethod.POST, "/api/students", "/api/instructors", "/api/courses").hasRole("ADMIN")
                                .requestMatchers(HttpMethod.PUT, "/api/students/**", "/api/instructors/**", "/api/courses/**").hasRole("ADMIN")
                                // Read operations (view, get, search) - ADMIN and INSTRUCTOR
                                .requestMatchers(HttpMethod.GET, "/api/students/**", "/api/instructors/**", "/api/courses/**").hasAnyRole("ADMIN", "INSTRUCTOR")
                                .requestMatchers(HttpMethod.POST, "/api/students/search", "/api/instructors/search", "/api/courses/search").hasAnyRole("ADMIN", "INSTRUCTOR")
                                // All other /api/** require ADMIN (e.g. future endpoints)
                                .requestMatchers("/api/**").hasRole("ADMIN")

                                // .anyRequest().permitAll() // For dev environment only! Permits all requests in order to avoid blocking by CORS policy.
                                // To be commented out or disabled in production environment.
                                .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptions) -> exceptions
                        .authenticationEntryPoint(myCustomAuthenticationEntryPoint())
                        .accessDeniedHandler(myCustomAccessDeniedHandler()));

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "https://coding-factory.apps.gov.gr",
                "http://localhost:3000",
                "http://localhost:5173",
                "http://127.0.0.1:3000",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "Accept"));
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);   // Cache preflight for 1 hour
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailsService userDetailsService,
                                                         PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AccessDeniedHandler myCustomAccessDeniedHandler() {
        return new CustomAccessDeniedHandler();
    }

    @Bean
    public AuthenticationEntryPoint myCustomAuthenticationEntryPoint() {
        return new CustomAuthenticationEntryPoint();
    }
}

