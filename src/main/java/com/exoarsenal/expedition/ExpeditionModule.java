package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.entity.*;
import net.minecraft.world.biome.Biome;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.*;
import net.minecraftforge.common.BiomeDictionary;
import java.util.*;

public final class ExpeditionModule {
    public static void preInit(ExoArsenal mod) {
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, "draedon"),
                com.exoarsenal.entity.EntityDraedon.class,
                "draedon",
                150,
                mod,
                160,
                2,
                true);
        AccessoryInventory.register();
        WulfrumArsenal.init();
        net.minecraftforge.fml.common.network.NetworkRegistry.INSTANCE.registerGuiHandler(
                mod, new AccessoryGuiHandler());
        SeaContent.init();
        PrebossContent.init();
        GeologyContent.init();
        WildModule.preInit(mod);
        DeepModule.preInit(mod);
        GameRegistry.registerTileEntity(
                LifeCrystalBlock.CrystalTile.class,
                new ResourceLocation(ExoArsenal.MODID, "life_crystal_formation"));
        GameRegistry.registerTileEntity(
                ShiverthornBlock.HerbTile.class,
                new ResourceLocation(ExoArsenal.MODID, "shiverthorn_plant"));
        register(mod, EntityWulfrum.Amplifier.class, "wulfrum_amplifier", 25);
        register(mod, EntityWulfrum.Drone.class, "wulfrum_drone", 26);
        register(mod, EntityWulfrum.Gyrator.class, "wulfrum_gyrator", 27);
        register(mod, EntityWulfrum.Hovercraft.class, "wulfrum_hovercraft", 28);
        register(mod, EntityWulfrum.Rover.class, "wulfrum_rover", 29);
        register(mod, EntityWulfrum.Mine.class, "wulfrum_mine", 30);
        register(mod, EntityWulfrum.Slime.class, "wulfrum_slime", 31);
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, "expedition_shot"),
                EntityExpeditionShot.class,
                "expedition_shot",
                32,
                mod,
                96,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, "expedition_minion"),
                EntityExpeditionMinion.class,
                "expedition_minion",
                33,
                mod,
                96,
                1,
                true);
        register(mod, EntityCnidrion.class, "cnidrion", 34);
        register(mod, EntitySeaCreature.Clam.class, "sunken_clam", 35);
        register(mod, EntitySeaCreature.Ray.class, "eutrophic_ray", 36);
        register(mod, EntitySeaCreature.GhostBell.class, "ghost_bell", 37);
        register(mod, EntitySeaCreature.PrismBack.class, "prism_back", 38);
        register(mod, EntitySeaCreature.SeaFloaty.class, "sea_floaty", 39);
        register(mod, EntitySeaCreature.SeaMinnow.class, "sea_minnow", 40);
        register(mod, EntitySeaCreature.BabyGhostBell.class, "baby_ghost_bell", 41);
        register(mod, EntitySeaCreature.GiantClam.class, "giant_clam", 42);
        register(mod, EntitySeaKing.class, "sea_king", 43);
        register(mod, EntityStormlion.class, "stormlion", 44);
        register(mod, EntitySnowFlinx.class, "snow_flinx", 45);
        register(mod, EntityCaveMob.GraniteGolem.class, "granite_golem", 47);
        register(mod, EntityCaveMob.GraniteElemental.class, "granite_elemental", 48);
        register(mod, EntityCaveMob.Hoplite.class, "hoplite", 49);
        register(mod, EntityCaveMob.SporeBat.class, "spore_bat", 50);
        register(mod, EntityCaveMob.WallCreeper.class, "wall_creeper", 51);
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, "preboss_shot"),
                EntityPrebossShot.class,
                "preboss_shot",
                46,
                mod,
                96,
                1,
                true);
        GameRegistry.registerWorldGenerator(new ExpeditionWorldGenerator(), 11000);
        GameRegistry.registerWorldGenerator(new GeologyWorld(), 10900);
    }

    private static void register(
            ExoArsenal mod, Class<? extends Entity> type, String name, int id) {
        EntityRegistry.registerModEntity(
                new ResourceLocation(ExoArsenal.MODID, name),
                type,
                name,
                id,
                mod,
                96,
                2,
                true,
                0x58684B,
                0xC4E86B);
        if (net.minecraft.entity.passive.EntityWaterMob.class.isAssignableFrom(type))
            EntitySpawnPlacementRegistry.setPlacementType(
                    type, EntityLiving.SpawnPlacementType.IN_WATER);
    }

    public static void postInit() {
        List<Biome> biomes = new ArrayList<>();
        for (Biome b : ForgeRegistries.BIOMES)
            if (!BiomeDictionary.hasType(b, BiomeDictionary.Type.NETHER)
                    && !BiomeDictionary.hasType(b, BiomeDictionary.Type.END)
                    && !BiomeDictionary.hasType(b, BiomeDictionary.Type.OCEAN)) biomes.add(b);
        Biome[] all = biomes.toArray(new Biome[0]);
        for (Biome b : all)
            if (BiomeDictionary.hasType(b, BiomeDictionary.Type.SANDY)
                    && b.getDefaultTemperature() > .8F) {
                EntityRegistry.addSpawn(
                        EntityStormlion.class, 5, 1, 2, EnumCreatureType.MONSTER, b);
                EntityRegistry.addSpawn(EntityCnidrion.class, 2, 1, 1, EnumCreatureType.MONSTER, b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.Clam.class, 8, 1, 2, EnumCreatureType.WATER_CREATURE, b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.Ray.class, 6, 1, 2, EnumCreatureType.WATER_CREATURE, b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.GhostBell.class,
                        6,
                        1,
                        2,
                        EnumCreatureType.WATER_CREATURE,
                        b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.PrismBack.class,
                        5,
                        1,
                        2,
                        EnumCreatureType.WATER_CREATURE,
                        b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.SeaFloaty.class,
                        5,
                        1,
                        2,
                        EnumCreatureType.WATER_CREATURE,
                        b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.SeaMinnow.class,
                        12,
                        2,
                        4,
                        EnumCreatureType.WATER_CREATURE,
                        b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.BabyGhostBell.class,
                        7,
                        2,
                        3,
                        EnumCreatureType.WATER_CREATURE,
                        b);
                EntityRegistry.addSpawn(
                        EntitySeaCreature.GiantClam.class,
                        2,
                        1,
                        1,
                        EnumCreatureType.WATER_CREATURE,
                        b);
            }
        WildModule.postInit();
        DeepModule.postInit();
        for (Biome b : all)
            if (com.exoarsenal.world.FrigidSpawnBiomes.isCold(b))
                EntityRegistry.addSpawn(
                        EntitySnowFlinx.class, 6, 1, 2, EnumCreatureType.MONSTER, b);
        EntityRegistry.addSpawn(
                EntityCaveMob.GraniteGolem.class, 8, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityCaveMob.GraniteElemental.class, 6, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityCaveMob.Hoplite.class, 8, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityCaveMob.SporeBat.class, 8, 1, 3, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityCaveMob.WallCreeper.class, 10, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityWulfrum.Amplifier.class, 3, 1, 1, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(EntityWulfrum.Drone.class, 5, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityWulfrum.Gyrator.class, 5, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(
                EntityWulfrum.Hovercraft.class, 4, 1, 1, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(EntityWulfrum.Rover.class, 5, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(EntityWulfrum.Slime.class, 4, 1, 2, EnumCreatureType.MONSTER, all);
        EntityRegistry.addSpawn(EntityWulfrum.Mine.class, 1, 1, 1, EnumCreatureType.MONSTER, all);
    }
}
