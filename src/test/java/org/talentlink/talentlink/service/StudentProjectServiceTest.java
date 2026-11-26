package org.talentlink.talentlink.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.talentlink.talentlink.dto.StudentProjectForm;
import org.talentlink.talentlink.entity.StudentProject;
import org.talentlink.talentlink.repository.StudentProjectRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentProjectServiceTest {

    @Mock
    private StudentProjectRepository studentProjectRepository;

    @InjectMocks
    private StudentProjectService studentProjectService;

    private StudentProjectForm validForm(String githubUrl, String demoUrl) {
        return StudentProjectForm.builder()
                .projectName("  TalentLink Portfolio  ")
                .roleName("  Backend Developer  ")
                .technologies("  Java, Spring Boot, MySQL  ")
                .githubUrl(githubUrl)
                .demoUrl(demoUrl)
                .description("  Personal project management feature  ")
                .build();
    }

    // --- 1. 20-project limit tests ---

    @Test
    void createProject_whenCountIs19_succeeds() {
        Long studentId = 10L;
        when(studentProjectRepository.countByStudentId(studentId)).thenReturn(19L);
        when(studentProjectRepository.save(any(StudentProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProjectForm form = validForm("https://github.com/example/talentlink", "   ");
        StudentProject saved = studentProjectService.createProject(studentId, form);

        assertThat(saved.getStudentId()).isEqualTo(studentId);
        assertThat(saved.getProjectName()).isEqualTo("TalentLink Portfolio");
        assertThat(saved.getRoleName()).isEqualTo("Backend Developer");
        assertThat(saved.getTechnologies()).isEqualTo("Java, Spring Boot, MySQL");
        assertThat(saved.getGithubUrl()).isEqualTo("https://github.com/example/talentlink");
        assertThat(saved.getDemoUrl()).isNull();
        assertThat(saved.getDescription()).isEqualTo("Personal project management feature");

        verify(studentProjectRepository).countByStudentId(studentId);
        verify(studentProjectRepository).save(any(StudentProject.class));
    }

    @Test
    void createProject_whenCountReaches20_throwsIllegalStateExceptionAndDoesNotSave() {
        Long studentId = 10L;
        when(studentProjectRepository.countByStudentId(studentId)).thenReturn(20L);

        StudentProjectForm form = validForm("https://github.com/example/repo", null);

        assertThatThrownBy(() -> studentProjectService.createProject(studentId, form))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("20");

        verify(studentProjectRepository, never()).save(any());
    }

    // --- 2. URL validation tests ---

    @ParameterizedTest
    @ValueSource(strings = {
            "",
            "   ",
            "github.com/example/repo",
            "ftp://github.com/example/repo",
            "javascript:alert(1)",
            "https://",
            "not a url"
    })
    void createProject_withInvalidOrBlankGithubUrl_throwsIllegalArgumentException(String invalidGithubUrl) {
        Long studentId = 10L;
        when(studentProjectRepository.countByStudentId(studentId)).thenReturn(0L);

        StudentProjectForm form = validForm(invalidGithubUrl, "https://demo.example.com");

        assertThatThrownBy(() -> studentProjectService.createProject(studentId, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Link GitHub");

        verify(studentProjectRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "demo.example.com",
            "ftp://demo.example.com",
            "http://",
            ":::invalid"
    })
    void createProject_withInvalidOptionalDemoUrl_throwsIllegalArgumentException(String invalidDemoUrl) {
        Long studentId = 10L;
        when(studentProjectRepository.countByStudentId(studentId)).thenReturn(0L);

        StudentProjectForm form = validForm("https://github.com/example/repo", invalidDemoUrl);

        assertThatThrownBy(() -> studentProjectService.createProject(studentId, form))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Link demo");

        verify(studentProjectRepository, never()).save(any());
    }

    @Test
    void createProject_withValidHttpAndHttpsUrls_normalizesAndSaves() {
        Long studentId = 10L;
        when(studentProjectRepository.countByStudentId(studentId)).thenReturn(2L);
        when(studentProjectRepository.save(any(StudentProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProjectForm form = validForm(
                "  http://github.com/example/project  ",
                "  https://my-demo.vercel.app  "
        );

        studentProjectService.createProject(studentId, form);

        ArgumentCaptor<StudentProject> captor = ArgumentCaptor.forClass(StudentProject.class);
        verify(studentProjectRepository).save(captor.capture());
        assertThat(captor.getValue().getGithubUrl()).isEqualTo("http://github.com/example/project");
        assertThat(captor.getValue().getDemoUrl()).isEqualTo("https://my-demo.vercel.app");
    }

    // --- 3. Owner check tests ---

    @Test
    void updateProject_whenOwnedByStudent_updatesFields() {
        Long projectId = 100L;
        Long ownerStudentId = 10L;

        StudentProject existing = StudentProject.builder()
                .id(projectId)
                .studentId(ownerStudentId)
                .projectName("Old Name")
                .githubUrl("https://github.com/old/repo")
                .build();

        when(studentProjectRepository.findByIdAndStudentId(projectId, ownerStudentId))
                .thenReturn(Optional.of(existing));
        when(studentProjectRepository.save(any(StudentProject.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        StudentProjectForm updateForm = validForm(
                "https://github.com/new/repo",
                "https://new-demo.example.com"
        );

        StudentProject updated = studentProjectService.updateProject(projectId, ownerStudentId, updateForm);

        assertThat(updated.getProjectName()).isEqualTo("TalentLink Portfolio");
        assertThat(updated.getGithubUrl()).isEqualTo("https://github.com/new/repo");
        assertThat(updated.getDemoUrl()).isEqualTo("https://new-demo.example.com");
        verify(studentProjectRepository).save(existing);
    }

    @Test
    void updateProject_whenNotOwnedByStudent_throwsIllegalArgumentExceptionAndDoesNotSave() {
        Long projectId = 100L;
        Long otherStudentId = 99L;

        when(studentProjectRepository.findByIdAndStudentId(projectId, otherStudentId))
                .thenReturn(Optional.empty());

        StudentProjectForm updateForm = validForm("https://github.com/hack/repo", null);

        assertThatThrownBy(() -> studentProjectService.updateProject(projectId, otherStudentId, updateForm))
                .isInstanceOf(IllegalArgumentException.class);

        verify(studentProjectRepository, never()).save(any());
    }

    @Test
    void deleteProject_whenOwnedByStudent_deletesProject() {
        Long projectId = 100L;
        Long ownerStudentId = 10L;

        StudentProject existing = StudentProject.builder()
                .id(projectId)
                .studentId(ownerStudentId)
                .projectName("To Delete")
                .githubUrl("https://github.com/user/repo")
                .build();

        when(studentProjectRepository.findByIdAndStudentId(projectId, ownerStudentId))
                .thenReturn(Optional.of(existing));

        studentProjectService.deleteProject(projectId, ownerStudentId);

        verify(studentProjectRepository).delete(existing);
    }

    @Test
    void deleteProject_whenNotOwnedByStudent_throwsIllegalArgumentExceptionAndDoesNotDelete() {
        Long projectId = 100L;
        Long otherStudentId = 99L;

        when(studentProjectRepository.findByIdAndStudentId(projectId, otherStudentId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> studentProjectService.deleteProject(projectId, otherStudentId))
                .isInstanceOf(IllegalArgumentException.class);

        verify(studentProjectRepository, never()).delete(any());
    }
}
