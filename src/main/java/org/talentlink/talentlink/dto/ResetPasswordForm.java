package org.talentlink.talentlink.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordForm {
    private String email;
    private String otpCode;
    private String newPassword;
    private String confirmPassword;
}
