package com.scapeandrun.frostbite.expedition.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.expedition.*;
import com.scapeandrun.frostbite.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ExpeditionClient {
    private static int shotDelay;

    @SubscribeEvent
    public static void models(ModelRegistryEvent e) {
        ExpeditionRender.ItemRenderer renderer = new ExpeditionRender.ItemRenderer();
        for (Item item : ExpeditionContent.ITEMS) {
            boolean gun =
                    item instanceof WulfrumArsenal.Weapon
                            || item == ExpeditionContent.BLUNDERBUSS
                            || item == ExpeditionContent.PROSTHESIS
                            || item == ExpeditionContent.DRILL
                            || item == SeaContent.BLOWGUN;
            ModelLoader.setCustomModelResourceLocation(
                    item,
                    0,
                    new ModelResourceLocation(
                            Frostbite.MODID + (gun ? ":expedition_gun" : ":expedition_item"),
                            "inventory"));
            item.setTileEntityItemStackRenderer(renderer);
        }
        RenderingRegistry.registerEntityRenderingHandler(
                com.scapeandrun.frostbite.entity.EntityDraedon.class, DraedonRenderer::new);
        for (Block b : ExpeditionContent.BLOCKS)
            ModelLoader.setCustomModelResourceLocation(
                    Item.getItemFromBlock(b),
                    0,
                    new ModelResourceLocation(b.getRegistryName(), "inventory"));
        Item crystal = Item.getItemFromBlock(PrebossContent.LIFE_FORMATION);
        ModelLoader.setCustomModelResourceLocation(
                crystal,
                0,
                new ModelResourceLocation(Frostbite.MODID + ":expedition_item", "inventory"));
        crystal.setTileEntityItemStackRenderer(renderer);
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                LifeCrystalBlock.CrystalTile.class, new PrebossClient.Crystal());
        Item herb = Item.getItemFromBlock(PrebossContent.SHIVERTHORN_PLANT);
        ModelLoader.setCustomModelResourceLocation(
                herb,
                0,
                new ModelResourceLocation(Frostbite.MODID + ":expedition_item", "inventory"));
        herb.setTileEntityItemStackRenderer(renderer);
        net.minecraftforge.fml.client.registry.ClientRegistry.bindTileEntitySpecialRenderer(
                ShiverthornBlock.HerbTile.class, new PrebossClient.Herb());
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Amplifier.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Drone.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Gyrator.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Hovercraft.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Rover.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Mine.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityWulfrum.Slime.class, ExpeditionRender.Robot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityExpeditionShot.class, ExpeditionRender.Shot::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityExpeditionMinion.class, ExpeditionRender.Minion::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityCnidrion.class, SeaRender.Cnidrion::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.Clam.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.Ray.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.GhostBell.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.PrismBack.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.SeaFloaty.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.SeaMinnow.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.BabyGhostBell.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySeaCreature.GiantClam.class, SeaRender.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(EntitySeaKing.class, SeaRender.King::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityStormlion.class, PrebossClient.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntitySnowFlinx.class, PrebossClient.Creature::new);
        RenderingRegistry.registerEntityRenderingHandler(
                EntityPrebossShot.class, PrebossClient.Shot::new);
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (shotDelay > 0) shotDelay--;
        if (mc.player != null
                && mc.currentScreen == null
                && ExpeditionEvents.active(mc.player)
                && (mc.gameSettings.keyBindUseItem.isKeyDown()
                        || mc.gameSettings.keyBindAttack.isKeyDown())
                && shotDelay == 0) {
            ModNetwork.CHANNEL.sendToServer(new PacketEquipmentAction(8));
            shotDelay = 12;
        }
    }

    @SubscribeEvent
    public static void hand(RenderSpecificHandEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !ExpeditionEvents.active(mc.player)) return;
        e.setCanceled(true);
        if (e.getHand() == net.minecraft.util.EnumHand.OFF_HAND) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(.6, -.5, -.8);
        GlStateManager.rotate(-90, 0, 1, 0);
        ExpeditionRender.mesh(ExpeditionModels.item("wulfrum_prosthesis"));
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void fog(EntityViewRenderEvent.FogColors e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null
                && mc.player.isInWater()
                && ExpeditionWorldGenerator.inSea(mc.world, mc.player.getPosition())) {
            e.setRed(.05F);
            e.setGreen(.32F);
            e.setBlue(.4F);
        }
    }
}
