package com.scapeandrun.frostbite;

import com.scapeandrun.frostbite.config.ModConfig;
import com.scapeandrun.frostbite.world.FrozenWorldGenerator;
import com.scapeandrun.frostbite.world.FrozenWorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Mod(
        modid = Frostbite.MODID,
        name = Frostbite.NAME,
        version = Frostbite.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies =
                "required-after:forge@[14.23.5.2864,);"
                        + "required-after:geckolib3;"
                        + "required-after:mantle;required-after:tconstruct@[1.12.2-2.13.0.183,);"
                        + "required-after:mekanism;required-after:ceramics;required-after:rustic;")
public class Frostbite {
    public static final String MODID = "exoarsenal";
    public static final String PREVIOUS_MODID = "sr_frostbite";
    public static final String LEGACY_MODID = "wastelandexpansion";
    public static final String NAME = "Exo Arsenal";
    public static final String VERSION = "0.1.0";

    @Mod.Instance(MODID)
    public static Frostbite INSTANCE;

    public static Logger LOGGER;
    public static FrozenWorldType WORLD_TYPE;

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        event.registerServerCommand(
                new com.scapeandrun.frostbite.command.FrostbiteDifficultyCommand());
        event.registerServerCommand(new com.scapeandrun.frostbite.command.DraedonReplyCommand());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        GeckoLib.initialize();
        com.scapeandrun.frostbite.registry.LegacyRegistryMappings.registerDataFixes();
        File configFile = new File(event.getModConfigurationDirectory(), MODID + ".cfg");
        migrateLegacyConfig(configFile);
        ModConfig.load(configFile);
        com.scapeandrun.frostbite.network.ModNetwork.init();
        com.scapeandrun.frostbite.expedition.ExpeditionModule.preInit(this);
        com.scapeandrun.frostbite.registry.ModEntities.register(this);
        GameRegistry.registerTileEntity(
                com.scapeandrun.frostbite.tile.TileEntityPotteryKiln.class,
                new net.minecraft.util.ResourceLocation(MODID, "pottery_kiln"));
        WORLD_TYPE = new FrozenWorldType();
        GameRegistry.registerWorldGenerator(new FrozenWorldGenerator(), 10000);
    }

    private static void migrateLegacyConfig(File configFile) {
        File legacyConfig = new File(configFile.getParentFile(), PREVIOUS_MODID + ".cfg");
        if (!legacyConfig.isFile()) {
            legacyConfig = new File(configFile.getParentFile(), LEGACY_MODID + ".cfg");
        }
        if (configFile.isFile() || !legacyConfig.isFile()) return;
        try {
            Files.copy(legacyConfig.toPath(), configFile.toPath());
            LOGGER.info(
                    "Migrated legacy configuration {} to {}",
                    legacyConfig.getName(),
                    configFile.getName());
        } catch (IOException error) {
            LOGGER.warn(
                    "Could not migrate legacy configuration {}; using new defaults",
                    legacyConfig,
                    error);
        }
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.event.WorldEventHandler());
        MinecraftForge.EVENT_BUS.register(
                new com.scapeandrun.frostbite.event.ParasiteEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.event.SoilEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.event.RmorEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.event.X10Systems());
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.event.KXSystems());
        MinecraftForge.EVENT_BUS.register(
                new com.scapeandrun.frostbite.event.EnergyShieldHandler());
        MinecraftForge.EVENT_BUS.register(new com.scapeandrun.frostbite.compat.TConstructCompat());
        com.scapeandrun.frostbite.compat.MekanismCompat.registerEnergyCoreRecipe();
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.scapeandrun.frostbite.entity.EntityX20Scout.class,
                1,
                1,
                1,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                com.scapeandrun.frostbite.world.ModBiomes.FROZEN_HILLS);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        com.scapeandrun.frostbite.expedition.ExpeditionModule.postInit();
        com.scapeandrun.frostbite.expedition.GeologyContent.integrate();
        com.scapeandrun.frostbite.world.FrigidSpawnBiomes.register();
        com.scapeandrun.frostbite.compat.EarlyProgressionCompat.registerKilnMetals();
        com.scapeandrun.frostbite.compat.WorldGeneratorSuppressor.apply();
        com.scapeandrun.frostbite.compat.MetallurgyRework.apply();
    }
}
