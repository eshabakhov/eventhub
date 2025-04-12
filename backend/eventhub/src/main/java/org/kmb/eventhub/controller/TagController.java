package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.*;
import org.kmb.eventhub.service.TagService;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Теги", description = "Управление тегами")
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Добавление нового тега.",
                    description = "Добавляет новый тег в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Тег успешно добавлен",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tag.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping
    public Tag create(@RequestBody @Valid TagDTO tagDTO) {
        return tagService.create(tagDTO);
    }

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
}
