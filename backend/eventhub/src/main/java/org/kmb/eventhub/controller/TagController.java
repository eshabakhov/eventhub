package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.ResponseDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.TagDTO;
import org.kmb.eventhub.mapper.TagMapper;
import org.kmb.eventhub.service.TagService;
import org.kmb.eventhub.tables.pojos.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/tags")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Теги", description = "Управление тегами")
public class TagController {

    private final TagService tagService;

    private final TagMapper tagMapper;

    @Operation(summary = "Добавление нового тега.",
                    description = "Добавляет нового тега в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Тег успешно добавлен",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tag.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @PostMapping
    public ResponseEntity<Tag> create(@RequestBody @Valid TagDTO tagDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(tagService.create(tagMapper.toEntity(tagDTO)));
    }

    @Operation(summary = "Получить список всех тегов",
                    description = "Возвращает все теги.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех тегов",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Tag.class)))
    @GetMapping
    public ResponseEntity<ResponseList<Tag>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(tagService.getList(page, pageSize));
    }
}
