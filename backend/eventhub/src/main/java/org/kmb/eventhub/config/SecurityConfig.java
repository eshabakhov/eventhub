//package org.kmb.eventhub.config;
//
//import org.kmb.eventhub.enums.RoleEnum;
//import org.kmb.eventhub.exception.UserNotFoundException;
//import org.kmb.eventhub.repository.UserRepository;
//import org.kmb.eventhub.tables.pojos.User;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
//import org.springframework.security.core.userdetails.UserDetailsService;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.web.SecurityFilterChain;
//
//import java.util.Objects;
//
//@Configuration
//@EnableWebSecurity
//public class SecurityConfig {
//
//
//    @Bean
//    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//        http
//                .csrf(AbstractHttpConfigurer::disable)
//                .cors(AbstractHttpConfigurer::disable)
//                .authorizeHttpRequests(auth -> auth
//                        .requestMatchers("/api/*/users/moderator").hasRole(RoleEnum.MODERATOR.name())
//                        .requestMatchers("/api/*/users/organizer").hasRole(RoleEnum.ORGANIZER.name())
//                        .requestMatchers("/api/*/users/member").hasRole(RoleEnum.MEMBER.name())
//                        .anyRequest().authenticated()
//                )
//                .httpBasic(httpBasic -> {});
//        return http.build();
//    }
//
//    @Bean
//    public UserDetailsService userDetailsService(UserRepository userRepository) {
//        return username -> {
//
//            User user = userRepository.fetchByUsername(username);
//            if (Objects.isNull(user)) {
//                throw new UserNotFoundException(-1L);
//            }
//
//            return org.springframework.security.core.userdetails.User.builder()
//                    .username(user.getUsername())
//                    .password(user.getPassword())
//                    .roles(user.getRole().getLiteral())
//                    .build();
//        };
//    }
//
//    @Bean
//    public PasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//}
