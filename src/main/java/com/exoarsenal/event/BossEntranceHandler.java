package com.exoarsenal.event;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityExcavator;
import com.exoarsenal.entity.EntityWulfrumEye;
import com.exoarsenal.entity.EntityX20Scout;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class BossEntranceHandler {
    private BossEntranceHandler() {}

    @SubscribeEvent
    public static void eggOnBlock(
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickBlock event) {
        net.minecraft.util.math.BlockPos at =
                event.getPos()
                        .offset(
                                event.getFace() == null
                                        ? net.minecraft.util.EnumFacing.UP
                                        : event.getFace());
        useEgg(event, new net.minecraft.util.math.Vec3d(at.getX() + .5, at.getY(), at.getZ() + .5));
    }

    @SubscribeEvent
    public static void eggInAir(
            net.minecraftforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        useEgg(
                event,
                event.getEntityPlayer()
                        .getPositionVector()
                        .add(event.getEntityPlayer().getLookVec().scale(8)));
    }

    // Spawn egg entrance
    private static void useEgg(
            net.minecraftforge.event.entity.player.PlayerInteractEvent event,
            net.minecraft.util.math.Vec3d at) {
        net.minecraft.item.ItemStack stack = event.getItemStack();
        if (!(stack.getItem() instanceof net.minecraft.item.ItemMonsterPlacer)) return;
        net.minecraft.util.ResourceLocation id =
                net.minecraft.item.ItemMonsterPlacer.getNamedIdFrom(stack);
        if (id == null || !ExoArsenal.MODID.equals(id.getResourceDomain())) return;
        String name = id.getResourcePath();
        if (!name.equals("x04_excavator")
                && !name.equals("x20_scout")
                && !name.equals("observer")
                && !name.equals("seer")) return;
        event.setCanceled(true);
        event.setCancellationResult(net.minecraft.util.EnumActionResult.SUCCESS);
        if (event.getWorld().isRemote
                || event.getEntityPlayer().getCooldownTracker().hasCooldown(stack.getItem()))
            return;
        net.minecraft.entity.Entity entity =
                net.minecraft.entity.EntityList.createEntityByIDFromName(id, event.getWorld());
        if (entity == null) return;
        entity.setPosition(at.x, at.y, at.z);
        ((net.minecraft.entity.EntityLiving) entity).setAttackTarget(event.getEntityPlayer());
        if (entity instanceof EntityExcavator) ((EntityExcavator) entity).prepareEntrance();
        else if (entity instanceof EntityX20Scout) ((EntityX20Scout) entity).prepareEntrance();
        else ((EntityWulfrumEye) entity).prepareEntrance();
        if (event.getWorld().spawnEntity(entity)) {
            event.getEntityPlayer().getCooldownTracker().setCooldown(stack.getItem(), 20);
            if (!event.getEntityPlayer().isCreative()) stack.shrink(1);
        }
    }

    @SubscribeEvent
    public static void enteringWorld(LivingUpdateEvent event) {
        if (event.getEntity().world.isRemote || event.getEntity().ticksExisted > 1) return;
        if (event.getEntity() instanceof EntityExcavator)
            ((EntityExcavator) event.getEntity()).prepareEntrance();
        else if (event.getEntity() instanceof EntityX20Scout)
            ((EntityX20Scout) event.getEntity()).prepareEntrance();
        else if (event.getEntity() instanceof EntityWulfrumEye)
            ((EntityWulfrumEye) event.getEntity()).prepareEntrance();
    }
}
