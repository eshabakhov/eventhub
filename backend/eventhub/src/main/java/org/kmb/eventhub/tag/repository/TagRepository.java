package org.kmb.eventhub.tag.repository;

import lombok.AllArgsConstructor;
import org.jooq.Condition;
import org.jooq.DSLContext;
import org.jooq.Insert;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tables.records.TagRecord;
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

    public List<Tag> fetch(List<String> tagNames) {
        return dslContext.selectFrom(TAG)
                .where(TAG.NAME.in(tagNames))
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
        return dslContext.fetchExists(
                dslContext.selectOne()
                        .from(EVENT_TAGS)
                        .where(EVENT_TAGS.TAG_ID.eq(id))
                        .limit(1)
        ) || dslContext.fetchExists(
                dslContext.selectOne()
                        .from(USER_TAGS)
                        .where(USER_TAGS.TAG_ID.eq(id))
                        .limit(1)
        );
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

    public void createTags(List<String> tagNames) {
        List<TagRecord> records = tagNames.stream()
                .map(name -> dslContext.newRecord(TAG).setName(name))
                .toList();

        dslContext.batchInsert(records).execute();
    }

    public Set<Long> getUsedTagIdsForEvent(Long eventId) {
        return dslContext.select(EVENT_TAGS.TAG_ID)
                .from(EVENT_TAGS)
                .where(EVENT_TAGS.EVENT_ID.eq(eventId))
                .fetchSet(EVENT_TAGS.TAG_ID);
    }

    public Set<Long> getUsedTagIdsForUser(Long userId) {
        return dslContext.select(USER_TAGS.TAG_ID)
                .from(USER_TAGS)
                .where(USER_TAGS.USER_ID.eq(userId))
                .fetchSet(USER_TAGS.TAG_ID);
    }

    public void assignNewEventTags(Long eventId, List<Tag> tags) {
        var batch = dslContext.batch(
                tags.stream()
                        .map(tag -> dslContext.insertInto(EVENT_TAGS)
                                .set(EVENT_TAGS.EVENT_ID, eventId)
                                .set(EVENT_TAGS.TAG_ID, tag.getId()))
                        .toArray(Insert[]::new)
        );
        batch.execute();
    }

    public void assignNewEventTag(Long eventId, Tag tag) {
        dslContext.insertInto(EVENT_TAGS)
                .set(EVENT_TAGS.EVENT_ID, eventId)
                .set(EVENT_TAGS.TAG_ID, tag.getId())
                .execute();
    }



    public void assignTagToUser(Long tagId, Long userId) {
        dslContext.insertInto(USER_TAGS)
                .set(USER_TAGS.USER_ID, userId)
                .set(USER_TAGS.TAG_ID, tagId)
                .execute();
    }

    public void deleteTagFromEvent(Long tagId, Long eventId) {
        dslContext.deleteFrom(EVENT_TAGS)
                .where(EVENT_TAGS.EVENT_ID.eq(eventId))
                .and(EVENT_TAGS.TAG_ID.eq(tagId))
                .execute();
    }

    public void deleteTagFromUser(Long tagId, Long userId) {
        dslContext.deleteFrom(USER_TAGS)
                .where(USER_TAGS.USER_ID.eq(userId))
                .and(USER_TAGS.TAG_ID.eq(tagId))
                .execute();
    }
}
