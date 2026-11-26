package org.talentlink.talentlink.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.talentlink.talentlink.dto.StudentProjectForm;
import org.talentlink.talentlink.entity.StudentProject;
import org.talentlink.talentlink.repository.StudentProjectRepository;

import java.net.URI;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentProjectService {

    public static final int MAX_PROJECTS_PER_PROFILE = 20;

    private final StudentProjectRepository studentProjectRepository;

    public List<StudentProject> getProjectsByStudentId(Long studentId) {
        return studentProjectRepository.findAllByStudentIdOrderByIdDesc(studentId);
    }

    public StudentProject getProjectByIdAndStudentId(Long projectId, Long studentId) {
        return studentProjectRepository.findByIdAndStudentId(projectId, studentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy dự án hoặc bạn không có quyền truy cập."));
    }

    @Transactional
    public StudentProject createProject(Long studentId, StudentProjectForm form) {
        long currentCount = studentProjectRepository.countByStudentId(studentId);
        if (currentCount >= MAX_PROJECTS_PER_PROFILE) {
            throw new IllegalStateException("Mỗi hồ sơ chỉ được thêm tối đa " + MAX_PROJECTS_PER_PROFILE + " dự án.");
        }

        String normalizedGithubUrl = validateAndNormalizeUrl(form.getGithubUrl(), true, "Link GitHub");
        String normalizedDemoUrl = validateAndNormalizeUrl(form.getDemoUrl(), false, "Link demo");

        StudentProject project = StudentProject.builder()
                .studentId(studentId)
                .projectName(form.getProjectName().trim())
                .roleName(trimToNull(form.getRoleName()))
                .technologies(trimToNull(form.getTechnologies()))
                .githubUrl(normalizedGithubUrl)
                .demoUrl(normalizedDemoUrl)
                .description(trimToNull(form.getDescription()))
                .build();

        return studentProjectRepository.save(project);
    }

    @Transactional
    public StudentProject updateProject(Long projectId, Long studentId, StudentProjectForm form) {
        StudentProject project = getProjectByIdAndStudentId(projectId, studentId);

        String normalizedGithubUrl = validateAndNormalizeUrl(form.getGithubUrl(), true, "Link GitHub");
        String normalizedDemoUrl = validateAndNormalizeUrl(form.getDemoUrl(), false, "Link demo");

        project.setProjectName(form.getProjectName().trim());
        project.setRoleName(trimToNull(form.getRoleName()));
        project.setTechnologies(trimToNull(form.getTechnologies()));
        project.setGithubUrl(normalizedGithubUrl);
        project.setDemoUrl(normalizedDemoUrl);
        project.setDescription(trimToNull(form.getDescription()));

        return studentProjectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long projectId, Long studentId) {
        StudentProject project = getProjectByIdAndStudentId(projectId, studentId);
        studentProjectRepository.delete(project);
    }

    private String validateAndNormalizeUrl(String rawUrl, boolean required, String fieldLabel) {
        String trimmed = trimToNull(rawUrl);
        if (trimmed == null) {
            if (required) {
                throw new IllegalArgumentException(fieldLabel + " không được để trống.");
            }
            return null;
        }

        try {
            URI uri = URI.create(trimmed);
            String scheme = uri.getScheme();
            if (scheme == null) {
                throw new IllegalArgumentException(fieldLabel + " phải bắt đầu bằng http:// hoặc https://.");
            }
            String lowerScheme = scheme.toLowerCase(Locale.ROOT);
            if (!"http".equals(lowerScheme) && !"https".equals(lowerScheme)) {
                throw new IllegalArgumentException(fieldLabel + " chỉ chấp nhận giao thức http hoặc https.");
            }
            if (uri.getHost() == null || uri.getHost().isBlank()) {
                throw new IllegalArgumentException(fieldLabel + " không hợp lệ.");
            }
            return trimmed;
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(fieldLabel + " không hợp lệ. Vui lòng nhập URL hợp lệ (http/https).");
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
