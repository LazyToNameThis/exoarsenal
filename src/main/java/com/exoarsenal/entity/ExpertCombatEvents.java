package com.exoarsenal.entity;

import com.exoarsenal.ExoArsenal;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ExpertCombatEvents {
    private ExpertCombatEvents() {}

    @SubscribeEvent
    public static void damage(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getEntityLiving() instanceof EntityX20Scout
                && ((EntityX20Scout) event.getEntityLiving()).isAmplified())
            event.setAmount(event.getAmount() * .8F);
        if (event.getSource().getTrueSource() instanceof EntityX20Scout
                && ((EntityX20Scout) event.getSource().getTrueSource()).isAmplified())
            event.setAmount(event.getAmount() * 1.25F);
        if (event.getSource().getTrueSource() instanceof EntityX20Scout
                && ((EntityX20Scout) event.getSource().getTrueSource()).isExpert())
            event.setAmount(event.getAmount() * 1.15F);
        else if (event.getSource().getTrueSource() instanceof EntityFrigidRobot
                && ((EntityFrigidRobot) event.getSource().getTrueSource()).isExpert())
            event.setAmount(event.getAmount() * 1.15F);
    }
}
