package dev.hieu.mcenterproject.security;

import dev.hieu.mcenterproject.Eum.UsersRole;
import dev.hieu.mcenterproject.model.Course;
import dev.hieu.mcenterproject.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(CustomUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain defaultFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Disable CSRF for simplicity
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/register", "/login", "/error", "/css/**", "/js/**", "/home", "/test", "/loginSuccess", "/forgot-password", "/confirm-email", "/giang-vien", "/giang-vien/**", "/about", "/tieng-hat-hoc-vien", "/contact", "/event","/event/**").permitAll() // Allow public access
                        .requestMatchers("/profile").authenticated() // Requires authentication for profile
                        .requestMatchers("/teacher/**").hasRole(String.valueOf(UsersRole.TEACHER)) // Restrict access to teacher routes
                        .requestMatchers("/admin/**").hasRole(String.valueOf(UsersRole.ADMIN)) // Only admins can access admin paths
                        .anyRequest().authenticated() // All other paths require authentication
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/loginSuccess", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @GetMapping("/admin/addCourse")
    public String addCoursePage(Model model) {
        model.addAttribute("course", new Course());
        return "addCourse";
    }

    @Bean
    public AuthenticationManager authManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authManagerBuilder.authenticationProvider(authenticationProvider());
        return authManagerBuilder.build();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }
}
