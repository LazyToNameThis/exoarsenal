package com.exoarsenal.client.particle;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityBrawler;
import com.exoarsenal.entity.EntityBrawlerEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.entity.Entity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class BrawlerParticles {
    private static final class Spark extends Particle {
        private final float scale;

        Spark(World world, Vec3d at, Vec3d velocity, boolean hot) {
            super(world, at.x, at.y, at.z);
            motionX = velocity.x;
            motionY = velocity.y;
            motionZ = velocity.z;
            particleMaxAge = 10 + rand.nextInt(9);
            particleGravity = .16F;
            scale = particleScale = .28F + rand.nextFloat() * .25F;
            particleRed = hot ? 1 : .46F;
            particleGreen = hot ? .83F : 1;
            particleBlue = hot ? .40F : .66F;
            setParticleTextureIndex(0);
        }

        @Override
        public int getBrightnessForRender(float partial) {
            return 0xF000F0;
        }

        @Override
        public void onUpdate() {
            super.onUpdate();
            float f = particleAge / (float) particleMaxAge;
            particleAlpha = 1 - f * f;
            particleScale = scale * (1 - f * .65F);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (event.phase != TickEvent.Phase.END
                || mc.world == null
                || mc.player == null
                || mc.isGamePaused()) return;
        int budget =
                mc.gameSettings.particleSetting == 2
                        ? 8
                        : mc.gameSettings.particleSetting == 1 ? 24 : 48;
        for (Entity raw : mc.world.loadedEntityList) {
            if (budget <= 0) break;
            if (raw.getDistanceSq(mc.player) > 4096) continue;
            if (raw instanceof EntityBrawler) {
                EntityBrawler boss = (EntityBrawler) raw;
                if (boss.deathTime == 0 && boss.jetThrust(0) > .1 && boss.ticksExisted % 2 == 0) {
                    for (int side : new int[] {-1, 1}) {
                        if (budget <= 0) break;
                        Vec3d nozzle = boss.localToWorld(new Vec3d(side * 2.05, -3.4, -3));
                        mc.effectRenderer.addEffect(
                                new Spark(
                                        mc.world,
                                        nozzle,
                                        new Vec3d(-boss.motionX * .15, -.4, -boss.motionZ * .15),
                                        boss.aerialRecovery() > 0));
                        budget--;
                    }
                }
                if (boss.clashTick() > 0 && boss.ticksExisted % 2 == 0) {
                    Vec3d at = boss.localToWorld(boss.hand(boss.clashArm(), 0));
                    for (int n = 0; n < Math.min(4, budget); n++) {
                        emit(mc, at, true, .22);
                        budget--;
                    }
                }
                continue;
            }
            if (!(raw instanceof EntityBrawlerEffect)) continue;
            EntityBrawlerEffect effect = (EntityBrawlerEffect) raw;
            if (!effect.active() || effect.ticksExisted % 2 != 0) continue;
            Vec3d at = effect.getPositionVector();
            int kind = effect.kind();
            if (kind == EntityBrawlerEffect.RING) {
                double radius =
                        effect.radius()
                                * Math.min(
                                        1,
                                        (effect.age() - effect.warning() + 1D)
                                                / Math.max(1, effect.life() - effect.warning()));
                for (int n = 0; n < Math.min(5, budget); n++) {
                    double angle = effect.world.rand.nextDouble() * Math.PI * 2;
                    Vec3d point =
                            at.addVector(Math.cos(angle) * radius, .08, Math.sin(angle) * radius);
                    mc.world.spawnParticle(
                            EnumParticleTypes.SMOKE_NORMAL,
                            point.x,
                            point.y,
                            point.z,
                            Math.cos(angle) * .025,
                            .04,
                            Math.sin(angle) * .025);
                    emit(mc, point, false, .09);
                    budget -= 2;
                }
            } else if (kind == EntityBrawlerEffect.CUT
                    || kind == EntityBrawlerEffect.BLADE
                    || kind == EntityBrawlerEffect.SHARD) {
                emit(mc, at, false, .04);
                budget--;
            } else if (kind == EntityBrawlerEffect.FAULT) {
                Vec3d point =
                        at.add(effect.end().subtract(at).scale(effect.world.rand.nextDouble()));
                emit(mc, point, true, .14);
                budget--;
            }
        }
    }

    private static void emit(Minecraft mc, Vec3d at, boolean hot, double speed) {
        double angle = mc.world.rand.nextDouble() * Math.PI * 2;
        mc.effectRenderer.addEffect(
                new Spark(
                        mc.world,
                        at,
                        new Vec3d(
                                Math.cos(angle) * speed,
                                .03 + mc.world.rand.nextDouble() * speed,
                                Math.sin(angle) * speed),
                        hot));
    }

    private BrawlerParticles() {}
}
