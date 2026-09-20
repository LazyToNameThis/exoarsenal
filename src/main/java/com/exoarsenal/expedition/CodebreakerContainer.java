package com.exoarsenal.expedition;

import net.minecraft.inventory.Container;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public final class CodebreakerContainer extends Container {
    public final BlockPos pos;

    public CodebreakerContainer(BlockPos pos) {
        this.pos = pos;
    }

    @Override
    public boolean canInteractWith(EntityPlayer p) {
        return p.getDistanceSq(pos) <= 64
                && p.world.getBlockState(pos).getBlock() == ExpeditionContent.CODEBREAKER_BASE;
    }
}
