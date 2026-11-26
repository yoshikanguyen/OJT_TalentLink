package org.talentlink.talentlink.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "student_certificates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCertificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "student_profile_id", nullable = false)
    private Long studentProfileId;

    @Column(name = "certificate_name", nullable = false, length = 150)
    private String certificateName;

    @Column(name = "issuing_organization", nullable = false, length = 150)
    private String issuingOrganization;

    @Column(name = "issue_date", nullable = false)
    private LocalDate issueDate;

    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    @Column(name = "original_file_name", nullable = false, length = 255)
    private String originalFileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "file_size_bytes", nullable = false)
    private Long fileSizeBytes;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
