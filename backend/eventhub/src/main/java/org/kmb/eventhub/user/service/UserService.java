package org.kmb.eventhub.user.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.exception.AlreadyExistsException;
import org.kmb.eventhub.common.exception.MissingFieldException;
import org.kmb.eventhub.common.exception.UnexpectedException;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.user.dto.*;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.enums.PrivacyEnum;
import org.kmb.eventhub.enums.PrivacyType;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.user.mapper.UserMapper;
import org.kmb.eventhub.user.repository.UserRepository;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.daos.OrganizerDao;
import org.kmb.eventhub.tables.pojos.*;
import org.kmb.eventhub.tables.daos.UserDao;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.kmb.eventhub.Tables.MEMBER;
import static org.kmb.eventhub.Tables.USER;
import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class  UserService {

    private final UserRepository userRepository;

    private final UserDao userDao;

    private final OrganizerDao organizerDao;

    private final MemberDao memberDao;

    private final ModeratorDao moderatorDao;

    private final UserMapper userMapper;

    private final AdminSecurityService adminSecurityService;

    private final UserDetailsService userDetailsService;

    private final UserSecurityService userSecurityService;

    public ResponseList<UserResponseDTO> getList(Integer page, Integer pageSize, String search) {

        ResponseList<UserResponseDTO> responseList = new ResponseList<>();

        Condition condition = trueCondition();

        if (Objects.nonNull(search)) {
            condition = condition
                    .and(USER.USERNAME.eq(search))
                    .and(USER.ROLE.eq(RoleType.MEMBER));
        }

        List<UserResponseDTO> list =  userRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<Member> getMembersList(Integer page, Integer pageSize, String search) {
        ResponseList<Member> responseList = new ResponseList<>();
        Condition condition = trueCondition();
        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            condition = condition.and(MEMBER.LAST_NAME.containsIgnoreCase(search));
            condition = condition.or(MEMBER.FIRST_NAME.containsIgnoreCase(search));
            condition = condition.or(MEMBER.PATRONYMIC.containsIgnoreCase(search));
        }

        List<Member> list =  userRepository.fetchMembers(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.countMembers(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<Organizer> getOrganizersList(Integer page, Integer pageSize, String search) {
        ResponseList<Organizer> responseList = new ResponseList<>();
        Condition condition = trueCondition();
        if (Objects.nonNull(search) && !search.trim().isEmpty()) {
            condition = condition.and(org.kmb.eventhub.tables.Organizer.ORGANIZER.NAME.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Organizer.ORGANIZER.ADDRESS.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Organizer.ORGANIZER.DESCRIPTION.containsIgnoreCase(search));
            condition = condition.or(org.kmb.eventhub.tables.Organizer.ORGANIZER.INDUSTRY.containsIgnoreCase(search));
        }

        List<Organizer> list =  userRepository.fetchOrganizers(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.countOrgs(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<User> getModeratorsList(Integer page, Integer pageSize,String search) {

        ResponseList<User> responseList = new ResponseList<>();

        if (adminSecurityService.isAdmin(userDetailsService.getAuthenticatedUser())) {
             responseList = new ResponseList<>();

            List<User> list = userRepository.fetchModerators(search, page, pageSize);

            responseList.setList(list);
            responseList.setTotal((long) list.size());
            responseList.setCurrentPage(page);
            responseList.setPageSize(pageSize);
            return responseList;
        }
        return responseList;
    }

    @Transactional
    public UserResponseDTO create(UserDTO userDTO) {

        if (Objects.isNull(userDTO.getUsername()))
            throw new MissingFieldException("username");
        if (Objects.isNull(userDTO.getDisplayName()))
            throw new MissingFieldException("displayName");
        if (Objects.isNull(userDTO.getPassword()))
            throw new MissingFieldException("password");
        if (Objects.isNull(userDTO.getEmail()))
            throw new MissingFieldException("email");
        if (Objects.isNull(userDTO.getRole()))
            throw new MissingFieldException("role");

        if (!userDao.fetchByUsername(userDTO.getUsername()).isEmpty())
            throw new AlreadyExistsException("user with username " + userDTO.getUsername());

        if (!userDao.fetchByEmail(userDTO.getEmail()).isEmpty())
            throw new AlreadyExistsException("user with email " + userDTO.getEmail());

        User user = userMapper.toEntity(userDTO);
        user.setIsActive(true);
        user.setPassword(new BCryptPasswordEncoder().encode(user.getPassword()));
        userDao.insert(user);
        if (RoleEnum.ORGANIZER.equals(userDTO.getRole())) {
            organizerDao.insert(userMapper.toOrganizer(user));
        }
        if (RoleEnum.MEMBER.equals(userDTO.getRole())) {
            Member member = userMapper.toMember(user);
            member.setPrivacy(PrivacyType.ONLY_FRIENDS);
            memberDao.insert(member);
        }
        if (RoleEnum.MODERATOR.equals(userDTO.getRole())) {
            moderatorDao.insert(userMapper.toModerator(user));
        }
        return userMapper.toResponse(user);
    }

    @Transactional
    public Organizer updateOrganizer(Long id, OrganizerDTO organizerDTO) {

        if (!userSecurityService.isUserOwnData(id, userDetailsService.getAuthenticatedUser()))
            throw new AccessDeniedException(String.format("У вас нет прав для редактирования пользователя с id %d", id));

        userDao.findOptionalById(id)
                .filter(e -> e.getIsActive() && RoleEnum.ORGANIZER.name().equals(e.getRole().toString()))
                .orElseThrow(() -> new UserNotFoundException(id));

        Organizer organizer = organizerDao.fetchOptionalById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (Objects.nonNull(organizerDTO.getName()))
            organizer.setName(organizerDTO.getName());
        if (Objects.nonNull(organizerDTO.getDescription()))
            organizer.setDescription(organizerDTO.getDescription());
        if (Objects.nonNull(organizerDTO.getIndustry()))
            organizer.setIndustry(organizerDTO.getIndustry());
        if (Objects.nonNull(organizerDTO.getAddress()))
            organizer.setAddress(organizerDTO.getAddress());
        if (Objects.nonNull(organizerDTO.getIsAccredited()))
            organizer.setIsAccredited(organizerDTO.getIsAccredited());

        organizerDao.update(organizer);
        return organizer;
    }

    @Transactional
    public Member updateMember(Long id, MemberDTO memberDTO) {

        if (!userSecurityService.isUserOwnData(id, userDetailsService.getAuthenticatedUser()))
            throw new AccessDeniedException(String.format("У вас нет прав для редактирования пользователя с id %d", id));

        userDao.findOptionalById(id)
                .filter(e -> e.getIsActive() && RoleEnum.MEMBER.name().equals(e.getRole().toString()))
                .orElseThrow(() -> new UserNotFoundException(id));

        Member member = memberDao.fetchOptionalById(id).orElseThrow(() -> new UserNotFoundException(id));

        if (Objects.nonNull(memberDTO.getFirstName()))
            member.setFirstName(memberDTO.getFirstName());
        if (Objects.nonNull(memberDTO.getLastName()))
            member.setLastName(memberDTO.getLastName());
        if (Objects.nonNull(memberDTO.getPatronymic()))
            member.setPatronymic(memberDTO.getPatronymic());
        if (Objects.nonNull(memberDTO.getBirthDate()))
            member.setBirthDate(memberDTO.getBirthDate());
        if (Objects.nonNull(memberDTO.getBirthCity()))
            member.setBirthCity(memberDTO.getBirthCity());
        if (Objects.nonNull(memberDTO.getPrivacy())) {
            if (PrivacyEnum.PRIVATE.equals(memberDTO.getPrivacy()))
                member.setPrivacy(PrivacyType.PRIVATE);
            if (PrivacyEnum.PUBLIC.equals(memberDTO.getPrivacy()))
                member.setPrivacy(PrivacyType.PUBLIC);
            if (PrivacyEnum.ONLY_FRIENDS.equals(memberDTO.getPrivacy()))
                member.setPrivacy(PrivacyType.ONLY_FRIENDS);
        }

        memberDao.update(member);
        return member;
    }

    @Transactional
    public Moderator updateModerator(Long id, ModeratorDTO moderatorDTO) {

        if (!userSecurityService.isUserOwnData(id, userDetailsService.getAuthenticatedUser()))
            throw new AccessDeniedException(String.format("У вас нет прав для редактирования пользователя с id %d", id));

        if (Objects.isNull(moderatorDTO.getIsAdmin()))
            throw new MissingFieldException("isAdmin");

        userDao.findOptionalById(id)
                .filter(e -> e.getIsActive() && RoleEnum.MODERATOR.name().equals(e.getRole().toString()))
                .orElseThrow(() -> new UserNotFoundException(id));
        Moderator moderator = userMapper.dtoToModerator(moderatorDTO);
        moderator.setId(id);
        moderatorDao.update(moderator);
        return moderator;
    }

    public User get(Long id) {
        return userDao.fetchOptionalById(id).
                filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User getByUsername(String username) {
        return userDao.fetchOptionalByUsername(username).
                filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(-1L));
    }

    public User getByEmail(String email) {
        return userDao.fetchOptionalByEmail(email).
                filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(-1L));
    }

    public Member getMember(Long id) {
        return memberDao.fetchOptionalById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public Organizer getOrganizer(Long id) {
        return organizerDao.fetchOptionalById(id)
                .orElseThrow(() -> new UserNotFoundException(id));

    }

    public Moderator getModerator(Long id) {
        return moderatorDao.fetchOptionalById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(Long id, UserDTO userDTO) {

        if (!userSecurityService.isUserOwnData(id, userDetailsService.getAuthenticatedUser()))
            throw new AccessDeniedException(String.format("У вас нет прав для редактирования пользователя с id %d", id));

        User user = userDao.findOptionalById(id)
                .filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (Objects.nonNull(userDTO.getRole())) {
            if (RoleEnum.MEMBER.equals(userDTO.getRole()))
                user.setRole(RoleType.MEMBER);
            if (RoleEnum.ORGANIZER.equals(userDTO.getRole()))
                user.setRole(RoleType.ORGANIZER);
            if (RoleEnum.MODERATOR.equals(userDTO.getRole()))
                user.setRole(RoleType.MODERATOR);
        }
        if (Objects.nonNull(userDTO.getEmail())) {
            user.setEmail(userDTO.getEmail());
        }
        if (Objects.nonNull(userDTO.getIsActive())) {
            user.setIsActive(userDTO.getIsActive());
        }
        if (Objects.nonNull(userDTO.getUsername()))
            user.setUsername(userDTO.getUsername());
        if (Objects.nonNull(userDTO.getDisplayName()))
            user.setDisplayName(userDTO.getDisplayName());
        if (Objects.nonNull(userDTO.getPassword()) && !userDTO.getPassword().isEmpty())
            user.setPassword(new BCryptPasswordEncoder().encode(userDTO.getPassword()));

        userDao.update(user);
        return user;
    }

    @Transactional
    public Long delete(Long id) {
        if (Objects.isNull(userRepository.fetchActive(id))) {
            throw new UserNotFoundException(id);
        }
        Long userDeletedId = userRepository.setInactive(id);
        if (Objects.isNull(userDeletedId)) {
            throw new UnexpectedException("The user was not deleted due to an unexpected error.");
        }
        return id;
    }
}
