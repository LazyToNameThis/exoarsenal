package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.entity.EntityPersonalMachine;
import com.exoarsenal.registry.MachineRewards;
import com.exoarsenal.client.render.ExcavatorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class MachineRewardClient {
    @SubscribeEvent
    public static void models(ModelRegistryEvent event) {
        for (Item item : MachineRewards.ITEMS) {
            ModelLoader.setCustomModelResourceLocation(
                    item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
            item.setTileEntityItemStackRenderer(new Icon());
        }
        RenderingRegistry.registerEntityRenderingHandler(
                EntityPersonalMachine.class, MachineRenderer::new);
    }

    private static final class MachineRenderer extends Render<EntityPersonalMachine> {
        MachineRenderer(RenderManager manager) {
            super(manager);
            shadowSize = .6F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityPersonalMachine e) {
            return ModelTexture.get("excavator");
        }

        @Override
        public void doRender(
                EntityPersonalMachine e, double x, double y, double z, float yaw, float partial) {
            bindTexture(getEntityTexture(e));
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + (e.mount() ? 1 : .4), z);
            GlStateManager.rotate(-yaw, 0, 1, 0);
            GlStateManager.scale(.065, .065, .065);
            if (e.mount()) {
                ExcavatorRenderer.draw(ExcavatorModel.HEAD);
                GlStateManager.pushMatrix();
                GlStateManager.rotate(
                        (e.ticksExisted + partial) * (e.action() == 1 ? 55 : 12), 0, 0, 1);
                ExcavatorRenderer.draw(ExcavatorModel.DRILL);
                GlStateManager.popMatrix();
                for (int i = 1; i <= 5; i++) {
                    GlStateManager.pushMatrix();
                    GlStateManager.translate(
                            Math.sin((e.ticksExisted + partial) * .09 - i * .5) * i * .65,
                            0,
                            -i * 15);
                    ExcavatorRenderer.draw(i == 5 ? ExcavatorModel.TAIL : ExcavatorModel.body(i));
                    GlStateManager.popMatrix();
                }
            } else {
                ExcavatorRenderer.draw(ExcavatorModel.PROBE);
                ExcavatorRenderer.draw(ExcavatorModel.PROBE_GUN);
            }
            GlStateManager.popMatrix();
            if (!e.mount() && e.action() > 0 && e.beamTarget() != null) {
                net.minecraft.util.math.Vec3d end =
                        e.beamTarget()
                                .getPositionVector()
                                .addVector(0, e.beamTarget().height * .5, 0)
                                .subtract(e.getPositionVector());
                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y + .4, z);
                com.exoarsenal.client.render.WulfrumRayRenderer.begin();
                BufferBuilder b = Tessellator.getInstance().getBuffer();
                b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
                com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                        b,
                        net.minecraft.util.math.Vec3d.ZERO,
                        end,
                        e.action() == 3 ? .08 : .015,
                        e.action() == 3 ? 0xB2FFD0 : 0xE6DD9A,
                        .8F);
                Tessellator.getInstance().draw();
                com.exoarsenal.client.render.WulfrumRayRenderer.finish();
                GlStateManager.popMatrix();
            }
        }
    }

    private static final class Icon extends TileEntityItemStackRenderer {
        @Override
        public void renderByItem(ItemStack stack, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(.5, .45, .5);
            Minecraft.getMinecraft().getTextureManager().bindTexture(ModelTexture.get("excavator"));
            if (stack.getItem() == MachineRewards.PROBE) {
                GlStateManager.scale(.027, .027, .027);
                GlStateManager.rotate(-35, 0, 1, 0);
                ExcavatorRenderer.draw(ExcavatorModel.PROBE);
                ExcavatorRenderer.draw(ExcavatorModel.PROBE_GUN);
            } else {
                GlStateManager.disableTexture2D();
                GlStateManager.disableLighting();
                if (stack.getItem() == MachineRewards.KEY) {
                    box(-.17, .08, -.06, .17, .37, .06, 0x526A4A);
                    box(-.11, .14, -.075, .11, .30, .075, 0xB6E781);
                    box(-.045, -.34, -.04, .045, .13, .04, 0xA1A790);
                    box(.035, -.32, -.04, .17, -.24, .04, 0x7B866A);
                    box(.035, -.17, -.04, .13, -.09, .04, 0x7B866A);
                } else {
                    box(-.29, -.30, -.15, .29, .26, .15, 0x3C4D39);
                    box(-.32, .18, -.17, .32, .30, .17, 0x819071);
                    box(-.25, -.24, -.17, -.17, .20, .17, 0x9AA58B);
                    box(.17, -.24, -.17, .25, .20, .17, 0x9AA58B);
                    box(
                            -.10,
                            -.06,
                            -.19,
                            .10,
                            .12,
                            -.16,
                            stack.getItem() == MachineRewards.SEER_BAG ? 0x79E9B0 : 0xD2E789);
                    box(-.08, .29, -.07, -.04, .40, .07, 0xA3AD90);
                    box(.04, .29, -.07, .08, .40, .07, 0xA3AD90);
                    box(-.08, .37, -.07, .08, .42, .07, 0xA3AD90);
                }
                GlStateManager.enableTexture2D();
                GlStateManager.enableLighting();
            }
            GlStateManager.color(1, 1, 1, 1);
            GlStateManager.popMatrix();
        }

        private static void box(
                double x, double y, double z, double X, double Y, double Z, int color) {
            double[][] p = {
                {x, y, z}, {X, y, z}, {X, Y, z}, {x, Y, z}, {x, y, Z}, {X, y, Z}, {X, Y, Z},
                {x, Y, Z}
            };
            int[][] faces = {
                {0, 3, 2, 1}, {4, 5, 6, 7}, {0, 4, 7, 3}, {1, 2, 6, 5}, {3, 7, 6, 2}, {0, 1, 5, 4}
            };
            BufferBuilder b = Tessellator.getInstance().getBuffer();
            b.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
            for (int f = 0; f < 6; f++) {
                float shade = f == 4 ? 1 : f == 5 ? .55F : f < 2 ? .85F : .7F;
                for (int v : faces[f])
                    b.pos(p[v][0], p[v][1], p[v][2])
                            .color(
                                    ((color >> 16) & 255) / 255F * shade,
                                    ((color >> 8) & 255) / 255F * shade,
                                    (color & 255) / 255F * shade,
                                    1)
                            .endVertex();
            }
            Tessellator.getInstance().draw();
        }
    }
}
