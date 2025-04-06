package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.EventDTO;
import org.kmb.eventhub.dto.ResponseError;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.mapper.EventMapper;
import org.kmb.eventhub.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/events")
@Tag(name = "Мероприятия", description = "Управление мероприятиями")
public class EventController {

    private final EventService eventService;

    private final EventMapper eventMapper;

    @Operation(summary = "Добавление нового мероприятия.",
                    description = "Добавляет новое мероприятие в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Мероприятие успешно добавлено",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = org.kmb.eventhub.tables.pojos.Event.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)))
    @PostMapping
    public ResponseEntity<org.kmb.eventhub.tables.pojos.Event> create(@RequestBody @Valid EventDTO eventDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(eventService.create(eventMapper.toEntity(eventDTO)));
    }

    @Operation(summary = "Получить список всех мероприятий",
                    description = "Возвращает все мероприятия.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех мероприятий",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = org.kmb.eventhub.tables.pojos.Event.class)))
    @GetMapping
    public ResponseEntity<ResponseList<org.kmb.eventhub.tables.pojos.Event>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(eventService.getList(page, pageSize));
    }
}
