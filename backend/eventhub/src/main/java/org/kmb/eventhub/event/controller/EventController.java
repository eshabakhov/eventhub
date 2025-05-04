package org.kmb.eventhub.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.dto.EventFileDTO;
import org.kmb.eventhub.tag.dto.EventTagsDTO;
import org.kmb.eventhub.event.service.EventFileService;
import org.kmb.eventhub.event.service.EventService;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/events")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Мероприятия", description = "Управление мероприятиями")
public class EventController {

    private final EventService eventService;

    private final EventFileService eventFileService;

    @Operation(summary = "Добавление нового мероприятия.",
                    description = "Добавляет новое мероприятие в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Мероприятие успешно добавлено",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping
    public Event create(@RequestBody @Valid EventDTO eventDTO) {
        return eventService.create(eventDTO);
    }

    @Operation(summary = "Получить список всех мероприятий",
                    description = "Возвращает все мероприятия.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех мероприятий",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @GetMapping
    public ResponseList<EventDTO> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tags", required = false) String tags) {
        return eventService.getList(page, pageSize, search, tags);
    }

    @Operation(summary = "Получить информацию о мероприятии.",
                        description = "Возвращает информацию о мероприятии по ID.")
    @ApiResponse(responseCode = "200",
                        description = "Информация о мероприятии.",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "404",
                        description = "Мероприятие не найдено",
                        content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{id}")
    public EventDTO get(@PathVariable Long id) {
        return eventService.get(id);
    }

    @Operation(summary = "Обновить информацию о мероприятии.",
            description = "Обновить информацию мероприятия по ID.")
    @ApiResponse(responseCode = "200",
            description = "Информация о мероприятии обновлена.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "404",
            description = "Мероприятие не найдено",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PatchMapping(value = "/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestBody @Valid EventDTO eventDTO) {
        return eventService.update(id, eventDTO);
    }

//    @Operation(summary = "Удалить мероприятие.",
//            description = "Удаляет мероприятие по ID.")
//    @ApiResponse(responseCode = "200",
//            description = "Мероприятие удалено.",
//            content = @Content(mediaType = "application/json",
//            schema = @Schema(implementation = Event.class)))
//    @ApiResponse(responseCode = "404",
//            description = "Мероприятие не найдено",
//            content = @Content(mediaType = "application/json",
//            schema = @Schema(implementation = ResponseDTO.class)))
//    @ResponseStatus(value = HttpStatus.NO_CONTENT)
//    @DeleteMapping(value = "/{id}")
//    public Long delete(@PathVariable Long id) {
//        return eventService.delete(id);
//    }

    @Operation(summary = "Добавление нового файла.",
            description = "Добавляет новый файл к событию.")
    @ApiResponse(responseCode = "201",
            description = "Файл успешно добавлен.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EventFile.class)))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping(value = "/{id}/eventFiles")
    public Long addFile(
            @PathVariable Long id,
            @RequestBody @Valid EventFileDTO eventFileDTO) {
        return eventFileService.create(id, eventFileDTO);
    }

    @Operation(summary = "Удалить файл.",
            description = "Удаляет файл у мероприятия.")
    @ApiResponse(responseCode = "200",
            description = "Файл удален.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = EventFile.class)))
    @ApiResponse(responseCode = "404",
            description = "файл не найден",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}/eventFiles")
    public Long deleteFile(@PathVariable Long id,
                       @RequestBody @Valid EventFileDTO eventFileDTO) {
        return eventFileService.delete(id, eventFileDTO);
    }
}
