package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraftforge.fml.common.registry.*;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.BiomeDictionary;

public final class DeepModule {
    public static final WildModule.Entry[] ENTRIES = {
        new WildModule.Entry(WildSpecies.ANGRY_BONES, EntityDeepMob.AngryBones.class),
        new WildModule.Entry(WildSpecies.DARK_CASTER, EntityDeepMob.DarkCaster.class),
        new WildModule.Entry(WildSpecies.CURSED_SKULL, EntityDeepMob.CursedSkull.class),
        new WildModule.Entry(WildSpecies.DUNGEON_SLIME, EntityDeepMob.DungeonSlime.class),
        new WildModule.Entry(WildSpecies.HELLBAT, EntityDeepMob.Hellbat.class),
        new WildModule.Entry(WildSpecies.LAVA_SLIME, EntityDeepMob.LavaSlime.class),
        new WildModule.Entry(WildSpecies.FIRE_IMP, EntityDeepMob.FireImp.class),
        new WildModule.Entry(WildSpecies.DEMON, EntityDeepMob.Demon.class),
        new WildModule.Entry(WildSpecies.BONE_SERPENT, EntityDeepMob.BoneSerpent.class)
    };

    public static void preInit(ExoArsenal mod) {
        DeepContent.init();
        for (WildModule.Entry entry : ENTRIES)
            EntityRegistry.registerModEntity(
                    new ResourceLocation(ExoArsenal.MODID, entry.species.id),
                    entry.type,
                    entry.species.id,
                    100 + entry.species.ordinal() - 32,
                    mod,
                    96,
                    2,
                    true,
                    entry.species.color,
                    0x382A40);
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, "relic_shot"),
                EntityRelicShot.class,
                "relic_shot",
                109,
                mod,
                96,
                1,
                true);
        GameRegistry.registerWorldGenerator(new DeepWorld(), 11100);
    }

    public static void postInit() {
        DeepContent.integrate();
        for (Biome biome : ForgeRegistries.BIOMES)
            for (WildModule.Entry entry : ENTRIES) {
                boolean nether = BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER);
                if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)) continue;
                if (nether != (entry.species.habitat == WildSpecies.Habitat.UNDERWORLD)) continue;
                EntityRegistry.addSpawn(
                        entry.type,
                        entry.species == WildSpecies.BONE_SERPENT ? 2 : 6,
                        1,
                        2,
                        EnumCreatureType.MONSTER,
                        biome);
            }
    }
}
