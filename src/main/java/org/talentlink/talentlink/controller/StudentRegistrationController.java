package org.talentlink.talentlink.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.StudentRegisterForm;
import org.talentlink.talentlink.service.StudentRegistrationService;

@Controller
@RequestMapping("/register/student")
public class StudentRegistrationController {

    private final StudentRegistrationService studentRegistrationService;

    public StudentRegistrationController(StudentRegistrationService studentRegistrationService) {
        this.studentRegistrationService = studentRegistrationService;
    }

    @GetMapping
    public String showRegisterForm(Model model) {
        if (!model.containsAttribute("registerForm")) {
            model.addAttribute("registerForm", new StudentRegisterForm());
        }
        return "account/register-student";
    }

    @PostMapping
    public String handleRegister(
            @Valid @ModelAttribute("registerForm") StudentRegisterForm form,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            return "account/register-student";
        }

        try {
            studentRegistrationService.registerStudent(form);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đăng ký tài khoản sinh viên thành công. Hồ sơ của bạn đã được tạo ở trạng thái Riêng tư."
            );
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "duplicate.email", ex.getMessage());
            return "account/register-student";
        }
    }
}
