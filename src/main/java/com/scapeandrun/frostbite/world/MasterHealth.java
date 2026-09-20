package com.scapeandrun.frostbite.world;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.*;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class MasterHealth {
    private static final UUID ID = UUID.fromString("54d40dc2-84ca-4b5b-97b5-d14c9b07dfe4");

    @SubscribeEvent
    public static void tick(LivingEvent.LivingUpdateEvent event) {
        if (!(event.getEntityLiving() instanceof EntityLiving)) return;
        EntityLiving mob = (EntityLiving) event.getEntityLiving();
        if (mob.world.isRemote
                || mob instanceof EntityScoutShard
                || mob instanceof EntityScoutHardpoint) return;
        if (!mob.getEntityData().hasKey("FrostbiteMasterSnapshot"))
            mob.getEntityData()
                    .setBoolean(
                            "FrostbiteMasterSnapshot",
                            FrostbiteWorldSettings.get(mob.world).isMaster());
        if (!mob.getEntityData().getBoolean("FrostbiteMasterSnapshot")) return;
        IAttributeInstance health = mob.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH);
        if (health == null || health.getModifier(ID) != null) return;
        float fraction = mob.getHealth() / Math.max(1, mob.getMaxHealth());
        health.applyModifier(new AttributeModifier(ID, "Frostbite Master health", .5, 2));
        mob.setHealth(mob.getMaxHealth() * fraction);
    }
}
