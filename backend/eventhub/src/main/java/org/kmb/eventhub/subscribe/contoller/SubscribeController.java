package org.kmb.eventhub.subscribe.contoller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseDTO;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.event.dto.EventDTO;
import org.kmb.eventhub.event.dto.EventMemberDTO;
import org.kmb.eventhub.event.service.EventService;
import org.kmb.eventhub.subscribe.service.SubscribeService;
import org.kmb.eventhub.tables.pojos.*;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/members/{memberId}")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Участие в мероприятие", description = "Управление участием в мероприятии")
public class SubscribeController {

    private final SubscribeService subscribeService;

    private final EventService eventService;

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
    @PostMapping(value = "/subscribe/{eventId}")
    public void subscribeToEvent(
            @PathVariable Long eventId,
            @PathVariable Long memberId) {
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
    @DeleteMapping(value = "/subscribe/{eventId}")
    public void unsubscribeFromEvent(
            @PathVariable Long eventId,
            @PathVariable Long memberId) {
        subscribeService.unsubscribeFromEvent(eventId, memberId);
    }

    @Operation(summary = "Получить список мероприятий пользователя.",
            description = "Возвращает все мероприятия, в которых участвует пользователь.")
    @ApiResponse(responseCode = "200",
            description = "Список всех мероприятий.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/events")
    public ResponseList<EventDTO> getMemberEvents(
            @PathVariable Long memberId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tags", required = false) List<String> tags) {

        return eventService.getListByMemberId(page, pageSize, search, tags, memberId);
    }

    @Operation(summary = "Получить мероприятие, если пользователь участвует.",
            description = "Возвращаем id участника и мероприятия.")
    @ApiResponse(responseCode = "200",
            description = "Id участника и мероприятия.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @ApiResponse(responseCode = "404",
            description = "Мероприятие не найдено",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/subscribe/{eventId}")
    public EventMemberDTO getEventIfSubscribed(@PathVariable Long eventId, @PathVariable Long memberId) {
        return subscribeService.checkSubscription(eventId, memberId);
    }

    @Operation(summary = "Подписаться на организатора.",
            description = "Добавить организатора в избранное.")
    @ApiResponse(responseCode = "201",
            description = "Организатор добавлен в избранное",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/organizers/{organizerId}")
    public void subscribeToOrganizer(@PathVariable Long organizerId, @PathVariable Long memberId) {
        subscribeService.subscribeToOrganizer(organizerId, memberId);
    }

    @Operation(summary = "Отписаться от организатора.",
            description = "Удалить организатора из избранного.")
    @ApiResponse(responseCode = "201",
            description = "Организатор успешно удален из избранного",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping(value = "/organizers/{organizerId}")
    public void unsubscribeFromOrganizer(@PathVariable Long organizerId, @PathVariable Long memberId) {
        subscribeService.unsubscribeFromOrganizer(organizerId, memberId);
    }

    @Operation(summary = "Проверить подписку на организатора.",
            description = "Проверить подписку на организатора.")
    @ApiResponse(responseCode = "201",
            description = "Участник подписан на организатора",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)

    @GetMapping(value = "/organizers/{organizerId}")
    public MemberOrganizer checkOrganizerSubscribe(@PathVariable Long organizerId, @PathVariable Long memberId) {
        return subscribeService.checkSubscriptionToOrganizer(organizerId, memberId);
    }

    @Operation(summary = "Получить список избранных организаторов.",
            description = "Получить список избранных организаторов пользователя.")
    @ApiResponse(responseCode = "201",
            description = "Список избранных организаторов",
            content = @Content(mediaType = "application/json"))
    @ApiResponse(responseCode = "400",
            description = "Ошибка валидации",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/favorite-organizers")
    public ResponseList<Organizer> getFavoriteOrganizers(
            @PathVariable Long memberId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return subscribeService.getFavoriteOrganizersList(memberId, page, pageSize);
    }
}
