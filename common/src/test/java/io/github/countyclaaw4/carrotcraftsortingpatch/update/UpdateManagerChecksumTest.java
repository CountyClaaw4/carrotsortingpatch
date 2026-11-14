package io.github.countyclaaw4.carrotcraftsortingpatch.update;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UpdateManagerChecksumTest {

    @TempDir
    Path tempDir;

    @Test
    void findsChecksumForExactAssetName() {
        String checksumFile = "a".repeat(64) + "  first.jar\n"
                + "b".repeat(64) + "  target.jar\n";

        assertEquals(Optional.of("b".repeat(64)), UpdateManager.findChecksum(checksumFile, "target.jar"));
        assertEquals(Optional.empty(), UpdateManager.findChecksum(checksumFile, "other.jar"));
    }

    @Test
    void rejectsMalformedOrDuplicateEntries() {
        assertEquals(Optional.empty(), UpdateManager.findChecksum("not-a-checksum  target.jar\n", "target.jar"));
        String duplicate = "a".repeat(64) + "  target.jar\n" + "b".repeat(64) + "  target.jar\n";
        assertEquals(Optional.empty(), UpdateManager.findChecksum(duplicate, "target.jar"));
    }

    @Test
    void calculatesSha256OfDownloadedFile() throws IOException {
        Path file = tempDir.resolve("payload.jar");
        Files.writeString(file, "abc");

        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                UpdateManager.calculateSha256(file));
    }
}