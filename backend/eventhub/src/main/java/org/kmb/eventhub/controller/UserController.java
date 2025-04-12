package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.*;
import org.kmb.eventhub.service.UserService;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.Moderator;
import org.kmb.eventhub.tables.pojos.Organizer;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/users")
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Добавление нового пользователя.",
                    description = "Добавляет нового пользователя в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Пользователь успешно добавлен.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping
    public User create(@RequestBody @Valid UserDTO userDTO) {
        return userService.create(userDTO);
    }

    @Operation(summary = "Получить список всех пользователей.",
                    description = "Возвращает всех пользователей.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех пользователей.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping
    public ResponseList<User> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return userService.getList(page, pageSize);
    }

    @Operation(summary = "Получить информацию о пользователе.",
                    description = "Возвращает информацию о пользователе по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Информация о пользователе.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{id}")
    public User get(@PathVariable Long id) {
        return userService.get(id);
    }

    @Operation(summary = "Обновить информацию пользователя.",
            description = "Обновить информацию пользователя по ID.")
    @ApiResponse(responseCode = "200",
            description = "Информация о пользователе обновлена.",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Organizer.class)))
    @ApiResponse(responseCode = "404",
            description = "Пользователь не найден",
            content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody @Valid User user) {
        return userService.update(id, user);
    }

    @Operation(summary = "Обновить информацию организатора.",
                    description = "Обновить информацию организатора по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Информация об обрганизаторе обновлена.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Organizer.class)))
    @ApiResponse(responseCode = "404",
                    description = "Организатор не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/organizer/{id}")
    public Organizer updateOrganizer(
            @PathVariable Long id,
            @RequestBody @Valid OrganizerDTO organizerDTO) {
        return userService.updateOrganizer(id, organizerDTO);
    }

    @Operation(summary = "Обновить информацию участника.",
                    description = "Обновить информацию участника по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Информация об участнике обновлена.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Member.class)))
    @ApiResponse(responseCode = "404",
                    description = "Участник не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/member/{id}")
    public Member updateMember(
            @PathVariable Long id,
            @RequestBody @Valid MemberDTO memberDTO) {
        return userService.updateMember(id, memberDTO);
    }

    @Operation(summary = "Получить информацию об участнике.",
                    description = "Возвращает информацию об участнике по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Информация об участнике.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Member.class)))
    @ApiResponse(responseCode = "404",
                    description = "Участник не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/member/{id}")
    public Member getMember(@PathVariable Long id) {
        return userService.getMember(id);
    }

    @Operation(summary = "Получить информацию об организаторе.",
            description = "Возвращает информацию об организаторе по ID.")
    @ApiResponse(responseCode = "200",
            description = "Информация об организаторе.",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Organizer.class)))
    @ApiResponse(responseCode = "404",
            description = "Организатор не найден",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/organizer/{id}")
    public Organizer getOrganizer(@PathVariable Long id) {
        return userService.getOrganizer(id);
    }

    @Operation(summary = "Обновить информацию модератора.",
                    description = "Обновить информацию модератора по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Информация о модераторе обновлена.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Moderator.class)))
    @ApiResponse(responseCode = "404",
                    description = "Участник не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/moderator/{id}")
    public Moderator updateModerator(
            @PathVariable Long id,
            @RequestBody @Valid ModeratorDTO moderatorDTO) {
        return userService.updateModerator(id, moderatorDTO);
    }

    @Operation(summary = "Удалить пользователя.",
                    description = "Удаляет пользователя по ID.")
    @ApiResponse(responseCode = "200",
                    description = "Пользователь удален.",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "404",
                    description = "Пользователь не найден",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseDTO.class)))
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public Long delete(@PathVariable Long id) {
        return userService.delete(id);
    }
}