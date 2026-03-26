package com.bolsaempleo.config;

import com.bolsaempleo.security.CustomUserDetails;
import com.bolsaempleo.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;

import java.io.IOException;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public routes
                .requestMatchers(
                    "/", "/login", "/logout",
                    "/registro/empresa", "/registro/oferente",
                    "/puestos/publicos", "/puestos/buscar",
                    "/css/**", "/js/**", "/images/**", "/webjars/**"
                ).permitAll()
                // Role-protected routes
                .requestMatchers("/empresa/**").hasRole("EMPRESA")
                .requestMatchers("/oferente/**").hasRole("OFERENTE")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(roleBasedSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            // Disable CSRF only for file-upload endpoints if needed; keep it enabled otherwise
            .csrf(csrf -> csrf.ignoringRequestMatchers("/oferente/cv/subir"));

        return http.build();
    }

    /**
     * After a successful login, redirect the user to their role-specific dashboard.
     */
    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return new SimpleUrlAuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                                HttpServletResponse response,
                                                Authentication authentication) throws IOException {
                CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
                String target = switch (user.getRole()) {
                    case "ROLE_EMPRESA"  -> "/empresa/dashboard";
                    case "ROLE_OFERENTE" -> "/oferente/dashboard";
                    case "ROLE_ADMIN"    -> "/admin/dashboard";
                    default              -> "/";
                };
                clearAuthenticationAttributes(request);
                response.sendRedirect(request.getContextPath() + target);
            }
        };
    }
}
