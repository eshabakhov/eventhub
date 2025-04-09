package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.kmb.eventhub.dto.*;
import org.kmb.eventhub.enums.RoleEnum;
import org.kmb.eventhub.exception.MissingFieldException;
import org.kmb.eventhub.exception.UnexpectedException;
import org.kmb.eventhub.exception.ImmutableFieldException;
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
import org.kmb.eventhub.tables.daos.UserDao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

import static org.jooq.impl.DSL.trueCondition;

@Service
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final UserDao userDao;

    private final OrganizerDao organizerDao;

    private final MemberDao memberDao;

    private final ModeratorDao moderatorDao;

    private final UserMapper userMapper;

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
        if (Objects.isNull(userDTO.getIsActive()))
            throw new MissingFieldException("isActive");
        if (Objects.isNull(userDTO.getRole()))
            throw new MissingFieldException("role");

        User user = userMapper.toEntity(userDTO);
        user.setIsActive(true);
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

        if (Objects.isNull(organizerDTO.getName()))
            throw new MissingFieldException("name");
        if (Objects.isNull(organizerDTO.getDescription()))
            throw new MissingFieldException("description");
        if (Objects.isNull(organizerDTO.getIndustry()))
            throw new MissingFieldException("industry");
        if (Objects.isNull(organizerDTO.getAddress()))
            throw new MissingFieldException("address");
        if (Objects.isNull(organizerDTO.getIsAccredited()))
            throw new MissingFieldException("isAccredited");

        userDao.findOptionalById(id)
                .filter(e -> e.getIsActive() && RoleEnum.ORGANIZER.name().equals(e.getRole().toString()))
                .orElseThrow(() -> new UserNotFoundException(id));
        Organizer organizer = userMapper.dtoToOrganizer(organizerDTO);
        organizer.setId(id);
        organizerDao.update(organizer);
        return organizer;
    }

    @Transactional
    public Member updateMember(Long id, MemberDTO memberDTO) {

        if (Objects.isNull(memberDTO.getFirstName()))
            throw new MissingFieldException("firstName");
        if (Objects.isNull(memberDTO.getLastName()))
            throw new MissingFieldException("lastName");
        if (Objects.isNull(memberDTO.getPatronymic()))
            throw new MissingFieldException("patronymic");
        if (Objects.isNull(memberDTO.getBirthDate()))
            throw new MissingFieldException("birthDate");
        if (Objects.isNull(memberDTO.getBirthCity()))
            throw new MissingFieldException("birthCity");
        if (Objects.isNull(memberDTO.getPrivacy()))
            throw new MissingFieldException("privacy");

        userDao.findOptionalById(id)
                .filter(e -> e.getIsActive() && RoleEnum.MEMBER.name().equals(e.getRole().toString()))
                .orElseThrow(() -> new UserNotFoundException(id));
        Member member = userMapper.dtoToMember(memberDTO);
        member.setId(id);
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

    @Transactional
    public User update(Long id, User user) {

        if (Objects.isNull(user))
            throw new UserNotFoundException(id);
        if (!user.getIsActive())
            throw new ImmutableFieldException("isActive");

        user.setId(id);
        User userFromDb = userDao.findOptionalById(id)
                .filter(User::getIsActive)
                .orElseThrow(() -> new UserNotFoundException(id));

        if (!Objects.equals(user.getEmail(), userFromDb.getEmail()))
            throw new ImmutableFieldException("Email");

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
}
