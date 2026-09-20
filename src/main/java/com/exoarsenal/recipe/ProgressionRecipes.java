package com.exoarsenal.recipe;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.compat.MekanismCompat;
import com.exoarsenal.registry.ModContent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.init.Blocks;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistryModifiable;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ProgressionRecipes {
    private static int recipeId;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void register(RegistryEvent.Register<IRecipe> event) {
        recipeId = 0;
        removeTinkersDefaults(event);
        removeVanillaMetalGear(event);
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.FROZEN_PLANKS, 2),
                        "ss",
                        "ss",
                        's',
                        MekanismCompat.sawdust(1)));
        add(event, new RecipeCordage());
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.CANVAS),
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.DRYGRASS,
                        ModContent.DRYGRASS,
                        ModContent.DRYGRASS,
                        ModContent.DRYGRASS));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.TINDER),
                        ModContent.DRYGRASS,
                        Items.STICK,
                        Items.STICK));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.FLINT_KNIFE),
                        Items.STICK,
                        ModContent.DRYGRASS,
                        ModContent.FLINT_SHARD));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.HEWN_STICKS),
                        Items.STICK,
                        Items.STICK,
                        Items.STICK,
                        ModContent.DRYGRASS));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.FIRECLAY_BALL),
                        Items.CLAY_BALL,
                        new ItemStack(Items.DYE, 1, 15),
                        "dustCharcoal"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.CLAY_RIVER_PAN),
                        "c c",
                        "ccc",
                        'c',
                        Items.CLAY_BALL));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.FRIGID_ALLOY),
                        ModContent.FRIGID_METAL,
                        "ingotGold",
                        "ingotManasteel"));
        add(event, new RecipeMoldCast(false));
        add(event, new RecipeMoldCast(true));

        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.CANVAS_HELMET),
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.CANVAS_CHESTPLATE),
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.CANVAS_LEGGINGS),
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.CANVAS_BOOTS),
                        ModContent.CANVAS,
                        ModContent.CANVAS,
                        ModContent.CRUDE_CORDAGE,
                        ModContent.CRUDE_CORDAGE));

        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.RMOR_HELMET),
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        "alloyAdvanced",
                        "alloyAdvanced",
                        "ingotSteel",
                        "ingotSteel"));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.RMOR_CHESTPLATE),
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        "alloyAdvanced",
                        "alloyAdvanced",
                        "ingotSteel",
                        "ingotSteel",
                        "ingotSteel",
                        "ingotSteel",
                        "ingotSteel"));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.RMOR_LEGGINGS),
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        "alloyAdvanced",
                        "alloyAdvanced",
                        "ingotSteel",
                        "ingotSteel",
                        "ingotSteel",
                        "ingotSteel"));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.RMOR_BOOTS),
                        "alloyAdvanced",
                        "alloyAdvanced",
                        "ingotSteel",
                        "ingotSteel"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.R_BLADE),
                        "sas",
                        "aca",
                        " r ",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "blockRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.R_TOOL),
                        "sas",
                        "aca",
                        "srs",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "blockRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_BLADE),
                        "aya",
                        "yby",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'b',
                        ModContent.R_BLADE,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_MULTITOOL),
                        "aya",
                        "yty",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        't',
                        ModContent.R_TOOL,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));

        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.PROTOTYPE_ENERGY_PISTOL),
                        " sa",
                        "acr",
                        " sa",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "dustRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.PROTOTYPE_ENERGY_SHOTGUN),
                        "ssa",
                        "acr",
                        "ssa",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "blockRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.PROTOTYPE_ENERGY_CANNON),
                        "saa",
                        "acr",
                        "saa",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "blockRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.PROTOTYPE_LASER_MACHINEGUN),
                        "asa",
                        "rcr",
                        "asa",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "dustRedstone"));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.PROTOTYPE_ENERGY_SHIELD),
                        "sas",
                        "aca",
                        " r ",
                        's',
                        "ingotSteel",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        'r',
                        "blockRedstone"));

        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_ENERGY_SHOTGUN),
                        "aya",
                        "ygy",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'g',
                        ModContent.PROTOTYPE_ENERGY_SHOTGUN,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_ENERGY_CANNON),
                        "aya",
                        "ygy",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'g',
                        ModContent.PROTOTYPE_ENERGY_CANNON,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_ENERGY_MACHINEGUN),
                        "aya",
                        "ygy",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'g',
                        ModContent.PROTOTYPE_LASER_MACHINEGUN,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_ENERGY_NET_LAUNCHER),
                        "aya",
                        "ngn",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'n',
                        Items.STRING,
                        'g',
                        ModContent.PROTOTYPE_ENERGY_SHOTGUN,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_ENERGY_SHIELD),
                        "aya",
                        "ygy",
                        "aca",
                        'a',
                        "alloyElite",
                        'y',
                        "ingotGold",
                        'g',
                        ModContent.PROTOTYPE_ENERGY_SHIELD,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));

        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_HELMET),
                        ModContent.RMOR_HELMET,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_CHESTPLATE),
                        ModContent.RMOR_CHESTPLATE,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_LEGGINGS),
                        ModContent.RMOR_LEGGINGS,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.X10_BOOTS),
                        ModContent.RMOR_BOOTS,
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY,
                        ModContent.FRIGID_ALLOY));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.POTTERY_KILN),
                        "bbb",
                        "bcb",
                        "bbb",
                        'b',
                        Items.BRICK,
                        'c',
                        Items.CLAY_BALL));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.CLIMATE_STABILIZER),
                        "pip",
                        "aca",
                        "srs",
                        'p',
                        Blocks.PACKED_ICE,
                        'i',
                        "circuitAdvanced",
                        'a',
                        "alloyAdvanced",
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE,
                        's',
                        "ingotSteel",
                        'r',
                        "blockRedstone"));

        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.KX_GEAR),
                        "faf",
                        "aca",
                        "faf",
                        'f',
                        ModContent.FRIGID_ALLOY,
                        'a',
                        "alloyElite",
                        'c',
                        ModContent.MACHINED_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_HELMET),
                        ModContent.X10_HELMET,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_CHESTPLATE),
                        ModContent.X10_CHESTPLATE,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_LEGGINGS),
                        ModContent.X10_LEGGINGS,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_BOOTS),
                        ModContent.X10_BOOTS,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_BLADE),
                        "gag",
                        "gbg",
                        " c ",
                        'g',
                        ModContent.KX_GEAR,
                        'a',
                        "alloyElite",
                        'b',
                        ModContent.X10_BLADE,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapedOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_MULTITOOL),
                        "gag",
                        "gtg",
                        " c ",
                        'g',
                        ModContent.KX_GEAR,
                        'a',
                        "alloyElite",
                        't',
                        ModContent.X10_MULTITOOL,
                        'c',
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_SHOTGUN),
                        ModContent.X10_ENERGY_SHOTGUN,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_TRIBOW),
                        ModContent.X10_ENERGY_NET_LAUNCHER,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        Items.BOW,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_MINIGUN),
                        ModContent.X10_ENERGY_MACHINEGUN,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.PROTOTYPE_ENERGY_CORE));
        add(
                event,
                new ShapelessOreRecipe(
                        null,
                        new ItemStack(ModContent.KX20_RAILGUN),
                        ModContent.X10_ENERGY_CANNON,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.KX_GEAR,
                        ModContent.MACHINED_CORE));

        Item pattern =
                ForgeRegistries.ITEMS.getValue(new ResourceLocation("tconstruct", "pattern"));
        Block tables =
                ForgeRegistries.BLOCKS.getValue(new ResourceLocation("tconstruct", "tooltables"));
        if (pattern != null) {
            add(event, new RecipeKnifePattern());
        }
        if (pattern != null && tables != null) {
            add(
                    event,
                    new ShapelessOreRecipe(
                            null,
                            new ItemStack(tables, 1, 2),
                            ModContent.HEWN_STICKS,
                            ModContent.HEWN_STICKS,
                            ModContent.DRYGRASS,
                            new ItemStack(pattern)));
            add(
                    event,
                    new ShapelessOreRecipe(
                            null,
                            new ItemStack(tables, 1, 1),
                            ModContent.HEWN_STICKS,
                            ModContent.HEWN_STICKS,
                            ModContent.FLINT_KNIFE,
                            new ItemStack(pattern)));
            add(
                    event,
                    new ShapelessOreRecipe(
                            null,
                            new ItemStack(tables, 1, 3),
                            ModContent.HEWN_STICKS,
                            ModContent.HEWN_STICKS,
                            new ItemStack(pattern),
                            new ItemStack(pattern)));
        }
    }

    @SuppressWarnings("unchecked")
    private static void removeTinkersDefaults(RegistryEvent.Register<IRecipe> event) {
        if (!(event.getRegistry() instanceof IForgeRegistryModifiable)) return;
        IForgeRegistryModifiable<IRecipe> recipes =
                (IForgeRegistryModifiable<IRecipe>) event.getRegistry();
        recipes.remove(new ResourceLocation("tconstruct", "tools/pattern"));
        recipes.remove(new ResourceLocation("tconstruct", "tools/table/part_builder"));
        recipes.remove(new ResourceLocation("tconstruct", "tools/table/stencil_table"));
        recipes.remove(new ResourceLocation("tconstruct", "tools/table/tool_station"));
    }

    @SuppressWarnings("unchecked")
    private static void removeVanillaMetalGear(RegistryEvent.Register<IRecipe> event) {
        if (!(event.getRegistry() instanceof IForgeRegistryModifiable)) return;
        IForgeRegistryModifiable<IRecipe> recipes =
                (IForgeRegistryModifiable<IRecipe>) event.getRegistry();
        java.util.List<ResourceLocation> remove = new java.util.ArrayList<>();
        for (IRecipe recipe : event.getRegistry()) {
            ResourceLocation id = recipe.getRegistryName();
            ItemStack output = recipe.getRecipeOutput();
            if (id == null || output.isEmpty() || !"minecraft".equals(id.getResourceDomain()))
                continue;
            ResourceLocation itemId = output.getItem().getRegistryName();
            if (itemId == null) continue;
            String path = itemId.getResourcePath();
            boolean metal =
                    path.startsWith("iron_")
                            || path.startsWith("golden_")
                            || path.startsWith("diamond_");
            boolean gear =
                    output.getItem() instanceof net.minecraft.item.ItemTool
                            || output.getItem() instanceof net.minecraft.item.ItemSword
                            || output.getItem() instanceof net.minecraft.item.ItemArmor
                            || output.getItem() instanceof net.minecraft.item.ItemHoe;
            if (metal && gear) remove.add(id);
        }
        for (ResourceLocation id : remove) recipes.remove(id);
    }

    private static void add(RegistryEvent.Register<IRecipe> event, IRecipe recipe) {
        recipe.setRegistryName(new ResourceLocation(ExoArsenal.MODID, "progression_" + recipeId++));
        event.getRegistry().register(recipe);
    }
}
