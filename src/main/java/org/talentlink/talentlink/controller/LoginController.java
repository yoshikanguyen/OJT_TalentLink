package org.talentlink.talentlink.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String loginPage(
            @RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            HttpSession session,
            Model model
    ) {
        if (error != null && session != null) {
            Object sessionError = session.getAttribute("LOGIN_ERROR_MESSAGE");
            if (sessionError != null) {
                model.addAttribute("errorMessage", sessionError.toString());
                session.removeAttribute("LOGIN_ERROR_MESSAGE");
            } else {
                model.addAttribute("errorMessage", "Email hoặc mật khẩu không chính xác.");
            }
        }

        if (logout != null) {
            model.addAttribute("logoutMessage", "Bạn đã đăng xuất thành công.");
        }

        return "account/login";
    }
}
