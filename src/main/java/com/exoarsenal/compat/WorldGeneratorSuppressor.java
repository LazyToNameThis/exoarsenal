package com.exoarsenal.compat;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.config.ModConfig;
import net.minecraftforge.fml.common.IWorldGenerator;
import net.minecraftforge.fml.common.registry.GameRegistry;
import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class WorldGeneratorSuppressor {
    private static final String HBM_GENERATOR = "com.hbm.lib.HbmWorldGen";

    private WorldGeneratorSuppressor() {}

    @SuppressWarnings("unchecked")
    public static void apply() {
        if (!ModConfig.suppressHbmWorldGeneration) return;
        try {
            Field generatorsField = field("worldGenerators");
            Field indexField = field("worldGeneratorIndex");
            Field sortedField = field("sortedGeneratorList");
            Set<IWorldGenerator> generators = (Set<IWorldGenerator>) generatorsField.get(null);
            Map<IWorldGenerator, Integer> indices =
                    (Map<IWorldGenerator, Integer>) indexField.get(null);
            int removed = 0;
            Iterator<IWorldGenerator> iterator = generators.iterator();
            while (iterator.hasNext()) {
                IWorldGenerator generator = iterator.next();
                if (generator.getClass().getName().equals(HBM_GENERATOR)) {
                    iterator.remove();
                    indices.remove(generator);
                    removed++;
                }
            }
            sortedField.set(null, null);
            if (removed > 0)
                ExoArsenal.LOGGER.info(
                        "Suppressed {} HBM world generator instance(s) to prevent cascading generation and HBM ore generation",
                        removed);
            else
                ExoArsenal.LOGGER.info(
                        "HBM world generator was not registered; no suppression was necessary");
        } catch (ReflectiveOperationException | RuntimeException error) {
            ExoArsenal.LOGGER.error(
                    "Could not suppress HBM world generation; cascading chunk generation may occur",
                    error);
        }
    }

    private static Field field(String name) throws NoSuchFieldException {
        Field field = GameRegistry.class.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
