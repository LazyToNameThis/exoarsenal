package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class PrebossContent {
    public static final Item FALLEN_STAR = item("fallen_star", PrebossItem.Kind.STAR);
    public static final Item MANA_CRYSTAL = item("mana_crystal", PrebossItem.Kind.MANA_CRYSTAL);
    public static final Item LIFE_CRYSTAL = item("life_crystal", PrebossItem.Kind.LIFE_CRYSTAL);
    public static final Item MANA_POTION = item("lesser_mana_potion", PrebossItem.Kind.MANA_POTION);
    public static final Item SHIVERTHORN = item("shiverthorn", PrebossItem.Kind.MATERIAL);
    public static final Item MANDIBLE = item("stormlion_mandible", PrebossItem.Kind.MATERIAL);
    public static final Item FLINX_FUR = item("flinx_fur", PrebossItem.Kind.MATERIAL);
    public static final Item WOOD_BOOMERANG =
            item("wooden_boomerang", PrebossItem.Kind.WOOD_BOOMERANG);
    public static final Item ENCHANTED_BOOMERANG =
            item("enchanted_boomerang", PrebossItem.Kind.ENCHANTED_BOOMERANG);
    public static final Item WULFRUM_KNIFE = item("wulfrum_knife", PrebossItem.Kind.KNIFE);
    public static final Item CRYSTALLINE = item("crystalline", PrebossItem.Kind.CRYSTALLINE);
    public static final Item SPARKING = item("wand_of_sparking", PrebossItem.Kind.SPARKING);
    public static final Item FROSTING = item("wand_of_frosting", PrebossItem.Kind.FROSTING);
    public static final Item FROST_BOLT = item("frost_bolt", PrebossItem.Kind.FROST_BOLT);
    public static final Item STORM_SPEAR = item("storm_spear", PrebossItem.Kind.STORM_SPEAR);
    public static final Item THUNDER_ZAPPER =
            item("thunder_zapper", PrebossItem.Kind.THUNDER_ZAPPER);
    public static final Item STORMJAW = item("stormjaw_staff", PrebossItem.Kind.STORMJAW);
    public static final Item FLINX_STAFF = item("flinx_staff", PrebossItem.Kind.FLINX);
    public static final Item DIAMOND_STAFF = item("diamond_staff", PrebossItem.Kind.DIAMOND);
    public static final Item EMERALD_STAFF = item("emerald_staff", PrebossItem.Kind.EMERALD);
    public static final Item REGEN_BAND = item("band_of_regeneration", PrebossItem.Kind.REGEN_BAND);
    public static final Item STARPOWER_BAND =
            item("band_of_starpower", PrebossItem.Kind.STARPOWER_BAND);
    public static final Item FLINX_COAT =
            ExpeditionContent.register(new FlinxFurCoat(), "flinx_fur_coat");
    public static final Item HERMES_BOOTS =
            accessory("hermes_boots", ExplorationAccessory.Kind.HERMES);
    public static final Item AGLET = accessory("aglet", ExplorationAccessory.Kind.AGLET);
    public static final Item WIND_ANKLET =
            accessory("anklet_of_the_wind", ExplorationAccessory.Kind.ANKLET);
    public static final Item SHACKLE = accessory("shackle", ExplorationAccessory.Kind.SHACKLE);
    public static final Item OBSIDIAN_SKULL =
            accessory("obsidian_skull", ExplorationAccessory.Kind.SKULL);
    public static final Item LUCKY_HORSESHOE =
            accessory("lucky_horseshoe", ExplorationAccessory.Kind.HORSESHOE);
    public static final Item FERAL_CLAWS =
            accessory("feral_claws", ExplorationAccessory.Kind.CLAWS);
    public static final Item CLOUD_BOTTLE =
            accessory("cloud_in_a_bottle", ExplorationAccessory.Kind.CLOUD);
    public static final Item BLIZZARD_BOTTLE =
            accessory("blizzard_in_a_bottle", ExplorationAccessory.Kind.BLIZZARD);
    public static final Item SANDSTORM_BOTTLE =
            accessory("sandstorm_in_a_bottle", ExplorationAccessory.Kind.SANDSTORM);
    public static final Item MAGIC_MIRROR =
            ExpeditionContent.register(new RecallItem(false), "magic_mirror");
    public static final Item ICE_MIRROR =
            ExpeditionContent.register(new RecallItem(false), "ice_mirror");
    public static final Item RECALL_POTION =
            ExpeditionContent.register(new RecallItem(true), "recall_potion");
    public static final net.minecraft.block.Block LIFE_FORMATION =
            ExpeditionContent.register(new LifeCrystalBlock(), "life_crystal_formation");
    public static final net.minecraft.block.Block SHIVERTHORN_PLANT =
            ExpeditionContent.register(new ShiverthornBlock(), "shiverthorn_plant");

    public static void init() {}

    private static Item item(String id, PrebossItem.Kind kind) {
        return ExpeditionContent.register(new PrebossItem(kind), id);
    }

    private static Item accessory(String id, ExplorationAccessory.Kind kind) {
        return ExpeditionContent.register(new ExplorationAccessory(kind), id);
    }

    private static void recipe(
            RegistryEvent.Register<IRecipe> event, Item output, Object... input) {
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "preboss"),
                                        new ItemStack(output),
                                        input)
                                .setRegistryName(output.getRegistryName()));
    }

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<IRecipe> event) {
        recipe(
                event,
                MANA_CRYSTAL,
                FALLEN_STAR,
                FALLEN_STAR,
                FALLEN_STAR,
                FALLEN_STAR,
                FALLEN_STAR);
        recipe(event, ENCHANTED_BOOMERANG, WOOD_BOOMERANG, FALLEN_STAR);
        recipe(
                event,
                CRYSTALLINE,
                WULFRUM_KNIFE,
                Items.DIAMOND,
                Items.DIAMOND,
                Items.DIAMOND,
                FALLEN_STAR,
                FALLEN_STAR,
                FALLEN_STAR);
        event.getRegistry()
                .register(
                        new ShapedOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "preboss"),
                                        new ItemStack(WOOD_BOOMERANG),
                                        "WW ",
                                        "  W",
                                        'W',
                                        "plankWood")
                                .setRegistryName(WOOD_BOOMERANG.getRegistryName()));
        recipe(event, WULFRUM_KNIFE, ExpeditionContent.SCRAP, ExpeditionContent.SCRAP, Items.STICK);
        recipe(event, FROSTING, SPARKING, Blocks.ICE, Blocks.ICE, Blocks.ICE, Blocks.TORCH);
        recipe(
                event,
                FROST_BOLT,
                Items.BOOK,
                Blocks.ICE,
                Blocks.ICE,
                Blocks.SNOW,
                Blocks.SNOW,
                SHIVERTHORN,
                SHIVERTHORN,
                new ItemStack(Items.DYE, 1, 4));
        recipe(
                event,
                FLINX_STAFF,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                Items.GOLD_INGOT,
                Items.GOLD_INGOT,
                Items.STICK);
        recipe(
                event,
                DIAMOND_STAFF,
                Items.DIAMOND,
                Items.DIAMOND,
                Items.DIAMOND,
                Items.GOLD_INGOT,
                Items.GOLD_INGOT,
                Items.STICK,
                FALLEN_STAR);
        recipe(
                event,
                EMERALD_STAFF,
                Items.EMERALD,
                Items.EMERALD,
                Items.EMERALD,
                Items.IRON_INGOT,
                Items.IRON_INGOT,
                Items.STICK,
                FALLEN_STAR);
        recipe(event, MANA_POTION, Items.GLASS_BOTTLE, FALLEN_STAR, Blocks.BROWN_MUSHROOM);
        recipe(
                event,
                FLINX_COAT,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                FLINX_FUR,
                Items.LEATHER,
                Items.STRING,
                Items.GOLD_INGOT);
        recipe(
                event,
                OBSIDIAN_SKULL,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN,
                Blocks.OBSIDIAN);
        recipe(event, RECALL_POTION, Items.GLASS_BOTTLE, Items.FISH, Blocks.YELLOW_FLOWER);
    }
}
