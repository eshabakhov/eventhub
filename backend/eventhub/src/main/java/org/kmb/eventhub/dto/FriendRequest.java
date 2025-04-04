package org.kmb.eventhub.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.kmb.eventhub.enums.FriendRequestStatusEnum;

import java.time.LocalDate;

@Entity
@Table(name="Friend_Request")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class FriendRequest {

    @Id
    @Column(name = "requester_id", nullable = false)
    private Long requesterId;

    @Id
    @Column(name = "addressee_id", nullable = false)
    private Long addresseeId;

    @Column(name = "status", nullable = false)
    private FriendRequestStatusEnum status;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;
}

