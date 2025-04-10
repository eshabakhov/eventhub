package org.kmb.eventhub.service;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.EventFileDTO;
import org.kmb.eventhub.exception.*;
import org.kmb.eventhub.mapper.EventFileMapper;
import org.kmb.eventhub.repository.EventFileRepository;
import org.kmb.eventhub.tables.daos.EventDao;
import org.kmb.eventhub.tables.daos.EventFileDao;
import org.kmb.eventhub.tables.pojos.Event;
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
    public Long create(EventFileDTO eventFileDTO) {
        if (Objects.isNull(eventFileDTO.getEventId())) {
            throw new MissingFieldException("eventId");
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
        Event event = eventDao.findOptionalById(eventFileDTO.getEventId()).orElseThrow(() -> new EventNotFoundException(eventFileDTO.getEventId()));
        EventFile eventFile = eventFileMapper.toEntity(eventFileDTO);

        if (eventFileRepository.isExist(eventFileDTO.getFileName(), eventFileDTO.getFileType(), eventFileDTO.getEventId())) {
            throw new AlreadyExistsException(String.format("file with name %s and type %s", eventFileDTO.getFileName(), eventFileDTO.getFileType()));
        }

        eventFileDao.insert(eventFile);
        return eventFile.getFileId();
    }
    @Transactional
    public void delete(Long id) {
        if (eventFileDao.fetchByFileId(id).isEmpty()) {
            throw new EventFileNotFoundException(id);
        }
        eventFileDao.deleteById(id);
    }
}
