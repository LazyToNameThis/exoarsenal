package com.scapeandrun.frostbite.client.particle;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ScoutParticles {
    private static final TextureAtlasSprite[] FLASH = new TextureAtlasSprite[4],
            SHARD = new TextureAtlasSprite[1],
            WISP = new TextureAtlasSprite[1];
    private static long budgetTick = Long.MIN_VALUE;
    private static int emitted;

    private ScoutParticles() {}

    @SubscribeEvent
    public static void stitch(TextureStitchEvent.Pre event) {
        for (int i = 0; i < 4; i++)
            FLASH[i] =
                    event.getMap()
                            .registerSprite(
                                    new ResourceLocation(
                                            Frostbite.MODID, "particle/ice_flash_" + i));
        SHARD[0] =
                event.getMap()
                        .registerSprite(
                                new ResourceLocation(Frostbite.MODID, "particle/ice_shard"));
        WISP[0] =
                event.getMap()
                        .registerSprite(
                                new ResourceLocation(Frostbite.MODID, "particle/frost_wisp"));
    }

    public static void burst(int type, Vec3d at) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null
                || mc.player == null
                || FLASH[0] == null
                || mc.player.getDistanceSq(at.x, at.y, at.z) > 4096) return;
        long now = mc.world.getTotalWorldTime();
        if (budgetTick != now) {
            budgetTick = now;
            emitted = 0;
        }
        int allowance =
                mc.gameSettings.particleSetting == 2
                        ? 12
                        : mc.gameSettings.particleSetting == 1 ? 36 : 72;
        int count =
                Math.min(
                        allowance - emitted, type == 1 ? 32 : type == 2 ? 22 : type == 3 ? 24 : 14);
        if (count <= 0) return;
        emitted += count;
        mc.effectRenderer.addEffect(
                new FrostParticle(
                        mc.world,
                        at.x,
                        at.y,
                        at.z,
                        0,
                        0,
                        0,
                        type == 3 ? 2 : 0,
                        type == 3 ? WISP : FLASH));
        for (int i = 1; i < count; i++) {
            double angle = mc.world.rand.nextDouble() * Math.PI * 2,
                    speed = .08 + mc.world.rand.nextDouble() * (type == 1 ? .35 : .2);
            boolean mist = type == 3 || i % 4 == 0;
            mc.effectRenderer.addEffect(
                    new FrostParticle(
                            mc.world,
                            at.x,
                            at.y,
                            at.z,
                            Math.cos(angle) * speed,
                            .04 + mc.world.rand.nextDouble() * .22,
                            Math.sin(angle) * speed,
                            mist ? 2 : 1,
                            mist ? WISP : SHARD));
        }
    }
}
