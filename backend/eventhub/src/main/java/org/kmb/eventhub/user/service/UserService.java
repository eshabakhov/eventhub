package org.kmb.eventhub.user.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.auth.service.CustomUserDetailsService;
import org.kmb.eventhub.common.exception.AlreadyExistsException;
import org.kmb.eventhub.common.exception.MissingFieldException;
import org.kmb.eventhub.common.exception.UnexpectedException;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.user.exception.UserNotFoundException;
import org.kmb.eventhub.user.enums.PrivacyEnum;
import org.kmb.eventhub.enums.PrivacyType;
import org.kmb.eventhub.user.enums.RoleEnum;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.tag.service.TagService;
import org.kmb.eventhub.tag.mapper.TagMapper;
import org.kmb.eventhub.user.mapper.UserMapper;
import org.kmb.eventhub.user.repository.UserRepository;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.daos.OrganizerDao;
import org.kmb.eventhub.tables.pojos.*;
import org.kmb.eventhub.tables.daos.UserDao;
import org.kmb.eventhub.user.dto.MemberDTO;
import org.kmb.eventhub.user.dto.ModeratorDTO;
import org.kmb.eventhub.user.dto.OrganizerDTO;
import org.kmb.eventhub.user.dto.UserDTO;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

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

    private final CustomUserDetailsService customUserDetailsService;

    public ResponseList<User> getList(Integer page, Integer pageSize, String search) {

        ResponseList<User> responseList = new ResponseList<>();

        Condition condition = trueCondition();

        if (Objects.nonNull(search)) {
            condition = condition
                    .and(USER.USERNAME.eq(search))
                    .and(USER.ROLE.eq(RoleType.MEMBER));
        }

        List<User> list =  userRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
        return responseList;
    }

    public ResponseList<Organizer> getOrgList(Integer page, Integer pageSize, String search) {
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

    public ResponseList<User> getModerList(Integer page, Integer pageSize,String search) {

        ResponseList<User> responseList = new ResponseList<>();

        if (adminSecurityService.isAdmin(customUserDetailsService.getAuthenticatedUser())) {
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
    public User create(UserDTO userDTO) {

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
            memberDao.insert(userMapper.toMember(user));
        }
        if (RoleEnum.MODERATOR.equals(userDTO.getRole())) {
            moderatorDao.insert(userMapper.toModerator(user));
        }
        return user;
    }

    @Transactional
    public Organizer updateOrganizer(Long id, OrganizerDTO organizerDTO) {

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

    public Member getMember(Long id) {
        return memberDao.fetchOptionalById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public Organizer getOrganizer(Long id) {
        return organizerDao.fetchOptionalById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Transactional
    public User update(Long id, UserDTO userDTO) {

        User user = userDao.findOptionalById(id)
                .filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (Objects.nonNull(userDTO.getRole())) {
            /*if (!RoleType.MODERATOR.getLiteral().equals(userDTO.getRole().name())) {
                throw new ImmutableFieldException("role");
            }*/
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
