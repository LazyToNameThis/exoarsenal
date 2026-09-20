package com.scapeandrun.frostbite.client.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.World;

final class FrostParticle extends Particle {
    private final TextureAtlasSprite[] frames;
    private final float initialScale;
    private final int kind;

    FrostParticle(
            World world,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            int kind,
            TextureAtlasSprite[] frames) {
        super(world, x, y, z);
        this.frames = frames;
        this.kind = kind;
        motionX = vx;
        motionY = vy;
        motionZ = vz;
        particleMaxAge = kind == 0 ? 8 : kind == 1 ? 22 : 28;
        initialScale = kind == 0 ? 5F : kind == 1 ? .9F : 2.2F;
        particleScale = initialScale;
        particleGravity = kind == 1 ? .045F : 0;
        particleRed = particleGreen = particleBlue = 1;
        setParticleTexture(frames[0]);
        if (kind == 1) {
            particleAngle = (float) (rand.nextDouble() * Math.PI * 2);
            prevParticleAngle = particleAngle;
        }
    }

    @Override
    public int getFXLayer() {
        return 1;
    }

    @Override
    public int getBrightnessForRender(float partial) {
        return 0xF000F0;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        float life = particleAge / (float) particleMaxAge;
        particleAlpha = Math.max(0, 1 - life * life);
        particleScale =
                initialScale * (kind == 2 ? 1 + life * .8F : kind == 0 ? 1 + life * .35F : 1);
        prevParticleAngle = particleAngle;
        if (kind == 1) particleAngle += .11F;
        setParticleTexture(
                frames[Math.min(frames.length - 1, particleAge * frames.length / particleMaxAge)]);
        if (kind == 2) {
            motionX *= .92;
            motionZ *= .92;
            motionY += .001;
        }
    }
}
