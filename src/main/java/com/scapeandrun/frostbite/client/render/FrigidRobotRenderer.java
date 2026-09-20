package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityFrigidRobot;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

public final class FrigidRobotRenderer extends GeoEntityRenderer<EntityFrigidRobot> {
    private EntityFrigidRobot rendering;

    public FrigidRobotRenderer(RenderManager manager) {
        super(manager, new RobotModel());
        shadowSize = .7F;
    }

    @Override
    public void doRender(
            EntityFrigidRobot e, double x, double y, double z, float yaw, float partial) {
        rendering = e;
        try {
            super.doRender(e, x, y, z, yaw, partial);
        } finally {
            rendering = null;
        }
    }

    @Override
    public void renderRecursively(
            net.minecraft.client.renderer.BufferBuilder b,
            software.bernie.geckolib3.geo.render.built.GeoBone bone,
            float r,
            float g,
            float blue,
            float a) {
        float next =
                rendering == null
                        ? -1
                        : com.scapeandrun.frostbite.entity.FrigidAttackTiming.next(
                                rendering.kind().ordinal(),
                                rendering.attack(),
                                rendering.phaseTwo(),
                                rendering.attackTick());
        boolean warning = rendering != null && next >= 0 && next - rendering.attackTick() <= 12;
        int color = com.scapeandrun.frostbite.client.FrigidPalette.color(bone.getName(), warning);
        super.renderRecursively(
                b,
                bone,
                (color >> 16 & 255) / 255F,
                (color >> 8 & 255) / 255F,
                (color & 255) / 255F,
                a);
    }

    private static final class RobotModel extends AnimatedGeoModel<EntityFrigidRobot> {
        public ResourceLocation getModelLocation(EntityFrigidRobot e) {
            return new ResourceLocation(
                    Frostbite.MODID,
                    e.kind() == EntityFrigidRobot.Kind.DRONE
                            ? "geo/frigid_drone.geo.json"
                            : "geo/frigid_robots.geo.json");
        }

        public ResourceLocation getTextureLocation(EntityFrigidRobot e) {
            return e.kind() == EntityFrigidRobot.Kind.DRONE
                    ? com.scapeandrun.frostbite.client.WholeModelTexture.get("frigid_drone")
                    : com.scapeandrun.frostbite.client.FrigidTexture.location();
        }

        public ResourceLocation getAnimationFileLocation(EntityFrigidRobot e) {
            return new ResourceLocation(Frostbite.MODID, "animations/frigid_robots.animation.json");
        }

        @Override
        public void setLivingAnimations(EntityFrigidRobot e, Integer id, AnimationEvent event) {
            super.setLivingAnimations(e, id, event);
            String name = e.kind().name().toLowerCase(java.util.Locale.ROOT);
            if (e.kind() == EntityFrigidRobot.Kind.DRONE) {
                float age = e.ticksExisted + event.getPartialTick(),
                        t = e.attackTick() + event.getPartialTick();
                float recoil =
                        com.scapeandrun.frostbite.entity.FrigidAttackTiming.recoil(
                                0, e.attack(), false, t);
                getBone("drone").setRotationZ((float) Math.sin(age * .035) * .14F);
                getBone("drone").setRotationX(-recoil * .28F);
                getBone("drone").setPositionZ(recoil * 1.6F);
                getBone("droneLeftRotor").setRotationY(age * .65F);
                getBone("droneRightRotor").setRotationY(-age * .65F);
                return;
            }
            for (String n : new String[] {"drone", "amplifier", "shielder", "rover", "arthropod"})
                getBone(n).setHidden(!n.equals(name));
            float age = e.ticksExisted + event.getPartialTick(),
                    t = e.attackTick() + event.getPartialTick();
            float swing = (float) Math.sin(e.limbSwing * .8) * Math.min(1, e.limbSwingAmount * 3);
            float contact =
                    e.kind() == EntityFrigidRobot.Kind.SHIELDER
                            ? (e.attack() == 1 ? 20 : e.attack() == 2 ? 22 : 28)
                            : e.kind() == EntityFrigidRobot.Kind.AMPLIFIER ? 24 : 18;
            float hit = e.attack() == 0 ? 0 : pulse(t, contact);
            float recoil =
                    com.scapeandrun.frostbite.entity.FrigidAttackTiming.recoil(
                            e.kind().ordinal(), e.attack(), e.phaseTwo(), t);
            getBone("drone").setRotationZ((float) Math.sin(age * .035) * .14F);
            getBone("drone").setRotationX(-recoil * .28F);
            getBone("drone").setPositionZ(recoil * 1.6F);
            getBone("droneLeftRotor").setRotationY(age * .65F);
            getBone("droneRightRotor").setRotationY(-age * .65F);
            getBone("amplifierCoil").setRotationY(age * .035F);
            getBone("amplifierCoil").setPositionY(hit * 1.5F - recoil);
            getBone("amplifier").setRotationX(-hit * .12F + recoil * .22F);
            for (String type : new String[] {"amplifier", "shielder"}) {
                getBone(type + "LeftLeg").setRotationX(swing * .35F);
                getBone(type + "RightLeg").setRotationX(-swing * .35F);
            }
            getBone("shielderShield").setRotationX(e.attack() == 2 ? hit * .7F : hit * .85F);
            getBone("shielderLeftArm").setRotationX(hit * .7F);
            getBone("shielder")
                    .setRotationX(e.attack() == 2 ? hit * .18F : e.attack() == 3 ? hit * .25F : 0);
            getBone("shielder").setPositionY(e.attack() == 3 ? -hit * 1.8F : 0);
            getBone("roverTurret")
                    .setRotationY((float) Math.toRadians(-(e.rotationYawHead - e.renderYawOffset)));
            getBone("roverTurret").setPositionZ(e.attack() == 1 ? recoil * 1.5F : 0);
            getBone("rover").setRotationX(e.attack() == 2 ? -hit * .15F : recoil * .06F);
            getBone("roverShieldIce").setHidden(e.shield() <= 0);
            getBone("arthropod")
                    .setPositionY(e.attack() == 2 || e.attack() == 5 ? -pulse(t, 28) * 3 : 0);
            getBone("arthropod")
                    .setRotationX(
                            e.attack() == 3 ? -.12F : e.attack() == 2 ? -pulse(t, 28) * .18F : 0);
            float vent = e.phaseTwo() ? .18F : 0;
            if (e.attack() == 4 || e.attack() == 6) vent += .12F * (float) Math.sin(t * .15F);
            getBone("arthropodCarapaceLeft").setRotationZ(.314F + vent);
            getBone("arthropodCarapaceRight").setRotationZ(-.314F - vent);
            getBone("arthropodReactorGlow")
                    .setScaleY(e.phaseTwo() ? 1.3F + .2F * (float) Math.sin(age * .12F) : 1);
            for (String leg : new String[] {"FrontLeft", "FrontRight", "RearLeft", "RearRight"}) {
                float side = leg.endsWith("Left") ? 1 : -1,
                        phase = leg.equals("FrontLeft") || leg.equals("RearRight") ? 1 : -1;
                float lift = Math.max(0, swing * phase),
                        restYaw = (leg.startsWith("Front") ? 1 : -1) * side * .31416F;
                IBone b = getBone("arthropod" + leg + "Leg");
                b.setRotationY(restYaw + phase * swing * .25F);
                b.setRotationZ(-side * (.20944F + lift * .12F));
                getBone("arthropod" + leg + "Knee").setRotationZ(side * (.41888F + lift * .3F));
                getBone("arthropod" + leg + "Foot").setRotationZ(-side * (.20944F + lift * .18F));
            }
            float
                    leftSnap =
                            e.attack() == 1
                                    ? Math.max(pulse(t, 22), e.phaseTwo() ? pulse(t, 54) : 0)
                                    : e.attack() == 7
                                            ? pulse(t, 32)
                                            : e.attack() == 9
                                                    ? Math.max(pulse(t, 28), pulse(t, 48))
                                                    : 0,
                    rightSnap = e.attack() == 1 ? pulse(t, 38) : leftSnap;
            getBone("arthropodLeftPincer").setRotationY(-leftSnap * .75F);
            getBone("arthropodRightPincer").setRotationY(rightSnap * .75F);
            float lift =
                    e.attack() == 2
                            ? pulse(t, 18) * .8F
                            : e.attack() == 5 && t < 26
                                    ? .6F
                                    : e.attack() == 4 || e.attack() == 6
                                            ? .25F
                                            : e.attack() == 8 ? .45F : 0;
            getBone("arthropodLeftPincer").setRotationX(lift - leftSnap * .15F);
            getBone("arthropodRightPincer").setRotationX(lift - rightSnap * .15F);
            boolean claw = e.attack() == 1 || e.attack() == 7 || e.attack() == 9;
            getBone("arthropodLeftJaw").setRotationX(claw ? .65F * (1 - leftSnap) : .12F);
            getBone("arthropodRightJaw").setRotationX(claw ? .65F * (1 - rightSnap) : .12F);
            getBone("arthropodLeftLowerJaw")
                    .setRotationX(-getBone("arthropodLeftJaw").getRotationX());
            getBone("arthropodRightLowerJaw")
                    .setRotationX(-getBone("arthropodRightJaw").getRotationX());
        }

        private static float pulse(float t, float contact) {
            return com.scapeandrun.frostbite.entity.ArthropodPattern.impulse(t, contact);
        }
    }
}
