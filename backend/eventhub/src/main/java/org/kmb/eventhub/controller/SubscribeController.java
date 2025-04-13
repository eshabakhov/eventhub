package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.ResponseDTO;
import org.kmb.eventhub.service.SubscribeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/subscribe")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Участие в мероприятие", description = "Управление участием в мероприятии")
public class SubscribeController {

    private final SubscribeService subscribeService;

    @Operation(summary = "Добавить участие в мероприятии.",
            description = "Участвовать в мероприятии.")
    @ApiResponse(responseCode = "201",
            description = "Участие успешно добавлено",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/{eventId}")
    public void subscribeToEvent(
            @PathVariable Long eventId,
            @RequestParam Long memberId) {
        subscribeService.subscribeToEvent(eventId, memberId);
    }

    @Operation(summary = "Отказаться от участия в мероприятии.",
            description = "Отказаться от участия.")
    @ApiResponse(responseCode = "201",
            description = "Участие успешно удалено",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping(value = "/{eventId}")
    public void unsubscribeFromEvent(
            @PathVariable Long eventId,
            @RequestParam Long memberId) {
        subscribeService.unsubscribeFromEvent(eventId, memberId);
    }
}
