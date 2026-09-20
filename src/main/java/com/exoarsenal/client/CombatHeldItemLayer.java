package com.exoarsenal.client;

import com.exoarsenal.combat.WeaponDiscipline;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerHeldItem;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;

public final class CombatHeldItemLayer extends LayerHeldItem {
    private EntityLivingBase wearer;
    private float partial;

    public CombatHeldItemLayer(RenderLivingBase<?> renderer) {
        super(renderer);
    }

    @Override
    public void doRenderLayer(
            EntityLivingBase e,
            float limb,
            float amount,
            float partial,
            float age,
            float yaw,
            float pitch,
            float scale) {
        this.wearer = e;
        this.partial = partial;
        try {
            super.doRenderLayer(e, limb, amount, partial, age, yaw, pitch, scale);
        } finally {
            wearer = null;
        }
    }

    @Override
    protected void translateToHand(EnumHandSide side) {
        super.translateToHand(side);
        if (wearer == null || side != wearer.getPrimaryHand()) return;
        NBTTagCompound n = wearer.getEntityData();
        int action = n.getInteger("FrostCombatAction"), index = n.getInteger("FrostCombatProfile");
        if ((action != 1 && action != 2) || index < 0 || index >= WeaponDiscipline.values().length)
            return;
        WeaponDiscipline d = WeaponDiscipline.values()[index];
        float age = wearer.world.getTotalWorldTime() - n.getLong("FrostCombatStart") + partial;
        if (age < 0 || age > d.duration(action == 2)) return;
        float wrist = WeaponPose.wrist(d, age, action == 2);

        GlStateManager.translate(0, 0.625F, 0);
        GlStateManager.rotate(wrist, 1, 0, 0);
        GlStateManager.translate(0, -0.625F, 0);
        float contact = d.contact(action == 2),
                fade = Math.max(0, 1 - Math.abs(age - contact) / 3F);
        if (fade > 0) {

            GlStateManager.pushMatrix();
            GlStateManager.translate(0, .625F, 0);
            boolean lighting = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_LIGHTING),
                    cull = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_CULL_FACE),
                    blend = org.lwjgl.opengl.GL11.glIsEnabled(org.lwjgl.opengl.GL11.GL_BLEND);
            int source = org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_BLEND_SRC),
                    destination =
                            org.lwjgl.opengl.GL11.glGetInteger(org.lwjgl.opengl.GL11.GL_BLEND_DST);
            com.exoarsenal.client.render.WulfrumRayRenderer.begin();
            net.minecraft.client.renderer.BufferBuilder b =
                    net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
            b.begin(
                    org.lwjgl.opengl.GL11.GL_QUADS,
                    net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
            int color =
                    d == WeaponDiscipline.ENERGY
                            ? 0xFF7059
                            : d == WeaponDiscipline.KATANA ? 0xFFF1A0 : 0xDAE6EE;
            for (int i = 0; i < 8; i++) {
                double a = (i / 8D - .5) * 1.1, c = ((i + 1) / 8D - .5) * 1.1;
                com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                        b,
                        new net.minecraft.util.math.Vec3d(
                                Math.sin(a) * .85, -.1, -Math.cos(a) * .85),
                        new net.minecraft.util.math.Vec3d(
                                Math.sin(c) * .85, -.1, -Math.cos(c) * .85),
                        .018,
                        color,
                        fade * (i + 1) / 16F);
            }
            net.minecraft.client.renderer.Tessellator.getInstance().draw();
            com.exoarsenal.client.render.WulfrumRayRenderer.finish();
            if (!lighting) GlStateManager.disableLighting();
            if (!cull) GlStateManager.disableCull();
            if (blend) GlStateManager.enableBlend();
            GlStateManager.blendFunc(source, destination);
            GlStateManager.popMatrix();
        }
    }
}
