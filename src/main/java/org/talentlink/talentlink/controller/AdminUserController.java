package org.talentlink.talentlink.controller;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.entity.User;
import org.talentlink.talentlink.security.RoleMappingHelper;
import org.talentlink.talentlink.service.AdminUserService;

import java.security.Principal;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;
    private final RoleMappingHelper roleMappingHelper;

    public AdminUserController(AdminUserService adminUserService, RoleMappingHelper roleMappingHelper) {
        this.adminUserService = adminUserService;
        this.roleMappingHelper = roleMappingHelper;
    }

    @GetMapping
    public String listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            Model model
    ) {
        Page<User> usersPage = adminUserService.getUsers(keyword, roleId, status, page, size);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("keyword", keyword != null ? keyword : "");
        model.addAttribute("roleId", roleId);
        model.addAttribute("status", status != null ? status : "");
        model.addAttribute("size", size);
        model.addAttribute("roleMappingHelper", roleMappingHelper);
        model.addAttribute("currentAdminEmail", principal != null ? principal.getName() : "");

        return "admin/users";
    }

    @PostMapping("/partners")
    public String createPartner(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam(required = false) String phone,
            @RequestParam String companyName,
            RedirectAttributes redirectAttributes
    ) {
        try {
            adminUserService.createPartnerAccount(fullName, email, password, phone, companyName);
            redirectAttributes.addFlashAttribute("successMessage", "Đã tạo tài khoản đối tác và công ty thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Principal principal,
            RedirectAttributes redirectAttributes
    ) {
        String currentAdminEmail = principal != null ? principal.getName() : null;
        try {
            boolean nowActive = adminUserService.toggleUserActiveStatus(id, currentAdminEmail);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    nowActive ? "Đã mở khoá tài khoản." : "Đã khoá tài khoản."
            );
        } catch (IllegalStateException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        if (keyword != null && !keyword.isBlank()) {
            redirectAttributes.addAttribute("keyword", keyword);
        }
        if (roleId != null) {
            redirectAttributes.addAttribute("roleId", roleId);
        }
        if (status != null && !status.isBlank()) {
            redirectAttributes.addAttribute("status", status);
        }
        redirectAttributes.addAttribute("page", page);
        redirectAttributes.addAttribute("size", size);

        return "redirect:/admin/users";
    }
}
