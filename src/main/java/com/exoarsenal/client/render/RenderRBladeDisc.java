package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityRBladeDisc;
import com.exoarsenal.item.EnergyUtil;
import com.exoarsenal.item.ItemRBlade;
import com.exoarsenal.registry.ModContent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nullable;

public class RenderRBladeDisc extends Render<EntityRBladeDisc> {
    private static final ItemStack DISPLAY = createDisplayStack(false);
    private static final ItemStack X10_DISPLAY = createDisplayStack(true);

    public RenderRBladeDisc(RenderManager manager) {
        super(manager);
        shadowSize = 0.35F;
    }

    private static ItemStack createDisplayStack(boolean x10) {
        ItemStack stack = new ItemStack(x10 ? ModContent.X10_BLADE : ModContent.R_BLADE);
        stack.setTagCompound(new NBTTagCompound());
        stack.getTagCompound().setBoolean("Active", true);
        stack.getTagCompound().setBoolean("Thrown", true);
        IEnergyStorage energy = EnergyUtil.get(stack);
        if (energy != null) energy.receiveEnergy(ItemRBlade.CAPACITY, false);
        return stack;
    }

    @Override
    public void doRender(
            EntityRBladeDisc entity,
            double x,
            double y,
            double z,
            float entityYaw,
            float partialTicks) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        float spin =
                entity.prevRotationYaw
                        + (entity.rotationYaw - entity.prevRotationYaw) * partialTicks;
        float tumble =
                entity.prevRotationPitch
                        + (entity.rotationPitch - entity.prevRotationPitch) * partialTicks;
        GlStateManager.rotate(spin, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(tumble, 0.0F, 0.0F, 1.0F);
        GlStateManager.scale(0.72F, 0.72F, 0.72F);
        Minecraft.getMinecraft()
                .getRenderItem()
                .renderItem(
                        entity.isX10() ? X10_DISPLAY : DISPLAY,
                        ItemCameraTransforms.TransformType.FIXED);
        GlStateManager.popMatrix();
        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRBladeDisc entity) {
        return null;
    }
}
