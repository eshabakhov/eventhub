package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.EventFileDTO;
import org.kmb.eventhub.exception.EventNotFoundException;
import org.kmb.eventhub.exception.UserNotFoundException;
import org.kmb.eventhub.mapper.EventFileMapper;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.EventFileDao;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@AllArgsConstructor
public class EventFileService {

    private final EventFileMapper eventFileMapper;

    private final EventDao eventDao;
    private final EventFileDao eventFileDao;

    @Transactional
    public Long create(EventFileDTO eventFileDTO) {
        if (Objects.isNull(eventFileDTO.getEventId())) {
            return null;
        }
        if (Objects.isNull(eventFileDTO.getFileName())) {
            return null;
        }
        if (Objects.isNull(eventFileDTO.getFileSize())) {
            return null;
        }
        if (Objects.isNull(eventFileDTO.getFileType())) {
            return null;
        }
        Event event = eventDao.findOptionalById(eventFileDTO.getEventId()).orElseThrow(() -> new EventNotFoundException(eventFileDTO.getEventId()));

        EventFile eventFile = eventFileMapper.toEntity(eventFileDTO);
        eventFileDao.insert(eventFile);
        return eventFile.getFileId();
    }
}
