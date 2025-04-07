package org.kmb.eventhub.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.kmb.eventhub.dto.UserDTO;
import org.kmb.eventhub.exception.EmailFormatException;

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
        if (!Pattern.matches(EMAIL_REGEX, email)) {
            throw new EmailFormatException(email);
        }
        return true;
    }
}
