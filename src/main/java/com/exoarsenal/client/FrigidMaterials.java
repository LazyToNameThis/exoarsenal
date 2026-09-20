package com.exoarsenal.client;

import java.awt.image.BufferedImage;
import java.util.List;

public final class FrigidMaterials {
    public static final String[] TILES = {"plate", "trim", "joint", "optic", "core", "edge"};

    private FrigidMaterials() {}

    public static BufferedImage compose(BufferedImage atlas, List<BufferedImage> tiles) {
        if (atlas == null
                || atlas.getWidth() % 160 != 0
                || atlas.getWidth() < 160
                || atlas.getHeight() != atlas.getWidth() / 10
                || tiles.size() != 6)
            throw new IllegalArgumentException("Invalid Frigid atlas dimensions");
        int scale = atlas.getWidth() / 160;
        for (int slot = 0; slot < tiles.size(); slot++) {
            BufferedImage tile = tiles.get(slot);
            if (tile.getWidth() != 16 || tile.getHeight() != 16)
                throw new IllegalArgumentException("Frigid tile must be 16x16: " + TILES[slot]);
            for (int y = 0; y < 16 * scale; y++)
                for (int x = 0; x < 16 * scale; x++)
                    atlas.setRGB(slot * 16 * scale + x, y, tile.getRGB(x / scale, y / scale));
        }
        return atlas;
    }
}
