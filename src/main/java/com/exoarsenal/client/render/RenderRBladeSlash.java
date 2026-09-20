package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityRBladeSlash;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nullable;

public class RenderRBladeSlash extends Render<EntityRBladeSlash> {
    public RenderRBladeSlash(RenderManager manager) {
        super(manager);
        shadowSize = 0.0F;
    }

    @Override
    public void doRender(
            EntityRBladeSlash slash,
            double x,
            double y,
            double z,
            float entityYaw,
            float partialTicks) {}

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(EntityRBladeSlash entity) {
        return null;
    }
}
