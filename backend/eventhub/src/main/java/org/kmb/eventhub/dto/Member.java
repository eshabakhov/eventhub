package org.kmb.eventhub.dto;

import jakarta.persistence.*;
import lombok.*;
import org.kmb.eventhub.enums.PrivacyEnum;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="Member")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Member {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "patronymic")
    private String patronymic;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Column(name = "birth_city", nullable = false)
    private String birthCity;

    @Column(name = "privacy", nullable = false)
    private PrivacyEnum privacy;

    @ManyToMany(mappedBy = "members")
    private Set<Event> events = new HashSet<>();
}
