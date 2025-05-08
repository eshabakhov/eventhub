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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

    private final EventFileRepository eventFileRepository;

    @Transactional
    public Long create(Long eventId, EventFileDTO eventFileDTO) {
        eventDao.findOptionalById(eventId).orElseThrow(() -> new EventNotFoundException(eventId));

        eventFileDTO.setEventId(eventId);

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

    public ResponseEntity<byte[]> getFileById(Long fileId) {
        EventFile eventFile = eventFileRepository.fetchById(fileId);

        if (eventFile == null) {
            return ResponseEntity.notFound().build();
        }

        EventFileDTO fileDTO = eventFileMapper.toDto(eventFile);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(fileDTO.getFileType()));
        headers.setContentDispositionFormData("attachment", fileDTO.getFileName());
        headers.setContentLength(fileDTO.getFileSize());

        return new ResponseEntity<>(fileDTO.getFileContent(), headers, HttpStatus.OK);
    }
}
