package com.scapeandrun.frostbite.item;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;

public final class ItemDesertScourgeBag extends Item {
    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand h) {
        ItemStack s = p.getHeldItem(h);
        if (!w.isRemote) {
            s.shrink(1);
            for (ItemStack drop :
                    com.scapeandrun.frostbite.expedition.ScourgeLoot.roll(w.rand, true))
                give(p, drop);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, s);
    }

    private static void give(EntityPlayer p, ItemStack s) {
        if (!p.inventory.addItemStackToInventory(s)) p.dropItem(s, false);
    }
}
