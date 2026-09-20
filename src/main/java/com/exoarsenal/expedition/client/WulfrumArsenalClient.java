package com.exoarsenal.expedition.client;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.expedition.*;
import com.exoarsenal.network.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.*;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID, value = Side.CLIENT)
public final class WulfrumArsenalClient {
    private static final class Pose {
        PacketWulfrumState state;
        long received, impact = -100;
    }

    private static final Map<Integer, Pose> POSES = new HashMap<>();
    private static Object world;
    private static boolean useHeld;
    private static int delay;

    public static void receive(PacketWulfrumState packet) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null) return;
        if (world != mc.world) {
            POSES.clear();
            world = mc.world;
        }
        Pose old = POSES.get(packet.id), pose = new Pose();
        pose.state = packet;
        pose.received = mc.world.getTotalWorldTime();
        if (old != null) {
            pose.impact = old.impact;
            if (old.state.move == 6 && packet.move < 0) pose.impact = pose.received;
        }
        POSES.put(packet.id, pose);
    }

    private static Pose pose(EntityPlayer p) {
        return world == p.world ? POSES.get(p.getEntityId()) : null;
    }

    private static boolean robot(EntityPlayer p) {
        Pose s = pose(p);
        return s != null && s.state.android;
    }

    private static float age(EntityPlayer p, float partial) {
        Pose s = pose(p);
        return s == null
                ? 0
                : s.state.age + Math.min(5, p.world.getTotalWorldTime() - s.received) + partial;
    }

    private static float strike(EntityPlayer p, float partial) {
        Pose s = pose(p);
        if (s == null || s.state.move < 0) return 0;
        float t = age(p, partial), contact = s.state.move == 0 ? 7 : s.state.move == 5 ? 22 : 5;
        return (float) Math.sin(Math.PI * Math.min(1, t / (contact * 2)));
    }

    private static boolean controls(EntityPlayer p) {
        return p.getHeldItemMainhand().getItem() instanceof WulfrumArsenal.Weapon
                || robot(p) && p.getHeldItemMainhand().isEmpty();
    }

    @SubscribeEvent
    public static void effects(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || world != mc.world) return;
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                -mc.getRenderManager().viewerPosX,
                -mc.getRenderManager().viewerPosY,
                -mc.getRenderManager().viewerPosZ);
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(770, 771);
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (Map.Entry<Integer, Pose> entry : POSES.entrySet()) {
            net.minecraft.entity.Entity entity = mc.world.getEntityByID(entry.getKey());
            if (!(entity instanceof EntityPlayer)) continue;
            EntityPlayer p = (EntityPlayer) entity;
            Pose pose = entry.getValue();
            float t = age(p, event.getPartialTicks());
            if (pose.state.move == 5 && t >= 3 && t < 28) {
                net.minecraft.util.math.Vec3d
                        from = p.getPositionEyes(event.getPartialTicks()).addVector(0, -.35, 0),
                        to;
                net.minecraft.entity.Entity target = mc.world.getEntityByID(pose.state.target);
                if (target != null)
                    to = target.getPositionVector().addVector(0, target.height * .5, 0);
                else
                    to =
                            from.add(
                                    p.getLookVec()
                                            .scale(12 * Math.sin(Math.PI * Math.min(1, t / 28))));
                net.minecraft.util.math.Vec3d delta = to.subtract(from);
                int links = Math.min(48, Math.max(2, (int) (delta.lengthVector() * 4)));
                for (int i = 0; i < links; i++)
                    com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                            b,
                            from.add(delta.scale(i / (double) links)),
                            from.add(delta.scale((i + .8) / links)),
                            i % 2 == 0 ? .055 : .04,
                            i % 3 == 0 ? 0x86D2A4 : 0x647969,
                            1);
                net.minecraft.util.math.Vec3d side =
                        p.getLookVec()
                                .crossProduct(new net.minecraft.util.math.Vec3d(0, 1, 0))
                                .normalize()
                                .scale(.25);
                for (int s : new int[] {-1, 1})
                    com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                            b,
                            to.add(side.scale(s)).addVector(0, .25, 0),
                            to.add(side.scale(s * .3)),
                            .09,
                            0xC3D6B4,
                            1);
            }
            float elapsed = mc.world.getTotalWorldTime() - pose.impact + event.getPartialTicks();
            if (elapsed >= 0 && elapsed < 12) {
                double radius = .4 + elapsed * .35;
                net.minecraft.util.math.Vec3d center = p.getPositionVector().addVector(0, .1, 0);
                for (int i = 0; i < 6; i++) {
                    double a = i * Math.PI / 3, z = (i + 1) * Math.PI / 3;
                    com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                            b,
                            center.addVector(Math.cos(a) * radius, 0, Math.sin(a) * radius),
                            center.addVector(Math.cos(z) * radius, 0, Math.sin(z) * radius),
                            .06,
                            0x86FFB7,
                            1 - elapsed / 12);
                }
            }
        }
        Tessellator.getInstance().draw();
        GlStateManager.disableBlend();
        GlStateManager.enableCull();
        GlStateManager.enableLighting();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
        GlStateManager.color(1, 1, 1, 1);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void mouse(MouseEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null || !controls(mc.player)) return;
        if (e.getButton() == 0 || e.getButton() == 1) {
            e.setCanceled(true);
            if (e.getButton() == 1) useHeld = e.isButtonstate();
            if (e.isButtonstate()) {
                ModNetwork.CHANNEL.sendToServer(
                        new PacketEquipmentAction(e.getButton() == 0 ? 40 : 41));
                delay = 4;
            }
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent e) {
        if (e.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world != world) {
            POSES.clear();
            world = mc.world;
            useHeld = false;
        }
        if (delay > 0) delay--;
        if (mc.player == null || mc.currentScreen != null) {
            useHeld = false;
            return;
        }
        if (useHeld && !org.lwjgl.input.Mouse.isButtonDown(1)) useHeld = false;
        if (useHeld
                && delay == 0
                && mc.player.getHeldItemMainhand().getItem() == WulfrumArsenal.LITTLE_BOY) {
            ModNetwork.CHANNEL.sendToServer(new PacketEquipmentAction(41));
            delay = 4;
        }
    }

    public static void mesh(String name) {
        mesh(WulfrumArsenalModels.get(name));
    }

    public static void mesh(ExpeditionMesh mesh) {
        GlStateManager.disableTexture2D();
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        BufferBuilder b = Tessellator.getInstance().getBuffer();
        b.begin(7, DefaultVertexFormats.POSITION_COLOR);
        for (ExpeditionMesh.Face f : mesh.faces) {
            float shade = .72F + .18F * f.ny - .1F * f.nz;
            int color = f.color;
            if (color == WulfrumArsenalModels.GREEN || color == WulfrumArsenalModels.CORE)
                shade = 1;
            for (double[] v : f.p)
                b.pos(v[0] / 16, v[1] / 16, v[2] / 16)
                        .color(
                                (color >> 16 & 255) / 255F * shade,
                                (color >> 8 & 255) / 255F * shade,
                                (color & 255) / 255F * shade,
                                1)
                        .endVertex();
        }
        Tessellator.getInstance().draw();
        GlStateManager.enableCull();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.color(1, 1, 1, 1);
    }

    private static void part(String id, double x, double y, double z, float angle) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, z);
        GlStateManager.rotate(angle, 1, 0, 0);
        mesh(id);
        GlStateManager.popMatrix();
    }

    private static void hand(float open) {
        mesh("palm");
        for (int i = 0; i < 4; i++) {
            GlStateManager.pushMatrix();
            GlStateManager.translate((i - 1.5) * .05, -.1, -.025);
            GlStateManager.rotate(55 - open * 50, 1, 0, 0);
            mesh("finger");
            GlStateManager.translate(0, -.075, 0);
            GlStateManager.rotate(55 - open * 40, 1, 0, 0);
            mesh("finger");
            GlStateManager.popMatrix();
        }
    }

    private static void vice(float open) {
        GlStateManager.pushMatrix();
        GlStateManager.scale(1.4, 1.2, 1.4);
        mesh("palm");
        for (int side : new int[] {-1, 1}) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .1, -.1, 0);
            GlStateManager.rotate(side * (15 + open * 40), 0, 0, 1);
            GlStateManager.scale(1.8, 1.4, 1.4);
            mesh("finger");
            GlStateManager.translate(0, -.08, 0);
            GlStateManager.rotate(-side * 45, 0, 0, 1);
            mesh("finger");
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private static void arm(int side, float swing, boolean lower, float open) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(side * (lower ? .3 : .38), lower ? 1.07 : 1.4, lower ? .13 : 0);
        GlStateManager.rotate(side * (lower ? 22 : 12), 0, 0, 1);
        GlStateManager.rotate(swing, 1, 0, 0);
        mesh("upper_arm");
        GlStateManager.translate(0, -.3, 0);
        GlStateManager.rotate(-18 - Math.max(0, -swing) * .25F, 1, 0, 0);
        mesh("forearm");
        GlStateManager.translate(0, -.32, 0);
        if (lower) vice(open);
        else hand(open);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void player(RenderPlayerEvent.Pre e) {
        EntityPlayer p = e.getEntityPlayer();
        if (!robot(p)) return;
        e.setCanceled(true);
        float partial = e.getPartialRenderTick(),
                walk = (float) Math.sin(p.limbSwing * .65) * p.limbSwingAmount * 35,
                hit = strike(p, partial);
        Pose state = pose(p);
        int move = state == null ? -1 : state.state.move,
                combo = state == null ? 0 : state.state.combo;
        GlStateManager.pushMatrix();
        GlStateManager.translate(e.getX(), e.getY(), e.getZ());
        GlStateManager.rotate(
                180
                        - (p.prevRenderYawOffset
                                + (p.renderYawOffset - p.prevRenderYawOffset) * partial),
                0,
                1,
                0);
        part("torso", 0, 1.15, 0, move == 6 ? hit * 25 : hit * 8);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0, 1.7, 0);
        GlStateManager.rotate(-(p.rotationYawHead - p.renderYawOffset), 0, 1, 0);
        GlStateManager.rotate(p.rotationPitch, 1, 0, 0);
        mesh("head");
        GlStateManager.popMatrix();
        for (int side : new int[] {-1, 1}) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(side * .14, .8, 0);
            GlStateManager.rotate(walk * side, 1, 0, 0);
            mesh("leg");
            GlStateManager.translate(0, -.3, 0);
            GlStateManager.rotate(Math.max(0, -walk * side) * .6F, 1, 0, 0);
            mesh("shin");
            GlStateManager.popMatrix();
        }
        for (int side : new int[] {-1, 1}) {
            boolean active = side == (combo % 2 == 0 ? 1 : -1);
            float reach =
                    move == 6 ? -125 * hit : move == 5 ? -90 * hit : active ? -110 * hit : 20 * hit;
            arm(side, -walk * side * .5F + reach, false, move == 5 ? hit : 0);
            arm(
                    side,
                    move == 6 ? -120 * hit : move == 5 ? -75 * hit : 20 + hit * 30,
                    true,
                    move == 5 ? hit : 0);
        }
        if (!p.getHeldItemMainhand().isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(-.44, .8, -.25);
            Minecraft.getMinecraft()
                    .getItemRenderer()
                    .renderItemSide(
                            p,
                            p.getHeldItemMainhand(),
                            ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND,
                            false);
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void hand(RenderSpecificHandEvent e) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayer p = mc.player;
        if (p == null) return;
        boolean robot = robot(p);
        ItemStack held = e.getItemStack();
        if (e.getHand() == EnumHand.OFF_HAND
                && held.isEmpty()
                && p.getHeldItemMainhand().getItem() == WulfrumArsenal.TALONS)
            held = p.getHeldItemMainhand();
        boolean pg = held.getItem() instanceof WulfrumArsenal.Weapon;
        if (!robot && !pg) return;
        e.setCanceled(true);
        int side = e.getHand() == EnumHand.MAIN_HAND ? 1 : -1;
        float hit = strike(p, e.getPartialTicks());
        Pose state = pose(p);
        GlStateManager.pushMatrix();
        GlStateManager.translate(side * .48, -.43, -.7 - hit * .35);
        GlStateManager.rotate(side * (pg ? -18 : 8) + side * hit * 35, 0, 1, 0);
        GlStateManager.rotate(-65 + hit * 25, 1, 0, 0);
        if (robot) {
            mesh("forearm");
            GlStateManager.translate(0, -.32, 0);
            hand(state != null && state.state.move == 5 ? hit : 0);
        }
        if (pg) {
            GlStateManager.rotate(65, 1, 0, 0);
            GlStateManager.rotate(90, 0, 1, 0);
            GlStateManager.scale(.8, .8, .8);
            mesh(held.getItem().getRegistryName().getResourcePath());
        } else if (!held.isEmpty()) {
            GlStateManager.rotate(65, 1, 0, 0);
            mc.getItemRenderer()
                    .renderItemSide(
                            p,
                            held,
                            side == 1
                                    ? ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                                    : ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND,
                            side < 0);
        }
        GlStateManager.popMatrix();
    }
}
