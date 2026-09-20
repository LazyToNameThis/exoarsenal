package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fluids.*;
import net.minecraftforge.oredict.OreDictionary;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class GeologyContent {
    public enum Ore implements IStringSerializable {
        COPPER("copper", "Copper", 0xCD8B63, 0, 1085, false),
        TIN("tin", "Tin", 0xC2CCD1, 0, 232, false),
        LEAD("lead", "Lead", 0x777DA4, 1, 327, false),
        SILVER("silver", "Silver", 0xDFE4E8, 1, 962, false),
        TUNGSTEN("tungsten", "Tungsten", 0x98B1A6, 1, 3422, false),
        PLATINUM("platinum", "Platinum", 0xC7DCE1, 2, 1768, false),
        AMETHYST("amethyst", "Amethyst", 0xB38BCC, 1, 0, true),
        TOPAZ("topaz", "Topaz", 0xE5B55E, 1, 0, true),
        SAPPHIRE("sapphire", "Sapphire", 0x648FD6, 1, 0, true),
        RUBY("ruby", "Ruby", 0xD65A77, 1, 0, true),
        AMBER("amber", "Amber", 0xDF934B, 0, 0, true),
        DEMONITE("demonite", "Demonite", 0xA682C8, 2, 1400, false),
        CRIMTANE("crimtane", "Crimtane", 0xB95463, 2, 1400, false);
        public final String id, label;
        public final int color, harvest, temperature;
        public final boolean gem;

        Ore(String id, String label, int color, int harvest, int temperature, boolean gem) {
            this.id = id;
            this.label = label;
            this.color = color;
            this.harvest = harvest;
            this.temperature = temperature;
            this.gem = gem;
        }

        @Override
        public String getName() {
            return id;
        }

        public String dictionary() {
            return (gem ? "gem" : "ingot") + label;
        }

        public static Ore at(int meta) {
            return values()[Math.max(0, Math.min(values().length - 1, meta))];
        }
    }

    public static final Item MATERIAL =
            ExpeditionContent.register(new GeologicalMaterial(), "geological_material");
    public static final Item JAVELIN =
            ExpeditionContent.register(new CaveJavelin(), "cave_javelin");
    public static final Item GLOW_MUSHROOM =
            ExpeditionContent.register(new ItemFood(2, .2F, false), "glowing_mushroom");
    public static final Block ORE = ExpeditionContent.register(new OreBlock(), "terraria_ore");
    public static final Block SILT =
            ExpeditionContent.register(new BlockFalling(Material.SAND).setHardness(.55F), "silt");
    public static final Block SLUSH =
            ExpeditionContent.register(new BlockFalling(Material.SAND).setHardness(.45F), "slush");
    public static final Block FOSSIL =
            ExpeditionContent.register(new Block(Material.ROCK).setHardness(1.5F), "desert_fossil");
    public static final Block MUSHROOM =
            ExpeditionContent.register(
                    new BlockBush() {
                        @Override
                        public Item getItemDropped(
                                IBlockState state, java.util.Random random, int fortune) {
                            return GLOW_MUSHROOM;
                        }

                        @Override
                        public boolean canBlockStay(
                                net.minecraft.world.World world, BlockPos pos, IBlockState state) {
                            Material below = world.getBlockState(pos.down()).getMaterial();
                            return below == Material.GRASS || below == Material.GROUND;
                        }

                        @Override
                        public boolean canPlaceBlockAt(
                                net.minecraft.world.World world, BlockPos pos) {
                            return world.isAirBlock(pos)
                                    && canBlockStay(world, pos, getDefaultState());
                        }
                    }.setLightLevel(.65F),
                    "glowing_mushroom_plant");
    public static final Block EXTRACTINATOR =
            ExpeditionContent.register(new ExtractinatorBlock(), "extractinator");

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<net.minecraft.item.crafting.IRecipe> event) {
        event.getRegistry()
                .register(
                        new net.minecraftforge.oredict.ShapedOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "geology"),
                                        new ItemStack(EXTRACTINATOR),
                                        "IPI",
                                        "SCS",
                                        "III",
                                        'I',
                                        "ingotIron",
                                        'P',
                                        net.minecraft.init.Blocks.PISTON,
                                        'S',
                                        ExpeditionContent.SCRAP,
                                        'C',
                                        "cobblestone")
                                .setRegistryName(ExoArsenal.MODID, "extractinator"));
        event.getRegistry()
                .register(
                        new net.minecraftforge.oredict.ShapelessOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "geology"),
                                        new ItemStack(JAVELIN, 8),
                                        net.minecraft.init.Items.STICK,
                                        net.minecraft.init.Items.FLINT,
                                        net.minecraft.init.Items.BONE)
                                .setRegistryName(ExoArsenal.MODID, "cave_javelin"));
        event.getRegistry()
                .register(
                        new net.minecraftforge.oredict.ShapelessOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "geology"),
                                        new ItemStack(ExpeditionContent.HEALING_POTION),
                                        GLOW_MUSHROOM,
                                        GLOW_MUSHROOM,
                                        net.minecraft.init.Items.GLASS_BOTTLE)
                                .setRegistryName(ExoArsenal.MODID, "mushroom_healing_potion"));
    }

    public static void init() {
        SILT.setHarvestLevel("shovel", 0);
        SLUSH.setHarvestLevel("shovel", 0);
        FOSSIL.setHarvestLevel("pickaxe", 0);
    }

    public static ItemStack material(Ore ore) {
        return new ItemStack(MATERIAL, 1, ore.ordinal());
    }

    public static IBlockState ore(Ore ore) {
        return ORE.getDefaultState().withProperty(OreBlock.TYPE, ore);
    }

    public static ItemStack preferred(Ore ore) {
        for (ItemStack stack : OreDictionary.getOres(ore.dictionary(), false))
            if (stack.getItem() != MATERIAL) {
                ItemStack copy = stack.copy();
                copy.setCount(1);
                return copy;
            }
        return material(ore);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void dictionary(RegistryEvent.Register<Item> event) {
        for (Ore ore : Ore.values()) {
            OreDictionary.registerOre(ore.dictionary(), material(ore));
            OreDictionary.registerOre("ore" + ore.label, new ItemStack(ORE, 1, ore.ordinal()));
        }
    }

    public static void integrate() {
        for (Ore ore : Ore.values())
            if (!ore.gem) {
                Fluid fluid = FluidRegistry.getFluid(ore.id);
                if (fluid == null) {
                    fluid =
                            new Fluid(
                                            ore.id,
                                            new ResourceLocation("minecraft:blocks/lava_still"),
                                            new ResourceLocation("minecraft:blocks/lava_flow"))
                                    .setTemperature(ore.temperature + 273)
                                    .setColor(0xFF000000 | ore.color);
                    FluidRegistry.registerFluid(fluid);
                    fluid = FluidRegistry.getFluid(ore.id);
                }
                slimeknights.tconstruct.library.TinkerRegistry.registerMelting(
                        new ItemStack(ORE, 1, ore.ordinal()), fluid, 288);
                slimeknights.tconstruct.library.TinkerRegistry.registerMelting(
                        material(ore), fluid, 144);
                slimeknights.tconstruct.library.TinkerRegistry.registerTableCasting(
                        preferred(ore),
                        slimeknights.tconstruct.smeltery.TinkerSmeltery.castIngot,
                        fluid,
                        144);
                if (ore.temperature <= 1100)
                    com.exoarsenal.tile.TileEntityPotteryKiln.registerLowTemperatureMetalRecipe(
                            new ItemStack(ORE, 1, ore.ordinal()), preferred(ore));
            }
    }

    public static final class GeologicalMaterial extends Item {
        GeologicalMaterial() {
            setHasSubtypes(true);
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            return super.getUnlocalizedName() + "." + Ore.at(stack.getMetadata()).id;
        }

        @Override
        public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
            if (isInCreativeTab(tab)) for (Ore ore : Ore.values()) items.add(material(ore));
        }
    }

    public static final class OreItem extends ItemBlock {
        public OreItem(Block block) {
            super(block);
            setHasSubtypes(true);
        }

        @Override
        public int getMetadata(int damage) {
            return Ore.at(damage).ordinal();
        }

        @Override
        public String getUnlocalizedName(ItemStack stack) {
            return super.getUnlocalizedName() + "." + Ore.at(stack.getMetadata()).id;
        }
    }

    public static final class OreBlock extends Block {
        public static final PropertyEnum<Ore> TYPE = PropertyEnum.create("type", Ore.class);

        OreBlock() {
            super(Material.ROCK);
            setHardness(3);
            setResistance(5);
            setDefaultState(blockState.getBaseState().withProperty(TYPE, Ore.COPPER));
            for (Ore ore : Ore.values())
                setHarvestLevel("pickaxe", ore.harvest, getDefaultState().withProperty(TYPE, ore));
        }

        @Override
        protected BlockStateContainer createBlockState() {
            return new BlockStateContainer(this, TYPE);
        }

        @Override
        public int getMetaFromState(IBlockState state) {
            return state.getValue(TYPE).ordinal();
        }

        @Override
        public IBlockState getStateFromMeta(int meta) {
            return getDefaultState().withProperty(TYPE, Ore.at(meta));
        }

        @Override
        public int damageDropped(IBlockState state) {
            return getMetaFromState(state);
        }

        @Override
        public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
            for (Ore ore : Ore.values()) items.add(new ItemStack(this, 1, ore.ordinal()));
        }

        @Override
        public void getDrops(
                NonNullList<ItemStack> drops,
                IBlockAccess world,
                BlockPos pos,
                IBlockState state,
                int fortune) {
            Ore ore = state.getValue(TYPE);
            if (ore.gem) {
                ItemStack gem = preferred(ore);
                gem.setCount(1 + (fortune > 0 ? RANDOM.nextInt(Math.min(3, fortune) + 1) : 0));
                drops.add(gem);
            } else drops.add(new ItemStack(this, 1, ore.ordinal()));
        }
    }
}
