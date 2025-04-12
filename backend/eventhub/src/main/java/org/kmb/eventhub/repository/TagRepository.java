package org.kmb.eventhub.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.EventFileDTO;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

import static org.kmb.eventhub.Tables.*;

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

    public List<Tag> fetch(Long eventId) {
        return dslContext
                .select(TAG.fields())
                .from(TAG)
                .innerJoin(EVENT_TAGS).on(EVENT_TAGS.TAG_ID.eq(TAG.ID))
                .where(EVENT_TAGS.EVENT_ID.eq(eventId))
                .fetchInto(Tag.class);
    }

    public Long count(Condition condition) {
        return dslContext
                .selectCount()
                .from(TAG)
                .where(condition)
                .fetchOneInto(Long.class);
    }

    public boolean tagIsUsed(Long id) {
        return dslContext
                .selectCount()
                .from(EVENT_TAGS)
                .where(EVENT_TAGS.TAG_ID.eq(id))
                .fetchOneInto(Long.class) > 0;
    }

    public Tag findTagByName(String name) {
        return dslContext.selectFrom(TAG)
                .where(TAG.NAME.equalIgnoreCase(name))
                .fetchOneInto(Tag.class);
    }

    public Tag createTag(String name){
        return dslContext.insertInto(TAG)
                .set(TAG.NAME, name)
                .returning()
                .fetchOneInto(Tag.class);
    }

    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return dslContext.select(EVENT_TAGS.TAG_ID)
                .from(EVENT_TAGS)
                .where(EVENT_TAGS.EVENT_ID.eq(eventId))
                .fetchSet(EVENT_TAGS.TAG_ID);
    }

    public void assignNewEventTag(Long eventId, List<Tag> tags) {
        var batch = dslContext.batch(
                tags.stream()
                        .map(tag -> dslContext.insertInto(EVENT_TAGS)
                                .set(EVENT_TAGS.EVENT_ID, eventId)
                                .set(EVENT_TAGS.TAG_ID, tag.getId()))
                        .toArray(Insert[]::new)
        );
        batch.execute();
    }

    public void delete(Long id, EventDTO eventDTO) {
        dslContext.deleteFrom(EVENT_TAGS)
                .where(EVENT_TAGS.EVENT_ID.eq(eventDTO.getId()))
                .and(EVENT_TAGS.TAG_ID.eq(id))
                .execute();
    }



}
