package com.scapeandrun.frostbite.client.render;

import static com.scapeandrun.frostbite.client.model.KingSlimeModel.*;

import com.scapeandrun.frostbite.entity.*;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public final class KingSlimeRenderer extends Render<EntityKingSlime> {
    public KingSlimeRenderer(RenderManager m) {
        super(m);
        shadowSize = 1.8F;
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityKingSlime e) {
        return null;
    }

    @Override
    public void doRender(
            EntityKingSlime e, double x, double y, double z, float yaw, float partial) {
        start(x, y, z);
        GlStateManager.rotate(180 - e.renderYawOffset, 0, 1, 0);
        float size = e.bodySize() * e.visibleScale(),
                s = e.previousSquash + (e.squash - e.previousSquash) * partial;
        GlStateManager.scale(size * (1 - s * .5), size * (1 + s), size * (1 - s * .5));
        if (!e.ninjaEscaped()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0, .2, 0);
            GlStateManager.rotate(
                    12 + (float) Math.sin((e.ticksExisted + partial) * .08) * 5, 0, 0, 1);
            GlStateManager.scale(.23, .23, .23);
            ninja(0, e.ticksExisted + partial);
            GlStateManager.popMatrix();
        }
        gel(0x328CEA, .62F);
        crown(
                .64F + (float) Math.sin((e.ticksExisted + partial) * .14) * .009F,
                e.fraction() > .75F);
        end();
        super.doRender(e, x, y, z, yaw, partial);
    }

    static void start(double x, double y, double z) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.disableCull();
    }

    static void end() {
        GlStateManager.depthMask(true);
        GlStateManager.enableCull();
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.popMatrix();
    }

    public static final class Support extends Render<EntityKingSlimeSupport> {
        public Support(RenderManager m) {
            super(m);
            shadowSize = .4F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityKingSlimeSupport e) {
            return null;
        }

        @Override
        public void doRender(
                EntityKingSlimeSupport e, double x, double y, double z, float yaw, float p) {
            start(x, y, z);
            GlStateManager.rotate(180 - e.renderYawOffset, 0, 1, 0);
            if (e.kind() == 1) {
                if (e.pose() == 1) GlStateManager.rotate((e.ticksExisted + p) * 30, 1, 0, 0);
                ninja(e.pose(), e.ticksExisted + p);
            } else if (e.kind() == 2) {
                gel(e.spiked() ? 0xD9B231 : 0xF1CE43, .85F);
                if (e.spiked())
                    for (int i = 0; i < 4; i++) {
                        GlStateManager.pushMatrix();
                        GlStateManager.rotate(i * 90, 0, 1, 0);
                        GlStateManager.rotate(35, 0, 0, 1);
                        box(.2, .4, -.08, .3, .85, .08, 0xF8EEBD, 1);
                        GlStateManager.popMatrix();
                    }
            } else {
                GlStateManager.translate(0, .4, 0);
                GlStateManager.rotate((e.ticksExisted + p) * 2, 0, 1, 0);
                GlStateManager.rotate(45, 0, 0, 1);
                int c = e.pose() == 1 ? 0x32EC80 : 0xF03761;
                box(-.26, -.26, -.2, .26, .26, .2, c, 1);
                box(-.18, -.18, -.26, .18, .18, .26, c, 1);
                box(-.09, -.09, -.29, .09, .09, .29, 0xFFF1DE, 1);
            }
            end();
        }
    }

    public static final class Shot extends Render<EntityKingSlimeShot> {
        public Shot(RenderManager m) {
            super(m);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityKingSlimeShot e) {
            return null;
        }

        @Override
        public void doRender(
                EntityKingSlimeShot e, double x, double y, double z, float yaw, float p) {
            start(x, y, z);
            GlStateManager.rotate((e.ticksExisted + p) * 35, 0, 1, 0);
            if (e.kind() == 1) {
                box(-.3, -.025, -.06, .3, .025, .06, 0xD8EAF0, 1);
                box(-.06, -.025, -.3, .06, .025, .3, 0xA4C0CE, 1);
                box(-.07, -.045, -.07, .07, .045, .07, 0x405268, 1);
            } else {
                GlStateManager.rotate(45, 0, 0, 1);
                box(-.12, -.12, -.12, .12, .12, .12, e.kind() == 0 ? 0xF74668 : 0xDAE894, 1);
                box(-.055, -.055, -.16, .055, .055, .16, 0xFFF3D6, 1);
            }
            end();
        }
    }
}
