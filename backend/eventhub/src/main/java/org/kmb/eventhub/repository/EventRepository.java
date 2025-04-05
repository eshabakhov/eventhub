package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.Event;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.EVENT;
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

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(EVENT)
                .where(condition)
                .fetchOneInto(Long.class);
    }
}
