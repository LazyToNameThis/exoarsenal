package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.Vec3d;

final class BrawlerIntro {
    private final EntityBrawler b;
    private Vec3d origin = Vec3d.ZERO;

    BrawlerIntro(EntityBrawler boss) {
        b = boss;
    }

    void tick(EntityLivingBase target) {
        int t = b.introTick();
        if (t == 1) {
            net.minecraft.util.math.BlockPos floor =
                    new net.minecraft.util.math.BlockPos(b.posX, b.posY, b.posZ);
            for (int n = 0;
                    n < 96 && !b.world.getBlockState(floor.down()).getMaterial().isSolid();
                    n++) floor = floor.down();
            origin = new Vec3d(b.posX, floor.getY() + .2, b.posZ);
        }
        if (t < 26) {
            b.motionX = b.motionY = b.motionZ = 0;
        } else if (t < 51) b.steer(origin.addVector(0, 16, 0), 1.7);
        else if (t < 96) {
            double angle = (t - 51) * Math.PI * 2 / 45;
            b.steer(origin.addVector(Math.sin(angle) * 8, 16 + Math.cos(angle) * 8, 0), 2.3);
        } else if (t < 119) b.steer(origin, 2.6);
        if (t == 118) {
            EntityBrawlerEffect ring =
                    new EntityBrawlerEffect(
                            b, EntityBrawlerEffect.RING, origin, Vec3d.ZERO, 0, 24, 11, 0);
            b.world.spawnEntity(ring);
            b.world.playSound(
                    null,
                    b.getPosition(),
                    net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE,
                    net.minecraft.util.SoundCategory.HOSTILE,
                    1.4F,
                    .65F);
        }
        if (t >= 119 && t < 140) b.steer(target.getPositionVector().addVector(0, 5, -9), 1.5);
        if (t >= 140 && t <= 160)
            b.introFistTarget(target.getPositionEyes(1).add(target.getLookVec().scale(.8)));
        if (t >= 160 && t < 171) {
            b.motionX = b.motionY = b.motionZ = 0;
            return;
        }
        b.introTick(t >= 220 ? 0 : t + 1);
    }
}
