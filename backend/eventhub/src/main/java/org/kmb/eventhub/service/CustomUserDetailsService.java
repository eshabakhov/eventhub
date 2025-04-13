package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)  {
        User user = userRepository.fetchByUsername(username);

        if (Objects.isNull(user))
                throw new UserNotFoundException(-1L);

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()))
        );
    }
}
