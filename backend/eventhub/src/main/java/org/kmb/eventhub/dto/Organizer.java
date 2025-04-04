package org.kmb.eventhub.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="Organizer")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Organizer {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", nullable = false)
    private String description;

    @Column(name = "industry", nullable = false)
    private String industry;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "is_accredited", nullable = false)
    private boolean isAccredited;
}
