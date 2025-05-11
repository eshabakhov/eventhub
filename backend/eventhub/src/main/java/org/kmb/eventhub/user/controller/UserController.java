package org.kmb.eventhub.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.user.dto.*;
import org.kmb.eventhub.user.service.UserService;
import org.kmb.eventhub.tables.pojos.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/users")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @Operation(summary = "Добавление нового пользователя.",
                    description = "Добавляет нового пользователя в систему.")
    @ResponseStatus(value = HttpStatus.CREATED)
    @PostMapping
    public void create(@RequestBody @Valid UserDTO userDTO) {
        userService.create(userDTO);
    }

    @Operation(summary = "Получить список всех пользователей.",
                    description = "Возвращает всех пользователей.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping
    public ResponseList<UserResponseDTO> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {
        return userService.getList(page, pageSize, search);
    }

    @Operation(summary = "Получить информацию о пользователе.",
                    description = "Возвращает информацию о пользователе по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/{id}")
    public User get(@PathVariable Long id) {
        return userService.get(id);
    }

    @Operation(summary = "Обновить информацию пользователя.",
            description = "Обновить информацию пользователя по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody @Valid UserDTO userDTO) {
        return userService.update(id, userDTO);
    }

    @Operation(summary = "Обновить информацию организатора.",
                    description = "Обновить информацию организатора по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/organizers/{id}")
    public Organizer updateOrganizer(
            @PathVariable Long id,
            @RequestBody @Valid OrganizerDTO organizerDTO) {
        return userService.updateOrganizer(id, organizerDTO);
    }

    @Operation(summary = "Обновить информацию участника.",
                    description = "Обновить информацию участника по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/members/{id}")
    public Member updateMember(
            @PathVariable Long id,
            @RequestBody @Valid MemberDTO memberDTO) {
        return userService.updateMember(id, memberDTO);
    }

    @Operation(summary = "Обновить информацию модератора.",
            description = "Обновить информацию модератора по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @PutMapping(value = "/moderators/{id}")
    public Moderator updateModerator(
            @PathVariable Long id,
            @RequestBody @Valid ModeratorDTO moderatorDTO) {
        return userService.updateModerator(id, moderatorDTO);
    }

    @Operation(summary = "Получить информацию об участнике.",
                    description = "Возвращает информацию об участнике по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/members/{id}")
    public Member getMember(@PathVariable Long id) {
        return userService.getMember(id);
    }

    @Operation(summary = "Получить информацию об организаторе.",
            description = "Возвращает информацию об организаторе по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/organizers/{id}")
    public Organizer getOrganizer(@PathVariable Long id) {
        return userService.getOrganizer(id);
    }

    @Operation(summary = "Получить информацию о модераторе.",
            description = "Возвращает информацию о модераторе по ID.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/moderators/{id}")
    public Moderator getModerator(@PathVariable Long id) {
        return userService.getModerator(id);
    }

    @Operation(summary = "Получить список всех организаторов.",
            description = "Возвращает всех организаторов.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/organizers")
    public ResponseList<Organizer> getAllOrganizers(
                     @RequestParam(value = "page", defaultValue = "1") Integer page,
                     @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
                     @RequestParam(value = "search", required = false) String search) {
        return userService.getOrgList(page, pageSize, search);
    }

    @Operation(summary = "Получить список всех модераторов.",
            description = "Возвращает всех модераторов.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/moderators")
    public ResponseList<User> getAllModerators(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {
        return userService.getModerList(page, pageSize, search);
    }

    @Operation(summary = "Удалить пользователя.",
                    description = "Удаляет пользователя по ID.")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @DeleteMapping(value = "/{id}")
    public Long delete(@PathVariable Long id) {
        return userService.delete(id);
    }
}