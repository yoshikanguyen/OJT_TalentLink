package org.talentlink.talentlink.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.security.CustomUserDetails;
import org.talentlink.talentlink.service.StudentSkillService;

@Controller
@RequestMapping("/student/skills")
@RequiredArgsConstructor
public class StudentSkillController {

    private final StudentSkillService studentSkillService;

    @GetMapping
    public String mySkillsPage(@AuthenticationPrincipal CustomUserDetails currentUser,
                               Model model) {
        Long studentId = resolveCurrentStudentId(currentUser);

        model.addAttribute("studentId", studentId);
        model.addAttribute("activeSkills", studentSkillService.findActiveSkills());
        model.addAttribute("studentSkills", studentSkillService.findByStudent(studentId));
        model.addAttribute("groupedSkills", studentSkillService.findGroupedByCategory(studentId));
        model.addAttribute("levelDescriptions", StudentSkillService.LEVEL_DESCRIPTIONS);
        return "student/my-skills";
    }

    @PostMapping("/add")
    public String addSkill(@AuthenticationPrincipal CustomUserDetails currentUser,
                           @RequestParam("skillId") Long skillId,
                           @RequestParam("proficiencyLevel") Integer proficiencyLevel,
                           RedirectAttributes redirectAttributes) {
        Long studentId = resolveCurrentStudentId(currentUser);
        try {
            studentSkillService.addSkill(studentId, skillId, proficiencyLevel);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm kỹ năng vào hồ sơ của bạn.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/skills";
    }

    @PostMapping("/update-level")
    public String updateLevel(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @RequestParam("skillId") Long skillId,
                              @RequestParam("proficiencyLevel") Integer proficiencyLevel,
                              RedirectAttributes redirectAttributes) {
        Long studentId = resolveCurrentStudentId(currentUser);
        try {
            studentSkillService.updateLevel(studentId, skillId, proficiencyLevel);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật mức kỹ năng.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/skills";
    }

    @PostMapping("/remove")
    public String removeSkill(@AuthenticationPrincipal CustomUserDetails currentUser,
                              @RequestParam("skillId") Long skillId,
                              RedirectAttributes redirectAttributes) {
        Long studentId = resolveCurrentStudentId(currentUser);
        try {
            studentSkillService.removeSkill(studentId, skillId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã bỏ kỹ năng khỏi hồ sơ.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/skills";
    }

    /**
     * TODO: Nối với StudentProfileRepository theo userId / email của currentUser
     * để lấy đúng student_profiles.id của sinh viên đang đăng nhập.
     */
    private Long resolveCurrentStudentId(CustomUserDetails currentUser) {
        if (currentUser == null) {
            throw new IllegalStateException("Chưa xác thực người dùng.");
        }
        return currentUser.getUserId();
    }
}
