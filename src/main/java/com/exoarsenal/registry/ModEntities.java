package com.exoarsenal.registry;

import com.exoarsenal.ExoArsenal;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import static com.exoarsenal.ExoArsenal.MODID;

public final class ModEntities {
    private ModEntities() {}

    public static void register(ExoArsenal mod) {
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "king_slime"),
                com.exoarsenal.entity.EntityKingSlime.class,
                "king_slime",
                152,
                mod,
                128,
                1,
                true,
                0x378CE5,
                0xEABD4F);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "king_slime_support"),
                com.exoarsenal.entity.EntityKingSlimeSupport.class,
                "king_slime_support",
                153,
                mod,
                128,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "king_slime_shot"),
                com.exoarsenal.entity.EntityKingSlimeShot.class,
                "king_slime_shot",
                154,
                mod,
                128,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "personal_machine"),
                com.exoarsenal.entity.EntityPersonalMachine.class,
                "personal_machine",
                132,
                mod,
                96,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "x05_brawler"),
                com.exoarsenal.entity.EntityBrawler.class,
                "x05_brawler",
                133,
                mod,
                128,
                1,
                true,
                0x7E8970,
                0x70F2A0);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "brawler_effect"),
                com.exoarsenal.entity.EntityBrawlerEffect.class,
                "brawler_effect",
                134,
                mod,
                160,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "brawler_arm"),
                com.exoarsenal.entity.EntityBrawlerArm.class,
                "brawler_arm",
                151,
                mod,
                160,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "x04_excavator"),
                com.exoarsenal.entity.EntityExcavator.class,
                "x04_excavator",
                128,
                mod,
                192,
                1,
                true,
                0x61715A,
                0x52DD88);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "excavator_segment"),
                com.exoarsenal.entity.EntityExcavatorSegment.class,
                "excavator_segment",
                129,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "excavator_probe"),
                com.exoarsenal.entity.EntityExcavatorProbe.class,
                "excavator_probe",
                130,
                mod,
                192,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "excavator_payload"),
                com.exoarsenal.entity.EntityExcavatorPayload.class,
                "excavator_payload",
                131,
                mod,
                192,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "seer"),
                com.exoarsenal.entity.EntityWulfrumEye.Seer.class,
                "seer",
                120,
                mod,
                192,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "observer"),
                com.exoarsenal.entity.EntityWulfrumEye.Observer.class,
                "observer",
                121,
                mod,
                192,
                1,
                true,
                0x3A4237,
                0xA1FFD1);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_ray"),
                com.exoarsenal.entity.EntityWulfrumRay.class,
                "wulfrum_ray",
                122,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_shard"),
                com.exoarsenal.entity.EntityWulfrumShard.class,
                "wulfrum_shard",
                123,
                mod,
                192,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_echo"),
                com.exoarsenal.entity.EntityWulfrumEcho.class,
                "wulfrum_echo",
                124,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_cut"),
                com.exoarsenal.entity.EntityWulfrumCut.class,
                "wulfrum_cut",
                125,
                mod,
                192,
                2,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_appendage"),
                com.exoarsenal.entity.EntityWulfrumAppendage.class,
                "wulfrum_appendage",
                126,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "wulfrum_nova"),
                com.exoarsenal.entity.EntityWulfrumNova.class,
                "wulfrum_nova",
                127,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scourge_segment"),
                com.exoarsenal.entity.EntityScourgeSegment.class,
                "scourge_segment",
                24,
                mod,
                192,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "desert_scourge"),
                com.exoarsenal.entity.EntityDesertScourge.class,
                "desert_scourge",
                20,
                mod,
                192,
                1,
                true,
                0x78583E,
                0xDDCBA2);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "desert_nuisance"),
                com.exoarsenal.entity.EntityDesertScourge.Nuisance.class,
                "desert_nuisance",
                21,
                mod,
                192,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scourge_sand"),
                com.exoarsenal.entity.EntityScourgeSand.class,
                "scourge_sand",
                22,
                mod,
                128,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "desert_vulture"),
                com.exoarsenal.entity.EntityDesertVulture.class,
                "desert_vulture",
                23,
                mod,
                128,
                1,
                true,
                0x463A31,
                0xD5BD8B);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_frozen_cut"),
                com.exoarsenal.entity.EntityScoutCut.class,
                "scout_frozen_cut",
                17,
                mod,
                160,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_arena"),
                com.exoarsenal.entity.EntityScoutArena.class,
                "scout_arena",
                18,
                mod,
                192,
                3,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_core_ray"),
                com.exoarsenal.entity.EntityScoutCoreRay.class,
                "scout_core_ray",
                19,
                mod,
                128,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "frigid_drone"),
                com.exoarsenal.entity.EntityFrigidRobot.Drone.class,
                "frigid_drone",
                12,
                mod,
                96,
                1,
                true,
                0x273D4C,
                0x6BE7FF);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "frigid_amplifier"),
                com.exoarsenal.entity.EntityFrigidRobot.Amplifier.class,
                "frigid_amplifier",
                13,
                mod,
                96,
                1,
                true,
                0x354655,
                0xA49AFF);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "frigid_shielder"),
                com.exoarsenal.entity.EntityFrigidRobot.Shielder.class,
                "frigid_shielder",
                14,
                mod,
                96,
                1,
                true,
                0x182B35,
                0xBCDCE8);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "frigid_rover"),
                com.exoarsenal.entity.EntityFrigidRobot.Rover.class,
                "frigid_rover",
                15,
                mod,
                96,
                1,
                true,
                0x2E3F48,
                0x418AD2);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "x01_arthropod"),
                com.exoarsenal.entity.EntityFrigidRobot.Arthropod.class,
                "x01_arthropod",
                16,
                mod,
                128,
                1,
                true,
                0x192D3B,
                0xC0EDFF);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "rblade_disc"),
                com.exoarsenal.entity.EntityRBladeDisc.class,
                "rblade_disc",
                1,
                mod,
                96,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "rblade_slash"),
                com.exoarsenal.entity.EntityRBladeSlash.class,
                "rblade_slash",
                2,
                mod,
                96,
                1,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "x20_scout"),
                com.exoarsenal.entity.EntityX20Scout.class,
                "x20_scout",
                3,
                mod,
                128,
                1,
                true,
                0x263841,
                0x7FDCE8);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_bomb"),
                com.exoarsenal.entity.EntityScoutBomb.class,
                "scout_bomb",
                4,
                mod,
                96,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "aurora_field"),
                com.exoarsenal.entity.EntityAuroraField.class,
                "aurora_field",
                5,
                mod,
                128,
                2,
                false);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "aurora_shard"),
                com.exoarsenal.entity.EntityAuroraShard.class,
                "aurora_shard",
                6,
                mod,
                96,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "ice_fragment"),
                com.exoarsenal.entity.EntityIceFragment.class,
                "ice_fragment",
                7,
                mod,
                72,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_hardpoint"),
                com.exoarsenal.entity.EntityScoutHardpoint.class,
                "scout_hardpoint",
                9,
                mod,
                160,
                1,
                true);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "x20_pilot"),
                com.exoarsenal.entity.EntityX20Pilot.class,
                "x20_pilot",
                10,
                mod,
                128,
                1,
                true,
                0x12191F,
                0x5FE8EA);
        EntityRegistry.registerModEntity(
                new ResourceLocation(MODID, "scout_shard"),
                com.exoarsenal.entity.EntityScoutShard.class,
                "scout_shard",
                11,
                mod,
                128,
                1,
                true);
    }
}
