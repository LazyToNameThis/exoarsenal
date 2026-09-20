package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityX20Scout;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class ScoutCapturedPlayerRenderer {
    private static boolean rendering;
    private static CapturedRenderer normal, slim;
    private static final java.util.Map<AbstractClientPlayer, Transition> transitions =
            new java.util.WeakHashMap<>();

    private static final class Transition {
        int mode;
        float started, from, angle;

        float sample(int next, float time) {
            if (next != mode) {
                from = angle;
                mode = next;
                started = time;
            }
            float target =
                    mode == 2
                            ? -65
                            : mode == 3 ? 155 : mode == 4 ? (float) Math.sin(time * .16) * 75 : 8;
            float u = Math.max(0, Math.min(1, (time - started) / 6));
            u = u * u * (3 - 2 * u);
            angle = from + (target - from) * u;
            return angle;
        }
    }

    private ScoutCapturedPlayerRenderer() {}

    @SubscribeEvent
    public static void render(RenderPlayerEvent.Pre event) {
        if (rendering || !(event.getEntityPlayer() instanceof AbstractClientPlayer)) return;
        EntityX20Scout owner = null;
        for (EntityX20Scout boss :
                event.getEntityPlayer()
                        .world
                        .getEntitiesWithinAABB(
                                EntityX20Scout.class,
                                event.getEntityPlayer().getEntityBoundingBox().grow(64)))
            if (boss.getCaptured() == event.getEntityPlayer()) {
                owner = boss;
                break;
            }
        com.scapeandrun.frostbite.entity.EntityBrawler brawler = null;
        if (owner == null)
            for (com.scapeandrun.frostbite.entity.EntityBrawler boss :
                    event.getEntityPlayer()
                            .world
                            .getEntitiesWithinAABB(
                                    com.scapeandrun.frostbite.entity.EntityBrawler.class,
                                    event.getEntityPlayer().getEntityBoundingBox().grow(32)))
                if (boss.martialVictim() == event.getEntityPlayer().getEntityId()) {
                    brawler = boss;
                    break;
                }
        if (owner == null && brawler == null) {
            transitions.remove(event.getEntityPlayer());
            return;
        }
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntityPlayer();
        if (normal == null) {
            normal = new CapturedRenderer(false);
            slim = new CapturedRenderer(true);
        }
        CapturedRenderer renderer = player.getSkinType().equals("slim") ? slim : normal;
        renderer.pose.mode = brawler != null ? 2 : owner.getGrabMode();
        renderer.pose.time =
                (brawler != null ? brawler.ticksExisted : owner.ticksExisted)
                        + event.getPartialRenderTick();
        Transition transition = transitions.computeIfAbsent(player, p -> new Transition());
        renderer.pose.tilt =
                brawler != null
                        ? (float) brawler.martialPose(event.getPartialRenderTick()).pitch
                        : transition.sample(renderer.pose.mode, renderer.pose.time);
        event.setCanceled(true);
        rendering = true;
        try {
            renderer.doRender(
                    player,
                    event.getX(),
                    event.getY(),
                    event.getZ(),
                    player.rotationYaw,
                    event.getPartialRenderTick());
        } finally {
            rendering = false;
        }
    }

    private static final class CapturedRenderer extends RenderPlayer {
        final CapturedModel pose;

        CapturedRenderer(boolean slim) {
            super(Minecraft.getMinecraft().getRenderManager(), slim);
            pose = new CapturedModel(slim);
            mainModel = pose;
        }

        @Override
        protected void applyRotations(
                AbstractClientPlayer player, float age, float yaw, float partial) {
            super.applyRotations(player, age, yaw, partial);
            GlStateManager.translate(0, .9, 0);
            if (pose.mode == 4) GlStateManager.rotate(pose.tilt, 0, 0, 1);
            else GlStateManager.rotate(pose.tilt, 1, 0, 0);
            GlStateManager.translate(0, -.9, 0);
        }
    }

    private static final class CapturedModel extends ModelPlayer {
        int mode;
        float time, tilt;

        CapturedModel(boolean slim) {
            super(0, slim);
        }

        @Override
        public void setRotationAngles(
                float limb,
                float amount,
                float age,
                float yaw,
                float pitch,
                float scale,
                Entity entity) {
            super.setRotationAngles(limb, amount, age, yaw, pitch, scale, entity);
            float struggle = (float) Math.sin(time * .4) * .12F;
            bipedRightArm.rotateAngleX = -2.1F + struggle;
            bipedLeftArm.rotateAngleX = -2.1F - struggle;
            bipedRightArm.rotateAngleY = -.35F;
            bipedLeftArm.rotateAngleY = .35F;
            bipedRightLeg.rotateAngleX = .15F + struggle;
            bipedLeftLeg.rotateAngleX = .4F - struggle;
            bipedHead.rotateAngleX = -.25F;
            if (mode >= 2) {
                bipedRightArm.rotateAngleX = -.65F;
                bipedLeftArm.rotateAngleX = -.95F;
                bipedRightArm.rotateAngleZ = .5F;
                bipedLeftArm.rotateAngleZ = -.5F;
                bipedRightLeg.rotateAngleX = .6F;
                bipedLeftLeg.rotateAngleX = -.3F;
            }
            copyModelAngles(bipedRightArm, bipedRightArmwear);
            copyModelAngles(bipedLeftArm, bipedLeftArmwear);
            copyModelAngles(bipedRightLeg, bipedRightLegwear);
            copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
            copyModelAngles(bipedHead, bipedHeadwear);
        }
    }
}
