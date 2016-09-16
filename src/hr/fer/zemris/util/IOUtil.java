package hr.fer.zemris.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * IO utility methods used throughout this project.
 * 
 * @author Filip Hrenić
 * @version 1.0
 */
public final class IOUtil {

    /**
     * Can't be created.
     */
    private IOUtil() {
    }

    /**
     * Reads lines from given reader and returns them in a list
     * 
     * @param reader reader from which you want to read
     * @return a list of read lines
     * @throws IOException
     */
    public static List<String> readFromReader(BufferedReader reader) throws IOException {
        List<String> lines = new ArrayList<>();
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null || line.isEmpty()) {
                break;
            }
            lines.add(line);
        }
        return lines;

    }
}
