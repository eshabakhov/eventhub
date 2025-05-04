package org.kmb.eventhub.user.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kmb.eventhub.user.dto.UserDTO;

import java.util.Objects;
import java.util.regex.Pattern;

public class ContactValidator implements ConstraintValidator<ValidUserContact, UserDTO> {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    @Override
    public boolean isValid(UserDTO userDTO, ConstraintValidatorContext context) {

        String email = userDTO.getEmail();
        String password = userDTO.getPassword();

        if (Objects.isNull(email) || Objects.isNull(password)) {
            return false;
        }

        return Pattern.matches(EMAIL_REGEX, email);
    }
}
