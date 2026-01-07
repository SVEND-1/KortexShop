package org.example.kortex.config;

import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Collections;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;
    private final JwtFilter jwtFilter;

    public SecurityConfig(UserService userService,@Lazy JwtFilter jwtFilter) {
        this.userService = userService;
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http.csrf()
                .disable()
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)

                .authorizeRequests()
                .antMatchers("/", "/login", "/codeEmail", "/forgotPassword",
                        "/recoveryPassword", "/register", "/api/auth/**",
                        "/error", "/api/products/**", "/*.html", "/*.css", "/*.js")
                .permitAll()

                .antMatchers("/profile", "/cart", "/api/users/role-request/**",
                        "/api/users/**", "/api/orders/**", "/api/carts/**")
                .hasAnyRole("USER", "ADMIN", "COURIER", "SELLER")
                .antMatchers("/api/couriers/**", "/courier")
                .hasAnyRole("ADMIN", "COURIER")
                .antMatchers("/seller", "/api/sellers/**")
                .hasAnyRole("ADMIN", "SELLER")
                .antMatchers("/admin", "/api/admin/role-request/**")
                .hasRole("ADMIN")
                .anyRequest().permitAll()
                .and()

                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.sendRedirect("/login");
                        })
                )

                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/")
                        .deleteCookies("jwtToken")//TODO может это есть уже в authService
                        .permitAll()
                )
                .build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return new UserDetailsService() {

            @Override
            public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
                User user = userService.getByEmail(username);
                if(user == null)
                    throw new UsernameNotFoundException(username);
                Set<SimpleGrantedAuthority> roles = Collections.singleton(user.getRole().toAuthority());
                return new org.springframework.security.core.userdetails.User(user.getEmail(),user.getPassword(),roles);
            }
        };
    }

}