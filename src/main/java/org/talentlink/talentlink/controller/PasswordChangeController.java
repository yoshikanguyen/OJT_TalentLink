package org.talentlink.talentlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.ChangePasswordForm;
import org.talentlink.talentlink.service.PasswordChangeService;

import java.security.Principal;

@Controller
@RequestMapping("/account/change-password")
public class PasswordChangeController {

    private final PasswordChangeService passwordChangeService;

    public PasswordChangeController(PasswordChangeService passwordChangeService) {
        this.passwordChangeService = passwordChangeService;
    }

    @GetMapping
    public String showChangePasswordForm(Model model) {
        if (!model.containsAttribute("changePasswordForm")) {
            model.addAttribute("changePasswordForm", new ChangePasswordForm());
        }
        return "account/change-password";
    }

    @PostMapping
    public String handleChangePassword(
            @ModelAttribute("changePasswordForm") ChangePasswordForm form,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        if (principal == null || principal.getName() == null) {
            return "redirect:/login";
        }

        try {
            passwordChangeService.changePassword(
                    principal.getName(),
                    form.getCurrentPassword(),
                    form.getNewPassword(),
                    form.getConfirmPassword()
            );
            redirectAttributes.addFlashAttribute("successMessage", "Đổi mật khẩu thành công.");
            return "redirect:/account/change-password";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("changePasswordForm", form);
            return "redirect:/account/change-password";
        }
    }
}
