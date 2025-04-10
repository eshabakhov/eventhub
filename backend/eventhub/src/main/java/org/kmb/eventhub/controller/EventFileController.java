package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.ResponseDTO;
import org.kmb.eventhub.dto.EventFileDTO;
import org.kmb.eventhub.service.EventFileService;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/eventFiles")
@Tag(name = "Файлы событий", description = "Управление файлами события")
public class EventFileController {
    private final EventFileService eventFileService;

    @Operation(summary = "Добавление нового файла.",
            description = "Добавляет нового файл к событию.")
    @ApiResponse(responseCode = "201",
            description = "Файл успешно добавлен.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EventFile.class)))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping
    public Long create(@RequestBody @Valid EventFileDTO eventFileDTO) {
        return eventFileService.create(eventFileDTO);
    }

    @Operation(summary = "Удалить файл.",
            description = "Удаляет файл по ID.")
    @ApiResponse(responseCode = "200",
            description = "Файл удален.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = EventFile.class)))
    @ApiResponse(responseCode = "404",
            description = "файл не найден",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        eventFileService.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
