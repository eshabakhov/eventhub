package org.kmb.eventhub.statistics.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static org.kmb.eventhub.Tables.*;

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

    public Long getAllViews(Long organizerId) {
        return dsl.selectCount()
                .from(USER_EVENT_INTERACTIONS)
                .innerJoin(EVENT).on(EVENT.ID.eq(USER_EVENT_INTERACTIONS.EVENT_ID))
                .where(USER_EVENT_INTERACTIONS.INTERACTION_TYPE.eq("VIEW")
                        .and(EVENT.ORGANIZER_ID.eq(organizerId)))
                .fetchOneInto(Long.class);
    }

    public Long getOrganizerFavorites(Long organizerId) {
        return dsl.selectCount()
                .from(MEMBER_ORGANIZER)
                .where(MEMBER_ORGANIZER.ORGANIZER_ID.eq(organizerId))
                .groupBy(MEMBER_ORGANIZER.ORGANIZER_ID)
                .fetchOneInto(Long.class);
    }

    public Long getAllMembers(Long organizerId) {
        return dsl.selectCount()
                .from(EVENT_MEMBERS)
                .innerJoin(EVENT).on(EVENT.ID.eq(EVENT_MEMBERS.EVENT_ID))
                .where(EVENT.ORGANIZER_ID.eq(organizerId))
                .fetchOneInto(Long.class);
    }

    public Long getAllEvents(Long organizerId) {
        return dsl.selectCount()
                .from(EVENT)
                .where(EVENT.ORGANIZER_ID.eq(organizerId))
                .fetchOneInto(Long.class);
    }
}


