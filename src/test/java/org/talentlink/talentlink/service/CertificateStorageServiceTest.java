package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CertificateStorageServiceTest {

    @TempDir
    Path tempDir;

    private CertificateStorageService storageService;

    @BeforeEach
    void setUp() {
        storageService = new CertificateStorageService(tempDir.toString());
    }

    @Test
    void storeCertificateFile_validPdfJpgPng_storesWithRandomName() throws Exception {
        MockMultipartFile pdf = new MockMultipartFile(
                "proofFile", "jlpt.pdf", "application/pdf", "%PDF-1.4".getBytes()
        );
        var storedPdf = storageService.storeCertificateFile(pdf);
        assertTrue(storedPdf.storedFileName().endsWith(".pdf"));
        assertNotEquals("jlpt.pdf", storedPdf.storedFileName());
        assertTrue(Files.exists(tempDir.resolve(storedPdf.storedFileName())));

        MockMultipartFile png = new MockMultipartFile(
                "proofFile", "cert.png", "image/png", "png-bytes".getBytes()
        );
        var storedPng = storageService.storeCertificateFile(png);
        assertTrue(storedPng.storedFileName().endsWith(".png"));
        assertTrue(Files.exists(tempDir.resolve(storedPng.storedFileName())));
    }

    @Test
    void storeCertificateFile_mismatchedExtensionAndContentType_throws() {
        MockMultipartFile fakePdf = new MockMultipartFile(
                "proofFile", "evil.pdf", "image/png", "not-a-pdf".getBytes()
        );
        assertThrows(IllegalArgumentException.class, () -> storageService.storeCertificateFile(fakePdf));
    }

    @Test
    void storeCertificateFile_exceeds5Mb_throws() {
        byte[] tooLarge = new byte[(int) CertificateStorageService.MAX_CERTIFICATE_BYTES + 1];
        MockMultipartFile bigFile = new MockMultipartFile(
                "proofFile", "big.pdf", "application/pdf", tooLarge
        );
        assertThrows(IllegalArgumentException.class, () -> storageService.storeCertificateFile(bigFile));
    }

    @Test
    void deleteFileIfExists_removesStoredFileSafely() throws Exception {
        Path file = tempDir.resolve("sample.pdf");
        Files.writeString(file, "data");
        assertTrue(Files.exists(file));

        storageService.deleteFileIfExists("sample.pdf");
        assertFalse(Files.exists(file));
    }
}
