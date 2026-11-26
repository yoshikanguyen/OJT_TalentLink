package org.talentlink.talentlink.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.talentlink.talentlink.dto.StudentProjectForm;
import org.talentlink.talentlink.entity.StudentProject;
import org.talentlink.talentlink.service.ProfileService;
import org.talentlink.talentlink.service.StudentProjectService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(StudentProjectController.class)
@WithMockUser(username = "student@example.com", roles = "STUDENT")
class StudentProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StudentProjectService studentProjectService;

    @MockitoBean
    private ProfileService profileService;

    private static final Long STUDENT_ID = 10L;

    @Test
    void listProjects_rendersViewWithProjectsAndQuota() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);
        StudentProject p1 = StudentProject.builder()
                .id(1L)
                .studentId(STUDENT_ID)
                .projectName("TalentLink")
                .githubUrl("https://github.com/example/talentlink")
                .build();
        when(studentProjectService.getProjectsByStudentId(STUDENT_ID)).thenReturn(List.of(p1));

        mockMvc.perform(get("/student/my-projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-projects"))
                .andExpect(model().attributeExists("projects", "projectForm"))
                .andExpect(model().attribute("projectCount", 1))
                .andExpect(model().attribute("maxProjects", StudentProjectService.MAX_PROJECTS_PER_PROFILE))
                .andExpect(model().attribute("canAddMore", true));

        verify(profileService).resolveStudentProfileIdByEmail("student@example.com");
        verify(studentProjectService).getProjectsByStudentId(STUDENT_ID);
    }

    @Test
    void createProject_withValidInput_redirectsWithSuccessFlash() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);

        mockMvc.perform(post("/student/my-projects")
                        .with(csrf())
                        .param("projectName", "Portfolio App")
                        .param("roleName", "Backend Dev")
                        .param("technologies", "Java, Spring Boot")
                        .param("githubUrl", "https://github.com/example/portfolio")
                        .param("demoUrl", "https://portfolio.example.com")
                        .param("description", "Personal project"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/my-projects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(studentProjectService).createProject(eq(STUDENT_ID), any(StudentProjectForm.class));
    }

    @Test
    void createProject_whenValidationFails_returnsFormViewWithoutCallingService() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);
        when(studentProjectService.getProjectsByStudentId(STUDENT_ID)).thenReturn(List.of());

        mockMvc.perform(post("/student/my-projects")
                        .with(csrf())
                        .param("projectName", "")
                        .param("githubUrl", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-projects"))
                .andExpect(model().hasErrors());

        verify(studentProjectService, never()).createProject(any(), any());
    }

    @Test
    void createProject_whenLimitReached_redirectsWithErrorFlash() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);
        doThrow(new IllegalStateException("Mỗi hồ sơ chỉ được thêm tối đa 20 dự án."))
                .when(studentProjectService).createProject(eq(STUDENT_ID), any(StudentProjectForm.class));

        mockMvc.perform(post("/student/my-projects")
                        .with(csrf())
                        .param("projectName", "Project 21")
                        .param("githubUrl", "https://github.com/example/p21"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/my-projects"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void editForm_whenProjectOwnedByStudent_populatesFormAndEditingId() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);
        StudentProject existing = StudentProject.builder()
                .id(5L)
                .studentId(STUDENT_ID)
                .projectName("Existing Project")
                .roleName("Fullstack")
                .technologies("Java, Thymeleaf")
                .githubUrl("https://github.com/example/existing")
                .demoUrl("https://demo.example.com")
                .description("Desc")
                .build();
        when(studentProjectService.getProjectByIdAndStudentId(5L, STUDENT_ID)).thenReturn(existing);
        when(studentProjectService.getProjectsByStudentId(STUDENT_ID)).thenReturn(List.of(existing));

        mockMvc.perform(get("/student/my-projects/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("student/my-projects"))
                .andExpect(model().attribute("editingId", 5L))
                .andExpect(model().attributeExists("projectForm"));
    }

    @Test
    void updateProject_whenNotOwner_redirectsWithErrorFlash() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);
        doThrow(new IllegalArgumentException("Không tìm thấy dự án hoặc bạn không có quyền truy cập."))
                .when(studentProjectService).updateProject(eq(99L), eq(STUDENT_ID), any(StudentProjectForm.class));

        mockMvc.perform(post("/student/my-projects/99")
                        .with(csrf())
                        .param("projectName", "Updated Name")
                        .param("githubUrl", "https://github.com/example/updated"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/my-projects"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    void deleteProject_whenOwnedByStudent_redirectsWithSuccessFlash() throws Exception {
        when(profileService.resolveStudentProfileIdByEmail("student@example.com")).thenReturn(STUDENT_ID);

        mockMvc.perform(post("/student/my-projects/5/delete")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/student/my-projects"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(studentProjectService).deleteProject(5L, STUDENT_ID);
    }
}
