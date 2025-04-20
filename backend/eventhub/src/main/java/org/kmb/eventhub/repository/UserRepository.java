package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.ORGANIZER;
import static org.kmb.eventhub.Tables.USER;

@Repository
@AllArgsConstructor
public class UserRepository {

    private final DSLContext dslContext;

    public List<User> fetch(Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(USER)
                .where(condition)
                .and(USER.IS_ACTIVE.eq(true))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(User.class);
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
