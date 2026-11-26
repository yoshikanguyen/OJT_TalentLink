package org.talentlink.talentlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentCertificateForm {

    @NotBlank(message = "Tên chứng chỉ không được để trống")
    @Size(max = 150, message = "Tên chứng chỉ tối đa 150 ký tự")
    private String certificateName;

    @NotBlank(message = "Đơn vị cấp không được để trống")
    @Size(max = 150, message = "Đơn vị cấp tối đa 150 ký tự")
    private String issuingOrganization;

    @NotNull(message = "Ngày cấp không được để trống")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate issueDate;

    private MultipartFile proofFile;
}