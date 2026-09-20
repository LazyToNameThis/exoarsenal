package com.scapeandrun.frostbite.expedition;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.common.BiomeDictionary;

public final class WildModule {
    public static final class Entry {
        public final WildSpecies species;
        public final Class<? extends EntityWildMob> type;

        Entry(WildSpecies s, Class<? extends EntityWildMob> type) {
            species = s;
            this.type = type;
        }
    }

    public static final Entry[] ENTRIES = {
        new Entry(WildSpecies.CAVE_BAT, EntityWildMob.CaveBat.class),
        new Entry(WildSpecies.GIANT_WORM, EntityWildMob.GiantWorm.class),
        new Entry(WildSpecies.MOTHER_SLIME, EntityWildMob.MotherSlime.class),
        new Entry(WildSpecies.BABY_SLIME, EntityWildMob.BabySlime.class),
        new Entry(WildSpecies.UNDEAD_MINER, EntityWildMob.UndeadMiner.class),
        new Entry(WildSpecies.TIM, EntityWildMob.Tim.class),
        new Entry(WildSpecies.CRAWDAD, EntityWildMob.Crawdad.class),
        new Entry(WildSpecies.GIANT_SHELLY, EntityWildMob.GiantShelly.class),
        new Entry(WildSpecies.SALAMANDER, EntityWildMob.Salamander.class),
        new Entry(WildSpecies.COCHINEAL_BEETLE, EntityWildMob.CochinealBeetle.class),
        new Entry(WildSpecies.CYAN_BEETLE, EntityWildMob.CyanBeetle.class),
        new Entry(WildSpecies.LAC_BEETLE, EntityWildMob.LacBeetle.class),
        new Entry(WildSpecies.ICE_BAT, EntityWildMob.IceBat.class),
        new Entry(WildSpecies.ICE_SLIME, EntityWildMob.IceSlime.class),
        new Entry(WildSpecies.SPIKED_ICE_SLIME, EntityWildMob.SpikedIceSlime.class),
        new Entry(WildSpecies.SPORE_SKELETON, EntityWildMob.SporeSkeleton.class),
        new Entry(WildSpecies.JUNGLE_BAT, EntityWildMob.JungleBat.class),
        new Entry(WildSpecies.JUNGLE_SLIME, EntityWildMob.JungleSlime.class),
        new Entry(WildSpecies.SPIKED_JUNGLE_SLIME, EntityWildMob.SpikedJungleSlime.class),
        new Entry(WildSpecies.HORNET, EntityWildMob.Hornet.class),
        new Entry(WildSpecies.SNATCHER, EntityWildMob.Snatcher.class),
        new Entry(WildSpecies.MAN_EATER, EntityWildMob.ManEater.class),
        new Entry(WildSpecies.PIRANHA, EntityWildMob.Piranha.class),
        new Entry(WildSpecies.EATER_OF_SOULS, EntityWildMob.EaterOfSouls.class),
        new Entry(WildSpecies.DEVOURER, EntityWildMob.Devourer.class),
        new Entry(WildSpecies.CRIMERA, EntityWildMob.Crimera.class),
        new Entry(WildSpecies.FACE_MONSTER, EntityWildMob.FaceMonster.class),
        new Entry(WildSpecies.BLOOD_CRAWLER, EntityWildMob.BloodCrawler.class),
        new Entry(WildSpecies.ANTLION, EntityWildMob.Antlion.class),
        new Entry(WildSpecies.ANTLION_CHARGER, EntityWildMob.AntlionCharger.class),
        new Entry(WildSpecies.ANTLION_SWARMER, EntityWildMob.AntlionSwarmer.class),
        new Entry(WildSpecies.TOMB_CRAWLER, EntityWildMob.TombCrawler.class)
    };

    public static void preInit(Frostbite mod) {
        WildContent.init();
        for (Entry entry : ENTRIES) {
            EntityRegistry.registerModEntity(
                    new ResourceLocation(Frostbite.MODID, entry.species.id),
                    entry.type,
                    entry.species.id,
                    60 + entry.species.ordinal(),
                    mod,
                    80,
                    2,
                    true,
                    entry.species.color,
                    0xE3CFAC);
            if (entry.species.shape == WildSpecies.Shape.FISH)
                EntitySpawnPlacementRegistry.setPlacementType(
                        entry.type, EntityLiving.SpawnPlacementType.IN_WATER);
        }
        EntityRegistry.registerModEntity(
                new ResourceLocation(Frostbite.MODID, "wild_bolt"),
                EntityWildBolt.class,
                "wild_bolt",
                92,
                mod,
                80,
                1,
                true);
        GameRegistry.registerWorldGenerator(new WildWorld(), 10890);
    }

    public static void postInit() {
        for (Biome biome : ForgeRegistries.BIOMES) {
            if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.NETHER)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.END)
                    || BiomeDictionary.hasType(biome, BiomeDictionary.Type.OCEAN)) continue;
            for (Entry entry : ENTRIES) {
                WildSpecies s = entry.species;
                if (s == WildSpecies.BABY_SLIME) continue;
                boolean match;
                switch (s.habitat) {
                    case JUNGLE:
                    case JUNGLE_CAVE:
                        match = WildBiomes.jungle(biome);
                        break;
                    case CORRUPTION:
                        match = biome == WildBiomes.CORRUPTION;
                        break;
                    case CRIMSON:
                        match = biome == WildBiomes.CRIMSON;
                        break;
                    case ICE:
                        match = BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD);
                        break;
                    case DESERT:
                        match = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY);
                        break;
                    default:
                        match = true;
                }
                if (match)
                    EntityRegistry.addSpawn(
                            entry.type,
                            s == WildSpecies.TIM
                                    ? 1
                                    : s.shape == WildSpecies.Shape.WORM
                                            ? 3
                                            : s.shape == WildSpecies.Shape.BAT ? 9 : 5,
                            1,
                            s.shape == WildSpecies.Shape.BAT ? 3 : 2,
                            EnumCreatureType.MONSTER,
                            biome);
            }
        }
    }
}
