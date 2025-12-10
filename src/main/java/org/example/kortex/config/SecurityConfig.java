package org.example.kortex.config;

import org.example.kortex.users.db.User;
import org.example.kortex.users.domain.UserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collections;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final UserService userService;

    public SecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        return http.csrf()
                .disable()//Отключить защиту от кибер атак
                .authorizeRequests()

                .antMatchers("/","/login","/codeEmail","/forgotPassword","/recoveryPassword","/register",
                        "/api/auth/**","/error","/api/products/**","/*.html","/*.css","/*js")
                        .permitAll()
                .antMatchers("/profile",
                        "/api/users/role-request/**","/api/users/**","/api/orders/**","/api/carts/**")
                        .hasAnyRole(User.Role.USER.name(),User.Role.ADMIN.name(),User.Role.COURIER.name(),User.Role.SELLER.name())
                .antMatchers("/api/couriers/**")
                        .hasAnyRole(User.Role.ADMIN.name(),User.Role.COURIER.name())
                .antMatchers("/seller","/api/sellers/**")
                        .hasAnyRole(User.Role.ADMIN.name(),User.Role.SELLER.name())
                .antMatchers("/admin","/api/admin/role-request/**")
                        .hasRole(User.Role.ADMIN.name())
                .anyRequest().permitAll()

                .and().formLogin().loginPage("/login").permitAll().usernameParameter("email").defaultSuccessUrl("/")
                .and().logout().logoutUrl("/logout").permitAll().logoutSuccessUrl("/")
                .and().build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
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