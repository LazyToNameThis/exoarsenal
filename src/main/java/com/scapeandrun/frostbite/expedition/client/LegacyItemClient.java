package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.tileentity.TileEntityItemStackRenderer;
import net.minecraft.item.*;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class LegacyItemClient {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void models(ModelRegistryEvent event) {
        Renderer renderer = new Renderer();
        for (String id : LegacyItemModels.IDS) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(Frostbite.MODID, id));
            if (item == null || item == net.minecraft.init.Items.AIR)
                throw new IllegalStateException("Missing modeled item " + id);
            ModelLoader.setCustomModelResourceLocation(
                    item,
                    0,
                    new ModelResourceLocation(Frostbite.MODID + ":expedition_item", "inventory"));
            item.setTileEntityItemStackRenderer(renderer);
        }
    }

    public static final class Renderer extends TileEntityItemStackRenderer {
        @Override
        public void renderByItem(ItemStack stack, float partial) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(.5, .5, .5);
            ExpeditionRender.mesh(
                    LegacyItemModels.item(stack.getItem().getRegistryName().getResourcePath()));
            GlStateManager.popMatrix();
        }
    }
}
