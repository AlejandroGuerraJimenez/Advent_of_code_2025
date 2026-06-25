package aoc.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

/**
 * Lee la entrada de un día desde el classpath y normaliza los finales de línea
 * (\r\n -> \n), de modo que los parsers no tengan que preocuparse del formato.
 */
public class InputReader {

    public String read(int day) {
        String resource = "/Input" + String.format("%02d", day) + ".txt";
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            return readNormalised(stream, resource);
        } catch (IOException e) {
            throw new UncheckedIOException("No se pudo leer la entrada del día " + day, e);
        }
    }

    private static String readNormalised(InputStream stream, String resource) throws IOException {
        if (stream == null) throw new IllegalArgumentException("No se encontró la entrada: " + resource);
        return new String(stream.readAllBytes(), StandardCharsets.UTF_8).replace("\r\n", "\n");
    }
}
