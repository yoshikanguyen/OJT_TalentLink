package org.talentlink.talentlink.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.talentlink.talentlink.dto.StudentRegisterForm;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, StudentRegisterForm> {

    @Override
    public boolean isValid(StudentRegisterForm form, ConstraintValidatorContext context) {
        if (form == null) {
            return true;
        }
        String password = form.getPassword();
        String confirmPassword = form.getConfirmPassword();

        if (password == null || confirmPassword == null) {
            return true; // Để @NotBlank xử lý trường trống
        }

        boolean matched = password.equals(confirmPassword);
        if (!matched) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("confirmPassword")
                    .addConstraintViolation();
        }
        return matched;
    }
}
