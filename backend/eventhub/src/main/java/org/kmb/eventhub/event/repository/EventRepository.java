package org.kmb.eventhub.event.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.kmb.eventhub.tables.pojos.Event;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class EventRepository {

    private final DSLContext dslContext;

    public List<Event> fetch(Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(EVENT)
                .where(condition)
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Event.class);
    }

    public Event fetchById(Long id) {
        return dslContext
                .selectFrom(EVENT)
                .where(EVENT.ID.eq(id))
                .fetchOneInto(Event.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(EVENT)
                .where(condition)
                .fetchOneInto(Long.class);
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
