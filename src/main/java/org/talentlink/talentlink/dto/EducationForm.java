package org.talentlink.talentlink.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class EducationForm {

    @NotBlank(message = "Trường không được để trống")
    @Size(max = 150, message = "Tên trường tối đa 150 ký tự")
    private String schoolName;

    @NotBlank(message = "Chuyên ngành không được để trống")
    @Size(max = 150, message = "Chuyên ngành tối đa 150 ký tự")
    private String major;

    @NotNull(message = "Từ ngày không được để trống")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    @DecimalMin(value = "0.0", inclusive = true, message = "GPA phải từ 0.0 đến 4.0")
    @DecimalMax(value = "4.0", inclusive = true, message = "GPA phải từ 0.0 đến 4.0")
    private BigDecimal gpa;
}