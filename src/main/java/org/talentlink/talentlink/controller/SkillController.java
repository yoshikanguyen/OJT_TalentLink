package org.talentlink.talentlink.controller;

import org.talentlink.talentlink.service.SkillService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/skills")
@RequiredArgsConstructor
public class SkillController {

    private final SkillService skillService;

    @GetMapping
    public String listSkills(Model model) {
        model.addAttribute("skills", skillService.findAll());
        return "admin/skills";
    }

    @PostMapping("/create")
    public String createSkill(@RequestParam(defaultValue = "") String name,
                              @RequestParam(required = false) String category,
                              RedirectAttributes redirectAttributes) {
        return handleAction(() -> skillService.create(name, category),
                "Thêm kỹ năng thành công.", redirectAttributes);
    }

    @PostMapping("/{id}/update")
    public String updateSkill(@PathVariable Long id,
                              @RequestParam(defaultValue = "") String name,
                              @RequestParam(required = false) String category,
                              RedirectAttributes redirectAttributes) {
        return handleAction(() -> skillService.update(id, name, category),
                "Cập nhật kỹ năng thành công.", redirectAttributes);
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        return handleAction(() -> skillService.toggleActive(id),
                "Cập nhật trạng thái kỹ năng thành công.", redirectAttributes);
    }

    private String handleAction(Runnable action, String successMsg, RedirectAttributes redirectAttributes) {
        try {
            action.run();
            redirectAttributes.addFlashAttribute("successMessage", successMsg);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/skills";
    }
}
