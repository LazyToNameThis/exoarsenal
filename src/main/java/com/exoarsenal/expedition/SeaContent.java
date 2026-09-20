package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.ShapelessOreRecipe;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class SeaContent {
    public static final Item REMAINS = material("sea_remains");
    public static final Item FOSSIL = material("sturdy_fossil");
    public static final Item WHITE_PEARL = material("white_pearl");
    public static final Item BLACK_PEARL = material("black_pearl");
    public static final Item PINK_PEARL = material("pink_pearl");
    public static final Item ILMERIS_SPARK = weapon("ilmeris_spark", SeaWeapon.Kind.SPARK);
    public static final Item MACE = weapon("urchin_mace", SeaWeapon.Kind.MACE);
    public static final Item SPEAR = weapon("redtide_spear", SeaWeapon.Kind.SPEAR);
    public static final Item BLOWGUN = weapon("reed_blowgun", SeaWeapon.Kind.BLOWGUN);
    public static final Item SPOUT = weapon("coral_spout", SeaWeapon.Kind.SPOUT);
    public static final Item CNIDARIAN = weapon("cnidarian", SeaWeapon.Kind.CNIDARIAN);
    public static final Item BOOMERANG = weapon("fishbone_boomerang", SeaWeapon.Kind.BOOMERANG);
    public static final Item SHIELD = weapon("shield_of_the_ocean", SeaWeapon.Kind.SHIELD);
    public static final Item PICKAXE =
            ExpeditionContent.register(new SeaTool(false), "greatbay_pickaxe");
    public static final Item HAMAXE =
            ExpeditionContent.register(new SeaTool(true), "reefclaw_hamaxe");
    public static final Item SHELLMET = armor("victide_shellmet", EntityEquipmentSlot.HEAD, 0);
    public static final Item TURBAN = armor("victide_coral_turban", EntityEquipmentSlot.HEAD, 1);
    public static final Item HERMIT = armor("victide_hermit_helmet", EntityEquipmentSlot.HEAD, 2);
    public static final Item MASK = armor("victide_mask", EntityEquipmentSlot.HEAD, 3);
    public static final Item HEADCRAB = armor("victide_headcrab", EntityEquipmentSlot.HEAD, 4);
    public static final Item CHEST = armor("victide_breastplate", EntityEquipmentSlot.CHEST, 0);
    public static final Item LEGS = armor("victide_greaves", EntityEquipmentSlot.LEGS, 0);
    public static final Item PENDANT = weapon("amidias_pendant", SeaWeapon.Kind.PENDANT);
    public static final Item GIANT_PEARL = weapon("giant_pearl", SeaWeapon.Kind.PEARL);

    public static void init() {}

    private static Item material(String id) {
        return ExpeditionContent.register(new Item(), id);
    }

    private static Item weapon(String id, SeaWeapon.Kind kind) {
        return ExpeditionContent.register(new SeaWeapon(kind), id);
    }

    private static Item armor(String id, EntityEquipmentSlot slot, int style) {
        return ExpeditionContent.register(new VictideArmor(slot, style), id);
    }

    private static void remainsRecipe(
            RegistryEvent.Register<IRecipe> event, Item output, int count, Object marker) {
        Object[] ingredients = new Object[count + 1];
        java.util.Arrays.fill(ingredients, REMAINS);
        ingredients[count] = marker;
        recipe(event, output, ingredients);
    }

    private static void recipe(
            RegistryEvent.Register<IRecipe> event, Item output, Object... ingredients) {
        event.getRegistry()
                .register(
                        new ShapelessOreRecipe(
                                        new ResourceLocation(ExoArsenal.MODID, "sunken_sea"),
                                        new ItemStack(output),
                                        ingredients)
                                .setRegistryName(output.getRegistryName()));
    }

    @SubscribeEvent
    public static void recipes(RegistryEvent.Register<IRecipe> event) {
        recipe(
                event,
                REMAINS,
                ExpeditionContent.PEARL_SHARD,
                ExpeditionContent.PEARL_SHARD,
                ExpeditionContent.CORAL,
                ExpeditionContent.CORAL,
                ExpeditionContent.SEASHELL,
                ExpeditionContent.SEASHELL,
                ExpeditionContent.STARFISH,
                ExpeditionContent.STARFISH);
        remainsRecipe(event, MACE, 3, ExpeditionContent.CORAL);
        remainsRecipe(event, SPEAR, 4, net.minecraft.init.Items.STICK);
        remainsRecipe(event, BLOWGUN, 2, net.minecraft.init.Items.REEDS);
        remainsRecipe(event, CNIDARIAN, 2, ExpeditionContent.PRISM_SHARD);
        remainsRecipe(event, BOOMERANG, 3, net.minecraft.init.Items.BONE);
        remainsRecipe(event, PICKAXE, 4, net.minecraft.init.Items.WOODEN_PICKAXE);
        remainsRecipe(event, HAMAXE, 4, net.minecraft.init.Items.WOODEN_AXE);
        recipe(
                event,
                SPOUT,
                REMAINS,
                REMAINS,
                ExpeditionContent.CORAL,
                ExpeditionContent.CORAL,
                ExpeditionContent.CORAL,
                ExpeditionContent.CORAL,
                ExpeditionContent.CORAL);

        recipe(
                event,
                SHIELD,
                REMAINS,
                REMAINS,
                REMAINS,
                REMAINS,
                REMAINS,
                ExpeditionContent.STARFISH);
        remainsRecipe(event, SHELLMET, 3, ExpeditionContent.SEASHELL);
        remainsRecipe(event, TURBAN, 3, net.minecraft.init.Items.ARROW);
        remainsRecipe(event, HERMIT, 3, ExpeditionContent.PRISM_SHARD);
        remainsRecipe(event, MASK, 3, net.minecraft.init.Items.STRING);
        remainsRecipe(event, HEADCRAB, 3, ExpeditionContent.STARFISH);
        recipe(event, CHEST, REMAINS, REMAINS, REMAINS, REMAINS, REMAINS);
        recipe(event, LEGS, REMAINS, REMAINS, REMAINS, REMAINS);
    }
}
