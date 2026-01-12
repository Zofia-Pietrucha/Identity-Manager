package com.example.identitymanager.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FileStorageServiceTest {

    @TempDir
    Path tempDir;

    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService(tempDir.toString());
    }

    // ==================== STORE FILE TESTS ====================

    @Test
    void shouldStoreFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-image.jpg",
                "image/jpeg",
                "test image content".getBytes()
        );

        // When
        String storedFilename = fileStorageService.storeFile(file);

        // Then
        assertThat(storedFilename).isNotNull();
        assertThat(storedFilename).endsWith(".jpg");
        assertThat(Files.exists(tempDir.resolve(storedFilename))).isTrue();
    }

    @Test
    void shouldStoreFileWithUniqueFilename() {
        // Given
        MockMultipartFile file1 = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content1".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "content2".getBytes()
        );

        // When
        String filename1 = fileStorageService.storeFile(file1);
        String filename2 = fileStorageService.storeFile(file2);

        // Then
        assertThat(filename1).isNotEqualTo(filename2);
    }

    @Test
    void shouldStoreFileWithoutExtension() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "testfile",
                "application/octet-stream",
                "test content".getBytes()
        );

        // When
        String storedFilename = fileStorageService.storeFile(file);

        // Then
        assertThat(storedFilename).isNotNull();
        assertThat(storedFilename).doesNotContain(".");
        assertThat(Files.exists(tempDir.resolve(storedFilename))).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenStoringEmptyFile() {
        // Given
        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.jpg",
                "image/jpeg",
                new byte[0]
        );

        // When & Then
        assertThatThrownBy(() -> fileStorageService.storeFile(emptyFile))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot store empty file");
    }

    // ==================== LOAD FILE TESTS ====================

    @Test
    void shouldLoadFileAsResource() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        String storedFilename = fileStorageService.storeFile(file);

        // When
        Resource resource = fileStorageService.loadFileAsResource(storedFilename);

        // Then
        assertThat(resource).isNotNull();
        assertThat(resource.exists()).isTrue();
    }

    @Test
    void shouldThrowExceptionWhenLoadingNonExistentFile() {
        // When & Then
        assertThatThrownBy(() -> fileStorageService.loadFileAsResource("non-existent.jpg"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("File not found");
    }

    // ==================== DELETE FILE TESTS ====================

    @Test
    void shouldDeleteFile() {
        // Given
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "to-delete.jpg",
                "image/jpeg",
                "test content".getBytes()
        );
        String storedFilename = fileStorageService.storeFile(file);
        assertThat(Files.exists(tempDir.resolve(storedFilename))).isTrue();

        // When
        fileStorageService.deleteFile(storedFilename);

        // Then
        assertThat(Files.exists(tempDir.resolve(storedFilename))).isFalse();
    }

    @Test
    void shouldNotThrowExceptionWhenDeletingNonExistentFile() {
        // When & Then - should not throw exception
        fileStorageService.deleteFile("non-existent.jpg");
    }

    // ==================== CONSTRUCTOR TESTS ====================

    @Test
    void shouldCreateUploadDirectoryIfNotExists() {
        // Given
        Path newDir = tempDir.resolve("new-upload-dir");
        assertThat(Files.exists(newDir)).isFalse();

        // When
        new FileStorageService(newDir.toString());

        // Then
        assertThat(Files.exists(newDir)).isTrue();
    }
}