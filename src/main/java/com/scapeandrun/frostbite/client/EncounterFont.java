package com.scapeandrun.frostbite.client;

import com.google.gson.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public final class EncounterFont {
    public static final float UNIT = .16F;
    public static final Map<Character, Glyph> GLYPHS = load();

    public static final class Glyph {
        public final int x, y, width, height, cropX, cropY;
        public final float left, body, right;

        Glyph(JsonObject json) {
            JsonObject bounds = json.getAsJsonObject("bounds"),
                    crop = json.getAsJsonObject("cropping"),
                    kern = json.getAsJsonObject("kerning");
            x = bounds.get("x").getAsInt();
            y = bounds.get("y").getAsInt();
            width = bounds.get("width").getAsInt();
            height = bounds.get("height").getAsInt();
            cropX = crop.get("x").getAsInt();
            cropY = crop.get("y").getAsInt();
            left = kern.get("x").getAsFloat();
            body = kern.get("y").getAsFloat();
            right = kern.get("z").getAsFloat();
        }

        public float advance(char c) {
            return (left + body + right + 10 + (Character.toLowerCase(c) == 'i' ? 9 : 0)) * UNIT;
        }
    }

    private static Map<Character, Glyph> load() {
        Map<Character, Glyph> result = new HashMap<>();
        try (InputStream stream =
                EncounterFont.class.getResourceAsStream("/assets/exoarsenal/font/infernum.json")) {
            if (stream == null) throw new IOException("Missing Infernum glyph metrics");
            JsonObject root =
                    new JsonParser()
                            .parse(new InputStreamReader(stream, StandardCharsets.UTF_8))
                            .getAsJsonObject();
            for (JsonElement element : root.getAsJsonArray("glyphs")) {
                JsonObject glyph = element.getAsJsonObject();
                result.put(glyph.get("character").getAsString().charAt(0), new Glyph(glyph));
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot load encounter font", exception);
        }
        return Collections.unmodifiableMap(result);
    }

    public static Glyph glyph(char c) {
        Glyph glyph = GLYPHS.get(c);
        return glyph == null ? GLYPHS.get('*') : glyph;
    }

    public static float measure(String text) {
        float width = 0;
        for (int i = 0; i < text.length(); i++)
            width += glyph(text.charAt(i)).advance(text.charAt(i));
        return Math.max(0, width - 10 * UNIT);
    }

    private EncounterFont() {}
}
