package org.kmb.eventhub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kmb.eventhub.dto.UserDTO;

import java.util.Objects;

public class ContactValidator implements ConstraintValidator<ValidUserContact, UserDTO> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    @Override
    public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {
        if (Objects.isNull(userDTO.getEmail()) || Objects.isNull(userDTO.getPassword())) {
            return false;
        }

        return userDTO.getEmail().matches(EMAIL_REGEX);
    }
}
