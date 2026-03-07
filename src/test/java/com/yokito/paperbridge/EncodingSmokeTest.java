package com.yokito.paperbridge;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

class EncodingSmokeTest {

    private static final List<Path> ROOTS = List.of(
            Path.of("src/main/java"),
            Path.of("src/main/resources"),
            Path.of("src/test/java")
    );

    @Test
    void sourceAndResourceFilesShouldRemainUtf8WithoutBom() throws IOException {
        for (Path root : ROOTS) {
            if (!Files.exists(root)) {
                continue;
            }

            try (Stream<Path> paths = Files.walk(root)) {
                paths.filter(Files::isRegularFile)
                        .forEach(this::assertUtf8WithoutBom);
            }
        }
    }

    private void assertUtf8WithoutBom(Path path) {
        byte[] bytes = assertDoesNotThrow(() -> Files.readAllBytes(path), () -> "Unable to read file: " + path);
        assertFalse(hasUtf8Bom(bytes), () -> "UTF-8 BOM is not allowed: " + path);
        assertDoesNotThrow(() -> decodeUtf8(bytes), () -> "File is not valid UTF-8: " + path);
    }

    private boolean hasUtf8Bom(byte[] bytes) {
        return bytes.length >= 3
                && bytes[0] == (byte) 0xEF
                && bytes[1] == (byte) 0xBB
                && bytes[2] == (byte) 0xBF;
    }

    private void decodeUtf8(byte[] bytes) throws CharacterCodingException {
        StandardCharsets.UTF_8.newDecoder()
                .onMalformedInput(CodingErrorAction.REPORT)
                .onUnmappableCharacter(CodingErrorAction.REPORT)
                .decode(ByteBuffer.wrap(bytes));
    }
}
