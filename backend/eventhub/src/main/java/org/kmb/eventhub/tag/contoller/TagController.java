package org.kmb.eventhub.tag.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tag.dto.EventTagsDTO;
import org.kmb.eventhub.tag.dto.TagDTO;
import org.kmb.eventhub.tag.dto.UserTagsDTO;
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

    @Operation(summary = "Добавление новых тегов в избранное пользователя.",
            description = "Добавляет новые теги в избранное пользователя мероприятию.")
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
    public List<Tag> addTagsToUser(
            @PathVariable Long id,
            @RequestBody UserTagsDTO userTagsDTO) {
        return tagService.addTagsToUser(id, userTagsDTO.getTags());
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
        return tagService.addTagsToEvent(id, eventTagsDTO.getTags());
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
    @DeleteMapping(value = "/{id}/tag")
    public Long deleteTagFromUser(
            @PathVariable Long id,
            @RequestBody @Valid TagDTO tagDTO) {
        return tagService.deleteTagFromUser(id, tagDTO);
    }
}
