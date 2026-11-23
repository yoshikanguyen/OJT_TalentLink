package org.talentlink.talentlink.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.ForgotPasswordForm;
import org.talentlink.talentlink.dto.ResetPasswordForm;
import org.talentlink.talentlink.service.PasswordResetService;

@Controller
@RequestMapping("/account")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPasswordForm(Model model) {
        if (!model.containsAttribute("forgotPasswordForm")) {
            model.addAttribute("forgotPasswordForm", new ForgotPasswordForm());
        }
        return "account/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(
            @ModelAttribute("forgotPasswordForm") ForgotPasswordForm form,
            RedirectAttributes redirectAttributes
    ) {
        try {
            passwordResetService.requestOtp(form.getEmail());
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Nếu email hợp lệ, mã OTP 6 số đã được tạo (hiệu lực 10 phút) và ghi ra log hệ thống."
            );
            redirectAttributes.addFlashAttribute("prefillEmail", form.getEmail());
            return "redirect:/account/reset-password";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("forgotPasswordForm", form);
            return "redirect:/account/forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPasswordForm(
            @RequestParam(value = "email", required = false) String emailParam,
            Model model
    ) {
        if (!model.containsAttribute("resetPasswordForm")) {
            ResetPasswordForm form = new ResetPasswordForm();
            Object prefillEmail = model.asMap().get("prefillEmail");
            if (prefillEmail != null) {
                form.setEmail(prefillEmail.toString());
            } else if (emailParam != null && !emailParam.isBlank()) {
                form.setEmail(emailParam);
            }
            model.addAttribute("resetPasswordForm", form);
        }
        return "account/reset-password";
    }

    @PostMapping("/reset-password")
    public String handleResetPassword(
            @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
            RedirectAttributes redirectAttributes
    ) {
        if (form.getNewPassword() == null || form.getNewPassword().isBlank()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Vui lòng nhập mật khẩu mới.");
            redirectAttributes.addFlashAttribute("resetPasswordForm", form);
            return "redirect:/account/reset-password";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Mật khẩu mới và xác nhận mật khẩu không khớp.");
            redirectAttributes.addFlashAttribute("resetPasswordForm", form);
            return "redirect:/account/reset-password";
        }

        try {
            passwordResetService.resetPasswordWithOtp(
                    form.getEmail(),
                    form.getOtpCode(),
                    form.getNewPassword()
            );
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đặt lại mật khẩu thành công. Bạn có thể đăng nhập bằng mật khẩu mới."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("resetPasswordForm", form);
            return "redirect:/account/reset-password";
        }
    }
}
