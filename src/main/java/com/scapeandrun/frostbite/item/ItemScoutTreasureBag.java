package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class ItemScoutTreasureBag extends Item
        implements software.bernie.geckolib3.core.IAnimatable {
    private final software.bernie.geckolib3.core.manager.AnimationFactory factory =
            new software.bernie.geckolib3.core.manager.AnimationFactory(this);

    @Override
    public software.bernie.geckolib3.core.manager.AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(software.bernie.geckolib3.core.manager.AnimationData data) {}

    public ItemScoutTreasureBag() {
        setMaxStackSize(16);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack bag = player.getHeldItem(hand);
        if (!world.isRemote) {
            bag.shrink(1);
            give(player, new ItemStack(ModContent.SCOUT_ENERGY_CORE));
            give(player, new ItemStack(ModContent.MACHINED_CORE, 3 + world.rand.nextInt(2)));
            give(player, new ItemStack(ModContent.FRIGID_METAL, 12 + world.rand.nextInt(9)));
            Item[] weapons = {
                ModContent.SCOUT_PINCER,
                ModContent.MODIFIED_RAILGUN,
                ModContent.GLACIER_SMASHER,
                ModContent.AURORA_BOREALIS
            };
            int first = world.rand.nextInt(4), second = (first + 1 + world.rand.nextInt(3)) % 4;
            give(player, new ItemStack(weapons[first]));
            give(player, new ItemStack(weapons[second]));
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, bag);
    }

    private static void give(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack) && !stack.isEmpty())
            player.dropItem(stack, false);
    }
}
