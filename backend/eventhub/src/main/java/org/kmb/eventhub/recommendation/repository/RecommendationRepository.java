package org.kmb.eventhub.recommendation.repository;

import lombok.AllArgsConstructor;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.kmb.eventhub.tables.pojos.Event;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class RecommendationRepository {

    private final DSLContext dslContext;

    public List<Event> getRecommendedEventsCond(
            Long userId,
            double lng,
            double lat,
            int page,
            int pageSize,
            Condition additionalConditions
    ) {

        Field<Double> distanceScore = DSL
                .val(0.8).mul(
                        DSL.val(1.0).div(
                                DSL.val(1.0).add(
                                        DSL.field("ST_Distance(ST_SetSRID(ST_Point({0}, {1}), 4326), ST_SetSRID(ST_Point({2}, {3}), 4326)) / 1000.0",
                                                Double.class,
                                                EVENT.LONGITUDE, EVENT.LATITUDE,
                                                DSL.val(lng), DSL.val(lat)
                                        )
                                )
                        )
                );

        Field<Integer> viewScore = DSL.select(DSL.inline(1))
                .from(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.EVENT_ID.eq(EVENT.ID))
                .and(USER_EVENT_INTERACTIONS.USER_ID.eq(userId))
                .and(USER_EVENT_INTERACTIONS.INTERACTION_TYPE.eq("VIEW"))
                .asField();

        Field<Integer> interactionScore = DSL.select(DSL.inline(1))
                .from(USER_EVENT_INTERACTIONS)
                .where(USER_EVENT_INTERACTIONS.EVENT_ID.eq(EVENT.ID))
                //.and(USER_EVENT_INTERACTIONS.USER_ID.eq(userId))
                .asField();

        /*Field<BigDecimal> tagScore = DSL.select(
                        DSL.count().cast(BigDecimal.class)
                                .div(
                                        DSL.nullif(
                                                DSL.selectCount()
                                                        .from(EVENT_TAGS)
                                                        .where(EVENT_TAGS.EVENT_ID.eq(EVENT.ID)),
                                                0
                                        )
                                )
                )
                .from(USER_TAGS)
                .join(EVENT_TAGS).on(USER_TAGS.TAG_ID.eq(EVENT_TAGS.TAG_ID).and(EVENT_TAGS.EVENT_ID.eq(EVENT.ID)))
                .where(USER_TAGS.USER_ID.eq(userId))
                .asField();*/

        Field<Double> totalScore = DSL
                .val(0.8).mul(distanceScore.cast(BigDecimal.class))
                .plus(DSL.val(0.2).mul(DSL.coalesce(viewScore.cast(BigDecimal.class), BigDecimal.ZERO)))
                .plus(DSL.val(0.3).mul(DSL.coalesce(interactionScore.cast(BigDecimal.class), BigDecimal.ZERO)));
                //.plus(DSL.val(0.2).mul(DSL.coalesce(tagScore, BigDecimal.ZERO)));

        // Основной подзапрос
        SelectConditionStep<?> baseQuery = dslContext
                .select(
                        EVENT.ID,
                        EVENT.TITLE,
                        EVENT.SHORT_DESCRIPTION,
                        EVENT.DESCRIPTION,
                        EVENT.FORMAT,
                        EVENT.START_DATE_TIME,
                        EVENT.END_DATE_TIME,
                        EVENT.LOCATION,
                        EVENT.LATITUDE,
                        EVENT.LONGITUDE,
                        EVENT.ORGANIZER_ID,
                        EVENT.PICTURES,
                        totalScore.as("score")
                )
                .from(EVENT)
                .where(DSL.trueCondition());

        // Применяем все условия
        baseQuery = baseQuery.and(additionalConditions);

        return dslContext.selectFrom(baseQuery.asTable("e"))
                .orderBy(DSL.field("score").desc())
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Event.class);

    }

    public List<Long> fetchEventIdsBySelectedTags(List<String> tags) {
        return dslContext
                .select(EVENT.ID)
                .from(EVENT)
                .join(EVENT_TAGS).on(EVENT.ID.eq(EVENT_TAGS.EVENT_ID))
                .join(TAG).on(TAG.ID.eq(EVENT_TAGS.TAG_ID))
                .where(TAG.NAME.in(tags))
                .groupBy(EVENT.ID)
                .having(DSL.countDistinct(TAG.NAME).eq(tags.size()))
                .fetchInto(Long.class);
    }

}
