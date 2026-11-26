package org.talentlink.talentlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentProjectForm {

    @NotBlank(message = "Tên dự án không được để trống")
    @Size(max = 150, message = "Tên dự án tối đa 150 ký tự")
    private String projectName;

    @Size(max = 120, message = "Vai trò tối đa 120 ký tự")
    private String roleName;

    @Size(max = 255, message = "Công nghệ sử dụng tối đa 255 ký tự")
    private String technologies;

    @NotBlank(message = "Link GitHub không được để trống")
    @Size(max = 500, message = "Link GitHub tối đa 500 ký tự")
    private String githubUrl;

    @Size(max = 500, message = "Link demo tối đa 500 ký tự")
    private String demoUrl;

    @Size(max = 5000, message = "Mô tả tối đa 5000 ký tự")
    private String description;
}
