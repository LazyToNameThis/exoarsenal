package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.*;
import com.exoarsenal.client.render.WulfrumRayRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class WulfrumArrivalVisuals {
    @SubscribeEvent
    public static void title(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null || mc.gameSettings.hideGUI) return;
        for (Entity e : mc.world.loadedEntityList)
            if (e instanceof EntityDraedon && e.getDistanceSq(mc.player) < 192 * 192) {
                float age = ((EntityDraedon) e).titleAge() + event.getPartialTicks(),
                        alpha = WulfrumArrival.title(age);
                if (alpha > 0) {
                    EncounterTitle.draw(
                            SeerObserverIntro.TITLE,
                            SeerObserverIntro.NAME,
                            event.getResolution().getScaledWidth(),
                            event.getResolution().getScaledHeight(),
                            alpha,
                            age - 72);
                    return;
                }
            }
    }

    @SubscribeEvent
    public static void beams(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        Entity camera = mc.getRenderViewEntity();
        if (mc.world == null || camera == null) return;
        float partial = event.getPartialTicks();
        double x = camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partial,
                y = camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partial,
                z = camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partial;
        GlStateManager.pushMatrix();
        GlStateManager.translate(-x, -y, -z);
        WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(770, 1);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Entity e : mc.world.loadedEntityList)
            if (e instanceof EntityDraedon) {
                NBTTagCompound arrivals = ((EntityDraedon) e).arrivals();
                for (String key : arrivals.getKeySet()) {
                    NBTTagCompound a = arrivals.getCompoundTag(key);
                    float age = a.getInteger("Age") + partial,
                            strength = WulfrumArrival.strength(age);
                    if (strength <= 0) continue;
                    Vec3d base =
                            new Vec3d(a.getDouble("X"), a.getDouble("Y") - 3, a.getDouble("Z"));
                    double floor = WulfrumArrival.beamBottom(age);
                    WulfrumRayRenderer.tube(
                            b,
                            base.addVector(0, floor, 0),
                            base.addVector(0, 64, 0),
                            .18 * strength,
                            0xD8FFE7,
                            strength * .85F);
                    WulfrumRayRenderer.tube(
                            b,
                            base.addVector(0, floor, 0),
                            base.addVector(0, 64, 0),
                            1.1 * strength,
                            0x50EF9B,
                            strength * .17F);
                    WulfrumRayRenderer.tube(
                            b,
                            base.addVector(0, floor, 0),
                            base.addVector(0, 64, 0),
                            2.1 * strength,
                            0x22AD68,
                            strength * .06F);
                    for (int strand = 0; strand < 3; strand++) {
                        Vec3d old = null;
                        for (int step = 0; step <= 64; step++) {
                            double h = floor + (64 - floor) * step / 64,
                                    angle = h * .35 + age * .12 + strand * Math.PI * 2 / 3;
                            Vec3d p =
                                    base.addVector(
                                            Math.cos(angle) * 1.65, h, Math.sin(angle) * 1.65);
                            if (old != null)
                                WulfrumRayRenderer.tube(b, old, p, .04, 0x8DFFC1, strength * .65F);
                            old = p;
                        }
                    }
                    if (age > 14)
                        for (int ring = 0; ring < 2; ring++) {
                            double radius = 2.4 + ring * .7 + .15 * Math.sin(age * .15);
                            for (int i = 0; i < 48; i++) {
                                double u = i * Math.PI / 24, v = (i + 1) * Math.PI / 24;
                                WulfrumRayRenderer.tube(
                                        b,
                                        base.addVector(
                                                Math.cos(u) * radius, .12, Math.sin(u) * radius),
                                        base.addVector(
                                                Math.cos(v) * radius, .12, Math.sin(v) * radius),
                                        .035,
                                        0xA3FFD0,
                                        strength * .6F);
                            }
                        }
                }
            }
        Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }
}
