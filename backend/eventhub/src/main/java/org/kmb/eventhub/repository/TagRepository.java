package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.TAG;
@Repository
@AllArgsConstructor
public class TagRepository {

    private final DSLContext dslContext;

    public List<Tag> fetch(Condition condition, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(TAG)
                .where(condition)
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Tag.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(TAG)
                .where(condition)
                .fetchOneInto(Long.class);
    }
}
