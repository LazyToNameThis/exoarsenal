package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class DeepContent {
    public static final Set<Item> ITEMS = new HashSet<>();
    public static final Item GOLD_KEY = item("golden_key"),
            SHADOW_KEY = item("shadow_key"),
            HELLSTONE = item("hellstone_shard"),
            HELLSTONE_BAR = item("hellstone_bar");
    public static final Item JUNGLE_SCHEMATIC = item("jungle_encrypted_schematic"),
            HELL_SCHEMATIC = item("underworld_encrypted_schematic");
    public static final Item JUNGLE_LOG = item("jungle_research_log"),
            HELL_LOG = item("underworld_research_log");
    public static final Item MURAMASA = weapon("muramasa", DeepWeapon.Kind.SWORD),
            WATER_BOLT = weapon("water_bolt_tome", DeepWeapon.Kind.WATER),
            DEMON_SCYTHE = weapon("demon_scythe", DeepWeapon.Kind.SCYTHE),
            FLAMELASH = weapon("flamelash", DeepWeapon.Kind.FLAME);
    public static final Item VOLCANO = weapon("volcano", DeepWeapon.Kind.FIRE_SWORD),
            BLADE_OF_GRASS = weapon("blade_of_grass", DeepWeapon.Kind.GRASS_SWORD);
    public static final Item MOLTEN_PICKAXE =
            ExpeditionContent.register(
                    new ItemPickaxe(Item.ToolMaterial.DIAMOND) {}, "molten_pickaxe");
    public static final Block DUNGEON_BRICK = block("dungeon_brick", Material.ROCK, 6),
            OBSIDIAN_BRICK = block("obsidian_brick", Material.ROCK, 5),
            ASH = block("underworld_ash", Material.GROUND, .5F),
            HELLSTONE_ORE = block("hellstone_ore", Material.ROCK, 5);
    public static final Block PLAGUEPLATE = block("plagueplate", Material.IRON, 5),
            THERMAL_PLATING = block("thermal_lab_plating", Material.IRON, 5);
    public static final Block
            DUNGEON_CACHE = ExpeditionContent.register(new Cache(false), "sealed_dungeon_cache"),
            SHADOW_CACHE = ExpeditionContent.register(new Cache(true), "sealed_shadow_cache");

    private static Item item(String id) {
        Item item = ExpeditionContent.register(new ArchiveItem(), id);
        ITEMS.add(item);
        return item;
    }

    private static Item weapon(String id, DeepWeapon.Kind kind) {
        Item item = ExpeditionContent.register(new DeepWeapon(kind), id);
        ITEMS.add(item);
        return item;
    }

    private static Block block(String id, Material material, float hardness) {
        Block block =
                ExpeditionContent.register(
                        new Block(material).setHardness(hardness).setResistance(20), id);
        block.setHarvestLevel(
                material == Material.GROUND ? "shovel" : "pickaxe",
                material == Material.GROUND ? 0 : 2);
        return block;
    }

    public static void init() {
        ITEMS.add(MOLTEN_PICKAXE);
    }

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<net.minecraft.item.crafting.IRecipe> event) {
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        null,
                                        new ItemStack(GOLD_KEY),
                                        "ingotGold",
                                        "ingotGold",
                                        Items.BONE,
                                        Items.BONE)
                                .setRegistryName(ExoArsenal.MODID, "golden_key"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        null,
                                        new ItemStack(OBSIDIAN_BRICK, 4),
                                        Blocks.OBSIDIAN,
                                        Blocks.OBSIDIAN,
                                        Blocks.OBSIDIAN,
                                        Blocks.OBSIDIAN)
                                .setRegistryName(ExoArsenal.MODID, "obsidian_brick"));
        event.getRegistry()
                .register(
                        new net.minecraftforge.oredict.ShapedOreRecipe(
                                        null,
                                        new ItemStack(MOLTEN_PICKAXE),
                                        "HHH",
                                        " V ",
                                        " V ",
                                        'H',
                                        HELLSTONE_BAR,
                                        'V',
                                        WildContent.VINE)
                                .setRegistryName(ExoArsenal.MODID, "molten_pickaxe"));
        event.getRegistry()
                .register(
                        new net.minecraftforge.oredict.ShapedOreRecipe(
                                        null,
                                        new ItemStack(VOLCANO),
                                        " H ",
                                        " H ",
                                        " V ",
                                        'H',
                                        HELLSTONE_BAR,
                                        'V',
                                        WildContent.VINE)
                                .setRegistryName(ExoArsenal.MODID, "volcano"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        null,
                                        new ItemStack(BLADE_OF_GRASS),
                                        WildContent.SPORES,
                                        WildContent.SPORES,
                                        WildContent.SPORES,
                                        WildContent.STINGER,
                                        WildContent.STINGER,
                                        WildContent.VINE)
                                .setRegistryName(ExoArsenal.MODID, "blade_of_grass"));
    }

    public static void integrate() {
        net.minecraftforge.fluids.Fluid fluid =
                net.minecraftforge.fluids.FluidRegistry.getFluid("hellstone");
        if (fluid == null) {
            fluid =
                    new net.minecraftforge.fluids.Fluid(
                                    "hellstone",
                                    new ResourceLocation("minecraft:blocks/lava_still"),
                                    new ResourceLocation("minecraft:blocks/lava_flow"))
                            .setTemperature(1773)
                            .setColor(0xFFFF773C);
            net.minecraftforge.fluids.FluidRegistry.registerFluid(fluid);
            fluid = net.minecraftforge.fluids.FluidRegistry.getFluid("hellstone");
        }
        slimeknights.tconstruct.library.TinkerRegistry.registerMelting(
                new ItemStack(HELLSTONE_ORE), fluid, 288);
        slimeknights.tconstruct.library.TinkerRegistry.registerMelting(
                new ItemStack(HELLSTONE), fluid, 144);
        slimeknights.tconstruct.library.TinkerRegistry.registerMelting(
                new ItemStack(HELLSTONE_BAR), fluid, 144);
        slimeknights.tconstruct.library.TinkerRegistry.registerTableCasting(
                new ItemStack(HELLSTONE_BAR),
                slimeknights.tconstruct.smeltery.TinkerSmeltery.castIngot,
                fluid,
                144);
        net.minecraftforge.oredict.OreDictionary.registerOre("ingotHellstone", HELLSTONE_BAR);
        net.minecraftforge.oredict.OreDictionary.registerOre("oreHellstone", HELLSTONE_ORE);
    }

    private static final class ArchiveItem extends Item {
        @Override
        public ActionResult<ItemStack> onItemRightClick(
                World world, EntityPlayer player, EnumHand hand) {
            if (this != JUNGLE_LOG
                    && this != HELL_LOG
                    && this != JUNGLE_SCHEMATIC
                    && this != HELL_SCHEMATIC) return super.onItemRightClick(world, player, hand);
            if (!world.isRemote)
                player.sendMessage(
                        new TextComponentString(
                                this == JUNGLE_LOG
                                        ? "BIO-CENTER / JUNGLE: Keep the central culture isolated. Service access runs through the two side shafts."
                                        : this == HELL_LOG
                                                ? "BIO-CENTER / UNDERWORLD: Coolant circulation has failed. The thermal chamber remains sealed; use the upper service gantry."
                                                : "Encrypted schematic recovered. This is an archive artifact; arsenal decryption is not implemented yet."));
            return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
        }
    }

    private static final class Cache extends Block {
        private final boolean shadow;

        Cache(boolean shadow) {
            super(Material.IRON);
            this.shadow = shadow;
            setHardness(-1);
            setResistance(6000000);
        }

        @Override
        public boolean onBlockActivated(
                World world,
                BlockPos pos,
                net.minecraft.block.state.IBlockState state,
                EntityPlayer player,
                EnumHand hand,
                EnumFacing face,
                float x,
                float y,
                float z) {
            if (hand != EnumHand.MAIN_HAND) return true;
            Item key = shadow ? SHADOW_KEY : GOLD_KEY;
            ItemStack held = player.getHeldItem(hand);
            if (held.getItem() != key && !player.isCreative()) {
                if (!world.isRemote)
                    player.sendStatusMessage(
                            new TextComponentString(
                                    shadow
                                            ? "Requires a Shadow Key. The key is reusable."
                                            : "Requires a Golden Key."),
                            true);
                return true;
            }
            if (world.isRemote) return true;
            if (!player.canPlayerEdit(pos, face, held)) return true;
            if (!world.setBlockToAir(pos)) return true;
            if (!shadow && !player.isCreative()) held.shrink(1);
            Random random = new Random(world.getSeed() ^ pos.toLong());
            for (ItemStack loot : DeepLoot.cache(shadow, random)) {
                if (!player.inventory.addItemStackToInventory(loot)) player.dropItem(loot, false);
            }
            world.playSound(null, pos, SoundEvents.BLOCK_CHEST_OPEN, SoundCategory.BLOCKS, .8F, 1);
            return true;
        }
    }
}
