package org.kmb.eventhub.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.ResponseError;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.dto.UserDTO;
import org.kmb.eventhub.mapper.UserMapper;
import org.kmb.eventhub.service.UserService;
import org.kmb.eventhub.tables.pojos.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/users")
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    private final UserMapper userMapper;

    @Operation(summary = "Добавление нового пользователя.",
                    description = "Добавляет нового пользователя в систему.")
    @ApiResponse(responseCode = "201",
                    description = "Пользователь успешно добавлен",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "400",
                    description = "Ошибка валидации",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = ResponseError.class)))
    @PostMapping
    public ResponseEntity<User> create(@RequestBody @Valid UserDTO userDTO) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userService.create(userMapper.toEntity(userDTO)));
    }

    @Operation(summary = "Получить список всех пользователей",
                    description = "Возвращает всех пользователей.")
    @ApiResponse(responseCode = "200",
                    description = "Список всех пользователей",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = User.class)))
    @GetMapping
    public ResponseEntity<ResponseList<User>> getList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(userService.getList(page, pageSize));
    }
}