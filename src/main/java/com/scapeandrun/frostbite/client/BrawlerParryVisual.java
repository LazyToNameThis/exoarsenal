package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityBrawler;
import com.scapeandrun.frostbite.entity.BrawlerClash;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.FOVUpdateEvent;
import net.minecraft.util.math.Vec3d;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class BrawlerParryVisual {
    private static int oldCamera = -1;
    private static EntityBrawler current;
    private static int lastTick;

    public static float framing(float tick) {
        return CombatMotion.smooth(tick / 8) * (1 - CombatMotion.smooth((tick - 118) / 14));
    }

    public static EntityBrawler encounter(EntityPlayer p) {
        if (p == null || p.world == null) return null;
        for (EntityBrawler b :
                p.world.getEntitiesWithinAABB(
                        EntityBrawler.class, p.getEntityBoundingBox().grow(18)))
            if (b.isEntityAlive() && b.clashTick() > 0 && b.clashPlayer() == p.getEntityId())
                return b;
        return null;
    }

    public static float brace(EntityPlayer p, float partial) {
        EntityBrawler b = encounter(p);
        return b == null ? 0 : (float) BrawlerClash.brace(b.clashTick() + partial);
    }

    public static float release(EntityPlayer p, float partial) {
        EntityBrawler b = encounter(p);
        if (b == null) return 0;
        double t = b.clashTick() + partial;
        return (float)
                (com.scapeandrun.frostbite.entity.BrawlerScore.smooth((t - 88) / 12)
                        * (1
                                - com.scapeandrun.frostbite.entity.BrawlerScore.smooth(
                                        (t - 112) / 18)));
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        EntityBrawler b = mc.currentScreen == null ? encounter(mc.player) : null;
        if (b != null) {
            if (oldCamera < 0) {
                oldCamera = mc.gameSettings.thirdPersonView;
                mc.gameSettings.thirdPersonView = 1;
            }
            int tick = b.clashTick();
            if (current != b || tick < lastTick) lastTick = 0;
            Vec3d at = b.localToWorld(b.hand(b.clashArm(), 0)),
                    direction =
                            b.getPositionVector()
                                    .subtract(mc.player.getPositionVector())
                                    .normalize();
            if (lastTick == 0 || tick >= 100 && lastTick < 100 || tick >= 118 && lastTick < 118)
                ExcavatorParryEffects.add(at, direction, 0);
            if (tick < 100 && tick / 12 != lastTick / 12)
                ExcavatorParryEffects.add(at, direction, 0);
            current = b;
            lastTick = tick;
        } else {
            if (oldCamera >= 0 && mc.gameSettings.thirdPersonView == 1)
                mc.gameSettings.thirdPersonView = oldCamera;
            oldCamera = -1;
            current = null;
            lastTick = 0;
        }
    }

    @SubscribeEvent
    public static void camera(EntityViewRenderEvent.CameraSetup event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityBrawler b = encounter(mc.player);
        if (b == null || mc.currentScreen != null || mc.gameSettings.thirdPersonView != 1) return;
        float age = b.clashTick() + (float) event.getRenderPartialTicks(), weight = framing(age);
        float side = b.clashArm() == 0 ? 1 : -1;
        event.setYaw(event.getYaw() + side * 32 * weight);
        event.setPitch(event.getPitch() + (7 - event.getPitch()) * .75F * weight);
        if (mc.gameSettings.viewBobbing && age < 100)
            event.setRoll(event.getRoll() + (float) Math.sin(age * 1.7) * .3F * weight);
    }

    @SubscribeEvent
    public static void fov(FOVUpdateEvent event) {
        EntityBrawler b = encounter(event.getEntity());
        if (b != null && Minecraft.getMinecraft().currentScreen == null)
            event.setNewfov(event.getNewfov() * (1 - .08F * framing(b.clashTick())));
    }
}
