package org.kmb.eventhub.subscribe.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventMembers;
import org.kmb.eventhub.tables.pojos.Member;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class SubscribeRepository {

    private final DSLContext dslContext;

    public EventMembers fetchOptionalByMemberIdAndEventId(Long memberId, Long eventId, Integer page, Integer pageSize) {
        return dslContext
                .selectFrom(EVENT_MEMBERS)
                .where(EVENT_MEMBERS.EVENT_ID.eq(eventId))
                .and(EVENT_MEMBERS.MEMBER_ID.eq(memberId))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchOneInto(EventMembers.class);
    }

    public List<Member> fetchMembersByEventId(Long eventId, Integer page, Integer pageSize) {
        return dslContext
                .select(MEMBER.fields())
                .from(MEMBER)
                .innerJoin(EVENT_MEMBERS).on(EVENT_MEMBERS.MEMBER_ID.eq(MEMBER.ID))
                .where(EVENT_MEMBERS.EVENT_ID.eq(eventId))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Member.class);
    }

    public List<Event> fetchEventsByMemberId(Long memberId, Integer page, Integer pageSize) {
        return dslContext
                .select(EVENT.fields())
                .from(EVENT)
                .innerJoin(EVENT_MEMBERS).on(EVENT_MEMBERS.EVENT_ID.eq(EVENT.ID))
                .where(EVENT_MEMBERS.MEMBER_ID.eq(memberId))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Event.class);
    }
    public List<Long> fetchEventsIDsByMemberId(Long memberId, Integer page, Integer pageSize) {
        return dslContext
                .select(EVENT.ID)
                .from(EVENT)
                .innerJoin(EVENT_MEMBERS).on(EVENT_MEMBERS.EVENT_ID.eq(EVENT.ID))
                .where(EVENT_MEMBERS.MEMBER_ID.eq(memberId))
                .limit(pageSize)
                .offset((page - 1) * pageSize)
                .fetchInto(Long.class);
    }
}
