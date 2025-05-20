package org.kmb.eventhub.statistics.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.kmb.eventhub.Tables.USER_EVENT_INTERACTIONS;

@Repository
@AllArgsConstructor
public class UserEventInteractionRepository {

    private final DSLContext dsl;

    // Получить общее число просмотров по событию
    public int countViewsByEventId(Long eventId) {
        return dsl.selectCount()
                .from(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.EVENT_ID.eq(eventId)
                        .and(USER_EVENT_INTERACTIONS.INTERACTION_TYPE.eq("VIEW")))
                .fetchOne(0, int.class);
    }

    // Получить общее число просмотров по пользователю
    public int countViewsByUserId(Long userId) {
        return dsl.selectCount()
                .from(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.USER_ID.eq(userId)
                        .and(USER_EVENT_INTERACTIONS.INTERACTION_TYPE.eq("VIEW")))
                .fetchOne(0, int.class);
    }

    // Получить количество просмотров конкретного события конкретным пользователем
    public int countViewsByUserAndEvent(Long userId, Long eventId) {
        return dsl.selectCount()
                .from(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.USER_ID.eq(userId)
                        .and(USER_EVENT_INTERACTIONS.EVENT_ID.eq(eventId))
                        .and(USER_EVENT_INTERACTIONS.INTERACTION_TYPE.eq("VIEW")))
                .fetchOne(0, int.class);
    }
}


