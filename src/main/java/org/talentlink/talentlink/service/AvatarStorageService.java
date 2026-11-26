package org.talentlink.talentlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class AvatarStorageService {

    public static final long MAX_AVATAR_BYTES = 2L * 1024L * 1024L; // 2MB
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");

    private final Path avatarStoragePath;
    private final String publicPrefix;

    public AvatarStorageService(
            @Value("${app.upload.avatar-dir:uploads/avatars}") String avatarDir,
            @Value("${app.upload.avatar-public-prefix:/uploads/avatars}") String publicPrefix
    ) {
        this.avatarStoragePath = Paths.get(avatarDir).toAbsolutePath().normalize();
        this.publicPrefix = normalizePrefix(publicPrefix);
    }

    public String saveAvatar(MultipartFile file) {
        validateAvatar(file);

        String extension = resolveExtension(file);
        String fileName = UUID.randomUUID() + extension;

        try {
            Files.createDirectories(avatarStoragePath);
            Path targetPath = avatarStoragePath.resolve(fileName).normalize();
            if (!targetPath.startsWith(avatarStoragePath)) {
                throw new IllegalArgumentException("Đường dẫn lưu ảnh không hợp lệ.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return publicPrefix + "/" + fileName;
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu ảnh đại diện. Vui lòng thử lại.", ex);
        }
    }

    public void deleteAvatarIfExists(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.isBlank()) {
            return;
        }
        String prefixWithSlash = publicPrefix + "/";
        if (!avatarUrl.startsWith(prefixWithSlash)) {
            return;
        }
        String fileName = avatarUrl.substring(prefixWithSlash.length());
        if (fileName.isBlank() || fileName.contains("/") || fileName.contains("\\") || fileName.contains("..")) {
            return;
        }
        Path targetPath = avatarStoragePath.resolve(fileName).normalize();
        if (targetPath.startsWith(avatarStoragePath)) {
            try {
                Files.deleteIfExists(targetPath);
            } catch (IOException ignored) {
                // Tránh làm hỏng giao dịch cập nhật hồ sơ nếu file tạm không còn trên đĩa
            }
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file ảnh đại diện.");
        }
        if (file.getSize() > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("Ảnh đại diện vượt quá dung lượng tối đa 2MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new IllegalArgumentException("Chỉ hỗ trợ ảnh định dạng JPG hoặc PNG.");
        }
    }

    private String resolveExtension(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            String lower = originalFilename.toLowerCase(Locale.ROOT);
            if (lower.endsWith(".png")) {
                return ".png";
            }
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
                return ".jpg";
            }
        }
        String contentType = file.getContentType();
        if ("image/png".equalsIgnoreCase(contentType)) {
            return ".png";
        }
        return ".jpg";
    }

    private String normalizePrefix(String prefix) {
        String p = (prefix == null || prefix.isBlank()) ? "/uploads/avatars" : prefix.trim();
        if (!p.startsWith("/")) {
            p = "/" + p;
        }
        return p.endsWith("/") ? p.substring(0, p.length() - 1) : p;
    }
}
