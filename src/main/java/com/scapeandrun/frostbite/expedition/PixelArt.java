package com.scapeandrun.frostbite.expedition;

import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class PixelArt {
    private PixelArt() {}

    public static BufferedImage read(InputStream input) throws IOException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(input, StandardCharsets.US_ASCII));
        String line = reader.readLine();
        if (line == null) throw new IOException("Missing palette");
        String[] colors = line.trim().split(" +");
        if (colors.length != 16) throw new IOException("Expected sixteen palette colors");
        int[] palette = new int[16];
        for (int i = 0; i < 16; i++) {
            if (!colors[i].matches("[0-9a-fA-F]{6}"))
                throw new IOException("Invalid RGB palette entry " + i);
            palette[i] = 0xFF000000 | Integer.parseInt(colors[i], 16);
        }
        List<String> rows = new ArrayList<>();
        while ((line = reader.readLine()) != null)
            if (!line.trim().isEmpty()) rows.add(line.trim().replace(" ", ""));
        if (rows.isEmpty()) throw new IOException("No pixel rows");
        int width = rows.get(0).length();
        BufferedImage result = new BufferedImage(width, rows.size(), BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < rows.size(); y++) {
            if (rows.get(y).length() != width) throw new IOException("Unequal pixel row " + y);
            for (int x = 0; x < width; x++) {
                int index = Character.digit(rows.get(y).charAt(x), 16);
                if (index < 0) throw new IOException("Invalid pixel at " + x + "," + y);
                result.setRGB(x, y, palette[index]);
            }
        }
        return result;
    }
}
