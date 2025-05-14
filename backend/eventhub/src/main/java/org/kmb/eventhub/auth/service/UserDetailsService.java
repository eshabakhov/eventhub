package org.kmb.eventhub.auth.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.dto.AuthResponse;
import org.kmb.eventhub.auth.util.CustomUserDetails;
import org.kmb.eventhub.config.jwt.JwtProperties;
import org.kmb.eventhub.config.jwt.JwtUtil;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.mapper.UserMapper;
import org.kmb.eventhub.user.repository.UserRepository;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.daos.OrganizerDao;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService {

    private UserRepository userRepository;

    private final OrganizerDao organizerDao;
    private final MemberDao memberDao;
    private final ModeratorDao moderatorDao;

    private final UserMapper userMapper;

    private JwtUtil jwtUtil;

    private JwtProperties jwtProperties;

    @Override
    public UserDetails loadUserByUsername(String username)  {

        User user = userRepository.fetchByUsername(username);

        if (Objects.isNull(user))
                throw new UserNotFoundException(-1L);

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()))
        );
    }

    public UserDetails loadUserByEmail(String email)  {

        User user = userRepository.fetchByEmail(email);

        if (Objects.isNull(user))
            throw new UserNotFoundException(-1L);

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.getRole(),
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().toString()))
        );
    }
    
    public User getAuthenticatedUser() {
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.fetchByUsername(authenticatedUsername);
    }

    public AuthResponse createAuthResponse(UserDetails userDetails) {

        User user = userRepository.fetchByUsername(userDetails.getUsername());

        AuthResponse authResponse = new AuthResponse();
        authResponse.setUser(userMapper.toDto(user));

        if (user.getRole().equals(RoleType.ORGANIZER)) {
            Organizer organizer = organizerDao.fetchOneById(user.getId());
            authResponse.setCustomUser(organizer);
        }

        if (user.getRole().equals(RoleType.MEMBER)) {
            Member member = memberDao.fetchOneById(user.getId());
            authResponse.setCustomUser(member);
        }

        if (user.getRole().equals(RoleType.MODERATOR)) {
            Moderator moderator = moderatorDao.fetchOneById(user.getId());
            authResponse.setCustomUser(moderator);
        }

        return authResponse;
    }
}
