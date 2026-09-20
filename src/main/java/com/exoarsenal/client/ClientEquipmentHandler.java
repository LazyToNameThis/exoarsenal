package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.client.render.RBladeRenderer;
import com.exoarsenal.client.render.RmorArmorRenderer;
import com.exoarsenal.client.render.RToolRenderer;
import com.exoarsenal.client.render.RenderRBladeDisc;
import com.exoarsenal.client.render.RenderRBladeSlash;
import com.exoarsenal.client.render.X10ArmorRenderer;
import com.exoarsenal.client.render.X10BladeRenderer;
import com.exoarsenal.client.render.X10ToolRenderer;
import com.exoarsenal.client.render.EnergyGunRenderer;
import com.exoarsenal.client.render.KXArmorRenderer;
import com.exoarsenal.client.render.KXBladeRenderer;
import com.exoarsenal.client.render.KXToolRenderer;
import com.exoarsenal.client.render.EnergyShieldRenderer;
import com.exoarsenal.client.render.ScoutWeaponRenderer;
import com.exoarsenal.client.render.X20ScoutRenderer;
import com.exoarsenal.client.render.X20PilotRenderer;
import com.exoarsenal.client.render.RenderScoutHardpoint;
import com.exoarsenal.client.render.RenderAuroraField;
import com.exoarsenal.client.render.ScoutVfxRenderer;
import com.exoarsenal.client.gui.GuiRToolSchematics;
import com.exoarsenal.entity.EntityRBladeDisc;
import com.exoarsenal.entity.EntityRBladeSlash;
import com.exoarsenal.entity.EntityX20Scout;
import com.exoarsenal.entity.EntityScoutBomb;
import com.exoarsenal.entity.EntityScoutHardpoint;
import com.exoarsenal.entity.EntityX20Pilot;
import com.exoarsenal.entity.EntityAuroraField;
import com.exoarsenal.entity.EntityAuroraShard;
import com.exoarsenal.entity.EntityIceFragment;
import com.exoarsenal.item.ItemRBlade;
import com.exoarsenal.item.ItemRmorArmor;
import com.exoarsenal.item.ItemRTool;
import com.exoarsenal.item.ItemX10Armor;
import com.exoarsenal.item.ItemKXArmor;
import com.exoarsenal.event.RmorEventHandler;
import com.exoarsenal.event.X10Systems;
import com.exoarsenal.event.KXSystems;
import com.exoarsenal.network.ModNetwork;
import com.exoarsenal.network.PacketEquipmentAction;
import com.exoarsenal.network.PacketRToolEffect;
import com.exoarsenal.network.PacketCycleRmorMode;
import com.exoarsenal.network.PacketStartRBladeSpin;
import com.exoarsenal.network.PacketStopRBladeSpin;
import com.exoarsenal.network.PacketRBladeAirAttack;
import com.exoarsenal.registry.ModContent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import software.bernie.geckolib3.renderers.geo.GeoArmorRenderer;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class ClientEquipmentHandler {
    private static final KeyBinding MODE =
            new KeyBinding("key.exoarsenal.rmor_mode", Keyboard.KEY_F, "key.categories.exoarsenal");
    private static boolean registered;
    private static boolean spinning;
    private static EnumHand spinningHand = EnumHand.MAIN_HAND;
    private static long lastDownTap;
    private static boolean jumpWasDown;
    private static boolean rtoolUsing;
    private static int leftShotCooldown;
    private static int rightShotCooldown;
    private static final List<EquipmentEffect> EFFECTS = new ArrayList<>();

    private ClientEquipmentHandler() {}

    @SubscribeEvent
    public static void models(ModelRegistryEvent event) {
        if (!registered) {
            ClientRegistry.registerKeyBinding(MODE);
            GeoArmorRenderer.registerArmorRenderer(ItemRmorArmor.class, new RmorArmorRenderer());
            GeoArmorRenderer.registerArmorRenderer(ItemX10Armor.class, new X10ArmorRenderer());
            GeoArmorRenderer.registerArmorRenderer(ItemKXArmor.class, new KXArmorRenderer());
            ModContent.R_BLADE.setTileEntityItemStackRenderer(new RBladeRenderer());
            ModContent.R_TOOL.setTileEntityItemStackRenderer(new RToolRenderer());
            ModContent.X10_BLADE.setTileEntityItemStackRenderer(new X10BladeRenderer());
            ModContent.X10_MULTITOOL.setTileEntityItemStackRenderer(new X10ToolRenderer());
            ModContent.KX20_BLADE.setTileEntityItemStackRenderer(new KXBladeRenderer());
            ModContent.KX20_MULTITOOL.setTileEntityItemStackRenderer(new KXToolRenderer());
            ModContent.PROTOTYPE_ENERGY_SHOTGUN.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.PROTOTYPE_ENERGY_CANNON.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.PROTOTYPE_ENERGY_PISTOL.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.PROTOTYPE_LASER_MACHINEGUN.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.X10_ENERGY_SHOTGUN.setTileEntityItemStackRenderer(new EnergyGunRenderer());
            ModContent.X10_ENERGY_CANNON.setTileEntityItemStackRenderer(new EnergyGunRenderer());
            ModContent.X10_ENERGY_MACHINEGUN.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.X10_ENERGY_NET_LAUNCHER.setTileEntityItemStackRenderer(
                    new EnergyGunRenderer());
            ModContent.KX20_SHOTGUN.setTileEntityItemStackRenderer(new EnergyGunRenderer(true));
            ModContent.KX20_TRIBOW.setTileEntityItemStackRenderer(new EnergyGunRenderer(true));
            ModContent.KX20_MINIGUN.setTileEntityItemStackRenderer(new EnergyGunRenderer(true));
            ModContent.KX20_RAILGUN.setTileEntityItemStackRenderer(new EnergyGunRenderer(true));
            ModContent.PROTOTYPE_ENERGY_SHIELD.setTileEntityItemStackRenderer(
                    new EnergyShieldRenderer());
            ModContent.X10_ENERGY_SHIELD.setTileEntityItemStackRenderer(new EnergyShieldRenderer());
            ModContent.SCOUT_PINCER.setTileEntityItemStackRenderer(new ScoutWeaponRenderer());
            ModContent.MODIFIED_RAILGUN.setTileEntityItemStackRenderer(new ScoutWeaponRenderer());
            ModContent.GLACIER_SMASHER.setTileEntityItemStackRenderer(new ScoutWeaponRenderer());
            ModContent.AURORA_BOREALIS.setTileEntityItemStackRenderer(new ScoutWeaponRenderer());
            ModContent.SCOUT_ENERGY_CORE.setTileEntityItemStackRenderer(
                    new com.exoarsenal.client.render.ScoutCoreRenderer());
            ModContent.SCOUT_TREASURE_BAG.setTileEntityItemStackRenderer(
                    new com.exoarsenal.client.render.ScoutTreasureBagRenderer());
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityRBladeDisc.class, RenderRBladeDisc::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityRBladeSlash.class, RenderRBladeSlash::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityX20Scout.class, X20ScoutRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityFrigidRobot.Drone.class,
                    com.exoarsenal.client.render.FrigidRobotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityFrigidRobot.Amplifier.class,
                    com.exoarsenal.client.render.FrigidRobotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityFrigidRobot.Shielder.class,
                    com.exoarsenal.client.render.FrigidRobotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityFrigidRobot.Rover.class,
                    com.exoarsenal.client.render.FrigidRobotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityFrigidRobot.Arthropod.class,
                    com.exoarsenal.client.render.FrigidRobotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScoutShard.class,
                    com.exoarsenal.client.render.ScoutShardRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScoutCut.class,
                    com.exoarsenal.client.render.ScoutCutRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScoutArena.class,
                    com.exoarsenal.client.render.ScoutArenaRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScoutCoreRay.class,
                    com.exoarsenal.client.render.ScoutCoreRayRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumEye.Seer.class,
                    com.exoarsenal.client.render.WulfrumEyeRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumEye.Observer.class,
                    com.exoarsenal.client.render.WulfrumEyeRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumRay.class,
                    com.exoarsenal.client.render.WulfrumRayRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumShard.class,
                    com.exoarsenal.client.render.WulfrumShardRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumEcho.class,
                    com.exoarsenal.client.render.WulfrumEchoRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumCut.class,
                    com.exoarsenal.client.render.WulfrumCutRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumAppendage.class,
                    com.exoarsenal.client.render.WulfrumAppendageRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityWulfrumNova.class,
                    com.exoarsenal.client.render.WulfrumNovaRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityDesertScourge.class,
                    com.exoarsenal.client.render.DesertScourgeRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityKingSlime.class,
                    com.exoarsenal.client.render.KingSlimeRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityKingSlimeSupport.class,
                    com.exoarsenal.client.render.KingSlimeRenderer.Support::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityKingSlimeShot.class,
                    com.exoarsenal.client.render.KingSlimeRenderer.Shot::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityExcavator.class,
                    com.exoarsenal.client.render.ExcavatorRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityExcavatorSegment.class,
                    com.exoarsenal.client.render.ExcavatorRenderer.Segment::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityExcavatorProbe.class,
                    com.exoarsenal.client.render.ExcavatorRenderer.Probe::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityExcavatorPayload.class,
                    com.exoarsenal.client.render.ExcavatorPayloadRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityDesertScourge.Nuisance.class,
                    com.exoarsenal.client.render.DesertScourgeRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScourgeSand.class,
                    com.exoarsenal.client.render.ScourgeSandRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityDesertVulture.class,
                    com.exoarsenal.client.render.DesertVultureRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    com.exoarsenal.entity.EntityScourgeSegment.class,
                    manager ->
                            new net.minecraft.client.renderer.entity.Render<
                                    com.exoarsenal.entity.EntityScourgeSegment>(manager) {
                                @Override
                                protected net.minecraft.util.ResourceLocation getEntityTexture(
                                        com.exoarsenal.entity.EntityScourgeSegment e) {
                                    return null;
                                }

                                @Override
                                public void doRender(
                                        com.exoarsenal.entity.EntityScourgeSegment e,
                                        double x,
                                        double y,
                                        double z,
                                        float yaw,
                                        float partial) {}
                            });
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityScoutHardpoint.class, RenderScoutHardpoint::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityX20Pilot.class, X20PilotRenderer::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityAuroraField.class, RenderAuroraField::new);
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityScoutBomb.class,
                    manager ->
                            new net.minecraft.client.renderer.entity.RenderSnowball<>(
                                    manager,
                                    ModContent.MACHINED_CORE,
                                    Minecraft.getMinecraft().getRenderItem()));
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityAuroraShard.class,
                    manager ->
                            new net.minecraft.client.renderer.entity.RenderSnowball<>(
                                    manager,
                                    ModContent.SMALL_FROZEN_TIN,
                                    Minecraft.getMinecraft().getRenderItem()));
            RenderingRegistry.registerEntityRenderingHandler(
                    EntityIceFragment.class,
                    manager ->
                            new net.minecraft.client.renderer.entity.RenderSnowball<>(
                                    manager,
                                    ModContent.SMALL_FROZEN_COPPER,
                                    Minecraft.getMinecraft().getRenderItem()));
            registered = true;
        }
    }

    @SubscribeEvent
    public static void key(InputEvent.KeyInputEvent event) {
        if (MODE.isPressed()
                && (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                        || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))) {
            ModNetwork.CHANNEL.sendToServer(new PacketCycleRmorMode());
        }
        if (!Keyboard.getEventKeyState() || Minecraft.getMinecraft().player == null) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.currentScreen != null) return;
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_DOWN) {
            long now = Minecraft.getSystemTime();
            if (now - lastDownTap <= 360L
                    && (RmorEventHandler.hasPoweredSet(minecraft.player)
                            || com.exoarsenal.expedition.WulfrumArmor.full(minecraft.player))) {
                if (RmorEventHandler.hasPoweredSet(minecraft.player)) {
                    boolean defense = !RmorEventHandler.isDefenseForm(minecraft.player);
                    RmorEventHandler.setDefenseForm(minecraft.player, defense);
                }
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.TOGGLE_DEFENSE));
                lastDownTap = 0L;
            } else lastDownTap = now;
        }
        if (RmorEventHandler.isDefenseForm(minecraft.player)) return;
        ItemStack tool = heldRTool(minecraft.player);
        if (tool.isEmpty()) return;
        if (key == Keyboard.KEY_LEFT) {
            ItemRTool.cycleForm(tool);
            ModNetwork.CHANNEL.sendToServer(
                    new PacketEquipmentAction(PacketEquipmentAction.RTOOL_FORM));
        } else if (key == Keyboard.KEY_RIGHT) {
            ItemRTool.cycleMode(tool);
            ModNetwork.CHANNEL.sendToServer(
                    new PacketEquipmentAction(PacketEquipmentAction.RTOOL_MODE));
        }
    }

    @SubscribeEvent
    public static void rbladeSpin(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isRemote || !GuiScreen.isCtrlKeyDown()) return;
        ItemStack stack = event.getItemStack();
        if (stack.getItem() != ModContent.R_BLADE || !ItemRBlade.isActive(stack)) return;
        event.setCanceled(true);
        event.setCancellationResult(EnumActionResult.SUCCESS);
        spinningHand = event.getHand();
        spinning = ItemRBlade.beginSpin(event.getEntityPlayer(), spinningHand);
        if (spinning) ModNetwork.CHANNEL.sendToServer(new PacketStartRBladeSpin(spinningHand));
    }

    @SubscribeEvent
    public static void rbladeAirAttack(PlayerInteractEvent.LeftClickEmpty event) {
        if (!event.getWorld().isRemote) return;
        EnumHand hand = event.getHand();
        ItemStack stack = event.getEntityPlayer().getHeldItem(hand);
        if (hand == EnumHand.MAIN_HAND && com.exoarsenal.combat.WeaponDiscipline.of(stack) != null)
            ModNetwork.CHANNEL.sendToServer(new com.exoarsenal.network.PacketCombatAction(1, 0, 0));
    }

    @SubscribeEvent
    public static void clientTick(
            net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent event) {
        if (event.phase != net.minecraftforge.fml.common.gameevent.TickEvent.Phase.END) return;
        Minecraft minecraft = Minecraft.getMinecraft();
        if (spinning
                && (minecraft.player == null
                        || !minecraft.gameSettings.keyBindUseItem.isKeyDown()
                        || minecraft.player.getHeldItem(spinningHand).getItem()
                                != ModContent.R_BLADE)) {
            ModNetwork.CHANNEL.sendToServer(new PacketStopRBladeSpin());
            if (minecraft.player != null) minecraft.player.stopActiveHand();
            spinning = false;
        }
        if (minecraft.player == null) return;
        if (leftShotCooldown > 0) leftShotCooldown--;
        if (rightShotCooldown > 0) rightShotCooldown--;
        boolean defense = RmorEventHandler.isDefenseForm(minecraft.player);
        boolean inputAllowed = minecraft.currentScreen == null;
        boolean jumpDown = minecraft.gameSettings.keyBindJump.isKeyDown();
        if (defense && inputAllowed) {
            if (Mouse.isButtonDown(0) && leftShotCooldown <= 0) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.SHOTGUN_LEFT));
                leftShotCooldown = 8;
            }
            if (Mouse.isButtonDown(1) && rightShotCooldown <= 0) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.SHOTGUN_RIGHT));
                rightShotCooldown = 8;
            }
            if (jumpDown && !jumpWasDown && !minecraft.player.onGround) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.DEFENSE_JUMP));
            }
            if (rtoolUsing) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.RTOOL_STOP));
                rtoolUsing = false;
            }
        } else {
            ItemStack tool = heldRTool(minecraft.player);
            boolean using =
                    inputAllowed
                            && !tool.isEmpty()
                            && Mouse.isButtonDown(0)
                            && ItemRTool.getForm(tool) != ItemRTool.BUILD;
            if (using && ItemRTool.getForm(tool) == ItemRTool.SAW)
                using =
                        minecraft.objectMouseOver != null
                                && minecraft.objectMouseOver.typeOfHit
                                        == net.minecraft.util.math.RayTraceResult.Type.BLOCK;
            if (using) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.RTOOL_USE));
                rtoolUsing = true;
            } else if (rtoolUsing) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(PacketEquipmentAction.RTOOL_STOP));
                rtoolUsing = false;
            }
        }
        jumpWasDown = jumpDown;
    }

    @SubscribeEvent
    public static void mouse(MouseEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player == null || minecraft.currentScreen != null || !event.isButtonstate())
            return;
        if (RmorEventHandler.isDefenseForm(minecraft.player)
                && (event.getButton() == 0 || event.getButton() == 1)) {
            event.setCanceled(true);
            return;
        }
        if (!heldRTool(minecraft.player).isEmpty() && event.getButton() == 0)
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void hideDefenseHotbar(RenderGameOverlayEvent.Pre event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (event.getType() == RenderGameOverlayEvent.ElementType.HOTBAR
                && minecraft.player != null
                && RmorEventHandler.isDefenseForm(minecraft.player)) event.setCanceled(true);
    }

    public static boolean isRToolUsing(ItemStack stack) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.player != null
                && minecraft.player.getHeldItemMainhand() == stack
                && stack.getItem() instanceof ItemRTool
                && ItemRTool.getForm(stack) == ItemRTool.SAW) {
            int action = minecraft.player.getEntityData().getInteger("FrostCombatAction");
            if (action == 1 || action == 2) return true;
        }
        return rtoolUsing
                && minecraft.player != null
                && !stack.isEmpty()
                && (minecraft.player.getHeldItemMainhand() == stack
                        || minecraft.player.getHeldItemOffhand() == stack);
    }

    private static ItemStack heldRTool(EntityLivingBase wearer) {
        ItemStack main = wearer.getHeldItemMainhand();
        if (main.getItem() instanceof ItemRTool) return main;
        ItemStack off = wearer.getHeldItemOffhand();
        return off.getItem() instanceof ItemRTool ? off : ItemStack.EMPTY;
    }

    public static void openSchematicScreen() {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (!(minecraft.currentScreen instanceof GuiRToolSchematics)) {
            minecraft.displayGuiScreen(new GuiRToolSchematics());
        }
    }

    public static void acceptEffect(int sourceId, int type, List<Vec3d> targets) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null) return;
        long expires = minecraft.world.getTotalWorldTime() + 4L;
        boolean continuous =
                type == PacketRToolEffect.HELIX
                        || type == PacketRToolEffect.SAW
                        || type == PacketRToolEffect.GUN_LASER
                        || type == PacketRToolEffect.GUN_LASER_X10
                        || type == PacketRToolEffect.GUN_KX;
        if (continuous) {
            for (Iterator<EquipmentEffect> it = EFFECTS.iterator(); it.hasNext(); ) {
                EquipmentEffect effect = it.next();
                if (effect.sourceId == sourceId && effect.type == type) it.remove();
            }
        }
        EFFECTS.add(new EquipmentEffect(sourceId, type, targets, expires));
    }

    @SubscribeEvent
    public static void renderWorld(RenderWorldLastEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (minecraft.world == null || minecraft.player == null) return;
        ScoutVfxRenderer.render(event.getPartialTicks());
        long now = minecraft.world.getTotalWorldTime();
        EFFECTS.removeIf(
                effect ->
                        effect.expires < now
                                || minecraft.world.getEntityByID(effect.sourceId) == null);
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        GlStateManager.depthMask(false);
        renderArmorFlightVfx(event.getPartialTicks());
        for (EquipmentEffect effect : EFFECTS) renderEffect(effect, event.getPartialTicks());
        renderSchematic(minecraft.player);
        GlStateManager.depthMask(true);
        GlStateManager.disableBlend();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GL11.glLineWidth(1.0F);
        GlStateManager.popMatrix();
    }

    private static void renderEffect(EquipmentEffect effect, float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        Entity sourceEntity = minecraft.world.getEntityByID(effect.sourceId);
        if (!(sourceEntity instanceof EntityLivingBase) || effect.targets.isEmpty()) return;
        EntityLivingBase source = (EntityLivingBase) sourceEntity;
        Vec3d start = interpolatedEyes(source, partialTicks);
        if (effect.type == PacketRToolEffect.HELIX) drawHelix(start, effect.targets.get(0));
        else if (effect.type == PacketRToolEffect.SAW) drawSaw(start, effect.targets.get(0));
        else if (effect.type == PacketRToolEffect.KX_SCYTHE) drawKXScythe(effect.targets.get(0));
        else if (effect.type == PacketRToolEffect.SCOUT_RAILGUN
                || effect.type == PacketRToolEffect.GUN_KX_RAIL) drawRailgun(start, effect.targets);
        else if (effect.type == PacketRToolEffect.GUN_NET) {
            drawGlowLine(start, effect.targets.get(0), 0.92F, 0.82F, 0.18F);
            drawMuzzleBloom(start, effect.targets.get(0), 0.35F, 1.0F, 0.82F, 0.32D);
            drawEnergyNet(effect.targets.get(0));
            drawImpactBurst(effect.targets.get(0), 0.92F, 0.82F, 0.18F, effect.sourceId);
        } else if (effect.type == PacketRToolEffect.GUN_CANNON
                || effect.type == PacketRToolEffect.GUN_CANNON_X10) {
            boolean x10 = effect.type == PacketRToolEffect.GUN_CANNON_X10;
            drawCannonBeam(start, effect.targets.get(0), x10);
        } else {
            for (int i = 0; i < effect.targets.size(); i++) {
                Vec3d offset = new Vec3d(0.0D, -0.28D, 0.0D);
                boolean aqua =
                        effect.type == PacketRToolEffect.GUN_KX
                                || effect.type == PacketRToolEffect.GUN_KX_SPREAD;
                boolean yellow =
                        effect.type == PacketRToolEffect.GUN_LASER_X10
                                || effect.type == PacketRToolEffect.GUN_SHOTGUN_X10;
                boolean gunShotgun =
                        effect.type == PacketRToolEffect.GUN_SHOTGUN
                                || effect.type == PacketRToolEffect.GUN_SHOTGUN_X10;
                float r = aqua ? 0.18F : 1.0F;
                float g = aqua ? 1.0F : yellow ? 0.76F : (gunShotgun ? 0.18F : 0.32F);
                float b = aqua ? 0.92F : yellow ? 0.12F : 0.06F;
                if (effect.type == PacketRToolEffect.SHOTGUN) g = 0.14F;
                if (gunShotgun && i > 0)
                    drawLine(start.add(offset), effect.targets.get(i), 1.45F, r, g, b, 0.72F);
                else drawGlowLine(start.add(offset), effect.targets.get(i), r, g, b);
                if (i == 0)
                    drawMuzzleBloom(
                            start.add(offset),
                            effect.targets.get(i),
                            r,
                            g,
                            b,
                            gunShotgun ? 0.34D : 0.22D);
                drawImpactBurst(effect.targets.get(i), r, g, b, effect.sourceId + i * 31);
            }
        }
    }

    private static void renderArmorFlightVfx(float partialTicks) {
        Minecraft minecraft = Minecraft.getMinecraft();
        for (EntityPlayer player : minecraft.world.playerEntities) {
            boolean kx = KXSystems.hasSet(player), x10 = !kx && X10Systems.hasSet(player);
            if ((!kx && !x10) || RmorEventHandler.getPowerLevel(player) == 0 || player.onGround)
                continue;
            if (!player.capabilities.isFlying
                    && player.motionY <= .035D
                    && Math.abs(player.motionX) + Math.abs(player.motionZ) < .18D) continue;
            Vec3d center =
                    new Vec3d(
                            player.prevPosX + (player.posX - player.prevPosX) * partialTicks,
                            player.prevPosY
                                    + (player.posY - player.prevPosY) * partialTicks
                                    + 1.15D,
                            player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks);
            double yaw = Math.toRadians(-player.renderYawOffset),
                    sin = Math.sin(yaw),
                    cos = Math.cos(yaw);
            Vec3d side = new Vec3d(cos, 0, sin), back = new Vec3d(sin, 0, -cos);
            int jets = kx ? 4 : 2;
            for (int i = 0; i < jets; i++) {
                double across = kx ? (-.48D + i * .32D) : (-.28D + i * .56D);
                double height = kx && ((i & 1) == 0) ? .22D : 0D;
                Vec3d nozzle =
                        center.add(side.scale(across))
                                .add(back.scale(.28D))
                                .addVector(0, height, 0);
                double length = kx ? 1.25D : 0.78D;
                Vec3d end = nozzle.add(back.scale(length)).addVector(0, -.3D - (i & 1) * .08D, 0);
                float r = kx ? (i < 2 ? .16F : .54F) : 1F, g = kx ? .9F : .7F, b = kx ? 1F : .08F;
                drawLine(nozzle, end, kx ? 9F : 6F, r, g, b, .13F);
                drawLine(nozzle, end, kx ? 2.8F : 2F, r, g, b, .88F);
                drawMuzzleBloom(nozzle, end, r, g, b, kx ? .24D : .17D);
            }
        }
    }

    private static void drawKXScythe(Vec3d center) {
        double phase = Minecraft.getSystemTime() * 0.006D;
        GL11.glLineWidth(4.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i <= 20; i++) {
            double angle = -1.15D + i * (2.3D / 20.0D) + phase % 0.08D;
            Vec3d point = center.addVector(Math.cos(angle) * 3.2D, 0.18D, Math.sin(angle) * 3.2D);
            vertex(buffer, point, 0.15F, 1.0F, 0.88F, 0.82F);
        }
        tessellator.draw();
        drawImpactBurst(center, 0.15F, 1.0F, 0.88F, 73);
    }

    private static void drawCannonBeam(Vec3d start, Vec3d end, boolean x10) {
        float green = x10 ? 0.78F : 0.16F;
        float blue = x10 ? 0.12F : 0.04F;
        drawLine(start, end, x10 ? 15.0F : 11.0F, 1.0F, green * 0.28F, blue, 0.18F);
        drawLine(start, end, x10 ? 5.0F : 3.8F, 1.0F, green, blue, 0.96F);
        drawMuzzleBloom(start, end, 1.0F, green, blue, x10 ? 0.52D : 0.4D);
        Vec3d axis = end.subtract(start);
        if (axis.lengthSquared() < 0.001D) return;
        axis = axis.normalize();
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < 0.001D) side = axis.crossProduct(new Vec3d(1, 0, 0));
        side = side.normalize();
        Vec3d up = axis.crossProduct(side).normalize();
        double phase = Minecraft.getSystemTime() * 0.014D;
        GL11.glLineWidth(x10 ? 3.2F : 2.4F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 16; i++) {
            double angle = phase + i * Math.PI * 2.0D / 16.0D;
            double radius = (x10 ? 1.15D : 0.78D) * (i % 2 == 0 ? 1.0D : 0.72D);
            vertex(
                    buffer,
                    end.add(side.scale(Math.cos(angle) * radius))
                            .add(up.scale(Math.sin(angle) * radius)),
                    1.0F,
                    green,
                    blue,
                    0.86F);
        }
        tessellator.draw();
        drawImpactBurst(end, 1.0F, green, blue, 79);
    }

    private static void drawMuzzleBloom(
            Vec3d start, Vec3d end, float r, float g, float b, double radius) {
        Vec3d axis = end.subtract(start);
        if (axis.lengthSquared() < 0.001D) return;
        axis = axis.normalize();
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < 0.001D) side = axis.crossProduct(new Vec3d(1, 0, 0));
        side = side.normalize();
        Vec3d up = axis.crossProduct(side).normalize();
        Vec3d diagonalA = side.add(up).normalize();
        Vec3d diagonalB = side.subtract(up).normalize();
        GL11.glLineWidth(2.2F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        bloomRay(buffer, start, side, radius, r, g, b);
        bloomRay(buffer, start, up, radius, r, g, b);
        bloomRay(buffer, start, diagonalA, radius * 0.72D, r, g, b);
        bloomRay(buffer, start, diagonalB, radius * 0.72D, r, g, b);
        tessellator.draw();
    }

    private static void bloomRay(
            BufferBuilder buffer,
            Vec3d center,
            Vec3d axis,
            double radius,
            float r,
            float g,
            float b) {
        vertex(buffer, center.add(axis.scale(-radius)), r, g, b, 0.08F);
        vertex(buffer, center, r, g, b, 0.95F);
        vertex(buffer, center, r, g, b, 0.95F);
        vertex(buffer, center.add(axis.scale(radius)), r, g, b, 0.08F);
    }

    private static void drawRailgun(Vec3d start, List<Vec3d> points) {
        if (points.isEmpty()) return;
        GL11.glLineWidth(11.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        vertex(buffer, start, 0.25F, 0.78F, 1.0F, 0.18F);
        for (Vec3d point : points) vertex(buffer, point, 0.25F, 0.78F, 1.0F, 0.18F);
        tessellator.draw();
        GL11.glLineWidth(3.2F);
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
        vertex(buffer, start, 0.78F, 0.96F, 1.0F, 0.96F);
        for (Vec3d point : points) vertex(buffer, point, 0.78F, 0.96F, 1.0F, 0.96F);
        tessellator.draw();
        for (int i = 0; i < points.size(); i++)
            drawImpactBurst(points.get(i), 0.3F, 0.84F, 1.0F, 101 + i * 17);
    }

    private static void drawImpactBurst(Vec3d center, float r, float g, float b, int seed) {
        double phase = Minecraft.getSystemTime() * 0.016D + seed * 0.73D;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        GL11.glLineWidth(2.1F);
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 12; i++) {
            double angle = phase + i * Math.PI * 2.0D / 12.0D;
            double lift = Math.sin(phase * 0.7D + i * 2.13D) * 0.62D;
            double length = 0.24D + ((i * 37 + seed) & 7) * 0.055D;
            Vec3d direction = new Vec3d(Math.cos(angle), lift, Math.sin(angle)).normalize();
            vertex(buffer, center.add(direction.scale(0.08D)), r, g, b, 0.92F);
            vertex(buffer, center.add(direction.scale(length)), r, g, b, 0.04F);
        }
        tessellator.draw();
        GL11.glLineWidth(1.6F);
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        double radius = 0.28D + Math.sin(phase * .8D) * .05D;
        for (int i = 0; i < 24; i++) {
            double angle = i * Math.PI * 2.0D / 24.0D;
            vertex(
                    buffer,
                    center.addVector(Math.cos(angle) * radius, 0.03D, Math.sin(angle) * radius),
                    r,
                    g,
                    b,
                    0.58F);
        }
        tessellator.draw();
    }

    private static void drawEnergyNet(Vec3d center) {
        double radius = 0.9D;
        GL11.glLineWidth(2.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 2.0D / 8.0D;
            Vec3d ring = center.addVector(Math.cos(angle) * radius, Math.sin(angle) * radius, 0.0D);
            vertex(buffer, center.addVector(0, -0.35D, 0), 1.0F, 0.82F, 0.16F, 0.88F);
            vertex(buffer, ring, 1.0F, 0.82F, 0.16F, 0.88F);
            Vec3d next =
                    center.addVector(
                            Math.cos(angle + Math.PI * 0.25D) * radius,
                            Math.sin(angle + Math.PI * 0.25D) * radius,
                            0.0D);
            vertex(buffer, ring, 1.0F, 0.82F, 0.16F, 0.72F);
            vertex(buffer, next, 1.0F, 0.82F, 0.16F, 0.72F);
        }
        tessellator.draw();
    }

    private static Vec3d interpolatedEyes(EntityLivingBase entity, float partialTicks) {
        return new Vec3d(
                entity.prevPosX + (entity.posX - entity.prevPosX) * partialTicks,
                entity.prevPosY
                        + (entity.posY - entity.prevPosY) * partialTicks
                        + entity.getEyeHeight()
                        - 0.18D,
                entity.prevPosZ + (entity.posZ - entity.prevPosZ) * partialTicks);
    }

    private static void drawHelix(Vec3d start, Vec3d end) {
        Vec3d axis = end.subtract(start);
        double length = axis.lengthVector();
        if (length < 0.01D) return;
        axis = axis.normalize();
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < 0.01D) side = axis.crossProduct(new Vec3d(1, 0, 0));
        side = side.normalize();
        Vec3d up = axis.crossProduct(side).normalize();
        double phase = Minecraft.getSystemTime() * 0.018D;
        for (int strand = 0; strand < 3; strand++) {
            GL11.glLineWidth(strand == 0 ? 5.0F : 2.2F);
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0; i <= 56; i++) {
                double t = i / 56.0D;
                double angle = phase + t * Math.PI * 14.0D + strand * Math.PI * 2.0D / 3.0D;
                double radius = 0.06D + Math.sin(t * Math.PI) * 0.11D;
                Vec3d point =
                        start.add(axis.scale(length * t))
                                .add(side.scale(Math.cos(angle) * radius))
                                .add(up.scale(Math.sin(angle) * radius));
                vertex(
                        buffer,
                        point,
                        1.0F,
                        strand == 0 ? 0.12F : 0.38F,
                        0.04F,
                        strand == 0 ? 0.34F : 0.86F);
            }
            tessellator.draw();
        }
        drawGlowLine(start, end, 1.0F, 0.46F, 0.10F);
    }

    private static void drawSaw(Vec3d start, Vec3d end) {
        drawGlowLine(start, end, 1.0F, 0.22F, 0.04F);
        Vec3d axis = end.subtract(start).normalize();
        Vec3d side = axis.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < 0.01D) side = new Vec3d(1, 0, 0);
        side = side.normalize();
        Vec3d up = axis.crossProduct(side).normalize();
        double phase = Minecraft.getSystemTime() * 0.022D;
        GL11.glLineWidth(3.0F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < 24; i++) {
            double angle = phase + i * Math.PI * 2.0D / 24.0D;
            double radius = i % 2 == 0 ? 0.54D : 0.40D;
            vertex(
                    buffer,
                    end.add(side.scale(Math.cos(angle) * radius))
                            .add(up.scale(Math.sin(angle) * radius)),
                    1.0F,
                    0.25F,
                    0.04F,
                    0.9F);
        }
        tessellator.draw();
    }

    private static void drawGlowLine(Vec3d start, Vec3d end, float r, float g, float b) {
        drawLine(start, end, 7.0F, r, g * 0.45F, b, 0.22F);
        drawLine(start, end, 2.4F, r, g, b, 0.92F);
    }

    private static void drawLine(
            Vec3d start, Vec3d end, float width, float r, float g, float b, float a) {
        GL11.glLineWidth(width);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        vertex(buffer, start, r, g, b, a);
        vertex(buffer, end, r, g, b, a);
        tessellator.draw();
    }

    private static void renderSchematic(EntityLivingBase player) {
        ItemStack tool = heldRTool(player);
        if (tool.isEmpty() || ItemRTool.getForm(tool) != ItemRTool.BUILD || !tool.hasTagCompound())
            return;
        NBTTagCompound nbt = tool.getTagCompound();
        if (!nbt.hasKey("Schematic", 9) || !nbt.hasKey("PrintOriginX")) return;
        NBTTagList blocks = nbt.getTagList("Schematic", 10);
        BlockPos origin =
                new BlockPos(
                        nbt.getInteger("PrintOriginX"),
                        nbt.getInteger("PrintOriginY"),
                        nbt.getInteger("PrintOriginZ"));
        GL11.glLineWidth(1.1F);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder buffer = tessellator.getBuffer();
        buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR);
        for (int i = 0; i < blocks.tagCount(); i++) {
            NBTTagCompound entry = blocks.getCompoundTagAt(i);
            BlockPos pos =
                    origin.add(entry.getShort("X"), entry.getShort("Y"), entry.getShort("Z"));
            box(
                    buffer,
                    pos.getX() + 0.06D,
                    pos.getY() + 0.06D,
                    pos.getZ() + 0.06D,
                    pos.getX() + 0.94D,
                    pos.getY() + 0.94D,
                    pos.getZ() + 0.94D,
                    1.0F,
                    0.14F + (i % 4) * 0.06F,
                    0.04F,
                    0.28F);
        }
        tessellator.draw();
    }

    private static void box(
            BufferBuilder b,
            double x0,
            double y0,
            double z0,
            double x1,
            double y1,
            double z1,
            float r,
            float g,
            float blue,
            float a) {
        line(b, x0, y0, z0, x1, y0, z0, r, g, blue, a);
        line(b, x1, y0, z0, x1, y0, z1, r, g, blue, a);
        line(b, x1, y0, z1, x0, y0, z1, r, g, blue, a);
        line(b, x0, y0, z1, x0, y0, z0, r, g, blue, a);
        line(b, x0, y1, z0, x1, y1, z0, r, g, blue, a);
        line(b, x1, y1, z0, x1, y1, z1, r, g, blue, a);
        line(b, x1, y1, z1, x0, y1, z1, r, g, blue, a);
        line(b, x0, y1, z1, x0, y1, z0, r, g, blue, a);
        line(b, x0, y0, z0, x0, y1, z0, r, g, blue, a);
        line(b, x1, y0, z0, x1, y1, z0, r, g, blue, a);
        line(b, x1, y0, z1, x1, y1, z1, r, g, blue, a);
        line(b, x0, y0, z1, x0, y1, z1, r, g, blue, a);
    }

    private static void line(
            BufferBuilder b,
            double x0,
            double y0,
            double z0,
            double x1,
            double y1,
            double z1,
            float r,
            float g,
            float blue,
            float a) {
        vertex(b, new Vec3d(x0, y0, z0), r, g, blue, a);
        vertex(b, new Vec3d(x1, y1, z1), r, g, blue, a);
    }

    private static void vertex(
            BufferBuilder buffer, Vec3d point, float r, float g, float b, float a) {
        Minecraft minecraft = Minecraft.getMinecraft();
        buffer.pos(
                        point.x - minecraft.getRenderManager().viewerPosX,
                        point.y - minecraft.getRenderManager().viewerPosY,
                        point.z - minecraft.getRenderManager().viewerPosZ)
                .color(r, g, b, a)
                .endVertex();
    }

    private static final class EquipmentEffect {
        final int sourceId;
        final int type;
        final List<Vec3d> targets;
        final long expires;

        EquipmentEffect(int sourceId, int type, List<Vec3d> targets, long expires) {
            this.sourceId = sourceId;
            this.type = type;
            this.targets = targets;
            this.expires = expires;
        }
    }
}
