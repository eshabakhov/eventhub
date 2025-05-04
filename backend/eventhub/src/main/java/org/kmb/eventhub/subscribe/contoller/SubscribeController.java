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
import org.kmb.eventhub.tables.pojos.Event;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/members/{memberId}/subscribe")
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
    @PostMapping(value = "/{eventId}")
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
    @DeleteMapping(value = "/{eventId}")
    public void unsubscribeFromEvent(
            @PathVariable Long eventId,
            @RequestParam Long memberId) {
        subscribeService.unsubscribeFromEvent(eventId, memberId);
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

    @Operation(summary = "Получить список мероприятий пользователя.",
            description = "Возвращает все мероприятия, в которых участвует пользователь.")
    @ApiResponse(responseCode = "200",
            description = "Список всех мероприятий.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Event.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/members/{id}/events")
    public ResponseList<EventDTO> getMemberEvents(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "tags", required = false) String tags) {

        return eventService.getListByMemberId(page, pageSize, search, tags, id);
    }
    @Operation(summary = "Отказаться от участия.",
            description = "Удаляет участие пользователя в мероприятии.")
    @ApiResponse(responseCode = "200",
            description = "Участие удалено.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404",
            description = "Участник не найден",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/members/{id}/events")
    public void deleteParticipation(
            @PathVariable Long id,
            @RequestParam Long eventId) {
        subscribeService.unsubscribeFromEvent(eventId, id);
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
    @GetMapping(value = "/{eventId}")
    public EventMemberDTO getEventIfSubscribed(@PathVariable Long eventId, @PathVariable Long memberId) {
        return subscribeService.checkSubscription(eventId, memberId);
    }
}
