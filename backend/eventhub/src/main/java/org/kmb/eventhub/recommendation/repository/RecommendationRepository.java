package org.kmb.eventhub.recommendation.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.kmb.eventhub.tables.pojos.UserHistory;
import org.springframework.stereotype.Repository;

import java.util.List;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class RecommendationRepository {

    private final DSLContext dslContext;

    public List<UserHistory> findByUser(Long user) {
        return dslContext.
                selectFrom(USER_HISTORY)
                .where(USER_HISTORY.USER_ID.eq(user))
                .and(USER_HISTORY.ACTION_TYPE.in("VIEW", "FAVORITE", "RATE", "SHARE"))
                .fetchInto(UserHistory.class);
    }
}
