package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.client.gui.GuiKXMobEditor;
import com.scapeandrun.frostbite.event.KXSystems;
import com.scapeandrun.frostbite.network.ModNetwork;
import com.scapeandrun.frostbite.network.PacketKXAction;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLiving;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class KXClientHandler {
    private static boolean destructHeld;
    private static int chainDelay;

    private KXClientHandler() {}

    private static boolean alt() {
        return Keyboard.isKeyDown(Keyboard.KEY_LMENU) || Keyboard.isKeyDown(Keyboard.KEY_RMENU);
    }

    private static boolean ctrl() {
        return Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)
                || Keyboard.isKeyDown(Keyboard.KEY_RCONTROL);
    }

    @SubscribeEvent
    public static void key(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null
                || mc.currentScreen != null
                || !Keyboard.getEventKeyState()
                || !KXSystems.hasSet(mc.player)) return;
        int key = Keyboard.getEventKey();
        if (key == Keyboard.KEY_DOWN && alt()) {
            ModNetwork.CHANNEL.sendToServer(new PacketKXAction(PacketKXAction.OVERLOAD, 0));
            return;
        }
        if (key == Keyboard.KEY_LBRACKET
                && GuiScreenShift.shift()
                && mc.pointedEntity instanceof EntityLiving) {
            int id = mc.pointedEntity.getEntityId();
            ModNetwork.CHANNEL.sendToServer(new PacketKXAction(PacketKXAction.EDITOR_BEGIN, id));
            mc.displayGuiScreen(new GuiKXMobEditor(id));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (chainDelay > 0) chainDelay--;
        boolean armed =
                mc.player != null
                        && mc.currentScreen == null
                        && KXSystems.hasSet(mc.player)
                        && ctrl();
        if (armed != destructHeld) {
            destructHeld = armed;
            ModNetwork.CHANNEL.sendToServer(
                    new PacketKXAction(PacketKXAction.SELF_DESTRUCT, armed ? 1 : 0));
        }
        if (mc.player != null
                && mc.currentScreen == null
                && KXSystems.hasSet(mc.player)
                && mc.player.getHeldItemOffhand().isEmpty()
                && alt()
                && Mouse.isButtonDown(0)
                && chainDelay == 0) {
            ModNetwork.CHANNEL.sendToServer(new PacketKXAction(PacketKXAction.CHAINSAW, 0));
            chainDelay = 2;
        }
    }

    @SubscribeEvent
    public static void mouse(MouseEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null
                && KXSystems.hasSet(mc.player)
                && mc.player.getHeldItemOffhand().isEmpty()
                && alt()
                && event.getButton() == 0) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void overlay(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || !KXSystems.hasSet(mc.player)) return;
        if (destructHeld) event.getRight().add("\u00a7cSELF-DESTRUCT ARMING \u2022 HOLD CTRL");
        if (KXSystems.overload(mc.player)) event.getRight().add("\u00a7bOVERLOAD \u2022 4x OUTPUT");
    }

    private static final class GuiScreenShift {
        static boolean shift() {
            return Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT);
        }
    }
}
