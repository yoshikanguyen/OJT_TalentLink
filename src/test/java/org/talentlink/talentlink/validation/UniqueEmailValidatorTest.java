package org.talentlink.talentlink.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UniqueEmailValidatorTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ConstraintValidatorContext context;

    private UniqueEmailValidator validator;

    @BeforeEach
    void setUp() {
        validator = new UniqueEmailValidator(userRepository);
    }

    @Test
    void isValid_shouldReturnTrue_whenEmailIsNotUsed() {
        when(userRepository.findByEmailIgnoreCase("new@example.com")).thenReturn(Optional.empty());

        assertTrue(validator.isValid("  new@example.com  ", context));
        verify(userRepository, times(1)).findByEmailIgnoreCase("new@example.com");
    }

    @Test
    void isValid_shouldReturnFalse_whenEmailAlreadyExists() {
        when(userRepository.findByEmailIgnoreCase("used@example.com"))
                .thenReturn(Optional.of(new User()));

        assertFalse(validator.isValid("used@example.com", context));
    }

    @Test
    void isValid_shouldReturnTrue_whenBlankOrNull() {
        assertTrue(validator.isValid(null, context));
        assertTrue(validator.isValid("   ", context));
        verifyNoInteractions(userRepository);
    }
}
