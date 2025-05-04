package org.kmb.eventhub.tag.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.tables.pojos.Tag;
import org.kmb.eventhub.tag.service.TagService;
import org.springframework.web.bind.annotation.*;

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
}
