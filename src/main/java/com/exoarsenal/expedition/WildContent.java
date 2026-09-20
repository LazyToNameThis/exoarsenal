package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.item.*;
import net.minecraft.init.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class WildContent {
    public static final Set<Item> MATERIALS = new HashSet<>();
    public static final Item STINGER = material("stinger"),
            VINE = material("jungle_vine"),
            SPORES = material("jungle_spores");
    public static final Item ROTTEN_CHUNK = material("rotten_chunk"),
            VERTEBRA = material("vertebra"),
            GEL = material("gel");
    public static final Item COCHINEAL = material("cochineal_husk"),
            CYAN = material("cyan_husk"),
            LAC = material("lac_husk");
    public static final Block EBONSTONE = rock("ebonstone", 4, 2),
            CRIMSTONE = rock("crimstone", 4, 2);
    public static final Block MUD =
            ExpeditionContent.register(
                    new Block(Material.GROUND) {
                        {
                            setSoundType(SoundType.GROUND);
                        }
                    }.setHardness(.6F),
                    "jungle_mud");
    public static final Block SPORE_PLANT =
            ExpeditionContent.register(
                    new BlockBush() {
                        @Override
                        public Item getItemDropped(
                                net.minecraft.block.state.IBlockState state,
                                Random random,
                                int fortune) {
                            return SPORES;
                        }

                        @Override
                        public boolean canBlockStay(
                                net.minecraft.world.World world,
                                net.minecraft.util.math.BlockPos pos,
                                net.minecraft.block.state.IBlockState state) {
                            return world.getBlockState(pos.down()).getMaterial() == Material.GROUND
                                    || world.getBlockState(pos.down()).getMaterial()
                                            == Material.GRASS;
                        }
                    }.setLightLevel(.45F),
                    "jungle_spore_cluster");

    private static Item material(String id) {
        Item item = ExpeditionContent.register(new Item(), id);
        MATERIALS.add(item);
        return item;
    }

    private static Block rock(String id, float hardness, int level) {
        Block block =
                ExpeditionContent.register(
                        new Block(Material.ROCK).setHardness(hardness).setResistance(12), id);
        block.setHarvestLevel("pickaxe", level);
        return block;
    }

    public static void init() {
        MUD.setHarvestLevel("shovel", 0);
    }

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<net.minecraft.item.crafting.IRecipe> event) {
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.SLIME_BALL), GEL, GEL)
                                .setRegistryName(ExoArsenal.MODID, "gel_slime_ball"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.STRING, 3), VINE)
                                .setRegistryName(ExoArsenal.MODID, "jungle_vine_string"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.DYE, 2, 1), COCHINEAL)
                                .setRegistryName(ExoArsenal.MODID, "cochineal_dye"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.DYE, 2, 6), CYAN)
                                .setRegistryName(ExoArsenal.MODID, "cyan_husk_dye"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.DYE, 2, 5), LAC)
                                .setRegistryName(ExoArsenal.MODID, "lac_husk_dye"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(null, new ItemStack(Items.DYE, 3, 15), VERTEBRA)
                                .setRegistryName(ExoArsenal.MODID, "vertebra_bone_meal"));
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        null, new ItemStack(Items.ROTTEN_FLESH, 2), ROTTEN_CHUNK)
                                .setRegistryName(ExoArsenal.MODID, "rotten_chunk_flesh"));
    }
}
