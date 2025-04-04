package org.kmb.eventhub.dto;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name="Event_File")
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class EventFile {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "event_file_generator")
    @SequenceGenerator(name = "event_file_generator", sequenceName = "event_file_seq", allocationSize = 1)
    @Column(name = "file_id", nullable = false, unique = true)
    private Long fileId;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "file_content", nullable = false)
    private String fileContent;
}
