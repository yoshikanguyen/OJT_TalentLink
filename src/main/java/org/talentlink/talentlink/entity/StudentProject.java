package org.talentlink.talentlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "student_projects")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_id", nullable = false)
    private Long studentId;

    @Column(name = "project_name", nullable = false, length = 150)
    private String projectName;

    @Column(name = "role_name", length = 120)
    private String roleName;

    @Column(name = "technologies", length = 255)
    private String technologies;

    @Column(name = "github_url", nullable = false, length = 500)
    private String githubUrl;

    @Column(name = "demo_url", length = 500)
    private String demoUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
