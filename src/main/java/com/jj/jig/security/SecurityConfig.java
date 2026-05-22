package com.jj.jig.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/jigs/new", "/jigs/*/edit").hasAnyRole("ADMIN", "SUPERVISOR", "ENGINEER")
                        .requestMatchers("/jigs/*/delete").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/jigs/*/status", "/jigs/*/due-date").hasAnyRole("ADMIN", "SUPERVISOR", "ENGINEER")
                        .requestMatchers("/jigs/files/*/delete").hasAnyRole("ADMIN", "SUPERVISOR")
                        .requestMatchers("/jigs/files/*/replace").hasAnyRole("ADMIN", "SUPERVISOR", "ENGINEER")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
