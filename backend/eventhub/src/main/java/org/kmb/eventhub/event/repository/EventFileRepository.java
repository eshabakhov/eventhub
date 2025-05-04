package org.kmb.eventhub.event.repository;

import lombok.AllArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import static org.kmb.eventhub.tables.EventFile.EVENT_FILE;

@Repository
@AllArgsConstructor
public class EventFileRepository {
    private final DSLContext dslContext;

    public boolean isExist(String fileName, String fileType, Long eventId) {
        Long count = Optional.ofNullable(
                dslContext
                        .selectCount()
                        .from(EVENT_FILE)
                        .where(EVENT_FILE.FILE_NAME.eq(fileName))
                        .and(EVENT_FILE.FILE_TYPE.eq(fileType))
                        .and(EVENT_FILE.EVENT_ID.eq(eventId))
                        .fetchOneInto(Long.class)
        ).orElse(0L);

        return count > 0L;
    }
}
