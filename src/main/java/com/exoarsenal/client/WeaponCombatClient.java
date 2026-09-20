package com.exoarsenal.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.combat.*;
import com.exoarsenal.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.*;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class WeaponCombatClient {
    private static final KeyBinding HEAVY =
            new KeyBinding(
                    "key.exoarsenal.heavy_attack", Keyboard.KEY_R, "key.categories.exoarsenal");
    private static final KeyBinding DODGE =
            new KeyBinding("key.exoarsenal.dodge", Keyboard.KEY_V, "key.categories.exoarsenal");
    private static final KeyBinding PARRY =
            new KeyBinding(
                    "key.exoarsenal.parry",
                    net.minecraftforge.client.settings.KeyConflictContext.IN_GAME,
                    net.minecraftforge.client.settings.KeyModifier.CONTROL,
                    Keyboard.KEY_X,
                    "key.categories.exoarsenal");
    private static boolean registered;
    private static final ModelPlayer NORMAL_ARMS = new ModelPlayer(0, false),
            SLIM_ARMS = new ModelPlayer(0, true);

    @SubscribeEvent
    public static void register(ModelRegistryEvent e) {
        if (!registered) {
            ClientRegistry.registerKeyBinding(HEAVY);
            ClientRegistry.registerKeyBinding(DODGE);
            ClientRegistry.registerKeyBinding(PARRY);
            registered = true;
        }
    }

    private static void request(int action) {
        Minecraft m = Minecraft.getMinecraft();
        if (m.player != null
                && m.currentScreen == null
                && (action == WeaponCombat.PARRY
                        || WeaponDiscipline.of(m.player.getHeldItemMainhand()) != null))
            ModNetwork.CHANNEL.sendToServer(
                    new PacketCombatAction(
                            action,
                            m.player.movementInput.moveForward,
                            m.player.movementInput.moveStrafe));
    }

    @SubscribeEvent
    public static void keys(InputEvent.KeyInputEvent e) {
        if (HEAVY.isPressed()) request(WeaponCombat.HEAVY);
        if (DODGE.isPressed()) request(WeaponCombat.DODGE);
        boolean pressed = PARRY.isPressed();
        if (pressed
                || Keyboard.getEventKeyState()
                        && Keyboard.getEventKey() == Keyboard.KEY_X
                        && (Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                                || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)))
            request(WeaponCombat.PARRY);
    }

    @SubscribeEvent
    public static void tooltip(net.minecraftforge.event.entity.player.ItemTooltipEvent e) {
        WeaponDiscipline d = WeaponDiscipline.of(e.getItemStack());
        if (d == null) return;
        e.getToolTip()
                .add(
                        net.minecraft.util.text.TextFormatting.DARK_GRAY
                                + "Combat: left click / "
                                + HEAVY.getDisplayName()
                                + " heavy");
        e.getToolTip()
                .add(
                        net.minecraft.util.text.TextFormatting.DARK_GRAY
                                + DODGE.getDisplayName()
                                + " dodge / "
                                + PARRY.getDisplayName()
                                + " parry");
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void click(MouseEvent e) {
        Minecraft m = Minecraft.getMinecraft();
        if (m.player == null || m.currentScreen != null || e.getButton() != 0 || !e.isButtonstate())
            return;
        if (com.exoarsenal.event.RmorEventHandler.isDefenseForm(m.player)) return;
        if (WeaponDiscipline.of(m.player.getHeldItemMainhand()) == null) return;
        if (m.objectMouseOver != null && m.objectMouseOver.typeOfHit == RayTraceResult.Type.BLOCK)
            return;
        e.setCanceled(true);
        request(WeaponCombat.LIGHT);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void model(RenderPlayerEvent.Pre e) {
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.color(1, 1, 1, 1);

        Class<?> type = e.getRenderer().getMainModel().getClass();
        if (type != ModelPlayer.class) return;
        boolean slim =
                e.getEntityPlayer() instanceof AbstractClientPlayer
                        && "slim"
                                .equals(((AbstractClientPlayer) e.getEntityPlayer()).getSkinType());
        ModelPlayer replacement = new CombatPlayerModel(slim);
        ObfuscationReflectionHelper.setPrivateValue(
                RenderLivingBase.class, e.getRenderer(), replacement, "mainModel", "field_77045_g");
        if (ExoArsenal.LOGGER != null)
            ExoArsenal.LOGGER.info(
                    "Installed combat player poses: {} -> {} (slim={})",
                    type.getName(),
                    replacement.getClass().getName(),
                    slim);
        java.util.List<net.minecraft.client.renderer.entity.layers.LayerRenderer<?>> layers =
                ObfuscationReflectionHelper.getPrivateValue(
                        RenderLivingBase.class,
                        e.getRenderer(),
                        "layerRenderers",
                        "field_177097_h");
        for (int i = 0; i < layers.size(); i++)
            if (layers.get(i).getClass()
                    == net.minecraft.client.renderer.entity.layers.LayerHeldItem.class)
                layers.set(i, new CombatHeldItemLayer(e.getRenderer()));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hand(RenderSpecificHandEvent e) {
        Minecraft m = Minecraft.getMinecraft();
        if (m.player == null) return;
        if (com.exoarsenal.event.RmorEventHandler.isDefenseForm(m.player)) return;
        boolean right =
                (m.player.getPrimaryHand() == EnumHandSide.RIGHT)
                        == (e.getHand() == EnumHand.MAIN_HAND);
        float sign = right ? 1 : -1;
        if (CombatPlayerModel.gun(e.getItemStack())) {
            e.setCanceled(true);
            GlStateManager.pushMatrix();
            try {
                float age =
                        e.getItemStack().hasTagCompound()
                                ? m.world.getTotalWorldTime()
                                        - e.getItemStack()
                                                .getTagCompound()
                                                .getLong("GunAnimationTick")
                                        + e.getPartialTicks()
                                : 99;
                float recoil = age >= 0 && age < 8 ? CombatMotion.recoil(age / 8) : 0;
                GlStateManager.translate(
                        sign * 0.38F,
                        -0.30F - e.getEquipProgress() * 0.5F,
                        -0.68F + recoil * 0.12F);
                GlStateManager.rotate(-recoil * 4, 1, 0, 0);
                m.getItemRenderer()
                        .renderItemSide(
                                m.player,
                                e.getItemStack(),
                                right
                                        ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                                        : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !right);
                if (!m.player.isInvisible()) {
                    int type =
                            e.getItemStack().getItem() instanceof com.exoarsenal.item.ItemEnergyGun
                                    ? ((com.exoarsenal.item.ItemEnergyGun)
                                                    e.getItemStack().getItem())
                                            .getType()
                                            .ordinal()
                                    : -1;
                    float[] grip = GunGripPose.socket(type, right, false);
                    renderArm(m, right, grip[0], grip[1], grip[2], -65, sign * 5);
                    net.minecraft.item.ItemStack other =
                            m.player.getHeldItem(
                                    e.getHand() == EnumHand.MAIN_HAND
                                            ? EnumHand.OFF_HAND
                                            : EnumHand.MAIN_HAND);
                    if (other.isEmpty() && CombatPlayerModel.braced(e.getItemStack())) {
                        float[] support = GunGripPose.socket(type, right, true);
                        renderArm(m, !right, support[0], support[1], support[2], -75, -sign * 28);
                    }
                }
            } finally {
                GlStateManager.popMatrix();
            }
            return;
        }
        if (e.getHand() != EnumHand.MAIN_HAND) return;
        float brace = ExcavatorParryEffects.brace(m.player, e.getPartialTicks());
        if (brace > 0 && WeaponDiscipline.of(e.getItemStack()) != null) {
            e.setCanceled(true);
            GlStateManager.pushMatrix();
            try {
                GlStateManager.translate(
                        sign * (.38F - .17F * brace), -.3F + .18F * brace, -.68F + .14F * brace);
                GlStateManager.rotate(-22 * brace, 1, 0, 0);
                GlStateManager.rotate(sign * 58 * brace, 0, 0, 1);
                m.getItemRenderer()
                        .renderItemSide(
                                m.player,
                                e.getItemStack(),
                                right
                                        ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                                        : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                                !right);
                if (!m.player.isInvisible()) {
                    renderArm(m, right, sign * .14F, -.04F, .16F, -58, sign * 18);
                    if (m.player.getHeldItemOffhand().isEmpty())
                        renderArm(m, !right, -sign * .12F, -.08F, .10F, -62, -sign * 26);
                }
            } finally {
                GlStateManager.popMatrix();
            }
            return;
        }
        NBTTagCompound n = m.player.getEntityData();
        int action = n.getInteger("FrostCombatAction"), index = n.getInteger("FrostCombatProfile");
        if ((action != WeaponCombat.LIGHT && action != WeaponCombat.HEAVY)
                || index >= WeaponDiscipline.values().length) return;
        WeaponDiscipline d = WeaponDiscipline.values()[index];
        float age =
                m.world.getTotalWorldTime() - n.getLong("FrostCombatStart") + e.getPartialTicks();
        if (age > d.duration(action == WeaponCombat.HEAVY)) return;
        WeaponPose pose =
                WeaponPose.sample(
                        d, age, action == WeaponCombat.HEAVY, n.getInteger("FrostCombatCombo"));
        GlStateManager.translate(sign * pose.y * 0.22F, -pose.lean * 0.35F, -pose.lean * 0.65F);
        GlStateManager.rotate((pose.x + 0.65F) * 45, 1, 0, 0);
        GlStateManager.rotate(sign * pose.y * 55, 0, 1, 0);
        GlStateManager.rotate(sign * pose.z * 55, 0, 0, 1);
        GlStateManager.rotate(WeaponPose.wrist(d, age, action == WeaponCombat.HEAVY), 1, 0, 0);
    }

    private static void renderArm(
            Minecraft m, boolean right, float x, float y, float z, float pitch, float yaw) {
        ModelPlayer model = "slim".equals(m.player.getSkinType()) ? SLIM_ARMS : NORMAL_ARMS;
        GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
        GlStateManager.enableTexture2D();
        GlStateManager.enableRescaleNormal();
        int light = m.player.getBrightnessForRender();
        OpenGlHelper.setLightmapTextureCoords(
                OpenGlHelper.lightmapTexUnit, light & 65535, light >> 16);
        RenderHelper.enableStandardItemLighting();
        m.getTextureManager().bindTexture(m.player.getLocationSkin());
        GlStateManager.color(1, 1, 1, 1);
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(pitch, 1, 0, 0);
        GlStateManager.rotate(yaw, 0, 0, 1);
        GlStateManager.scale(0.85F, 0.85F, 0.85F);
        GlStateManager.translate((right ? 5 : -5) / 16F, -11 / 16F, 0);
        if (right) {
            model.bipedRightArm.render(1 / 16F);
            model.bipedRightArmwear.render(1 / 16F);
        } else {
            model.bipedLeftArm.render(1 / 16F);
            model.bipedLeftArmwear.render(1 / 16F);
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void stamina(RenderGameOverlayEvent.Post e) {
        Minecraft m = Minecraft.getMinecraft();
        if (e.getType() != RenderGameOverlayEvent.ElementType.ALL
                || m.player == null
                || m.player.isSpectator()
                || WeaponDiscipline.of(m.player.getHeldItemMainhand()) == null) return;
        float value =
                m.player.getEntityData().hasKey("FrostStamina")
                        ? m.player.getEntityData().getFloat("FrostStamina")
                        : 100;
        int x = e.getResolution().getScaledWidth() / 2 - 45,
                y = e.getResolution().getScaledHeight() - 54;
        Gui.drawRect(x - 1, y - 1, x + 91, y + 5, 0xC010171C);
        Gui.drawRect(
                x,
                y,
                x + (int) (90 * Math.max(0, Math.min(100, value)) / 100),
                y + 4,
                value < 25 ? 0xFFE29B52 : 0xFF80CAB1);
    }
}
