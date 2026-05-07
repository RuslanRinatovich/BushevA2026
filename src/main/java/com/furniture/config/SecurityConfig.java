package com.furniture.config;

import com.furniture.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .userDetailsService(userDetailsService)
                .authorizeHttpRequests(auth -> auth
                        // Публичные страницы
                        .requestMatchers("/login", "/css/**", "/webjars/**", "/images/**").permitAll()
                        // Страницы доступные всем авторизованным
                        .requestMatchers("/dashboard").authenticated()
                        // Только директор
                        .requestMatchers("/admin/**", "/users/**").hasRole("DIRECTOR")
                        // Материалы – директор и мастер
                        .requestMatchers("/materials/**").hasAnyRole("DIRECTOR", "MASTER")
                        // Продукция – директор и мастер
                        .requestMatchers("/products/**").hasAnyRole("DIRECTOR", "MASTER")
                        // Производство – мастер
                        .requestMatchers("/production/**").hasRole("MASTER")
                        // Отгрузка – кладовщик
                        .requestMatchers("/shipments/**").hasRole("STOREKEEPER")
                        // Клиенты – все
                        .requestMatchers("/clients/**").authenticated()
                        // Отчёты – все авторизованные
                        .requestMatchers("/reports/**").authenticated()

                        .requestMatchers("/reports/balances").hasAnyRole("DIRECTOR", "MANAGER", "STOREKEEPER")
                        .requestMatchers("/reports/production").hasRole("DIRECTOR")
                        .requestMatchers("/reports/revenue").hasRole("DIRECTOR")
                        .requestMatchers("/reports/**").hasRole("DIRECTOR")
                        // Остальное – только авторизованные
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .defaultSuccessUrl("/dashboard", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                        .logoutSuccessUrl("/login?logout")
                        .permitAll()
                )
                .csrf(csrf -> csrf.disable());

        return http.build();
    }
}