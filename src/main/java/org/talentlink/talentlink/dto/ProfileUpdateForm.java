package org.talentlink.talentlink.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

@Getter
@Setter
public class ProfileUpdateForm {

    @NotBlank(message = "Họ tên không được để trống")
    @Size(max = 150, message = "Họ tên tối đa 150 ký tự")
    private String fullName;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Past(message = "Ngày sinh phải là ngày trong quá khứ")
    private LocalDate dateOfBirth;

    @Pattern(
            regexp = "^$|^(0|\\+84)[0-9]{9,10}$",
            message = "Số điện thoại phải bắt đầu bằng 0 hoặc +84 và gồm 10-11 chữ số"
    )
    @Size(max = 20, message = "Số điện thoại tối đa 20 ký tự")
    private String phone;

    @Size(max = 2000, message = "Giới thiệu bản thân tối đa 2000 ký tự")
    private String bio;

    @Size(max = 150, message = "Vị trí mong muốn tối đa 150 ký tự")
    private String desiredPosition;

    @Size(max = 150, message = "Nơi làm việc mong muốn tối đa 150 ký tự")
    private String desiredLocation;

    private MultipartFile avatarFile;

    private boolean removeAvatar;
}