package com.exoarsenal;

import com.exoarsenal.config.ModConfig;
import com.exoarsenal.world.FrozenWorldGenerator;
import com.exoarsenal.world.FrozenWorldType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import org.apache.logging.log4j.Logger;
import software.bernie.geckolib3.GeckoLib;

import java.io.File;

@Mod(
        modid = ExoArsenal.MODID,
        name = ExoArsenal.NAME,
        version = ExoArsenal.VERSION,
        acceptedMinecraftVersions = "[1.12.2]",
        dependencies =
                "required-after:forge@[14.23.5.2864,);"
                        + "required-after:geckolib3;"
                        + "required-after:mantle;required-after:tconstruct@[1.12.2-2.13.0.183,);"
                        + "required-after:mekanism;required-after:ceramics;required-after:rustic;")
public class ExoArsenal {
    public static final String MODID = "exoarsenal";
    public static final String NAME = "Calamity: Exo Arsenal";
    public static final String VERSION = "0.1.0";

    @Mod.Instance(MODID)
    public static ExoArsenal INSTANCE;

    public static Logger LOGGER;
    public static FrozenWorldType WORLD_TYPE;

    @Mod.EventHandler
    public void serverStarting(net.minecraftforge.fml.common.event.FMLServerStartingEvent event) {
        event.registerServerCommand(new com.exoarsenal.command.ExoArsenalDifficultyCommand());
        event.registerServerCommand(new com.exoarsenal.command.DraedonReplyCommand());
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        LOGGER = event.getModLog();
        GeckoLib.initialize();
        File configFile = new File(event.getModConfigurationDirectory(), MODID + ".cfg");
        ModConfig.load(configFile);
        com.exoarsenal.network.ModNetwork.init();
        com.exoarsenal.expedition.ExpeditionModule.preInit(this);
        com.exoarsenal.registry.ModEntities.register(this);
        GameRegistry.registerTileEntity(
                com.exoarsenal.tile.TileEntityPotteryKiln.class,
                new net.minecraft.util.ResourceLocation(MODID, "pottery_kiln"));
        WORLD_TYPE = new FrozenWorldType();
        GameRegistry.registerWorldGenerator(new FrozenWorldGenerator(), 10000);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.WorldEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.SoilEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.RmorEventHandler());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.X10Systems());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.KXSystems());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.event.EnergyShieldHandler());
        MinecraftForge.EVENT_BUS.register(new com.exoarsenal.compat.TConstructCompat());
        com.exoarsenal.compat.MekanismCompat.registerEnergyCoreRecipe();
        net.minecraftforge.fml.common.registry.EntityRegistry.addSpawn(
                com.exoarsenal.entity.EntityX20Scout.class,
                1,
                1,
                1,
                net.minecraft.entity.EnumCreatureType.MONSTER,
                com.exoarsenal.world.ModBiomes.FROZEN_HILLS);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        com.exoarsenal.expedition.ExpeditionModule.postInit();
        com.exoarsenal.expedition.GeologyContent.integrate();
        com.exoarsenal.world.FrigidSpawnBiomes.register();
        com.exoarsenal.compat.EarlyProgressionCompat.registerKilnMetals();
        com.exoarsenal.compat.WorldGeneratorSuppressor.apply();
        com.exoarsenal.compat.MetallurgyRework.apply();
    }
}
