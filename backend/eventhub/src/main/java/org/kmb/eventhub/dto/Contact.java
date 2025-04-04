package org.kmb.eventhub.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="Contact")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Contact {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "contact_generator")
    @SequenceGenerator(name = "contact_generator", sequenceName = "contact_seq", allocationSize = 1)
    @Column(name = "contact_id", nullable = false, unique = true)
    private Long contactId;

    @Column(name = "info", nullable = false)
    private String info;

    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;

}
