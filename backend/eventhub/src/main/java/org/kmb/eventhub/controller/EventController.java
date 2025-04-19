package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.*;
import org.kmb.eventhub.service.EventFileService;
import org.kmb.eventhub.service.EventService;
import org.kmb.eventhub.service.SubscribeService;
import org.kmb.eventhub.service.TagService;
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.EventFile;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tables.pojos.Member;
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

    private final SubscribeService subscribeService;

    private final TagService tagService;

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
            @RequestParam(value = "tags", required = false) String tags,
            @RequestParam(value = "organizerId", required = false) Long organizerId,
            @RequestParam(value = "memberId", required = false) Long memberId) {
        return eventService.getList(page, pageSize, search, tags, organizerId, memberId);
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

    @Operation(summary = "Получить участников мероприятия.",
            description = "Возвращает участников мероприятия по ID.")
    @ApiResponse(responseCode = "200",
            description = "Участники мероприятия.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "404",
            description = "Мероприятие не найдено",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{id}/members")
    public ResponseList<Member> get(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @PathVariable Long id) {
        return subscribeService.getMembersByEventId(id, page, pageSize);
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
    @PostMapping("/{id}/tag")
    public List<Tag> addTagsToEvent(
            @PathVariable Long id,
            @RequestBody EventTagsDTO eventTagsDTO) {
        return eventService.addTagsToEvent(id, eventTagsDTO.getTags());
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

    @Operation(summary = "Удалить тег у мероприятия.",
            description = "Удаляет тег по ID.")
    @ApiResponse(responseCode = "200",
            description = "Тег удален.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Tag.class)))
    @ApiResponse(responseCode = "404",
            description = "Тег не найден",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}/tag")
    public Long deleteTagFromEvent(
            @PathVariable Long id,
            @RequestBody @Valid TagDTO tagDTO) {
        return tagService.deleteTagFromEvent(id, tagDTO);
    }

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
