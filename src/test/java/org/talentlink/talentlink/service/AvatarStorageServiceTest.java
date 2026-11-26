package org.talentlink.talentlink.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AvatarStorageServiceTest {

    @TempDir
    Path tempDir;

    private AvatarStorageService avatarStorageService;

    @BeforeEach
    void setUp() {
        avatarStorageService = new AvatarStorageService(
                tempDir.toString(),
                "/uploads/avatars"
        );
    }

    @Test
    void saveAvatar_validJpgAndPng_savesFileAndReturnsPublicUrl() throws Exception {
        MockMultipartFile jpgFile = new MockMultipartFile(
                "avatarFile",
                "avatar.jpg",
                "image/jpeg",
                "fake-jpg-content".getBytes()
        );

        String jpgUrl = avatarStorageService.saveAvatar(jpgFile);

        assertTrue(jpgUrl.startsWith("/uploads/avatars/"));
        assertTrue(jpgUrl.endsWith(".jpg"));
        String jpgFileName = jpgUrl.substring("/uploads/avatars/".length());
        Path savedJpg = tempDir.resolve(jpgFileName);
        assertTrue(Files.exists(savedJpg));
        assertEquals("fake-jpg-content", Files.readString(savedJpg));

        MockMultipartFile pngFile = new MockMultipartFile(
                "avatarFile",
                "avatar.png",
                "image/png",
                "fake-png-content".getBytes()
        );

        String pngUrl = avatarStorageService.saveAvatar(pngFile);

        assertTrue(pngUrl.startsWith("/uploads/avatars/"));
        assertTrue(pngUrl.endsWith(".png"));
        String pngFileName = pngUrl.substring("/uploads/avatars/".length());
        assertTrue(Files.exists(tempDir.resolve(pngFileName)));
    }

    @Test
    void saveAvatar_emptyFile_throwsIllegalArgumentException() {
        MockMultipartFile emptyFile = new MockMultipartFile(
                "avatarFile",
                "empty.png",
                "image/png",
                new byte[0]
        );

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> avatarStorageService.saveAvatar(emptyFile)
        );
        assertNotNull(ex.getMessage());
    }

    @Test
    void saveAvatar_unsupportedContentType_throwsIllegalArgumentException() {
        MockMultipartFile gifFile = new MockMultipartFile(
                "avatarFile",
                "anim.gif",
                "image/gif",
                "GIF89a".getBytes()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> avatarStorageService.saveAvatar(gifFile)
        );
    }

    @Test
    void saveAvatar_exceeds2Mb_throwsIllegalArgumentException() {
        byte[] overLimitBytes = new byte[(int) AvatarStorageService.MAX_AVATAR_BYTES + 1];
        MockMultipartFile largeFile = new MockMultipartFile(
                "avatarFile",
                "big.png",
                "image/png",
                overLimitBytes
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> avatarStorageService.saveAvatar(largeFile)
        );
    }

    @Test
    void deleteAvatarIfExists_deletesMatchingFileAndIgnoresUnsafeOrExternalPaths() throws Exception {
        Path existingFile = tempDir.resolve("old-avatar.png");
        Files.writeString(existingFile, "old-content");
        assertTrue(Files.exists(existingFile));

        avatarStorageService.deleteAvatarIfExists("/uploads/avatars/old-avatar.png");
        assertFalse(Files.exists(existingFile));

        Path outsideFile = tempDir.getParent().resolve("keep-me.png");
        Files.writeString(outsideFile, "keep");
        try {
            avatarStorageService.deleteAvatarIfExists("/uploads/avatars/../../" + outsideFile.getFileName());
            assertTrue(Files.exists(outsideFile));
        } finally {
            Files.deleteIfExists(outsideFile);
        }
    }
}
