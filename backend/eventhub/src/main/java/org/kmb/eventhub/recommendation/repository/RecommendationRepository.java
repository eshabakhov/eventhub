package org.kmb.eventhub.recommendation.repository;

import lombok.AllArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.UserEventInteractions;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class RecommendationRepository {

    private final DSLContext dslContext;

    public List<Event> findEventsByLocationAndUserInterests(Long userId, double longitude, double latitude) {

        System.out.println(EVENT.LATITUDE);

        // Получаем интересы пользователя
        List<Long> userTagIds = dslContext.select(USER_TAGS.TAG_ID)
                .from(USER_TAGS)
                .where(USER_TAGS.USER_ID.eq(userId))
                .fetchInto(Long.class);

        // Базовое гео-условие
        Condition geoCondition = DSL.condition(
                "ST_DWithin(ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, " +
                        "ST_SetSRID(ST_MakePoint({0}, {1}), 4326)::geography, {4})",
                longitude, latitude,
                EVENT.LONGITUDE, EVENT.LATITUDE,
                50000
        );

        // Базовый order по расстоянию
        SortField<Double> distanceOrder = DSL.field(
                "ST_Distance(ST_SetSRID(ST_MakePoint(?, ?), 4326)::geography, " +
                        "ST_SetSRID(ST_MakePoint({0}, {1}), 4326)::geography)",
                Double.class,
                longitude, latitude,
                EVENT.LONGITUDE, EVENT.LATITUDE
        ).asc();

        // --- ПЕРВАЯ ПОПЫТКА: поиск по интересам ---
        List<Event> eventsWithTags = null;
        if (!userTagIds.isEmpty()) {
            eventsWithTags = dslContext.select(EVENT.asterisk())
                    .from(EVENT)
                    .join(EVENT_TAGS).on(EVENT_TAGS.EVENT_ID.eq(EVENT.ID))
                    .where(geoCondition)
                    .and(EVENT_TAGS.TAG_ID.in(userTagIds))
                    .orderBy(distanceOrder)
                    .fetchInto(Event.class);
        }

        // --- Fallback: если не найдено — ищем просто ближайшие мероприятия ---
        if (eventsWithTags == null || eventsWithTags.isEmpty()) {
            return dslContext.select(EVENT.asterisk())
                    .from(EVENT)
                    .where(geoCondition)
                    .orderBy(distanceOrder)
                    .fetchInto(Event.class);
        }

        return eventsWithTags;
    }

    public List<UserEventInteractions> findInteractionsByUserId(Long user) {
        return dslContext.
                selectFrom(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.USER_ID.eq(user))
                .fetchInto(UserEventInteractions.class);
    }
}
