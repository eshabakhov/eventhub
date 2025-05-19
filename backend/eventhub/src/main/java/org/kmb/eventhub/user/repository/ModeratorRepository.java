package org.kmb.eventhub.user.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import org.kmb.eventhub.tables.pojos.Moderator;

import static org.kmb.eventhub.Tables.*;

@Repository
@AllArgsConstructor
public class ModeratorRepository {
    private final DSLContext dslContext;

    public Moderator findById(Long id) {
        return dslContext.selectFrom(MODERATOR)
                .where(MODERATOR.ID.eq(id))
                .fetchOneInto(Moderator.class);
    }
}
