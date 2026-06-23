package aoc.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class InputReader {

    public String read(int day) {
        String resource = "/Input" + String.format("%02d", day) + ".txt";
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            if (stream == null) {
                throw new IllegalArgumentException("Input not found: " + resource);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
