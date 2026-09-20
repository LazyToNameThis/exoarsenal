package com.exoarsenal.expedition.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class PrebossClient {
    private static int mana = 20, maximum = 20, focus;
    private static boolean jumpHeld;

    @SubscribeEvent
    public static void controls(net.minecraftforge.client.event.InputUpdateEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null) {
            jumpHeld = true;
            return;
        }
        boolean pressed = event.getMovementInput().jump;
        if (pressed
                && !jumpHeld
                && !mc.player.onGround
                && AccessoryInventory.bottleTier(mc.player) > 0)
            com.exoarsenal.network.ModNetwork.CHANNEL.sendToServer(
                    new com.exoarsenal.network.PacketBottleJump());
        jumpHeld = pressed;
    }

    public static void setMana(int value, int max, int charge) {
        maximum = Math.max(20, Math.min(220, max));
        mana = Math.max(0, Math.min(maximum, value));
        focus = Math.max(0, Math.min(60, charge));
    }

    @SubscribeEvent
    public static void hud(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.gameSettings.hideGUI) return;
        boolean held =
                mc.player.getHeldItemMainhand().getItem() instanceof PrebossItem
                                && ((PrebossItem) mc.player.getHeldItemMainhand().getItem())
                                                .manaCost()
                                        > 0
                        || mc.player.getHeldItemOffhand().getItem() instanceof PrebossItem
                                && ((PrebossItem) mc.player.getHeldItemOffhand().getItem())
                                                .manaCost()
                                        > 0;
        int x = event.getResolution().getScaledWidth() - 112,
                y = event.getResolution().getScaledHeight() - 52;
        if (mc.player.getHeldItemMainhand().getItem() == PrebossContent.CRYSTALLINE) {
            Gui.drawRect(x, y - 22, x + 100, y - 18, 0xC0201830);
            Gui.drawRect(x, y - 22, x + 100 * focus / 60, y - 18, 0xFFBE91E6);
            mc.fontRenderer.drawStringWithShadow(
                    focus >= 60 ? "Crystalline: ready" : "Crystalline: focusing",
                    x,
                    y - 33,
                    0xDFC7F2);
        }
        if (!held && mana >= maximum) return;
        Gui.drawRect(x, y, x + 100, y + 4, 0xC0182639);
        Gui.drawRect(x, y, x + 100 * mana / maximum, y + 4, 0xFF73B8EE);
        mc.fontRenderer.drawStringWithShadow("Mana " + mana + " / " + maximum, x, y - 11, 0xBFDCFF);
    }

    private static void mesh(String id) {
        ExpeditionRender.mesh(PrebossModels.item(id));
    }

    public static final class Crystal
            extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer<
                    LifeCrystalBlock.CrystalTile> {
        @Override
        public void render(
                LifeCrystalBlock.CrystalTile tile,
                double x,
                double y,
                double z,
                float partial,
                int stage,
                float alpha) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + .5, y + .48, z + .5);
            GlStateManager.rotate(
                    ((tile.getPos().getX() * 31 + tile.getPos().getZ() * 17) & 3) * 180, 0, 1, 0);
            mesh("life_crystal_formation");
            GlStateManager.popMatrix();
        }
    }

    public static final class Herb
            extends net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer<
                    ShiverthornBlock.HerbTile> {
        @Override
        public void render(
                ShiverthornBlock.HerbTile tile,
                double x,
                double y,
                double z,
                float partial,
                int stage,
                float alpha) {
            net.minecraft.block.state.IBlockState state =
                    tile.getWorld().getBlockState(tile.getPos());
            if (state.getBlock() != PrebossContent.SHIVERTHORN_PLANT) return;
            int age = state.getValue(ShiverthornBlock.AGE);
            double scale = .5 + age * .25;
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + .5, y + .375 * scale, z + .5);
            GlStateManager.scale(scale, scale, scale);
            mesh(age == 2 ? "shiverthorn_plant" : "shiverthorn_shoot");
            GlStateManager.popMatrix();
        }
    }

    public static void stormlion(float walk, float amount, float bite) {
        mesh("stormlion_body");
        for (int side : new int[] {-1, 1}) {
            for (int leg = 0; leg < 3; leg++) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(side * .27, .31, (leg - 1) * .23);
                GlStateManager.scale(side, 1, 1);
                GlStateManager.rotate(
                        (float) Math.sin(walk + leg * 1.8 + side) * amount * 28, 0, 1, 0);
                mesh("stormlion_leg");
                GlStateManager.popMatrix();
            }
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .16, .29, -.4);
            GlStateManager.scale(side, 1, 1);
            GlStateManager.rotate(-10 + (float) Math.sin(bite * Math.PI) * 30, 0, 1, 0);
            mesh("stormlion_jaw");
            GlStateManager.popMatrix();
        }
    }

    public static void flinx(float time, boolean ground) {
        GlStateManager.pushMatrix();
        double squash = ground ? 1 + Math.sin(time * .22) * .03 : 1.08;
        GlStateManager.scale(1 / squash, squash, 1 / squash);
        mesh("flinx_body");
        GlStateManager.popMatrix();
        for (int side : new int[] {-1, 1}) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .15, 0, -.02);
            GlStateManager.rotate(ground ? 0 : -20, 1, 0, 0);
            mesh("flinx_foot");
            GlStateManager.popMatrix();
        }
    }

    public static void companion(EntityExpeditionMinion entity, float partial) {
        float time = entity.ticksExisted + partial;
        GlStateManager.scale(.65, .65, .65);
        if (entity.variant() == 4) {
            stormlion(time * .2F, .7F, 0);
            if (entity.flying()) {
                GlStateManager.pushMatrix();
                GlStateManager.translate(0, .4, 0);
                GlStateManager.rotate((float) Math.sin(time) * 20, 0, 0, 1);
                ExpeditionRender.mesh(SeaModels.item("ray_wing"));
                GlStateManager.scale(-1, 1, 1);
                ExpeditionRender.mesh(SeaModels.item("ray_wing"));
                GlStateManager.popMatrix();
            }
        } else flinx(time, entity.onGround);
    }

    public static final class Creature extends Render<EntityLivingBase> {
        public Creature(RenderManager manager) {
            super(manager);
            shadowSize = .5F;
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityLivingBase entity) {
            return null;
        }

        @Override
        public void doRender(
                EntityLivingBase entity, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(180 - yaw, 0, 1, 0);
            if (entity instanceof EntityStormlion)
                stormlion(
                        entity.limbSwing * .7F,
                        entity.limbSwingAmount,
                        entity.getSwingProgress(partial));
            else flinx(entity.ticksExisted + partial, entity.onGround);
            GlStateManager.popMatrix();
            super.doRender(entity, x, y, z, yaw, partial);
        }
    }

    public static final class Shot extends Render<EntityPrebossShot> {
        public Shot(RenderManager manager) {
            super(manager);
        }

        @Override
        protected ResourceLocation getEntityTexture(EntityPrebossShot entity) {
            return null;
        }

        @Override
        public void doRender(
                EntityPrebossShot entity, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(
                    entity.prevRotationYaw
                            + (entity.rotationYaw - entity.prevRotationYaw) * partial,
                    0,
                    1,
                    0);
            GlStateManager.rotate(-entity.rotationPitch, 1, 0, 0);
            GlStateManager.scale(.55, .55, .55);
            String id;
            boolean spin = false;
            switch (entity.kind()) {
                case JAVELIN:
                    id = "cave_javelin";
                    break;
                case WOOD_BOOMERANG:
                    id = "wooden_boomerang";
                    spin = true;
                    break;
                case ENCHANTED_BOOMERANG:
                    id = "enchanted_boomerang";
                    spin = true;
                    break;
                case KNIFE:
                    id = "wulfrum_knife";
                    break;
                case CRYSTALLINE:
                    id = "crystalline";
                    if (entity.generation() > 0) GlStateManager.scale(.6, .6, .6);
                    break;
                case SPARKING:
                    id = "spark";
                    break;
                case FROSTING:
                case FROST_BOLT:
                    id = "frost_orb";
                    if (entity.kind() == PrebossItem.Kind.FROSTING)
                        GlStateManager.scale(.5, .5, .5);
                    break;
                case STORM_SPEAR:
                case THUNDER_ZAPPER:
                    id = "electric_bolt";
                    break;
                default:
                    id = "mana_crystal";
                    GlStateManager.scale(.4, .7, .4);
                    break;
            }
            if (spin) GlStateManager.rotate((entity.ticksExisted + partial) * 32, 0, 0, 1);
            else GlStateManager.rotate(90, 1, 0, 0);
            mesh(id);
            GlStateManager.popMatrix();
        }
    }
}
