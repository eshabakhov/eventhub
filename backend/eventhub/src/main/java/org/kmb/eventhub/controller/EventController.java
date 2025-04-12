package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.EventTagsDTO;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.service.EventService;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@CrossOrigin(origins = "http://localhost:63343")
@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/events")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Мероприятия", description = "Управление мероприятиями")
public class EventController {

    private final EventService eventService;

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
    public ResponseList<Event> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return eventService.getList(page, pageSize);
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
    @PatchMapping(value = "/event/{id}")
    public Event updateEvent(
            @PathVariable Long id,
            @RequestBody @Valid EventDTO eventDTO) {
        return eventService.update(id, eventDTO);
    }

    @Operation(summary = "Добавление новых тегов к мероприятию.",
            description = "Добавляет новые теги к мероприятию.")
    @ApiResponse(responseCode = "201",
            description = "Теги успешно добавлены",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Tag.class)))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/add-tags")
    public List<Tag> addTagsToEvent(@RequestBody EventTagsDTO eventTagsDTO) {
        return eventService.addTagsToEvent(eventTagsDTO.getEventId(), eventTagsDTO.getTags());
    }

    @Operation(summary = "Удалить мероприятие.",
            description = "Удаляет мероприятие по ID.")
    @ApiResponse(responseCode = "200",
            description = "Мероприятие удалено.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "404",
            description = "Мероприятие не найдено",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public Long delete(@PathVariable Long id) {
        return eventService.delete(id);
    }
}
