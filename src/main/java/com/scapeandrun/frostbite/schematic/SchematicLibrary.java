package com.scapeandrun.frostbite.schematic;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class SchematicLibrary {
    public static final int MAX_BLOCKS = 3000;
    private static final long MAX_FILE_BYTES = 8L * 1024L * 1024L;

    private SchematicLibrary() {}

    public static final class Summary {
        public final String fileName;
        public final String displayName;
        public final String author;
        public final int blocks;
        public final int sizeX;
        public final int sizeY;
        public final int sizeZ;

        public Summary(
                String fileName,
                String displayName,
                String author,
                int blocks,
                int sizeX,
                int sizeY,
                int sizeZ) {
            this.fileName = fileName;
            this.displayName = displayName;
            this.author = author;
            this.blocks = blocks;
            this.sizeX = sizeX;
            this.sizeY = sizeY;
            this.sizeZ = sizeZ;
        }
    }

    public static final class Data {
        public final Summary summary;
        public final NBTTagList blocks;

        public Data(Summary summary, NBTTagList blocks) {
            this.summary = summary;
            this.blocks = blocks;
        }
    }

    public static File folder(MinecraftServer server) {
        File folder = server.getFile("config/" + Frostbite.MODID + "/schematics");
        File legacyFolder = server.getFile("config/" + Frostbite.PREVIOUS_MODID + "/schematics");
        if (!folder.isDirectory() && legacyFolder.isDirectory()) return legacyFolder;
        if (!folder.isDirectory() && !folder.mkdirs()) {
            Frostbite.LOGGER.warn("Could not create schematic library at {}", folder);
        }
        writeReadme(folder);
        return folder;
    }

    public static List<Summary> list(MinecraftServer server) {
        File folder = folder(server);
        File[] files =
                folder.listFiles(
                        file ->
                                file.isFile()
                                        && isSupported(file.getName())
                                        && file.length() <= MAX_FILE_BYTES);
        if (files == null || files.length == 0) return Collections.emptyList();
        List<Summary> result = new ArrayList<>();
        for (File file : files) {
            Data data = loadFile(file);
            if (data != null) result.add(data.summary);
        }
        result.sort(Comparator.comparing(summary -> summary.displayName.toLowerCase(Locale.ROOT)));
        return result;
    }

    @Nullable
    public static Data load(MinecraftServer server, String fileName) {
        if (fileName == null || fileName.length() > 128 || !isSupported(fileName)) return null;
        try {
            File folder = folder(server).getCanonicalFile();
            File selected = new File(folder, fileName).getCanonicalFile();
            if (!folder.equals(selected.getParentFile())
                    || !selected.isFile()
                    || selected.length() > MAX_FILE_BYTES) return null;
            return loadFile(selected);
        } catch (IOException error) {
            Frostbite.LOGGER.warn("Could not resolve schematic {}", fileName, error);
            return null;
        }
    }

    private static boolean isSupported(String name) {
        String lower = name.toLowerCase(Locale.ROOT);
        return lower.endsWith(".json") || lower.endsWith(".schematic");
    }

    @Nullable
    private static Data loadFile(File file) {
        try {
            return file.getName().toLowerCase(Locale.ROOT).endsWith(".json")
                    ? loadJson(file)
                    : loadClassic(file);
        } catch (Exception error) {
            Frostbite.LOGGER.warn(
                    "Ignoring invalid schematic {}: {}", file.getName(), error.getMessage());
            return null;
        }
    }

    private static Data loadJson(File file) throws IOException {
        JsonObject root;
        try (FileReader reader = new FileReader(file)) {
            root = new JsonParser().parse(reader).getAsJsonObject();
        }
        JsonArray source = root.getAsJsonArray("blocks");
        if (source == null) throw new IOException("missing blocks array");
        NBTTagList blocks = new NBTTagList();
        int maxX = -1, maxY = -1, maxZ = -1;
        for (JsonElement value : source) {
            JsonObject entry = value.getAsJsonObject();
            int x = integer(entry, "x", 0);
            int y = integer(entry, "y", 0);
            int z = integer(entry, "z", 0);
            if (x < 0
                    || y < 0
                    || z < 0
                    || x > Short.MAX_VALUE
                    || y > Short.MAX_VALUE
                    || z > Short.MAX_VALUE) {
                throw new IOException("block coordinate outside supported range");
            }
            String id = string(entry, "block", "minecraft:air");
            Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
            if (block == null || block == net.minecraft.init.Blocks.AIR) continue;
            int meta = integer(entry, "meta", 0) & 15;
            append(blocks, x, y, z, block, meta);
            if (blocks.tagCount() > MAX_BLOCKS)
                throw new IOException("more than " + MAX_BLOCKS + " non-air blocks");
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        int sx = integer(root, "width", maxX + 1);
        int sy = integer(root, "height", maxY + 1);
        int sz = integer(root, "length", maxZ + 1);
        String name = string(root, "name", prettyName(file.getName()));
        String author = string(root, "author", "Unknown");
        return data(file.getName(), name, author, blocks, sx, sy, sz);
    }

    private static Data loadClassic(File file) throws IOException {
        NBTTagCompound root;
        try (FileInputStream input = new FileInputStream(file)) {
            root = CompressedStreamTools.readCompressed(input);
        }
        int sx = root.getShort("Width") & 65535;
        int sy = root.getShort("Height") & 65535;
        int sz = root.getShort("Length") & 65535;
        long volume = (long) sx * sy * sz;
        byte[] ids = root.getByteArray("Blocks");
        byte[] metadata = root.getByteArray("Data");
        byte[] add = root.getByteArray("AddBlocks");
        if (sx <= 0
                || sy <= 0
                || sz <= 0
                || volume > Integer.MAX_VALUE
                || ids.length < volume
                || metadata.length < volume) {
            throw new IOException("invalid dimensions or block arrays");
        }
        NBTTagList blocks = new NBTTagList();
        for (int index = 0; index < (int) volume; index++) {
            int high = 0;
            if ((index >> 1) < add.length) {
                int packed = add[index >> 1] & 255;
                high = (index & 1) == 0 ? packed & 15 : packed >> 4 & 15;
            }
            Block block = Block.getBlockById((high << 8) | ids[index] & 255);
            if (block == null || block == net.minecraft.init.Blocks.AIR) continue;
            int x = index % sx;
            int z = index / sx % sz;
            int y = index / (sx * sz);
            append(blocks, x, y, z, block, metadata[index] & 15);
            if (blocks.tagCount() > MAX_BLOCKS)
                throw new IOException("more than " + MAX_BLOCKS + " non-air blocks");
        }
        String name = root.hasKey("Name", 8) ? root.getString("Name") : prettyName(file.getName());
        String author = root.hasKey("Author", 8) ? root.getString("Author") : "Unknown";
        return data(file.getName(), name, author, blocks, sx, sy, sz);
    }

    private static Data data(
            String file, String name, String author, NBTTagList blocks, int sx, int sy, int sz)
            throws IOException {
        if (file.length() > 128) throw new IOException("file name is too long");
        if (blocks.tagCount() == 0) throw new IOException("contains no placeable blocks");
        if (sx <= 0 || sy <= 0 || sz <= 0 || sx > 65535 || sy > 65535 || sz > 65535)
            throw new IOException("invalid dimensions");
        Summary summary =
                new Summary(file, trim(name, 48), trim(author, 32), blocks.tagCount(), sx, sy, sz);
        return new Data(summary, blocks);
    }

    private static void append(NBTTagList list, int x, int y, int z, Block block, int meta) {
        ResourceLocation id = block.getRegistryName();
        if (id == null) return;
        IBlockState state;
        try {
            state = block.getStateFromMeta(meta);
        } catch (RuntimeException ignored) {
            state = block.getDefaultState();
        }
        NBTTagCompound entry = new NBTTagCompound();
        entry.setShort("X", (short) x);
        entry.setShort("Y", (short) y);
        entry.setShort("Z", (short) z);
        entry.setString("Block", id.toString());
        entry.setByte("Meta", (byte) block.getMetaFromState(state));
        list.appendTag(entry);
    }

    private static int integer(JsonObject object, String key, int fallback) {
        return object.has(key) ? object.get(key).getAsInt() : fallback;
    }

    private static String string(JsonObject object, String key, String fallback) {
        return object.has(key) ? object.get(key).getAsString() : fallback;
    }

    private static String prettyName(String fileName) {
        String raw =
                fileName.replaceFirst("(?i)\\.(json|schematic)$", "")
                        .replace('_', ' ')
                        .replace('-', ' ')
                        .trim();
        StringBuilder result = new StringBuilder();
        boolean upper = true;
        for (char c : raw.toCharArray()) {
            result.append(upper ? Character.toUpperCase(c) : c);
            upper = Character.isWhitespace(c);
        }
        return result.length() == 0 ? "Unnamed Schematic" : result.toString();
    }

    private static String trim(String value, int max) {
        if (value == null || value.trim().isEmpty()) return "Unknown";
        String clean = value.trim().replace('\n', ' ').replace('\r', ' ');
        return clean.length() <= max ? clean : clean.substring(0, max);
    }

    private static void writeReadme(File folder) {
        if (!folder.isDirectory()) return;
        File readme = new File(folder, "README.txt");
        if (readme.isFile()) return;
        try (FileWriter writer = new FileWriter(readme)) {
            writer.write("Exo Arsenal - R-Tool Schematics\n\n");
            writer.write(
                    "Place classic .schematic files or Exo Arsenal .json files in this folder.\n");
            writer.write("Schematics may contain at most 3000 non-air blocks.\n\n");
            writer.write("JSON example:\n");
            writer.write(
                    "{\n  \"name\": \"Field Shelter\",\n  \"author\": \"Builder\",\n  \"width\": 2,\n  \"height\": 1,\n  \"length\": 1,\n");
            writer.write(
                    "  \"blocks\": [\n    {\"x\":0,\"y\":0,\"z\":0,\"block\":\"minecraft:stone\",\"meta\":0},\n");
            writer.write(
                    "    {\"x\":1,\"y\":0,\"z\":0,\"block\":\"minecraft:stone\",\"meta\":0}\n  ]\n}\n");
        } catch (IOException error) {
            Frostbite.LOGGER.warn("Could not write schematic library README", error);
        }
    }
}
