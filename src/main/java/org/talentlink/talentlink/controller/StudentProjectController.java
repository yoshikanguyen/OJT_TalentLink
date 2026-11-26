package org.talentlink.talentlink.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.talentlink.talentlink.dto.StudentProjectForm;
import org.talentlink.talentlink.entity.StudentProject;
import org.talentlink.talentlink.service.ProfileService;
import org.talentlink.talentlink.service.StudentProjectService;

@Controller
@RequestMapping("/student/my-projects")
@RequiredArgsConstructor
public class StudentProjectController {

    private final StudentProjectService studentProjectService;
    private final ProfileService profileService;

    @GetMapping
    public String listProjects(@AuthenticationPrincipal UserDetails principal,
                               Model model) {
        Long studentId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());
        var projects = studentProjectService.getProjectsByStudentId(studentId);

        model.addAttribute("projects", projects);
        model.addAttribute("projectCount", projects.size());
        model.addAttribute("maxProjects", StudentProjectService.MAX_PROJECTS_PER_PROFILE);
        model.addAttribute("canAddMore", projects.size() < StudentProjectService.MAX_PROJECTS_PER_PROFILE);
        if (!model.containsAttribute("projectForm")) {
            model.addAttribute("projectForm", new StudentProjectForm());
        }
        return "student/my-projects";
    }

    @PostMapping
    public String createProject(@AuthenticationPrincipal UserDetails principal,
                                @Valid @ModelAttribute("projectForm") StudentProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long studentId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());

        if (bindingResult.hasErrors()) {
            var projects = studentProjectService.getProjectsByStudentId(studentId);
            model.addAttribute("projects", projects);
            model.addAttribute("projectCount", projects.size());
            model.addAttribute("maxProjects", StudentProjectService.MAX_PROJECTS_PER_PROFILE);
            model.addAttribute("canAddMore", projects.size() < StudentProjectService.MAX_PROJECTS_PER_PROFILE);
            return "student/my-projects";
        }

        try {
            studentProjectService.createProject(studentId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã thêm dự án cá nhân thành công.");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("projectForm", form);
        }
        return "redirect:/student/my-projects";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long id,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Long studentId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());
        try {
            StudentProject project = studentProjectService.getProjectByIdAndStudentId(id, studentId);
            StudentProjectForm form = StudentProjectForm.builder()
                    .projectName(project.getProjectName())
                    .roleName(project.getRoleName())
                    .technologies(project.getTechnologies())
                    .githubUrl(project.getGithubUrl())
                    .demoUrl(project.getDemoUrl())
                    .description(project.getDescription())
                    .build();

            model.addAttribute("projectForm", form);
            model.addAttribute("editingId", id);
            model.addAttribute("projects", studentProjectService.getProjectsByStudentId(studentId));
            model.addAttribute("projectCount", studentProjectService.getProjectsByStudentId(studentId).size());
            model.addAttribute("maxProjects", StudentProjectService.MAX_PROJECTS_PER_PROFILE);
            model.addAttribute("canAddMore", true);
            return "student/my-projects";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            return "redirect:/student/my-projects";
        }
    }

    @PostMapping("/{id}")
    public String updateProject(@AuthenticationPrincipal UserDetails principal,
                                @PathVariable Long id,
                                @Valid @ModelAttribute("projectForm") StudentProjectForm form,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Long studentId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());

        if (bindingResult.hasErrors()) {
            var projects = studentProjectService.getProjectsByStudentId(studentId);
            model.addAttribute("editingId", id);
            model.addAttribute("projects", projects);
            model.addAttribute("projectCount", projects.size());
            model.addAttribute("maxProjects", StudentProjectService.MAX_PROJECTS_PER_PROFILE);
            model.addAttribute("canAddMore", projects.size() < StudentProjectService.MAX_PROJECTS_PER_PROFILE);
            return "student/my-projects";
        }

        try {
            studentProjectService.updateProject(id, studentId, form);
            redirectAttributes.addFlashAttribute("successMessage", "Đã cập nhật dự án thành công.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/my-projects";
    }

    @PostMapping("/{id}/delete")
    public String deleteProject(@AuthenticationPrincipal UserDetails principal,
                                @PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        Long studentId = profileService.resolveStudentProfileIdByEmail(principal.getUsername());
        try {
            studentProjectService.deleteProject(id, studentId);
            redirectAttributes.addFlashAttribute("successMessage", "Đã xoá dự án.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/student/my-projects";
    }
}
