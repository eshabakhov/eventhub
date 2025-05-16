package org.kmb.eventhub.tag.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tag.dto.EventTagsDTO;
import org.kmb.eventhub.tag.dto.TagDTO;
import org.kmb.eventhub.tag.service.TagService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Теги", description = "Управление тегами")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Получить список всех тегов",
                    description = "Возвращает все теги.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех тегов",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tag.class)))
    @GetMapping
    public ResponseList<Tag> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return tagService.getList(page, pageSize);
    }

    @Operation(summary = "Добавление тега в избранное пользователя.",
            description = "Добавляет тег в избранное пользователя.")
    @ApiResponse(responseCode = "201",
            description = "Теги успешно добавлены",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tag.class)))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping("/{id}/users/{userId}")
    public void addTagsToUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        tagService.addTagsToUser(id, userId);
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
    @PostMapping("/events/{id}")
    public Tag addTagsToEvent(
            @PathVariable Long id,
            @RequestBody TagDTO TagDTO) {
        return tagService.addTagsToEvent(id, TagDTO);
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
    @DeleteMapping(value = "/{id}/events/{eventId}")
    public void deleteTagFromEvent(
            @PathVariable Long id,
            @PathVariable Long eventId) {
        tagService.deleteTagFromEvent(id, eventId);
    }

    @Operation(summary = "Удалить тег из избранного пользователя.",
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
    @DeleteMapping(value = "/{id}/users/{userId}")
    public void deleteTagFromUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        tagService.deleteTagFromUser(id, userId);
    }
}
