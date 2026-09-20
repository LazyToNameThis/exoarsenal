package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

interface WulfrumCoordinationCombat {
    boolean tick(EntityPlayer player);

    void parried(EntityLivingBase actor, EntityPlayer player);

    void finish();
}
