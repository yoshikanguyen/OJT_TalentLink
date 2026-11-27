package org.talentlink.talentlink.controller;

import org.talentlink.talentlink.entity.Skill;
import org.talentlink.talentlink.service.SkillService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.security.test.context.support.WithMockUser;

@WebMvcTest(SkillController.class)
@WithMockUser(roles = "ADMIN")
class SkillControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SkillService skillService;

    @Test
    void listSkills_shouldRenderAdminSkillsView() throws Exception {
        when(skillService.findAll()).thenReturn(List.of(
                Skill.builder().id(1L).name("Java").category("Backend").isActive(true).build()
        ));

        mockMvc.perform(get("/admin/skills"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/skills"))
                .andExpect(model().attributeExists("skills"));
    }

    @Test
    void createSkill_shouldRedirectWithSuccessFlashMessage() throws Exception {
        mockMvc.perform(post("/admin/skills/create")
                        .with(csrf())
                        .param("name", "Thymeleaf")
                        .param("category", "Web"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(skillService).create("Thymeleaf", "Web");
    }

    @Test
    void createSkill_shouldRedirectWithErrorFlashMessageWhenInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Kỹ năng 'Java' đã tồn tại."))
                .when(skillService).create("Java", "Backend");

        mockMvc.perform(post("/admin/skills/create")
                        .with(csrf())
                        .param("name", "Java")
                        .param("category", "Backend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void updateSkill_shouldRedirectWithSuccessFlashMessage() throws Exception {
        mockMvc.perform(post("/admin/skills/1/update")
                        .with(csrf())
                        .param("name", "Spring Boot")
                        .param("category", "Backend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(skillService).update(1L, "Spring Boot", "Backend");
    }

    @Test
    void toggleActive_shouldRedirectWithSuccessFlashMessage() throws Exception {
        mockMvc.perform(post("/admin/skills/1/toggle-active")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(skillService).toggleActive(1L);
    }

    @Test
    void createSkill_shouldRedirectWithErrorFlashMessageWhenNameIsBlank() throws Exception {
        doThrow(new IllegalArgumentException("Tên kỹ năng không được để trống."))
                .when(skillService).create("   ", "Backend");

        mockMvc.perform(post("/admin/skills/create")
                        .with(csrf())
                        .param("name", "   ")
                        .param("category", "Backend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void updateSkill_shouldRedirectWithErrorFlashMessageWhenInvalid() throws Exception {
        doThrow(new IllegalArgumentException("Không tìm thấy kỹ năng hoặc tên kỹ năng không hợp lệ."))
                .when(skillService).update(99L, "", "Backend");

        mockMvc.perform(post("/admin/skills/99/update")
                        .with(csrf())
                        .param("name", "")
                        .param("category", "Backend"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/skills"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}
