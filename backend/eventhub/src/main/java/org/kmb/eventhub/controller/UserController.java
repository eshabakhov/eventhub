package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.OrganizerDTO;
import org.kmb.eventhub.dto.ResponseDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.UserDTO;
import org.kmb.eventhub.service.UserService;
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
                    description = "Информация о пользовалете.",
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
        return userService.updateOgranizer(id, organizerDTO);
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