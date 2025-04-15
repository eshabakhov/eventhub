package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.config.jwt.JwtUtil;
import org.kmb.eventhub.dto.AuthResponse;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.UserMapper;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.daos.OrganizerDao;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.Organizer;
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

    private final OrganizerDao organizerDao;
    private final MemberDao memberDao;
    private final ModeratorDao moderatorDao;

    private final UserMapper userMapper;

    private JwtUtil jwtUtil;

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

    public AuthResponse createAuthResponse(UserDetails userDetails) {

        User user = userRepository.fetchByUsername(userDetails.getUsername());

        final String token = jwtUtil.generateToken(userDetails);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setToken(token);
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
