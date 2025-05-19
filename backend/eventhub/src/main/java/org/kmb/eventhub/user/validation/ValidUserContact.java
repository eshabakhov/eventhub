package org.kmb.eventhub.user.validation;


import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ContactValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidUserContact {
    String message() default "Неверный формат почты";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
