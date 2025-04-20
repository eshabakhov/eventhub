package org.kmb.eventhub.controller;

import lombok.AllArgsConstructor;
import org.kmb.eventhub.dto.FriendRequestDTO;
import org.kmb.eventhub.dto.ResponseList;
import org.kmb.eventhub.service.FriendService;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/users/members/{id}/friends")
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
