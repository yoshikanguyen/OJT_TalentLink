package org.talentlink.talentlink.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.talentlink.talentlink.dto.StudentRegisterForm;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PasswordMatchesValidatorTest {

    private PasswordMatchesValidator validator;
    private ConstraintValidatorContext context;
    private ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;
    private ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext nodeBuilder;

    @BeforeEach
    void setUp() {
        validator = new PasswordMatchesValidator();
        context = mock(ConstraintValidatorContext.class);
        violationBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);
        nodeBuilder = mock(ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext.class);

        when(context.getDefaultConstraintMessageTemplate())
                .thenReturn("Mật khẩu nhập lại không khớp với mật khẩu");
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(violationBuilder);
        when(violationBuilder.addPropertyNode("confirmPassword")).thenReturn(nodeBuilder);
    }

    @Test
    void isValid_shouldReturnTrue_whenPasswordsMatch() {
        StudentRegisterForm form = new StudentRegisterForm();
        form.setPassword("Password123!");
        form.setConfirmPassword("Password123!");

        assertTrue(validator.isValid(form, context));
        verify(context, never()).disableDefaultConstraintViolation();
    }

    @Test
    void isValid_shouldReturnFalseAndAttachToConfirmPassword_whenPasswordsDoNotMatch() {
        StudentRegisterForm form = new StudentRegisterForm();
        form.setPassword("Password123!");
        form.setConfirmPassword("Different123!");

        assertFalse(validator.isValid(form, context));
        verify(context).disableDefaultConstraintViolation();
        verify(violationBuilder).addPropertyNode("confirmPassword");
        verify(nodeBuilder).addConstraintViolation();
    }

    @Test
    void isValid_shouldReturnTrue_whenFormOrFieldsAreNull() {
        assertTrue(validator.isValid(null, context));

        StudentRegisterForm formWithNull = new StudentRegisterForm();
        formWithNull.setPassword("Password123!");
        formWithNull.setConfirmPassword(null);

        assertTrue(validator.isValid(formWithNull, context));
    }
}
