package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class ScoutBossBar {
    private static final Map<UUID, Trail> TRAILS = new HashMap<>();

    private static final class Trail {
        final float[] value = {1, 1}, last = {1, 1};
        final long[] hit = {0, 0};
        long time = System.nanoTime();

        Trail(float a, float b) {
            value[0] = last[0] = a;
            value[1] = last[1] = b;
        }

        void update(float a, float b) {
            long now = System.nanoTime();
            float dt = Math.min(.1F, (now - time) / 1e9F);
            time = now;
            float[] hp = {a, b};
            for (int i = 0; i < 2; i++) {
                if (hp[i] < last[i]) hit[i] = now;
                if (hp[i] >= value[i]) value[i] = hp[i];
                else if (now - hit[i] > 350000000L)
                    value[i] = Math.max(hp[i], value[i] - dt * .28F);
                last[i] = hp[i];
            }
        }
    }

    private ScoutBossBar() {}

    @SubscribeEvent
    public static void render(RenderGameOverlayEvent.BossInfo event) {
        String name = event.getBossInfo().getName().getUnformattedText();
        boolean
                scout =
                        name.equals("Tundra Trekker")
                                || name.equals("X-05 \"Scout\"")
                                || name.equals("entity.x20_scout.name"),
                eyes = name.equals(SeerObserverIntro.NAME);
        boolean excavator = name.startsWith("X-04 \"Excavator\"");
        boolean brawler = name.equals("X-05 \"Brawler\"");
        if (!scout && !eyes && !excavator && !brawler) return;
        EntityBrawler machine = null;
        Minecraft mc = Minecraft.getMinecraft();
        EntityX20Scout tracked = null;
        EntityWulfrumEye pair = null;
        String id = event.getBossInfo().getUniqueId().toString();
        if (mc.world != null)
            for (Entity e : mc.world.loadedEntityList) {
                if (brawler
                        && e instanceof EntityBrawler
                        && ((EntityBrawler) e).bossBarId().equals(id)) machine = (EntityBrawler) e;
                if (scout
                        && e instanceof EntityX20Scout
                        && ((EntityX20Scout) e).getBossBarId().equals(id))
                    tracked = (EntityX20Scout) e;
                if (eyes
                        && e instanceof EntityWulfrumEye
                        && ((EntityWulfrumEye) e).bossBarId().equals(id))
                    pair = (EntityWulfrumEye) e;
            }
        if (eyes && pair == null) return;
        float a = eyes ? pair.seerHealth() : event.getBossInfo().getPercent(),
                b = eyes ? pair.observerHealth() : a;
        if (TRAILS.size() > 32) TRAILS.clear();
        Trail trail =
                TRAILS.computeIfAbsent(event.getBossInfo().getUniqueId(), key -> new Trail(a, b));
        trail.update(a, b);
        float scale =
                Math.min(1F, (event.getResolution().getScaledWidth() - 12F) / BossBarLayout.WIDTH);
        int
                x =
                        (event.getResolution().getScaledWidth()
                                        - Math.round(BossBarLayout.WIDTH * scale))
                                / 2,
                y = Math.max(2, event.getY() - 8);
        event.setCanceled(true);
        event.setIncrement((int) Math.ceil(BossBarLayout.HEIGHT * scale) + 4);
        float tick = mc.player == null ? 0 : mc.player.ticksExisted + event.getPartialTicks();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1);
        if (brawler) BrawlerBossHands.draw(false);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA,
                GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA,
                GlStateManager.SourceFactor.ONE,
                GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        List<Runnable> labels = new ArrayList<>();
        ScoutBossBarLayout.Canvas canvas =
                new ScoutBossBarLayout.Canvas() {
                    public void rect(int x1, int y1, int x2, int y2, int color) {
                        int a = color >>> 24,
                                r = color >> 16 & 255,
                                g = color >> 8 & 255,
                                b = color & 255;
                        buffer.pos(x1, y2, 0).color(r, g, b, a).endVertex();
                        buffer.pos(x2, y2, 0).color(r, g, b, a).endVertex();
                        buffer.pos(x2, y1, 0).color(r, g, b, a).endVertex();
                        buffer.pos(x1, y1, 0).color(r, g, b, a).endVertex();
                    }

                    public void label(String text, int center, int top, int color) {
                        labels.add(
                                () ->
                                        mc.fontRenderer.drawStringWithShadow(
                                                text,
                                                center - mc.fontRenderer.getStringWidth(text) / 2F,
                                                top,
                                                color));
                    }

                    public void heading(String text, int center, int top, int color, float size) {
                        labels.add(
                                () -> {
                                    String bold = "\u00a7l" + text;
                                    GlStateManager.pushMatrix();
                                    GlStateManager.translate(center, top, 0);
                                    GlStateManager.scale(size, size, 1);
                                    mc.fontRenderer.drawStringWithShadow(
                                            bold,
                                            -mc.fontRenderer.getStringWidth(bold) / 2F,
                                            0,
                                            color);
                                    GlStateManager.popMatrix();
                                });
                    }
                };
        if (brawler)
            BossBarLayout.brawler(
                    canvas,
                    a,
                    trail.value[0],
                    tick,
                    machine == null
                            ? new float[] {1, 1, 1, 1}
                            : new float[] {
                                machine.armHealth(0) / 100F,
                                machine.armHealth(1) / 100F,
                                machine.armHealth(2) / 100F,
                                machine.armHealth(3) / 100F
                            },
                    machine == null ? 1 : machine.phase(),
                    machine == null ? 0 : machine.shieldHits());
        else if (excavator) BossBarLayout.excavator(canvas, a, trail.value[0], tick);
        else if (eyes)
            BossBarLayout.eyes(
                    WulfrumSurvivorOverlay.fracture(
                            canvas, pair.seer(), pair.survivorTick(), pair.overclocked()),
                    a,
                    b,
                    trail.value[0],
                    trail.value[1],
                    tick);
        else
            BossBarLayout.scout(
                    canvas,
                    a,
                    trail.value[0],
                    tick,
                    tracked != null && tracked.getScene() == 8,
                    tracked != null && tracked.isOverheating()
                            ? ScoutOverheat.remaining(tracked.getOverheatTick())
                            : -1);
        if (eyes && (pair.survivorTick() > 0 || pair.overclocked()))
            WulfrumSurvivorOverlay.draw(canvas, pair, event.getPartialTicks());
        Tessellator.getInstance().draw();
        GlStateManager.enableTexture2D();
        for (Runnable label : labels) label.run();
        GlStateManager.disableBlend();
        if (brawler) BrawlerBossHands.draw(true);
        if (eyes) WulfrumSurvivorOverlay.actor(pair, event.getPartialTicks());
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }
}
