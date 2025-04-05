package org.kmb.eventhub.dto;

import lombok.Data;
import org.kmb.eventhub.enums.PrivacyEnum;

import java.time.LocalDate;

@Data
public class Member {
    private Long id;
    private String firstName;
    private String lastName;
    private String patronymic;
    private LocalDate birthDate;
    private String birthCity;
    private PrivacyEnum privacy;
}
