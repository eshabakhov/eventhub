package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.*;
import org.kmb.eventhub.enums.PrivacyEnum;
import org.kmb.eventhub.enums.PrivacyType;
import org.kmb.eventhub.enums.RoleEnum;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.exception.*;
import org.kmb.eventhub.mapper.TagMapper;
import org.kmb.eventhub.mapper.UserMapper;
import org.kmb.eventhub.repository.UserRepository;
import org.kmb.eventhub.tables.daos.MemberDao;
import org.kmb.eventhub.tables.daos.ModeratorDao;
import org.kmb.eventhub.tables.daos.OrganizerDao;
import org.kmb.eventhub.tables.pojos.*;
import org.kmb.eventhub.tables.daos.UserDao;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final TagService tagService;

    private final UserDao userDao;

    private final OrganizerDao organizerDao;

    private final MemberDao memberDao;

    private final ModeratorDao moderatorDao;

    private final UserMapper userMapper;

    private final TagMapper tagMapper;

    public ResponseList<User> getList(Integer page, Integer pageSize) {
        ResponseList<User> responseList = new ResponseList<>();
        Condition condition = trueCondition();

        List<User> list =  userRepository.fetch(condition, page, pageSize);

        responseList.setList(list);
        responseList.setTotal(userRepository.count(condition));
        responseList.setCurrentPage(page);
        responseList.setPageSize(pageSize);
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
            throw new UnexpectedException("Пользователь не был удален из-за непредвиденной ошибки");
        }
        return id;
    }

    @Transactional
    public List<Tag> addTagsToUser(Long userId, List<TagDTO> tagNamesDTO) {
        List<Tag> tagNames = tagNamesDTO.stream().map(tagMapper::toEntity).toList();
        userDao.findOptionalById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        //1. Добавление новых тегов, получение id всех тегов из запроса
        List<Tag> tagWithId = tagService.checkAllTags(tagNames);

        //2. Получение списка использованных тегов для мероприятия
        Set<Long> usedTagIds = tagService.getUsedTagIdsForUser(userId);

        //3. Получение id неиспользованных тегов
        List<Tag> newTags = tagWithId.stream()
                .filter(tag -> !usedTagIds.contains(tag.getId()))
                .toList();

        //4. Добавление связи для новых тегов и мероприятия
        if (!newTags.isEmpty()) {
            tagService.assignTagsToUser(userId, newTags);
        }

        return newTags;
    }
}
