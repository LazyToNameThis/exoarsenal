package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.expedition.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class DeepClient {
    @SubscribeEvent
    public static void register(ModelRegistryEvent event) {
        for (WildModule.Entry entry : DeepModule.ENTRIES)
            RenderingRegistry.registerEntityRenderingHandler(entry.type, WildRender::new);
        RenderingRegistry.registerEntityRenderingHandler(EntityRelicShot.class, Shot::new);
    }

    public static final class Shot extends Render<EntityRelicShot> {
        public Shot(RenderManager manager) {
            super(manager);
        }

        protected ResourceLocation getEntityTexture(EntityRelicShot e) {
            return null;
        }

        public void doRender(
                EntityRelicShot e, double x, double y, double z, float yaw, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, z);
            GlStateManager.rotate(e.rotationYaw, 0, 1, 0);
            if (e.type() == 2) GlStateManager.rotate((e.ticksExisted + partial) * 24, 0, 0, 1);
            ExpeditionRender.mesh(
                    DeepModels.item(
                            e.type() == 1
                                    ? "water_projectile"
                                    : e.type() == 2 ? "scythe_projectile" : "fire_projectile"),
                    8);
            GlStateManager.popMatrix();
        }
    }
}
