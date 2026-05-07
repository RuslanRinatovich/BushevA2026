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
                        .requestMatchers("/clients/**").authenticated()

                        // Только директор
                        .requestMatchers("/admin/**", "/users/**").hasRole("DIRECTOR")

                        // Материалы – директор и мастер
                        .requestMatchers("/materials/**").hasAnyRole("DIRECTOR", "MASTER")

                        // Продукция – директор и мастер
                        .requestMatchers("/products/**").hasAnyRole("DIRECTOR", "MASTER")

                        // Производство – директор и мастер (исправлено)
                        .requestMatchers("/production/**").hasAnyRole("DIRECTOR", "MASTER")

                        // Отгрузки – кладовщик
                        .requestMatchers("/shipments/**").hasRole("STOREKEEPER")

                        // Отчёты – по ролям
                        .requestMatchers("/reports/balances").hasAnyRole("DIRECTOR", "MASTER", "STOREKEEPER")
                        .requestMatchers("/reports/production").hasAnyRole("DIRECTOR", "MASTER")
                        .requestMatchers("/reports/revenue").hasRole("DIRECTOR")

                        // Экспорт в Excel – для соответствующих ролей
                        .requestMatchers("/materials/export/excel").hasAnyRole("DIRECTOR", "MASTER")
                        .requestMatchers("/products/export/excel").hasAnyRole("DIRECTOR", "MASTER")
                        .requestMatchers("/clients/export/excel").hasAnyRole("DIRECTOR", "MASTER")
                        .requestMatchers("/production/orders/export/excel").hasAnyRole("DIRECTOR", "MASTER")
                        .requestMatchers("/shipments/export/excel").hasRole("STOREKEEPER")

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