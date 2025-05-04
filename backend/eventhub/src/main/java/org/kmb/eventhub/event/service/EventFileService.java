package org.kmb.eventhub.event.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.exception.AlreadyExistsException;
import org.kmb.eventhub.common.exception.MissingFieldException;
import org.kmb.eventhub.event.exception.EventFileNotFoundException;
import org.kmb.eventhub.event.exception.EventIdMatchException;
import org.kmb.eventhub.event.exception.EventNotFoundException;
import org.kmb.eventhub.event.dto.EventFileDTO;
import org.kmb.eventhub.event.mapper.EventFileMapper;
import org.kmb.eventhub.event.repository.EventFileRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.EventFileDao;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@AllArgsConstructor
public class EventFileService {

    private final EventFileMapper eventFileMapper;

    private final EventDao eventDao;

    private final EventFileDao eventFileDao;

    private final EventFileRepository eventFileRepository;

    @Transactional
    public Long create(Long eventId, EventFileDTO eventFileDTO) {
        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        if (Objects.nonNull(eventFileDTO.getEventId())) {
            if (!eventFileDTO.getEventId().equals(eventId)) {
                throw new EventIdMatchException("Event IDs do not match");
            }
        }
        else {
            eventFileDTO.setEventId(eventId);
        }
        if (Objects.isNull(eventFileDTO.getFileName())) {
            throw new MissingFieldException("fileName");
        }
        if (Objects.isNull(eventFileDTO.getFileSize())) {
            throw new MissingFieldException("fileSize");
        }
        if (Objects.isNull(eventFileDTO.getFileType())) {
            throw new MissingFieldException("fileType");
        }
        EventFile eventFile = eventFileMapper.toEntity(eventFileDTO);

        if (eventFileRepository.isExist(eventFileDTO.getFileName(), eventFileDTO.getFileType(), eventId)) {
            throw new AlreadyExistsException(String.format("file with name %s and type %s", eventFileDTO.getFileName(), eventFileDTO.getFileType()));
        }

        eventFileDao.insert(eventFile);
        return eventFile.getFileId();
    }

    @Transactional
    public Long delete(Long eventId, EventFileDTO eventFileDTO) {
        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));
        Long fileId = eventFileDTO.getFileId();
        if (eventFileDao.fetchByFileId(fileId).isEmpty()) {
            throw new EventFileNotFoundException(fileId);
        }
        eventFileDao.deleteById(fileId);
        return fileId;
    }
}
