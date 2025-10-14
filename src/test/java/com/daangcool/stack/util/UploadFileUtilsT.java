package com.daangcool.stack.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UploadFileUtilsT {

    @TempDir
    Path tempDir;

    @Test
    void fileSaveCreatesPhysicalFileAndReturnsWebPath() throws IOException {
        MockMultipartFile multipartFile = new MockMultipartFile(
            "file",
            "example.txt",
            "text/plain",
            "sample-content".getBytes()
        );

        String webPath = UploadFileUtils.fileSave(tempDir.toString(), "NOTICE", multipartFile, true);

        assertThat(webPath).startsWith("/uploads/public/NOTICE/");

        Path storedFile = resolveFromWebPath(webPath);
        assertTrue(Files.exists(storedFile), "Stored file should exist");
        assertThat(Files.readString(storedFile)).isEqualTo("sample-content");
    }

    @Test
    void deleteFileRemovesStoredFileAndDirectories() throws IOException {
        Path noticeDir = tempDir
            .resolve("uploads")
            .resolve("public")
            .resolve("NOTICE");
        Files.createDirectories(noticeDir);
        Path storedFile = noticeDir.resolve("to-delete.txt");
        Files.writeString(storedFile, "delete-me");

        boolean deleted = UploadFileUtils.deleteFile(tempDir.toString(), "uploads/public/NOTICE/to-delete.txt", "/uploads");

        assertTrue(deleted, "deleteFile should return true when deletion succeeds");
        assertFalse(Files.exists(storedFile), "Stored file should be removed");
        assertFalse(Files.exists(noticeDir), "Empty directories should be cleaned up");
    }

    @Test
    void moveFileBetweenScopesMovesFileAndReturnsNewWebPath() throws IOException {
        LocalDate today = LocalDate.now();
        Path dateDir = tempDir
            .resolve("uploads")
            .resolve("public")
            .resolve("NOTICE")
            .resolve(String.valueOf(today.getYear()))
            .resolve(String.format("%02d", today.getMonthValue()));
        Files.createDirectories(dateDir);
        Path storedFile = dateDir.resolve("scope-change.txt");
        Files.writeString(storedFile, "move-me");

        String currentWebPath = String.format(
            "/uploads/public/NOTICE/%d/%02d/scope-change.txt",
            today.getYear(),
            today.getMonthValue()
        );

        String newWebPath = UploadFileUtils.moveFileBetweenScopes(tempDir.toString(), currentWebPath, "/uploads/private");

        assertThat(newWebPath).isEqualTo(currentWebPath.replace("/uploads/public", "/uploads/private"));

        Path newLocation = resolveFromWebPath(newWebPath);
        assertTrue(Files.exists(newLocation), "File should exist in new location");
        assertFalse(Files.exists(storedFile), "Original file should no longer exist");
    }

    @Test
    void utilityMethodsProvideConsistentPaths() {
        // In Windows, path separators are backslashes.
        // Web paths should always use forward slashes.
        // This test ensures that buildWebPath produces consistent web paths regardless of the OS.
        String datePath = Path.of("2025", "10").toString(); // Creates "2025/10" on Unix, "2025\10" on Windows.
        assertAll(
            () -> assertThat(UploadFileUtils.getExtension("archive.tar.gz")).isEqualTo("gz"),
            () -> assertThat(UploadFileUtils.buildWebPath("uploads/public", datePath, "file.txt"))
                .isEqualTo("/uploads/public/2025/10/file.txt")
        );
    }

    private Path resolveFromWebPath(String webPath) {
        String normalized = webPath.startsWith("/") ? webPath.substring(1) : webPath;
        Path resolved = tempDir;
        for (String segment : normalized.split("/")) {
            resolved = resolved.resolve(segment);
        }
        return resolved;
    }
}
