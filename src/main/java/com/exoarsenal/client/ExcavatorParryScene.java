package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class ExcavatorParryScene {
    private static Object world;
    private static long start;
    private static Vec3d contact;
    private static int previousView = -1;

    public static void begin(Vec3d at) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;
        if (previousView < 0) previousView = mc.gameSettings.thirdPersonView;
        mc.gameSettings.thirdPersonView = 1;
        world = mc.world;
        start = mc.world.getTotalWorldTime();
        contact = at;
    }

    private static void end() {
        Minecraft mc = Minecraft.getMinecraft();
        if (previousView >= 0 && mc.gameSettings.thirdPersonView == 1)
            mc.gameSettings.thirdPersonView = previousView;
        previousView = -1;
        contact = null;
        world = null;
    }

    public static float envelope(float age) {
        if (age < 0 || age >= 56) return 0;
        return CombatMotion.smooth(age / 4) * (1 - CombatMotion.smooth((age - 36) / 20));
    }

    @SubscribeEvent
    public static void tick(
            net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase == net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END
                && contact != null) weight(0);
    }

    private static float weight(float partial) {
        Minecraft mc = Minecraft.getMinecraft();
        if (contact == null) return 0;
        if (mc.world != world
                || mc.player == null
                || mc.currentScreen != null
                || mc.player.isDead) {
            end();
            return 0;
        }
        float age = mc.world.getTotalWorldTime() - start + partial;
        if (age >= 56) {
            end();
            return 0;
        }
        return envelope(age);
    }

    @SubscribeEvent
    public static void camera(EntityViewRenderEvent.CameraSetup event) {
        float blend = weight((float) event.getRenderPartialTicks());
        if (blend == 0) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.gameSettings.thirdPersonView == 1) {
            event.setYaw(event.getYaw() + 28 * blend);
            event.setPitch(event.getPitch() + (8 - event.getPitch()) * blend * .7F);
        }
    }

    @SubscribeEvent
    public static void fov(FOVUpdateEvent event) {
        float blend = weight(0);
        if (blend > 0) event.setNewfov(event.getNewfov() * (1 - .075F * blend));
    }

    @SubscribeEvent
    public static void overlay(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        float blend = weight(event.getPartialTicks());
        Minecraft mc = Minecraft.getMinecraft();
        if (blend == 0 || mc.gameSettings.hideGUI) return;
        int w = event.getResolution().getScaledWidth(),
                h = event.getResolution().getScaledHeight(),
                bar = Math.round(h * .045F * blend),
                color = ((int) (190 * blend)) << 24;
        Gui.drawRect(0, 0, w, bar, color);
        Gui.drawRect(0, h - bar, w, h, color);
    }

    private ExcavatorParryScene() {}
}
