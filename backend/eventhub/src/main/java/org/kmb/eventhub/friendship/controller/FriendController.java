package org.kmb.eventhub.friendship.controller;

import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.kmb.eventhub.auth.service.AuthService;
import org.kmb.eventhub.auth.service.UserDetailsService;
import org.kmb.eventhub.common.dto.ResponseList;
import org.kmb.eventhub.friendship.dto.FriendCheckDTO;
import org.kmb.eventhub.friendship.dto.FriendRequestDTO;
import org.kmb.eventhub.friendship.service.FriendService;
import org.kmb.eventhub.tables.pojos.FriendRequest;
import org.kmb.eventhub.tables.pojos.Member;
import org.kmb.eventhub.tables.pojos.User;
import org.kmb.eventhub.tables.records.FriendRequestRecord;
import org.kmb.eventhub.user.dto.UserDTO;
import org.kmb.eventhub.user.dto.UserResponseDTO;
import org.kmb.eventhub.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping(value = "/v1/friends")
@io.swagger.v3.oas.annotations.tags.Tag(name = "Друзья", description = "Управление друзьями участников")
public class FriendController {

    private final UserDetailsService userDetailsService;
    private final FriendService friendService;

    /*@ResponseStatus(value = HttpStatus.OK)
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
    }*/

    @Operation(summary = "Получить список всех участников.",
            description = "Возвращает всех участников.")
    @ResponseStatus(value = HttpStatus.OK)
    @GetMapping(value = "/")
    public ResponseList<UserResponseDTO> getAllMembers(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize,
            @RequestParam(value = "search", required = false) String search) {
        return friendService.getMemberList(page, pageSize, search);
    }

    @PostMapping("/{id}/send")
    public ResponseEntity<Void> sendRequest(@PathVariable Long id) {
        friendService.sendRequest(userDetailsService.getAuthenticatedUser().getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/accept")
    public ResponseEntity<Void> accept(@PathVariable Long id) {
        friendService.acceptRequest(id, userDetailsService.getAuthenticatedUser().getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(@PathVariable Long id) {
        friendService.rejectRequest(id, userDetailsService.getAuthenticatedUser().getId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/remove")
    public ResponseEntity<Void> remove(@PathVariable Long id) {
        friendService.removeFriend(id, userDetailsService.getAuthenticatedUser().getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/list")
    public ResponseList<UserDTO> listFriends(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return friendService.getFriends(page, pageSize, userDetailsService.getAuthenticatedUser().getId());
    }

    @GetMapping("/non-friends/list")
    public ResponseEntity<List<UserDTO>> listNonFriends() {
        return ResponseEntity.ok(friendService.getNonFriends(userDetailsService.getAuthenticatedUser().getId()));
    }

    @GetMapping("/incoming")
    public ResponseEntity<List<UserDTO>> incoming() {
        return ResponseEntity.ok(friendService.getIncomingRequests(userDetailsService.getAuthenticatedUser().getId()));
    }

    @GetMapping("/outgoing")
    public ResponseEntity<List<UserDTO>> outgoing() {
        return ResponseEntity.ok(friendService.getOutgoingRequests(userDetailsService.getAuthenticatedUser().getId()));
    }

    @GetMapping("/{id}/isfriend")
    public FriendCheckDTO isFriend(@PathVariable Long id) {
        return friendService.isFriend(id, userDetailsService.getAuthenticatedUser().getId());
    }
}
