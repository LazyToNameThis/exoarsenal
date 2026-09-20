package com.exoarsenal.client.render;

import com.exoarsenal.entity.EntityScoutHardpoint;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.util.ResourceLocation;

public final class RenderScoutHardpoint extends Render<EntityScoutHardpoint> {
    public RenderScoutHardpoint(RenderManager manager) {
        super(manager);
        shadowSize = 0;
    }

    @Override
    public void doRender(
            EntityScoutHardpoint entity,
            double x,
            double y,
            double z,
            float yaw,
            float partialTicks) {}

    @Override
    protected ResourceLocation getEntityTexture(EntityScoutHardpoint entity) {
        return TextureMap.LOCATION_BLOCKS_TEXTURE;
    }
}
