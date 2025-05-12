package org.kmb.eventhub.user.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.enums.RoleType;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.user.dto.UserResponseDTO;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class UserRepository {

    private final DSLContext dslContext;

    public List<UserResponseDTO> fetch(Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(USER)
                .where(condition)
                .and(USER.IS_ACTIVE.eq(true))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(UserResponseDTO.class);
    }

    public List<Member> fetchMembers (Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .select(MEMBER.fields())
                .from(MEMBER)
                .join(USER).on(MEMBER.ID.eq(USER.ID))
                .where(condition)
                .and(USER.IS_ACTIVE.eq(true))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Member.class);
    }

    public List<Organizer> fetchOrganizers (Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .select(ORGANIZER.fields())
                .from(ORGANIZER)
                .join(USER).on(ORGANIZER.ID.eq(USER.ID))
                .where(condition)
                .and(USER.IS_ACTIVE.eq(true))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Organizer.class);
    }

    public List<User> fetchModerators (String search, Integer page, Integer pageSize) {
        return dslContext
                .select(USER.fields())
                .from(USER)
                .innerJoin(MODERATOR).on(USER.ID.eq(MODERATOR.ID))
                .where(USER.ROLE.eq(RoleType.MODERATOR))
                .and(USER.IS_ACTIVE.eq(true))
                .and(MODERATOR.IS_ADMIN.eq(false))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(User.class);
    }

    public Long countOrgs(Condition condition) {
        return dslContext
                .selectCount()
                .from(ORGANIZER)
                .where(condition)
                .fetchOneInto(Long.class);
    }

    public User fetchByUsername(String username) {
        return dslContext
                .selectFrom(USER)
                .where(USER.USERNAME.eq(username))
                .and(USER.IS_ACTIVE.eq(true))
                .fetchOneInto(User.class);
    }

    public User fetchByEmail(String email) {
        return dslContext
                .selectFrom(USER)
                .where(USER.USERNAME.eq(email))
                .and(USER.IS_ACTIVE.eq(true))
                .fetchOneInto(User.class);
    }

    public User fetchActive(Long id) {
        return dslContext
                .selectFrom(USER)
                .where(USER.ID.eq(id))
                .and(USER.IS_ACTIVE.eq(true))
                .fetchOneInto(User.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(USER)
                .where(condition)
                .fetchOneInto(Long.class);
    }

    public Long setInactive(Long id) {
        int affectedRows = dslContext
                .update(USER)
                .set(USER.IS_ACTIVE, false)
                .where(USER.ID.eq(id))
                .execute();

        return affectedRows == 1 ? id : null;
    }
}
