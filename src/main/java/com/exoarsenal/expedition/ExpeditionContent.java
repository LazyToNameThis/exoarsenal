package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.registry.ModContent;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.init.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ExpeditionContent {
    public static final List<Item> ITEMS = new ArrayList<>();
    public static final List<Block> BLOCKS = new ArrayList<>();
    public static final Block NAVYSTONE = block("navystone", Material.ROCK, 2.2F, 8, 0);
    public static final Block EUTROPHIC_SAND = block("eutrophic_sand", Material.SAND, .6F, 1, 0);
    public static final Block SEA_PRISM = block("sea_prism", Material.GLASS, 2, 5, .8F);
    public static final Block PRISM_CLUSTER = register(new PrismClusterBlock(), "prism_cluster");
    public static final Block LAB_PLATING = block("lab_plating", Material.IRON, 4, 30, 0);
    public static final Block LAB_PANEL = block("lab_panel", Material.IRON, 3, 20, .85F);
    public static final Block CODEBREAKER_BASE =
            register(new CodebreakerBaseBlock(), "codebreaker_base");
    public static final Block TROPHY =
            register(new ScourgeDisplayBlock(false), "desert_scourge_trophy");
    public static final Block RELIC =
            register(new ScourgeDisplayBlock(true), "desert_scourge_relic");
    public static final Item PEARL_SHARD = item("pearl_shard", ExpeditionItem.Kind.MATERIAL);
    public static final Item CORAL = item("coral", ExpeditionItem.Kind.MATERIAL);
    public static final Item SEASHELL = item("seashell", ExpeditionItem.Kind.MATERIAL);
    public static final Item STARFISH = item("starfish", ExpeditionItem.Kind.MATERIAL);
    public static final Item PRISM_SHARD = item("prism_shard", ExpeditionItem.Kind.MATERIAL);
    public static final Item SAND_CLOAK = item("sand_cloak", ExpeditionItem.Kind.CLOAK);
    public static final Item OCEAN_CREST = item("ocean_crest", ExpeditionItem.Kind.CREST);
    public static final Item SAHARA_SLICERS = item("sahara_slicers", ExpeditionItem.Kind.SLICERS);
    public static final Item BARINADE = item("barinade", ExpeditionItem.Kind.BOW);
    public static final Item SANDSTREAM_SCEPTER =
            item("sandstream_scepter", ExpeditionItem.Kind.SAND);
    public static final Item BRITTLE_STAR_STAFF =
            item("brittle_star_staff", ExpeditionItem.Kind.STAR);
    public static final Item SCOURGE_OF_DESERT =
            item("scourge_of_the_desert", ExpeditionItem.Kind.SPEAR);
    public static final Item HEALING_POTION =
            item("lesser_healing_potion", ExpeditionItem.Kind.HEAL);
    public static final Item LORE = item("desert_scourge_lore", ExpeditionItem.Kind.LORE);
    public static final Item THANK_YOU = item("thank_you", ExpeditionItem.Kind.THANKS);
    public static final Item SCRAP = item("wulfrum_metal_scrap", ExpeditionItem.Kind.MATERIAL);
    public static final Item CORE = item("wulfrum_energy_core", ExpeditionItem.Kind.MATERIAL);
    public static final Item CIRCUITRY = item("mysterious_circuitry", ExpeditionItem.Kind.MATERIAL);
    public static final Item DUBIOUS_PLATING =
            item("dubious_plating", ExpeditionItem.Kind.MATERIAL);
    public static final Item POWER_CELL = item("draedon_power_cell", ExpeditionItem.Kind.CELL);
    public static final Item SEA_SCHEMATIC =
            item("sunken_sea_schematic", ExpeditionItem.Kind.SCHEMATIC);
    public static final Item SPACE_SCHEMATIC =
            item("planetoid_schematic", ExpeditionItem.Kind.SCHEMATIC);
    public static final Item SEA_LOG = item("draedon_sunken_sea_log", ExpeditionItem.Kind.LOG);
    public static final Item SPACE_LOG = item("draedon_planetoid_log", ExpeditionItem.Kind.LOG);
    public static final Item SEEKER = item("lab_seeking_mechanism", ExpeditionItem.Kind.SEEKER);
    public static final Item SCREWDRIVER =
            item("wulfrum_screwdriver", ExpeditionItem.Kind.SCREWDRIVER);
    public static final Item BLUNDERBUSS =
            item("wulfrum_blunderbuss", ExpeditionItem.Kind.BLUNDERBUSS);
    public static final Item PROSTHESIS =
            item("wulfrum_prosthesis", ExpeditionItem.Kind.PROSTHESIS);
    public static final Item CONTROLLER =
            item("wulfrum_controller", ExpeditionItem.Kind.CONTROLLER);
    public static final Item DRILL = register(new WulfrumDrill(), "wulfrum_drill");
    public static final Item BATTERY = item("wulfrum_battery", ExpeditionItem.Kind.BATTERY);
    public static final Item ROVER_DRIVE = item("rover_drive", ExpeditionItem.Kind.DRIVE);
    public static final Item ACROBATICS = item("wulfrum_acrobatics_pack", ExpeditionItem.Kind.PACK);
    public static final Item SCAFFOLD = item("wulfrum_scaffold_kit", ExpeditionItem.Kind.SCAFFOLD);
    public static final Item LURE = item("wulfrum_lure", ExpeditionItem.Kind.LURE);
    public static final Item HAT =
            register(new WulfrumArmor(EntityEquipmentSlot.HEAD), "wulfrum_hat");
    public static final Item JACKET =
            register(new WulfrumArmor(EntityEquipmentSlot.CHEST), "wulfrum_jacket");
    public static final Item OVERALLS =
            register(new WulfrumArmor(EntityEquipmentSlot.LEGS), "wulfrum_overalls");
    public static final Item MASK = register(new ScourgeMask(), "desert_scourge_mask");

    private static Item item(String name, ExpeditionItem.Kind kind) {
        return register(new ExpeditionItem(kind), name);
    }

    static Item register(Item item, String name) {
        item.setRegistryName(ExoArsenal.MODID, name)
                .setUnlocalizedName(ExoArsenal.MODID + "." + name)
                .setCreativeTab(ModContent.TAB);
        ITEMS.add(item);
        return item;
    }

    static Block register(Block block, String name) {
        block.setRegistryName(ExoArsenal.MODID, name)
                .setUnlocalizedName(ExoArsenal.MODID + "." + name)
                .setCreativeTab(ModContent.TAB);
        BLOCKS.add(block);
        return block;
    }

    private static Block block(
            String name, Material material, float hardness, float resistance, float light) {
        Block b =
                new Block(material)
                        .setHardness(hardness)
                        .setResistance(resistance)
                        .setLightLevel(light);
        b.setHarvestLevel(material == Material.SAND ? "shovel" : "pickaxe", 0);
        return register(b, name);
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.Register<Block> e) {
        for (Block b : BLOCKS) e.getRegistry().register(b);
    }

    @SubscribeEvent
    public static void items(RegistryEvent.Register<Item> e) {
        for (Item i : ITEMS) e.getRegistry().register(i);
        for (Block b : BLOCKS)
            e.getRegistry()
                    .register(
                            (b == GeologyContent.ORE
                                            ? new GeologyContent.OreItem(b)
                                            : new ItemBlock(b))
                                    .setRegistryName(b.getRegistryName()));
    }

    private static void recipe(RegistryEvent.Register<IRecipe> e, Item out, Object... pattern) {
        e.getRegistry()
                .register(
                        new ShapedOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "expedition"),
                                        new ItemStack(out),
                                        pattern)
                                .setRegistryName(out.getRegistryName()));
    }

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<IRecipe> e) {
        recipe(
                e,
                Item.getItemFromBlock(CODEBREAKER_BASE),
                "PCP",
                "WEW",
                "WWW",
                'P',
                DUBIOUS_PLATING,
                'C',
                CIRCUITRY,
                'W',
                SCRAP,
                'E',
                CORE);
        recipe(
                e,
                WulfrumArsenal.TALONS,
                "EEE",
                "ECE",
                " S ",
                'E',
                WulfrumArsenal.SCRAP,
                'C',
                CORE,
                'S',
                SCRAP);
        recipe(
                e,
                WulfrumArsenal.STRIKER,
                " E ",
                "ECE",
                " S ",
                'E',
                WulfrumArsenal.SCRAP,
                'C',
                CORE,
                'S',
                SCRAP);
        e.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "expedition"),
                                        new ItemStack(SEA_PRISM),
                                        PRISM_SHARD,
                                        PRISM_SHARD,
                                        PRISM_SHARD,
                                        PRISM_SHARD,
                                        PRISM_SHARD)
                                .setRegistryName(ExoArsenal.MODID, "sea_prism"));
        recipe(e, SCREWDRIVER, " S ", " S ", " W ", 'S', SCRAP, 'W', Items.STICK);
        recipe(e, BLUNDERBUSS, "SSS", " CW", "  W", 'S', SCRAP, 'C', CORE, 'W', Items.STICK);
        recipe(e, PROSTHESIS, " SS", " CS", " SS", 'S', SCRAP, 'C', CORE);
        recipe(e, CONTROLLER, " S ", "SCS", " S ", 'S', SCRAP, 'C', CORE);
        recipe(e, DRILL, " SS", " CS", "W  ", 'S', SCRAP, 'C', CORE, 'W', Items.STICK);
        recipe(e, HAT, "SSS", "SCS", 'S', SCRAP, 'C', CORE);
        recipe(e, JACKET, "S S", "SCS", "SSS", 'S', SCRAP, 'C', CORE);
        recipe(e, OVERALLS, "SCS", "S S", "S S", 'S', SCRAP, 'C', CORE);
        recipe(e, ACROBATICS, "S S", "SCS", "L L", 'S', SCRAP, 'C', CORE, 'L', Items.LEATHER);
        recipe(e, SCAFFOLD, "SSS", "W W", "W W", 'S', SCRAP, 'W', Items.STICK);
        recipe(e, LURE, " S ", "SCS", "SSS", 'S', SCRAP, 'C', CORE);
        recipe(e, SEEKER, " R ", "SCS", " S ", 'R', Items.REDSTONE, 'S', SCRAP, 'C', CORE);
    }
}
