package com.scapeandrun.frostbite.expedition;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class SeaEvents {
    private static final UUID SPEED = UUID.fromString("86a8d9f2-ce5d-4816-a7c0-7f812617061a");

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer p = event.player;
        if (event.phase != TickEvent.Phase.END || p.world.isRemote) return;
        boolean full = VictideArmor.full(p),
                water = p.isInWater(),
                shield = AccessoryInventory.has(p, SeaContent.SHIELD),
                pearl = AccessoryInventory.has(p, SeaContent.GIANT_PEARL);
        double speed =
                p.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == SeaContent.LEGS
                        ? (water ? .3 : .08)
                        : 0;
        if (full && shield) speed += .1;
        IAttributeInstance movement = p.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AttributeModifier old = movement.getModifier(SPEED);
        if (old != null && old.getAmount() != speed) {
            movement.removeModifier(SPEED);
            old = null;
        }
        if (speed > 0 && old == null)
            movement.applyModifier(
                    new AttributeModifier(SPEED, "Victide mobility", speed, 2).setSaved(false));
        if (full && water) {
            if (p.ticksExisted % 20 == 0) p.heal(.75F);
            if (p.moveForward != 0 || p.moveStrafing != 0) {
                Vec3d look = p.getLookVec();
                p.motionX += look.x * .012 * p.moveForward;
                p.motionZ += look.z * .012 * p.moveForward;
                if (!p.isSneaking()) p.motionY += look.y * .012 * p.moveForward;
            }
        }
        if (p.ticksExisted % 40 == 0) {
            if (full && shield) p.heal(.5F);
            if (water && pearl) p.heal(.5F);
        }
        if (VictideArmor.summoner(p)
                && p.ticksExisted % 40 == 0
                && p.world
                        .getEntitiesWithinAABB(
                                EntityExpeditionMinion.class,
                                p.getEntityBoundingBox().grow(64),
                                m -> m.variant() == 3 && p.getUniqueID().equals(m.owner()))
                        .isEmpty())
            p.world.spawnEntity(new EntityExpeditionMinion(p.world, p, 3, 0));
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntityLiving().world.isRemote) return;
        if (event.getEntityLiving() instanceof EntityPlayer) {
            EntityPlayer p = (EntityPlayer) event.getEntityLiving();
            if (p.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == SeaContent.CHEST)
                event.setAmount(event.getAmount() * (p.isInWater() ? .85F : .95F));
            if (AccessoryInventory.has(p, SeaContent.PENDANT)
                    && p.world.getTotalWorldTime()
                            >= p.getEntityData().getLong("FrostbitePendantReady")) {
                p.getEntityData()
                        .setLong("FrostbitePendantReady", p.world.getTotalWorldTime() + 100);
                for (int angle = 0; angle < 360; angle += 60)
                    ExpeditionItem.fire(p, EntityExpeditionShot.WATER, 4, 1.1F, angle);
            }
        }
        Entity source = event.getSource().getTrueSource();
        if (!(source instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) source;
        int style = VictideArmor.style(p);
        Entity immediate = event.getSource().getImmediateSource();
        boolean returning =
                immediate instanceof EntityExpeditionShot
                        && (((EntityExpeditionShot) immediate).type()
                                        == EntityExpeditionShot.FISHBONE
                                || ((EntityExpeditionShot) immediate).type()
                                        == EntityExpeditionShot.SLICER);
        boolean magic =
                event.getSource().isMagicDamage()
                        || immediate instanceof EntityExpeditionShot
                                && ((EntityExpeditionShot) immediate).damageClass() == 1;
        boolean summon =
                immediate instanceof EntityExpeditionMinion
                        || immediate instanceof EntityExpeditionShot
                                && ((EntityExpeditionShot) immediate).damageClass() == 2
                        || immediate instanceof EntityPrebossShot
                                && ((EntityPrebossShot) immediate).summonSource();
        boolean thrownMelee = false;
        if (immediate instanceof EntityPrebossShot) {
            PrebossItem.Kind kind = ((EntityPrebossShot) immediate).kind();
            returning |= kind == PrebossItem.Kind.CRYSTALLINE || kind == PrebossItem.Kind.KNIFE;
            thrownMelee =
                    !summon
                            && (kind == PrebossItem.Kind.WOOD_BOOMERANG
                                    || kind == PrebossItem.Kind.ENCHANTED_BOOMERANG
                                    || kind == PrebossItem.Kind.STORM_SPEAR);
        }
        boolean shell =
                immediate instanceof EntityExpeditionShot
                        && ((EntityExpeditionShot) immediate).type() == EntityExpeditionShot.SHELL;
        if (summon
                && p.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == PrebossContent.FLINX_COAT) event.setAmount(event.getAmount() * 1.05F);
        if (summon && AccessoryInventory.has(p, ExpeditionContent.BATTERY))
            event.setAmount(event.getAmount() * 1.1F);
        boolean boosted =
                !shell
                        && (style == 0
                                        && (!event.getSource().isProjectile() || thrownMelee)
                                        && !summon
                                        && !magic
                                || style == 1
                                        && event.getSource().isProjectile()
                                        && !magic
                                        && !returning
                                        && !summon
                                        && !thrownMelee
                                || style == 2 && magic
                                || style == 3 && summon
                                || style == 4 && returning);
        if (boosted)
            event.setAmount(
                    event.getAmount()
                            * (1
                                    + (style == 3 ? .1F : .05F)
                                    + (VictideArmor.full(p) && p.isInWater() ? .1F : 0)));

        if (VictideArmor.full(p)
                && !(immediate instanceof EntityExpeditionShot
                        && ((EntityExpeditionShot) immediate).type() == EntityExpeditionShot.SHELL)
                && p.getRNG().nextInt(10) == 0
                && p.world.getTotalWorldTime() >= p.getEntityData().getLong("VictideShellReady")) {
            p.getEntityData().setLong("VictideShellReady", p.world.getTotalWorldTime() + 10);
            ExpeditionItem.fire(p, EntityExpeditionShot.SHELL, 4, 1.2F, 0);
        }
    }
}
