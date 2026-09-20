package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.client.render.WulfrumRayRenderer;
import com.scapeandrun.frostbite.entity.ExcavatorParryMotion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;
import java.util.*;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ExcavatorParryEffects {
    private static final class Impact {
        final Vec3d at, direction;
        final long start;
        final int type;

        Impact(Vec3d a, Vec3d d, long s, int t) {
            at = a;
            direction = d;
            start = s;
            type = t;
        }
    }

    private static final List<Impact> impacts = new ArrayList<>();
    private static Object currentWorld;

    public static void add(Vec3d at, Vec3d direction, int type) {
        Minecraft mc = Minecraft.getMinecraft();
        if (currentWorld != mc.world) {
            impacts.clear();
            currentWorld = mc.world;
        }
        if (impacts.size() >= 8) impacts.remove(0);
        impacts.add(new Impact(at, direction.normalize(), mc.world.getTotalWorldTime(), type));
    }

    public static float brace(Entity p, float partial) {
        if (!p.getEntityData().hasKey("ExcavatorParryBrace")) return 0;
        return ExcavatorParryMotion.brace(
                p.world.getTotalWorldTime()
                        - p.getEntityData().getLong("ExcavatorParryBrace")
                        + partial);
    }

    @SubscribeEvent
    public static void camera(EntityViewRenderEvent.CameraSetup e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != currentWorld || mc.player == null || !mc.gameSettings.viewBobbing) return;
        double now = mc.world.getTotalWorldTime() + e.getRenderPartialTicks();
        float impulse = 0;
        for (Impact i : impacts) {
            double age = now - i.start;
            if (age < 0 || age > 12) continue;
            double proximity =
                    Math.max(0, 1 - mc.player.getDistanceSq(i.at.x, i.at.y, i.at.z) / 4096);
            impulse +=
                    (float)
                            (Math.sin(age * 2.1)
                                    * Math.exp(-age * .32)
                                    * proximity
                                    * (i.type == 0 ? 1.2 : .7));
        }
        e.setPitch(e.getPitch() + impulse);
    }

    @SubscribeEvent
    public static void draw(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != currentWorld || mc.getRenderViewEntity() == null) return;
        long now = mc.world.getTotalWorldTime();
        impacts.removeIf(i -> now - i.start > 56 || now < i.start);
        if (impacts.isEmpty()) return;
        Entity camera = mc.getRenderViewEntity();
        float partial = event.getPartialTicks();
        Vec3d eye =
                new Vec3d(
                        camera.lastTickPosX + (camera.posX - camera.lastTickPosX) * partial,
                        camera.lastTickPosY + (camera.posY - camera.lastTickPosY) * partial,
                        camera.lastTickPosZ + (camera.posZ - camera.lastTickPosZ) * partial);
        GlStateManager.pushMatrix();
        GlStateManager.translate(-eye.x, -eye.y, -eye.z);
        WulfrumRayRenderer.begin();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
        for (Impact impact : impacts) {
            double age = now - impact.start + partial;
            if (age < 0 || age > 48) continue;
            float alpha = (float) Math.max(0, 1 - age / 24);
            Vec3d side = impact.direction.crossProduct(new Vec3d(0, 1, 0));
            if (side.lengthSquared() < .01) side = new Vec3d(1, 0, 0);
            side = side.normalize();
            Vec3d up = side.crossProduct(impact.direction).normalize();
            if (impact.type == 0 && age < ExcavatorParryMotion.HITSTOP) {
                for (int k = 0; k < 12; k++) {
                    double life = (age + k * .63) % 7, a = k * 2.39996 + Math.floor(age / 7) * .7;
                    Vec3d ray =
                            side.scale(Math.cos(a))
                                    .add(up.scale(Math.sin(a)))
                                    .add(impact.direction.scale(-.25));
                    Vec3d tip =
                            impact.at
                                    .add(ray.scale(life * .18))
                                    .addVector(0, -life * life * .009, 0);
                    WulfrumRayRenderer.tube(
                            b,
                            tip.subtract(ray.scale(.17)),
                            tip,
                            .015,
                            k % 3 == 0 ? 0xDDFFD4 : 0xFFD78B,
                            (float) (1 - life / 7));
                }
            }
            if (impact.type == 0) {
                for (int k = 0; k < 18; k++) {
                    double a = k * Math.PI * 2 / 18;
                    Vec3d ray = side.scale(Math.cos(a)).add(up.scale(Math.sin(a)));
                    double radius = .4 + age * (.35 + (k % 3) * .08);
                    WulfrumRayRenderer.tube(
                            b,
                            impact.at.add(ray.scale(Math.max(0, radius - 1.4))),
                            impact.at.add(ray.scale(radius)),
                            .025 + (age < 3 ? .04 : 0),
                            k % 3 == 0 ? 0xFFFFFF : 0xA6FFD0,
                            alpha);
                }
                if (age < 3) {
                    WulfrumRayRenderer.tube(
                            b,
                            impact.at.subtract(side.scale(2.6)),
                            impact.at.add(side.scale(2.6)),
                            .15,
                            0xFFFFFF,
                            1);
                    WulfrumRayRenderer.tube(
                            b,
                            impact.at.subtract(up.scale(1.5)),
                            impact.at.add(up.scale(1.5)),
                            .10,
                            0xE2FFF0,
                            1);
                }
            } else
                for (int k = 0; k < 24; k++) {
                    double a = k * Math.PI / 12, c = (k + 1) * Math.PI / 12, r = age * .75;
                    WulfrumRayRenderer.tube(
                            b,
                            impact.at.addVector(Math.cos(a) * r, .15, Math.sin(a) * r),
                            impact.at.addVector(Math.cos(c) * r, .15, Math.sin(c) * r),
                            .07,
                            0xC4CABC,
                            alpha * .5F);
                }
        }
        Tessellator.getInstance().draw();
        WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }

    private ExcavatorParryEffects() {}
}
