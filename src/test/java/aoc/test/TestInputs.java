package aoc.test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class TestInputs {

    private TestInputs() {}

    public static String day(int number) {
        String resource = "/Input" + String.format("%02d", number) + ".txt";
        try (InputStream stream = TestInputs.class.getResourceAsStream(resource)) {
            if (stream == null) throw new IllegalStateException("No se encontró: " + resource);
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
        } catch (IOException e) {
            throw new RuntimeException("No se pudo leer " + resource, e);
        }
    }
}
