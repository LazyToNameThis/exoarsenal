package com.exoarsenal.registry;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.block.BlockClimateStabilizer;
import com.exoarsenal.block.BlockFrozenLog;
import com.exoarsenal.block.BlockFrozenStone;
import com.exoarsenal.block.BlockPermafrost;
import com.exoarsenal.block.BlockPotteryKiln;
import com.exoarsenal.block.BlockRecoveryFarmland;
import com.exoarsenal.block.BlockRecoverySoil;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.init.SoundEvents;
import net.minecraftforge.common.util.EnumHelper;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ModContent {
    public static final ItemArmor.ArmorMaterial CANVAS_ARMOR =
            EnumHelper.addArmorMaterial(
                    "EXOARSENAL_CANVAS",
                    ExoArsenal.MODID + ":canvas",
                    9,
                    new int[] {1, 2, 3, 1},
                    8,
                    SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
                    0.0F);
    public static final ItemArmor.ArmorMaterial RMOR_ARMOR =
            EnumHelper.addArmorMaterial(
                    "EXOARSENAL_RMOR",
                    ExoArsenal.MODID + ":rmor",
                    48,
                    new int[] {3, 6, 8, 3},
                    12,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    1.0F);
    public static final ItemArmor.ArmorMaterial X10_ARMOR =
            EnumHelper.addArmorMaterial(
                    "EXOARSENAL_X10",
                    ExoArsenal.MODID + ":x10",
                    72,
                    new int[] {4, 7, 9, 4},
                    16,
                    SoundEvents.ITEM_ARMOR_EQUIP_IRON,
                    2.0F);
    public static final ItemArmor.ArmorMaterial KX20_ARMOR =
            EnumHelper.addArmorMaterial(
                    "EXOARSENAL_KX20",
                    ExoArsenal.MODID + ":kx20",
                    108,
                    new int[] {5, 8, 10, 5},
                    20,
                    SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
                    4.0F);
    public static final CreativeTabs TAB =
            new CreativeTabs(ExoArsenal.MODID) {
                @Override
                public ItemStack getTabIconItem() {
                    return new ItemStack(FROZEN_STONE);
                }
            };

    public static final Block FROZEN_STONE = named(new BlockFrozenStone(), "frozen_stone");
    public static final Block PERMAFROST = named(new BlockPermafrost(), "permafrost");
    public static final Block FROSTBITTEN_SOIL = named(new BlockRecoverySoil(), "frostbitten_soil");
    public static final Block RECOVERY_FARMLAND =
            named(new BlockRecoveryFarmland(), "recovery_farmland");
    public static final Block FROZEN_PLANKS =
            simpleBlock("frozen_planks", Material.WOOD, 1.8F, 4F, SoundType.WOOD);
    public static final Block FROZEN_LOG = named(new BlockFrozenLog(), "frozen_log");
    public static final Block CLIMATE_STABILIZER =
            named(new BlockClimateStabilizer(), "climate_stabilizer");
    public static final Block POTTERY_KILN = named(new BlockPotteryKiln(), "pottery_kiln");

    public static final Item DRYGRASS = named(new Item(), "drygrass").setCreativeTab(TAB);
    public static final Item FLINT_SHARD = named(new Item(), "flint_shard").setCreativeTab(TAB);
    public static final Item CRUDE_CORDAGE = named(new Item(), "crude_cordage").setCreativeTab(TAB);
    public static final Item TINDER = named(new Item(), "tinder").setCreativeTab(TAB);
    public static final Item CANVAS = named(new Item(), "canvas").setCreativeTab(TAB);
    public static final Item HEWN_STICKS = named(new Item(), "hewn_sticks").setCreativeTab(TAB);
    public static final Item FLINT_KNIFE =
            named(new com.exoarsenal.item.ItemFlintKnife(), "flint_knife").setCreativeTab(TAB);
    public static final Item FIRECLAY_BALL = named(new Item(), "fireclay_ball").setCreativeTab(TAB);
    public static final Item UNFIRED_CLAY_CAST =
            named(new com.exoarsenal.item.ItemUnfiredCast(), "unfired_clay_cast")
                    .setCreativeTab(TAB);
    public static final Item CLAY_CAST =
            named(new com.exoarsenal.item.ItemMoldedCast(false), "clay_cast").setCreativeTab(TAB);
    public static final Item UNFIRED_FIRECLAY_CAST =
            named(new com.exoarsenal.item.ItemUnfiredCast(), "unfired_fireclay_cast")
                    .setCreativeTab(TAB);
    public static final Item FIRECLAY_CAST =
            named(new com.exoarsenal.item.ItemMoldedCast(true), "fireclay_cast")
                    .setCreativeTab(TAB);
    public static final Item PROTOTYPE_ENERGY_CORE =
            named(new Item().setMaxStackSize(16), "prototype_energy_core").setCreativeTab(TAB);
    public static final Item CLAY_RIVER_PAN =
            named(new com.exoarsenal.item.ItemClayRiverPan(), "clay_river_pan").setCreativeTab(TAB);
    public static final Item SMALL_FROZEN_COPPER =
            named(new Item(), "small_frozen_copper").setCreativeTab(TAB);
    public static final Item SMALL_FROZEN_TIN =
            named(new Item(), "small_frozen_tin").setCreativeTab(TAB);
    public static final Item FRIGID_METAL = named(new Item(), "frigid_metal").setCreativeTab(TAB);
    public static final Item DESERT_MEDALLION =
            tabbed(new com.exoarsenal.item.ItemDesertMedallion(), "desert_medallion");
    public static final Item DESERT_SCOURGE_BAG =
            tabbed(new com.exoarsenal.item.ItemDesertScourgeBag(), "desert_scourge_bag");
    public static final Item SUSPICIOUS_CONTROLLER =
            tabbed(new com.exoarsenal.item.ItemSuspiciousController(), "suspicious_controller");
    public static final Item SCOUT_ENERGY_CORE =
            tabbed(new com.exoarsenal.item.ItemScoutEnergyCore(), "scouts_energy_core");
    public static final Item SCOUT_TREASURE_BAG =
            tabbed(new com.exoarsenal.item.ItemScoutTreasureBag(), "scout_treasure_bag");
    public static final Item FRIGID_ALLOY = named(new Item(), "frigid_alloy").setCreativeTab(TAB);
    public static final Item MACHINED_CORE =
            named(new Item().setMaxStackSize(16), "machined_core").setCreativeTab(TAB);
    public static final Item KX_GEAR =
            named(new Item().setMaxStackSize(16), "kx_gear").setCreativeTab(TAB);
    public static final com.exoarsenal.item.ItemScoutWeapon SCOUT_PINCER =
            tabbed(
                    new com.exoarsenal.item.ItemScoutWeapon(
                            com.exoarsenal.item.ItemScoutWeapon.Type.PINCER),
                    "scouts_pincer");
    public static final com.exoarsenal.item.ItemScoutWeapon MODIFIED_RAILGUN =
            tabbed(
                    new com.exoarsenal.item.ItemScoutWeapon(
                            com.exoarsenal.item.ItemScoutWeapon.Type.RAILGUN),
                    "modified_railgun");
    public static final com.exoarsenal.item.ItemScoutWeapon GLACIER_SMASHER =
            tabbed(
                    new com.exoarsenal.item.ItemScoutWeapon(
                            com.exoarsenal.item.ItemScoutWeapon.Type.SMASHER),
                    "glacier_smasher");
    public static final com.exoarsenal.item.ItemScoutWeapon AURORA_BOREALIS =
            tabbed(
                    new com.exoarsenal.item.ItemScoutWeapon(
                            com.exoarsenal.item.ItemScoutWeapon.Type.AURORA),
                    "aurora_borealis");
    public static final com.exoarsenal.item.ItemRBlade R_BLADE =
            tabbed(new com.exoarsenal.item.ItemRBlade(), "prototype_r_blade");
    public static final com.exoarsenal.item.ItemRTool R_TOOL =
            tabbed(new com.exoarsenal.item.ItemRTool(), "prototype_r_tool");
    public static final com.exoarsenal.item.ItemX10Blade X10_BLADE =
            tabbed(new com.exoarsenal.item.ItemX10Blade(), "x10_energy_blade");
    public static final com.exoarsenal.item.ItemX10Multitool X10_MULTITOOL =
            tabbed(new com.exoarsenal.item.ItemX10Multitool(), "x10_multitool");
    public static final com.exoarsenal.item.ItemEnergyGun PROTOTYPE_ENERGY_SHOTGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.PROTOTYPE_SHOTGUN),
                    "prototype_energy_shotgun");
    public static final com.exoarsenal.item.ItemEnergyGun PROTOTYPE_ENERGY_CANNON =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.PROTOTYPE_CANNON),
                    "prototype_energy_cannon");
    public static final com.exoarsenal.item.ItemEnergyGun PROTOTYPE_ENERGY_PISTOL =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.PROTOTYPE_PISTOL),
                    "prototype_energy_pistol");
    public static final com.exoarsenal.item.ItemEnergyGun PROTOTYPE_LASER_MACHINEGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.PROTOTYPE_MACHINEGUN),
                    "prototype_laser_machinegun");
    public static final com.exoarsenal.item.ItemEnergyGun X10_ENERGY_SHOTGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.X10_SHOTGUN),
                    "x10_energy_shotgun");
    public static final com.exoarsenal.item.ItemEnergyGun X10_ENERGY_CANNON =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.X10_CANNON),
                    "x10_energy_cannon");
    public static final com.exoarsenal.item.ItemEnergyGun X10_ENERGY_MACHINEGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.X10_MACHINEGUN),
                    "x10_energy_machinegun");
    public static final com.exoarsenal.item.ItemEnergyGun X10_ENERGY_NET_LAUNCHER =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.X10_NET_LAUNCHER),
                    "x10_energy_net_launcher");
    public static final com.exoarsenal.item.ItemEnergyShield PROTOTYPE_ENERGY_SHIELD =
            tabbed(new com.exoarsenal.item.ItemEnergyShield(false), "prototype_energy_shield");
    public static final com.exoarsenal.item.ItemEnergyShield X10_ENERGY_SHIELD =
            tabbed(new com.exoarsenal.item.ItemEnergyShield(true), "x10_energy_shield");
    public static final com.exoarsenal.item.ItemKXBlade KX20_BLADE =
            tabbed(new com.exoarsenal.item.ItemKXBlade(), "kx20_fluxuated_blade");
    public static final com.exoarsenal.item.ItemKXMultitool KX20_MULTITOOL =
            tabbed(new com.exoarsenal.item.ItemKXMultitool(), "kx20_multitool");
    public static final com.exoarsenal.item.ItemEnergyGun KX20_SHOTGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.KX_SHOTGUN),
                    "kx20_fluxuated_shotgun");
    public static final com.exoarsenal.item.ItemEnergyGun KX20_TRIBOW =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.KX_TRIBOW),
                    "kx20_tribow");
    public static final com.exoarsenal.item.ItemEnergyGun KX20_MINIGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.KX_MINIGUN),
                    "kx20_minigun");
    public static final com.exoarsenal.item.ItemEnergyGun KX20_RAILGUN =
            tabbed(
                    new com.exoarsenal.item.ItemEnergyGun(
                            com.exoarsenal.item.ItemEnergyGun.Type.KX_RAILGUN),
                    "kx20_railgun");

    public static final Item CANVAS_HELMET =
            armor(
                    new com.exoarsenal.item.ItemCanvasArmor(
                            CANVAS_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.HEAD),
                    "canvas_helmet");
    public static final Item CANVAS_CHESTPLATE =
            armor(
                    new com.exoarsenal.item.ItemCanvasArmor(
                            CANVAS_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.CHEST),
                    "canvas_chestplate");
    public static final Item CANVAS_LEGGINGS =
            armor(
                    new com.exoarsenal.item.ItemCanvasArmor(
                            CANVAS_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.LEGS),
                    "canvas_leggings");
    public static final Item CANVAS_BOOTS =
            armor(
                    new com.exoarsenal.item.ItemCanvasArmor(
                            CANVAS_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.FEET),
                    "canvas_boots");
    public static final Item RMOR_HELMET =
            armor(
                    new com.exoarsenal.item.ItemRmorArmor(
                            RMOR_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.HEAD, 100000),
                    "rmor_helmet");
    public static final Item RMOR_CHESTPLATE =
            armor(
                    new com.exoarsenal.item.ItemRmorArmor(
                            RMOR_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.CHEST, 250000),
                    "rmor_chestplate");
    public static final Item RMOR_LEGGINGS =
            armor(
                    new com.exoarsenal.item.ItemRmorArmor(
                            RMOR_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.LEGS, 150000),
                    "rmor_leggings");
    public static final Item RMOR_BOOTS =
            armor(
                    new com.exoarsenal.item.ItemRmorArmor(
                            RMOR_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.FEET, 100000),
                    "rmor_boots");
    public static final Item X10_HELMET =
            armor(
                    new com.exoarsenal.item.ItemX10Armor(
                            X10_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.HEAD, 250000),
                    "x10_helmet");
    public static final Item X10_CHESTPLATE =
            armor(
                    new com.exoarsenal.item.ItemX10Armor(
                            X10_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.CHEST, 600000),
                    "x10_chestplate");
    public static final Item X10_LEGGINGS =
            armor(
                    new com.exoarsenal.item.ItemX10Armor(
                            X10_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.LEGS, 350000),
                    "x10_leggings");
    public static final Item X10_BOOTS =
            armor(
                    new com.exoarsenal.item.ItemX10Armor(
                            X10_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.FEET, 250000),
                    "x10_boots");
    public static final Item KX20_HELMET =
            armor(
                    new com.exoarsenal.item.ItemKXArmor(
                            KX20_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.HEAD, 1500000),
                    "kx20_helmet");
    public static final Item KX20_CHESTPLATE =
            armor(
                    new com.exoarsenal.item.ItemKXArmor(
                            KX20_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.CHEST, 4000000),
                    "kx20_chestplate");
    public static final Item KX20_LEGGINGS =
            armor(
                    new com.exoarsenal.item.ItemKXArmor(
                            KX20_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.LEGS, 2500000),
                    "kx20_leggings");
    public static final Item KX20_BOOTS =
            armor(
                    new com.exoarsenal.item.ItemKXArmor(
                            KX20_ARMOR, net.minecraft.inventory.EntityEquipmentSlot.FEET, 1500000),
                    "kx20_boots");

    private static Block simpleBlock(
            String name, Material material, float hardness, float resistance, SoundType sound) {
        return named(new Block(material).setHardness(hardness).setResistance(resistance), name);
    }

    private static <T extends Block> T named(T value, String name) {
        value.setRegistryName(ExoArsenal.MODID, name)
                .setUnlocalizedName(ExoArsenal.MODID + "." + name)
                .setCreativeTab(TAB);
        return value;
    }

    private static <T extends Item> T named(T value, String name) {
        value.setRegistryName(ExoArsenal.MODID, name)
                .setUnlocalizedName(ExoArsenal.MODID + "." + name);
        return value;
    }

    private static <T extends Item> T tabbed(T value, String name) {
        T result = named(value, name);
        result.setCreativeTab(TAB);
        return result;
    }

    private static <T extends Item> T armor(T value, String name) {
        return tabbed(value, name);
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.Register<Block> event) {
        event.getRegistry()
                .registerAll(
                        FROZEN_STONE,
                        PERMAFROST,
                        FROSTBITTEN_SOIL,
                        RECOVERY_FARMLAND,
                        FROZEN_PLANKS,
                        FROZEN_LOG,
                        CLIMATE_STABILIZER,
                        POTTERY_KILN);
    }

    @SubscribeEvent
    public static void items(RegistryEvent.Register<Item> event) {
        event.getRegistry()
                .registerAll(
                        DESERT_MEDALLION,
                        DESERT_SCOURGE_BAG,
                        SCOUT_ENERGY_CORE,
                        SCOUT_TREASURE_BAG,
                        SUSPICIOUS_CONTROLLER,
                        DRYGRASS,
                        FLINT_SHARD,
                        CRUDE_CORDAGE,
                        TINDER,
                        CANVAS,
                        HEWN_STICKS,
                        FLINT_KNIFE,
                        FIRECLAY_BALL,
                        UNFIRED_CLAY_CAST,
                        CLAY_CAST,
                        UNFIRED_FIRECLAY_CAST,
                        FIRECLAY_CAST,
                        PROTOTYPE_ENERGY_CORE,
                        CLAY_RIVER_PAN,
                        SMALL_FROZEN_COPPER,
                        SMALL_FROZEN_TIN,
                        FRIGID_METAL,
                        FRIGID_ALLOY,
                        MACHINED_CORE,
                        KX_GEAR,
                        SCOUT_PINCER,
                        MODIFIED_RAILGUN,
                        GLACIER_SMASHER,
                        AURORA_BOREALIS,
                        R_BLADE,
                        R_TOOL,
                        X10_BLADE,
                        X10_MULTITOOL,
                        PROTOTYPE_ENERGY_SHOTGUN,
                        PROTOTYPE_ENERGY_CANNON,
                        PROTOTYPE_ENERGY_PISTOL,
                        PROTOTYPE_LASER_MACHINEGUN,
                        X10_ENERGY_SHOTGUN,
                        X10_ENERGY_CANNON,
                        X10_ENERGY_MACHINEGUN,
                        X10_ENERGY_NET_LAUNCHER,
                        PROTOTYPE_ENERGY_SHIELD,
                        X10_ENERGY_SHIELD,
                        KX20_BLADE,
                        KX20_MULTITOOL,
                        KX20_SHOTGUN,
                        KX20_TRIBOW,
                        KX20_MINIGUN,
                        KX20_RAILGUN,
                        CANVAS_HELMET,
                        CANVAS_CHESTPLATE,
                        CANVAS_LEGGINGS,
                        CANVAS_BOOTS,
                        RMOR_HELMET,
                        RMOR_CHESTPLATE,
                        RMOR_LEGGINGS,
                        RMOR_BOOTS,
                        X10_HELMET,
                        X10_CHESTPLATE,
                        X10_LEGGINGS,
                        X10_BOOTS,
                        KX20_HELMET,
                        KX20_CHESTPLATE,
                        KX20_LEGGINGS,
                        KX20_BOOTS);
        event.getRegistry()
                .register(
                        new com.exoarsenal.item.ItemBlockRecoverySoil(FROSTBITTEN_SOIL)
                                .setRegistryName(FROSTBITTEN_SOIL.getRegistryName()));
        event.getRegistry()
                .register(
                        new com.exoarsenal.item.ItemBlockRecoverySoil(RECOVERY_FARMLAND)
                                .setRegistryName(RECOVERY_FARMLAND.getRegistryName()));
        for (Block block :
                new Block[] {
                    FROZEN_STONE,
                    PERMAFROST,
                    FROZEN_PLANKS,
                    FROZEN_LOG,
                    CLIMATE_STABILIZER,
                    POTTERY_KILN
                }) {
            event.getRegistry()
                    .register(new ItemBlock(block).setRegistryName(block.getRegistryName()));
        }
        net.minecraftforge.oredict.OreDictionary.registerOre("string", CRUDE_CORDAGE);
        net.minecraftforge.oredict.OreDictionary.registerOre("itemString", CRUDE_CORDAGE);
        net.minecraftforge.oredict.OreDictionary.registerOre("leather", CANVAS);
        net.minecraftforge.oredict.OreDictionary.registerOre("itemLeather", CANVAS);
        net.minecraftforge.oredict.OreDictionary.registerOre("toolKnife", FLINT_KNIFE);
        net.minecraftforge.oredict.OreDictionary.registerOre("tinder", TINDER);
        net.minecraftforge.oredict.OreDictionary.registerOre("shardFlint", FLINT_SHARD);
        net.minecraftforge.oredict.OreDictionary.registerOre(
                "plankWood", new ItemStack(FROZEN_PLANKS));
        net.minecraftforge.oredict.OreDictionary.registerOre("clayFire", FIRECLAY_BALL);
        net.minecraftforge.oredict.OreDictionary.registerOre("materialFrigid", FRIGID_METAL);
        net.minecraftforge.oredict.OreDictionary.registerOre("alloyFrigid", FRIGID_ALLOY);
        net.minecraftforge.oredict.OreDictionary.registerOre("gearKX", KX_GEAR);
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void models(ModelRegistryEvent event) {
        for (Block block :
                new Block[] {
                    FROZEN_STONE,
                    PERMAFROST,
                    FROZEN_PLANKS,
                    FROZEN_LOG,
                    CLIMATE_STABILIZER,
                    POTTERY_KILN
                }) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(block),
                    0,
                    new ModelResourceLocation(block.getRegistryName(), "inventory"));
        }
        for (Item item :
                new Item[] {
                    DESERT_MEDALLION,
                    DESERT_SCOURGE_BAG,
                    SCOUT_ENERGY_CORE,
                    SCOUT_TREASURE_BAG,
                    SUSPICIOUS_CONTROLLER,
                    DRYGRASS,
                    FLINT_SHARD,
                    CRUDE_CORDAGE,
                    TINDER,
                    CANVAS,
                    HEWN_STICKS,
                    FLINT_KNIFE,
                    FIRECLAY_BALL,
                    UNFIRED_CLAY_CAST,
                    CLAY_CAST,
                    UNFIRED_FIRECLAY_CAST,
                    FIRECLAY_CAST,
                    PROTOTYPE_ENERGY_CORE,
                    CLAY_RIVER_PAN,
                    SMALL_FROZEN_COPPER,
                    SMALL_FROZEN_TIN,
                    FRIGID_METAL,
                    FRIGID_ALLOY,
                    MACHINED_CORE,
                    KX_GEAR,
                    SCOUT_PINCER,
                    MODIFIED_RAILGUN,
                    GLACIER_SMASHER,
                    AURORA_BOREALIS,
                    R_BLADE,
                    R_TOOL,
                    X10_BLADE,
                    X10_MULTITOOL,
                    PROTOTYPE_ENERGY_SHOTGUN,
                    PROTOTYPE_ENERGY_CANNON,
                    PROTOTYPE_ENERGY_PISTOL,
                    PROTOTYPE_LASER_MACHINEGUN,
                    X10_ENERGY_SHOTGUN,
                    X10_ENERGY_CANNON,
                    X10_ENERGY_MACHINEGUN,
                    X10_ENERGY_NET_LAUNCHER,
                    PROTOTYPE_ENERGY_SHIELD,
                    X10_ENERGY_SHIELD,
                    KX20_BLADE,
                    KX20_MULTITOOL,
                    KX20_SHOTGUN,
                    KX20_TRIBOW,
                    KX20_MINIGUN,
                    KX20_RAILGUN,
                    CANVAS_HELMET,
                    CANVAS_CHESTPLATE,
                    CANVAS_LEGGINGS,
                    CANVAS_BOOTS,
                    RMOR_HELMET,
                    RMOR_CHESTPLATE,
                    RMOR_LEGGINGS,
                    RMOR_BOOTS,
                    X10_HELMET,
                    X10_CHESTPLATE,
                    X10_LEGGINGS,
                    X10_BOOTS,
                    KX20_HELMET,
                    KX20_CHESTPLATE,
                    KX20_LEGGINGS,
                    KX20_BOOTS
                }) {
            ModelLoader.setCustomModelResourceLocation(
                    item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
        }
        for (int meta = 0; meta < 7; meta++) {
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(FROSTBITTEN_SOIL),
                    meta,
                    new ModelResourceLocation(FROSTBITTEN_SOIL.getRegistryName(), "stage=" + meta));
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(RECOVERY_FARMLAND),
                    meta,
                    new ModelResourceLocation(
                            RECOVERY_FARMLAND.getRegistryName(), "stage=" + meta));
        }
    }
}
