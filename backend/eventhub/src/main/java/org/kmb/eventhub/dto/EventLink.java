package org.kmb.eventhub.dto;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="Event_link")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EventLink {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_link_generator")
    @SequenceGenerator(name = "event_link_generator", sequenceName = "event_link_seq", allocationSize = 1)
    @Column(name = "link_id", nullable = false, unique = true)
    private Long linkId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "link", nullable = false)
    private String link;
}
