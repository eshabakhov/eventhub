package org.kmb.eventhub.friendship.controller;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.friendship.dto.FriendRequestDTO;
import org.kmb.eventhub.friendship.service.FriendService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/friends/{id}/")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Друзья", description = "Управление друзьями участников")
public class FriendController {

    private final FriendService friendService;

    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping
    public ResponseList<FriendRequestDTO> getList(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return friendService.getFriendRequestList(id, page, pageSize);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/send/request")
    public void sendFriendRequest(
            @PathVariable Long id,
            @RequestParam Long idTo) {
        friendService.sendFriendRequest(id, idTo);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/accept/request")
    public void acceptFriendRequest(
            @PathVariable Long id,
            @RequestParam Long idFrom) {
        friendService.acceptFriendRequest(idFrom, id);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @PostMapping(value = "/reject/request")
    public void rejectFriendRequest(
            @PathVariable Long id,
            @RequestParam Long idFrom) {
        friendService.rejectFriendRequest(idFrom, id);
    }

    @ResponseStatus(value = HttpStatus.OK)
    @DeleteMapping
    public void removeUserFromFriends(
            @PathVariable Long id,
            @RequestParam Long idFrom) {
        friendService.removeUserFromFriends(idFrom, id);
    }

}
