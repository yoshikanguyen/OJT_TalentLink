package org.talentlink.talentlink.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class CertificateStorageService {

    public static final long MAX_CERTIFICATE_BYTES = 5L * 1024 * 1024; // 5MB

    private static final Map<String, Set<String>> EXTENSION_TO_CONTENT_TYPES = Map.of(
            ".pdf", Set.of("application/pdf"),
            ".jpg", Set.of("image/jpeg"),
            ".jpeg", Set.of("image/jpeg"),
            ".png", Set.of("image/png")
    );

    private final Path certificateRootDir;

    public CertificateStorageService(
            @Value("${app.upload.certificate-dir:uploads/certificates}") String certificateDir
    ) {
        this.certificateRootDir = Paths.get(certificateDir).toAbsolutePath().normalize();
    }

    public record StoredCertificateFile(
            String storedFileName,
            String originalFileName,
            String contentType,
            long fileSizeBytes
    ) {}

    public StoredCertificateFile storeCertificateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Vui lòng chọn file minh chứng (PDF, JPG hoặc PNG, tối đa 5MB).");
        }
        if (file.getSize() > MAX_CERTIFICATE_BYTES) {
            throw new IllegalArgumentException("File minh chứng vượt quá dung lượng tối đa 5MB.");
        }

        String rawOriginalName = StringUtils.cleanPath(
                file.getOriginalFilename() != null ? file.getOriginalFilename() : ""
        );
        String ext = extractLowerExtension(rawOriginalName);
        if (!EXTENSION_TO_CONTENT_TYPES.containsKey(ext)) {
            throw new IllegalArgumentException("Chỉ chấp nhận file định dạng PDF, JPG hoặc PNG.");
        }

        String normalizedContentType = normalizeContentType(file.getContentType());
        Set<String> allowedTypesForExt = EXTENSION_TO_CONTENT_TYPES.get(ext);
        if (!allowedTypesForExt.contains(normalizedContentType)) {
            throw new IllegalArgumentException("Định dạng nội dung (Content-Type) không khớp với phần mở rộng file.");
        }

        String storedFileName = UUID.randomUUID() + ext;

        try {
            Files.createDirectories(certificateRootDir);
            Path targetPath = certificateRootDir.resolve(storedFileName).normalize();
            if (!targetPath.startsWith(certificateRootDir)) {
                throw new SecurityException("Đường dẫn lưu file không hợp lệ.");
            }
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }
            return new StoredCertificateFile(
                    storedFileName,
                    rawOriginalName.isBlank() ? storedFileName : rawOriginalName,
                    normalizedContentType,
                    file.getSize()
            );
        } catch (IOException ex) {
            throw new IllegalStateException("Không thể lưu file minh chứng.", ex);
        }
    }

    public Path resolveExistingFile(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            throw new IllegalArgumentException("Không tìm thấy file minh chứng.");
        }
        Path resolved = certificateRootDir.resolve(storedFileName).normalize();
        if (!resolved.startsWith(certificateRootDir) || Files.isDirectory(resolved) || !Files.exists(resolved)) {
            throw new IllegalArgumentException("File minh chứng không tồn tại.");
        }
        return resolved;
    }

    public void deleteFileIfExists(String storedFileName) {
        if (storedFileName == null || storedFileName.isBlank()) {
            return;
        }
        try {
            Path resolved = certificateRootDir.resolve(storedFileName).normalize();
            if (!resolved.startsWith(certificateRootDir) || Files.isDirectory(resolved)) {
                return;
            }
            Files.deleteIfExists(resolved);
        } catch (IOException ignored) {
            // Tránh làm gián đoạn luồng chính nếu file đã không còn trên đĩa
        }
    }

    private String extractLowerExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(dotIndex).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        if (contentType == null) {
            return "";
        }
        int semicolonIdx = contentType.indexOf(';');
        String base = semicolonIdx >= 0 ? contentType.substring(0, semicolonIdx) : contentType;
        return base.trim().toLowerCase(Locale.ROOT);
    }
}
