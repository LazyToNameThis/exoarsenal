package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.expedition.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import static com.scapeandrun.frostbite.expedition.WildSpecies.*;

public final class WildRender extends Render<EntityWildMob> {
    public WildRender(RenderManager manager) {
        super(manager);
        shadowSize = .45F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityWildMob entity) {
        return null;
    }

    private static void part(WildSpecies s, String part) {
        int texture =
                s.shape == Shape.PLANT
                        ? 9
                        : s.shape == Shape.SLIME
                                ? 8
                                : s.shape == Shape.BEETLE
                                                || s.shape == Shape.HORNET
                                                || s.shape == Shape.CRAB
                                                || s.shape == Shape.SHELL
                                        ? 6
                                        : 7;
        ExpeditionRender.mesh(WildModels.part(s, part), texture);
    }

    @Override
    public void doRender(EntityWildMob e, double x, double y, double z, float yaw, float partial) {
        WildSpecies s = e.species();
        float time = e.ticksExisted + partial, walk = e.limbSwing, amount = e.limbSwingAmount;
        double ex = e.lastTickPosX + (e.posX - e.lastTickPosX) * partial,
                ey = e.lastTickPosY + (e.posY - e.lastTickPosY) * partial,
                ez = e.lastTickPosZ + (e.posZ - e.lastTickPosZ) * partial;
        if (s.shape == Shape.WORM) {
            for (int i = 0; i < e.segments.length; i++) {
                if (e.segments[i] == null) continue;
                Vec3d p =
                        e.previousSegments[i] == null
                                ? e.segments[i]
                                : e.previousSegments[i].add(
                                        e.segments[i]
                                                .subtract(e.previousSegments[i])
                                                .scale(partial));
                Vec3d ahead = i == 0 ? new Vec3d(ex, ey, ez) : e.segments[i - 1];
                Vec3d d = ahead.subtract(p);
                GlStateManager.pushMatrix();
                GlStateManager.translate(x + p.x - ex, y + p.y - ey + .2, z + p.z - ez);
                GlStateManager.rotate((float) -Math.toDegrees(Math.atan2(d.x, d.z)), 0, 1, 0);
                GlStateManager.rotate(
                        (float) Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z))),
                        1,
                        0,
                        0);
                float scale = 1 - i * .055F;
                GlStateManager.scale(scale, scale, .9);
                part(s, "segment");
                GlStateManager.popMatrix();
            }
        }
        if (s.shape == Shape.PLANT && e.root().distanceSq(ex, ey, ez) < 100) {
            Vec3d root = new Vec3d(e.root()).addVector(.5, 1, .5),
                    head = new Vec3d(ex, ey + .3, ez);
            Vec3d previous = root;
            for (int i = 1; i <= 8; i++) {
                double f = i / 8D;
                Vec3d next =
                        root.add(head.subtract(root).scale(f))
                                .addVector(0, -Math.sin(f * Math.PI) * .6, 0);
                link(s, previous, next, x - ex, y - ey, z - ez);
                previous = next;
            }
        }
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(180 - yaw, 0, 1, 0);
        if (s == MOTHER_SLIME) GlStateManager.scale(1.6, 1.5, 1.6);
        if (s == BABY_SLIME) GlStateManager.scale(.6, .6, .6);
        if (s.shape == Shape.SLIME) {
            double stretch = e.onGround ? 1 + Math.sin(time * .3) * .08 : 1.15;
            GlStateManager.scale(1 / stretch, stretch, 1 / stretch);
        }
        if (s.shape == Shape.WORM || s.shape == Shape.FISH)
            GlStateManager.rotate(-e.rotationPitch, 1, 0, 0);
        part(s, "body");
        if (s.shape == Shape.BAT || s.shape == Shape.HORNET)
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .1, s == DEMON ? 1 : .4, 0);
                GlStateManager.scale(side, 1, 1);
                GlStateManager.rotate(
                        (float) Math.sin(time * (s.shape == Shape.HORNET && s != DEMON ? 1.8 : .7))
                                * 45,
                        0,
                        0,
                        1);
                part(s, "wing");
                GlStateManager.popMatrix();
            }
        if (s.shape == Shape.HUMAN || s == DEMON) {
            float attack = e.attackTicks();
            float stroke =
                    attack == 0 ? 0 : attack > 8 ? -35 * (26 - attack) / 18 : 85 * (attack / 8);
            if (e instanceof EntityDeepMob) {
                int cast = ((EntityDeepMob) e).castTicks();
                if (cast > 0)
                    stroke = cast > 8 ? 75 * (1 - (cast - 8) * (cast - 8) / 144F) : 75 * cast / 8F;
            }
            for (int side : new int[] {-1, 1}) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .13, .57, 0);
                GlStateManager.rotate(
                        (float) Math.sin(walk * .7 + side * Math.PI * .5) * amount * 35, 1, 0, 0);
                part(s, "leg");
                GlStateManager.popMatrix();
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .25, 1.1, 0);
                GlStateManager.rotate(
                        side == -1 ? stroke : (float) Math.sin(walk * .7) * amount * 20, 1, 0, 0);
                part(s, "arm");
                GlStateManager.popMatrix();
            }
        }
        if (s.shape == Shape.BEETLE
                || s.shape == Shape.CRAB
                || s.shape == Shape.SPIDER
                || s.shape == Shape.LIZARD) {
            int legs = s.shape == Shape.SPIDER ? 4 : s.shape == Shape.LIZARD ? 2 : 3;
            for (int side : new int[] {-1, 1})
                for (int i = 0; i < legs; i++) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(side * .15, .2, (i - (legs - 1) * .5) * .16);
                    GlStateManager.scale(side, 1, 1);
                    GlStateManager.rotate(
                            (i - (legs - 1) * .5F) * 22
                                    + (float) Math.sin(walk * 1.2 + i * 2 + side) * amount * 35,
                            0,
                            1,
                            0);
                    part(s, "leg");
                    GlStateManager.popMatrix();
                }
        }
        if (s.shape == Shape.LIZARD || s.shape == Shape.FISH) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, .2, .3);
            GlStateManager.rotate((float) Math.sin(time * .3) * 18, 0, 1, 0);
            part(s, "tail");
            GlStateManager.popMatrix();
        }
        if (s.shape == Shape.PLANT) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, .23, 0);
            GlStateManager.rotate(e.attackTicks() > 8 ? 30 : e.attackTicks() > 0 ? 5 : 12, 1, 0, 0);
            part(s, "jaw");
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        super.doRender(e, x, y, z, yaw, partial);
    }

    private static void link(WildSpecies s, Vec3d from, Vec3d to, double x, double y, double z) {
        Vec3d d = to.subtract(from);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x + from.x, y + from.y, z + from.z);
        GlStateManager.rotate((float) -Math.toDegrees(Math.atan2(d.x, d.z)), 0, 1, 0);
        GlStateManager.rotate(
                (float) Math.toDegrees(Math.atan2(Math.sqrt(d.x * d.x + d.z * d.z), d.y)), 1, 0, 0);
        GlStateManager.scale(1, d.lengthVector(), 1);
        part(s, "stem");
        GlStateManager.popMatrix();
    }

    public static final class Bolt extends Render<EntityWildBolt> {
        public Bolt(RenderManager m) {
            super(m);
        }

        protected ResourceLocation getEntityTexture(EntityWildBolt e) {
            return null;
        }

        public void doRender(
                EntityWildBolt e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(e.rotationYaw, 0, 1, 0);
            GlStateManager.rotate(-e.rotationPitch + 90, 1, 0, 0);
            GlStateManager.scale(.4, .4, .4);
            ExpeditionRender.mesh(
                    e.type() == 4
                            ? DeepModels.item("fire_projectile")
                            : e.type() == 3
                                    ? GeologyClient.material(GeologyContent.Ore.AMBER.ordinal())
                                    : e.type() == 2
                                            ? PrebossModels.item("fallen_star")
                                            : e.type() == 1
                                                    ? GeologyClient.material(
                                                            GeologyContent.Ore.SAPPHIRE.ordinal())
                                                    : WildModels.material("stinger"));
            GlStateManager.popMatrix();
        }
    }
}
