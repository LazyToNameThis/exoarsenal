package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityX20Scout;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public final class X20ScoutModel extends AnimatedGeoModel<EntityX20Scout> {
    @Override
    public ResourceLocation getModelLocation(EntityX20Scout object) {
        return new ResourceLocation(Frostbite.MODID, "geo/x20_scout_humanoid.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(EntityX20Scout object) {
        return com.scapeandrun.frostbite.client.FrigidTexture.location();
    }

    @Override
    public ResourceLocation getAnimationFileLocation(EntityX20Scout object) {
        return new ResourceLocation(
                Frostbite.MODID, "animations/x20_scout_humanoid.animation.json");
    }

    @Override
    public void setLivingAnimations(EntityX20Scout scout, Integer id, AnimationEvent event) {
        super.setLivingAnimations(scout, id, event);
        IBone root = getBone("root");
        root.setScaleX(2.35F);
        root.setScaleY(2.35F);
        root.setScaleZ(2.35F);
        root.setRotationX(0);
        getBone("pilot").setPositionY(0);
        getBone("pilot").setPositionZ(0);
        getBone("leftSword").setRotationZ(0);
        getBone("leftSword").setRotationX(0);
        visible("entranceCape", false);
        getBone("clawArm").setPositionY(0);
        getBone("viceArm").setPositionY(0);
        getBone("leftSwordArm").setPositionZ(0);
        getBone("rightSwordArm").setPositionZ(0);
        visible("cockpitCrackIce", false);
        getBone("pilotLeftShin").setRotationX(-1.25F);
        getBone("pilotRightShin").setRotationX(-1.25F);

        getBone("leftSwordArm").setPositionX(-24F);
        getBone("rightSwordArm").setPositionX(24F);
        getBone("leftSwordArm").setPositionY(6F);
        getBone("rightSwordArm").setPositionY(6F);
        getBone("clawArm").setPositionX(-8F);
        getBone("viceArm").setPositionX(8F);
        getBone("clawArm").setPositionZ(-3F);
        getBone("viceArm").setPositionZ(-3F);
        boolean flying = scout.getHammerFlight() != 0;
        boolean flightVisible = flying && scout.getHammerFlight() != 6;
        int form = scout.getWeaponForm();
        if (scout.getScene() != 1 && scout.getScene() != 2) {
            getBone("leftBladeIce").setScaleZ(1);
            getBone("rightBladeIce").setScaleZ(1);
        }
        if (scout.getAttack() != com.scapeandrun.frostbite.entity.ScoutCombatPattern.MORPH) {
            getBone("leftHammer").setScaleX(1);
            getBone("leftHammer").setScaleY(1);
            getBone("leftHammer").setScaleZ(1);
        }
        visible("leftBladeIce", (form == 0 || form == 3) && !flying);
        getBone("leftBladeIce").setScaleX(form == 3 ? .42F : 1);
        getBone("leftBladeIce").setScaleZ(form == 3 ? 1.2F : 1);
        visible("rightBladeIce", false);
        visible("rightSword", false);
        visible("rightSwordCrestIce", false);
        visible("leftSword", !flying);
        visible("leftHammer", scout.isHammerForm() && !flying);
        visible("leftHammerIce", scout.isHammerForm() && !flying);
        visible("shieldIce", !scout.isGuardBroken());
        visible("leftScissors", form == 2 && !flying);
        visible("scissorAIce", form == 2 && !flying);
        visible("scissorBIce", form == 2 && !flying);
        visible("thrownHammer", flightVisible && form == 1);
        visible("thrownHammerIce", flightVisible && form == 1);
        float accretion =
                scout.getAttack()
                                == com.scapeandrun.frostbite.entity.ScoutCombatPattern
                                        .HAMMER_RICOCHET
                        ? 1 + scout.getRicochets() * .18F
                        : 1;
        getBone("thrownHammer").setScaleX(accretion);
        getBone("thrownHammer").setScaleY(accretion);
        getBone("thrownHammer").setScaleZ(accretion);
        visible("thrownSword", flightVisible && form == 0);
        visible("thrownSwordIce", flightVisible && form == 0);
        visible("thrownScissors", flightVisible && form == 2);
        visible("flyingScissorAIce", flightVisible && form == 2);
        visible("flyingScissorBIce", flightVisible && form == 2);
        if (flying) {
            float partial = event.getPartialTick();
            net.minecraft.util.math.Vec3d body =
                    new net.minecraft.util.math.Vec3d(
                            scout.lastTickPosX + (scout.posX - scout.lastTickPosX) * partial,
                            scout.lastTickPosY + (scout.posY - scout.lastTickPosY) * partial,
                            scout.lastTickPosZ + (scout.posZ - scout.lastTickPosZ) * partial);
            net.minecraft.util.math.Vec3d delta = scout.getHammerPosition(partial).subtract(body);
            double yaw =
                    scout.prevRenderYawOffset
                            + net.minecraft.util.math.MathHelper.wrapDegrees(
                                            scout.renderYawOffset - scout.prevRenderYawOffset)
                                    * partial;
            double[] position =
                    com.scapeandrun.frostbite.entity.ScoutSpace.boneTranslation(
                            yaw, delta.x, delta.y, delta.z);
            for (String name : new String[] {"thrownHammer", "thrownSword", "thrownScissors"}) {
                IBone hammer = getBone(name);
                hammer.setPositionX((float) position[0]);
                hammer.setPositionY((float) position[1]);
                hammer.setPositionZ((float) position[2]);
                hammer.setRotationX(
                        scout.getHammerFlight() == 2
                                ? 1.1F
                                : (scout.getAttackTick() + event.getPartialTick()) * .38F);
                hammer.setRotationY(0);
                hammer.setRotationZ(0);
                if (!name.equals("thrownHammer") && scout.getHammerFlight() != 4) {
                    net.minecraft.util.math.Vec3d direction =
                            scout.getAim().subtract(scout.getHammerPosition(partial));
                    double[] local =
                            com.scapeandrun.frostbite.entity.ScoutSpace.boneTranslation(
                                    yaw, direction.x, direction.y + .01, direction.z);
                    hammer.setRotationX(
                            (float)
                                    Math.atan2(
                                            local[1],
                                            Math.sqrt(local[0] * local[0] + local[2] * local[2])));
                    hammer.setRotationY((float) -Math.atan2(-local[0], -local[2]));
                }
            }
        }
        visible(
                "leftSwordCrestIce",
                scout.isHardpointAlive(
                        com.scapeandrun.frostbite.entity.EntityScoutHardpoint.MINIGUN_LEFT));
        visible("rightSwordCrestIce", false);
        visible(
                "leftShoulderIce",
                scout.isHardpointAlive(
                        com.scapeandrun.frostbite.entity.EntityScoutHardpoint.LASER_LEFT));
        visible(
                "rightShoulderIce",
                scout.isHardpointAlive(
                        com.scapeandrun.frostbite.entity.EntityScoutHardpoint.LASER_RIGHT));
        visible(
                "clawArm",
                scout.isHardpointAlive(com.scapeandrun.frostbite.entity.EntityScoutHardpoint.CLAW));
        visible(
                "viceArm",
                scout.isHardpointAlive(com.scapeandrun.frostbite.entity.EntityScoutHardpoint.VICE));
        visible("cockpitGlass", scout.getPhase() < EntityX20Scout.PHASE_DUO);
        visible("cockpitShards", scout.getPhase() >= EntityX20Scout.PHASE_DUO);
        visible("pilot", scout.getPhase() < EntityX20Scout.PHASE_DUO);
        int scene = scout.getScene();
        boolean drawing = scene == 2;
        boolean deployed = false;
        boolean formingShield = drawing && scout.getSceneTick() >= 224;
        visible("shield", (!drawing && scene != 1) || formingShield);
        visible("rightSword", false);
        visible("rightBladeIce", false);
        if (scene == 1 || drawing) visible("leftBladeIce", true);
        if (scene == 1 || drawing) {
            visible("leftSwordCrestIce", true);
            visible("rightSwordCrestIce", false);
            getBone("leftBladeIce").setScaleZ(1);
            getBone("rightBladeIce").setScaleZ(1);
        }
        float shieldScale =
                formingShield
                        ? Math.max(
                                .01F,
                                Math.min(
                                        1,
                                        (scout.getSceneTick() + event.getPartialTick() - 224)
                                                / 24F))
                        : 1;
        getBone("shield").setScaleX(shieldScale);
        getBone("shield").setScaleY(shieldScale);
        visible("clawUpperJaw", !deployed);
        visible("clawLowerJaw", !deployed);
        visible("viceUpperJaw", !deployed);
        visible("viceLowerJaw", !deployed);
        visible("leftViceIce", !deployed);
        visible("rightViceIce", !deployed);
        visible("phaseMinigun", deployed);
        visible("phaseCannon", deployed);
        visible("phaseMinigunIce", deployed);
        visible("phaseCannonIce", deployed);
        if (drawing || scene == 1) visible("shieldIce", false);

        visible("cockpitGlass", true);
        visible("cockpitShards", false);
        visible("pilot", true);
        visible("leftSwordCrestIce", form == 0 && !flying);
        animateHands(scout, event.getPartialTick());
        float tick = scout.getAttackTick() + event.getPartialTick();
        float spread =
                com.scapeandrun.frostbite.client.ScoutHandPose.scissorSpread(
                        scout.getAttack(), tick);
        getBone("scissorAIce").setRotationY(spread);
        getBone("scissorBIce").setRotationY(-spread);
        getBone("flyingScissorAIce").setRotationZ(.2F);
        getBone("flyingScissorBIce").setRotationZ(-.2F);
        float extension =
                form == 2
                        ? com.scapeandrun.frostbite.client.ScoutHandPose.scissorExtension(
                                scout.getAttack(), tick)
                        : 1;
        getBone("scissorAIce").setScaleZ(extension);
        getBone("scissorBIce").setScaleZ(extension);
        getBone("crownIce").setPositionY(15);
        boolean brawl = scout.getPhase() == EntityX20Scout.PHASE_BRAWL;
        boolean dropped =
                (scene == 3 && scout.getSceneTick() >= 24 && scout.getSceneTick() < 248)
                        || (scene == 6 && scout.getSceneTick() >= 24)
                        || brawl;
        if (dropped || brawl) {
            for (String n :
                    new String[] {
                        "leftSword",
                        "leftBladeIce",
                        "leftSwordCrestIce",
                        "leftHammer",
                        "leftHammerIce",
                        "leftScissors",
                        "scissorAIce",
                        "scissorBIce"
                    }) visible(n, false);
        }
        if (brawl) {
            visible("shield", false);
            visible("shieldIce", false);
        }
        visible("droppedSword", dropped);
        visible("droppedSwordIce", dropped);
        visible("droppedShield", brawl);
        positionWorld("droppedSword", scout.getDroppedPosition(), scout, event.getPartialTick());
        positionWorld("droppedShield", scout.getDroppedPosition(), scout, event.getPartialTick());
        float grow =
                scene == 6 || scene == 8
                        ? Math.max(
                                .01F,
                                Math.min(
                                        1,
                                        (scout.getSceneTick()
                                                        + event.getPartialTick()
                                                        - (scene == 8 ? 48 : 30))
                                                / 24F))
                        : 1;
        for (String n : new String[] {"leftWingIce", "rightWingIce"}) {
            visible(n, brawl || (scout.isExpert() && scout.getPhase() >= EntityX20Scout.PHASE_DUO));
            IBone wing = getBone(n);
            wing.setScaleX(grow);
            wing.setScaleY(grow);
            boolean folded =
                    scene == 0
                            && scout.getAttack()
                                    == com.scapeandrun.frostbite.entity.ScoutCombatPattern
                                            .EXPERT_BOXING
                            && tick < 196;
            float fold =
                    folded
                            ? com.scapeandrun.frostbite.client.ScoutBrawlPose.curve(
                                    tick, 0, 0, 14, 1, 180, 1, 196, 0)
                            : 0;
            wing.setRotationY(
                    (n.startsWith("left") ? 1 : -1)
                            * (.18F
                                    + fold * 1.2F
                                    + (float)
                                                    Math.sin(
                                                            (scout.ticksExisted
                                                                            + event
                                                                                    .getPartialTick())
                                                                    * .12)
                                            * .10F
                                            * (1 - fold)));
        }
        int projectile = scout.getProjectileKind();
        visible("brawlFist", projectile == 1 || projectile == 4);
        visible("brawlFistIce", projectile == 1 || projectile == 4);
        boolean rangeReel =
                scout.getAttack() == com.scapeandrun.frostbite.entity.ScoutCombatPattern.RANGE_REEL;
        visible("brawlVice", projectile == 3 && !rangeReel);
        visible("terrainChunk", projectile == 2);
        for (String n : new String[] {"brawlFist", "brawlVice", "terrainChunk"}) {
            positionWorld(
                    n,
                    scout.getHammerPosition(event.getPartialTick()),
                    scout,
                    event.getPartialTick());
            getBone(n).setRotationX(projectile == 2 ? tick * .14F : 0);
        }
        visible("leftPalm", projectile != 1);
        visible("rightPalm", projectile != 4);
        for (String n :
                new String[] {
                    "leftIndex",
                    "leftMiddle",
                    "leftRing",
                    "leftLittle",
                    "leftThumb",
                    "leftIndexTip",
                    "leftMiddleTip",
                    "leftRingTip",
                    "leftLittleTip",
                    "leftThumbTip"
                }) visible(n, projectile != 1);
        for (String n :
                new String[] {
                    "rightIndex",
                    "rightMiddle",
                    "rightRing",
                    "rightLittle",
                    "rightThumb",
                    "rightIndexTip",
                    "rightMiddleTip",
                    "rightRingTip",
                    "rightLittleTip",
                    "rightThumbTip"
                }) visible(n, projectile != 4);
        if (projectile == 3) {
            visible("viceUpperJaw", false);
            visible("viceLowerJaw", false);
            visible("rightViceIce", false);
        }
        for (boolean right : new boolean[] {false, true}) {
            double deployedVice =
                    com.scapeandrun.frostbite.entity.ScoutViceMotion.extension(
                            scout.getAttack(), tick, right);
            String anchor = right ? "anchorViceRight" : "anchorViceLeft";
            if (right && rangeReel && projectile == 3) {
                visible(anchor, true);
                positionWorld(
                        anchor,
                        scout.getHammerPosition(event.getPartialTick()),
                        scout,
                        event.getPartialTick());
                float jaw = scout.getCaptured() != null ? .12F : .65F;
                getBone("anchorRightOuter").setRotationZ(-jaw);
                getBone("anchorRightInner").setRotationZ(jaw);
                continue;
            }
            visible(anchor, scene == 0 && deployedVice > .001);
            if (scene == 0 && deployedVice > .001) {
                visible(right ? "viceUpperJaw" : "clawUpperJaw", false);
                visible(right ? "viceLowerJaw" : "clawLowerJaw", false);
                visible(right ? "rightViceIce" : "leftViceIce", false);
                positionWorld(
                        anchor,
                        scout.getDetachedVice(right, event.getPartialTick()),
                        scout,
                        event.getPartialTick());
                float open =
                        .12F
                                + (float)
                                                (1
                                                        - com.scapeandrun.frostbite.entity
                                                                .ScoutViceMotion.closure(
                                                                scout.getAttack(), tick, right))
                                        * .55F;
                getBone(right ? "anchorRightOuter" : "anchorLeftInner").setRotationZ(-open);
                getBone(right ? "anchorRightInner" : "anchorLeftOuter").setRotationZ(open);
            }
        }
        if (scout.isExpert() && scout.getPhase() == EntityX20Scout.PHASE_DUO) {
            visible("shield", scene == 8 && scout.getSceneTick() < 40);
            visible("shieldIce", false);
            boolean second = !dropped && (scene != 8 || scout.getSceneTick() > 60);
            visible("rightSword", second);
            visible("rightBladeIce", second);
        }
        float poseTick =
                (scene != 0 ? scout.getSceneTick() : scout.getAttackTick())
                        + event.getPartialTick();
        java.util.Map<String, float[]> poses =
                com.scapeandrun.frostbite.client.ScoutBrawlPose.sample(
                        scene,
                        scout.getAttack(),
                        poseTick,
                        scout.limbSwing,
                        scout.limbSwingAmount,
                        scout.ticksExisted + event.getPartialTick());
        if (scene == 0 && scout.isExpert() && scout.getPhase() == EntityX20Scout.PHASE_DUO)
            com.scapeandrun.frostbite.client.ScoutDualWeaponPose.apply(
                    poses, scout.getAttack(), poseTick);
        for (java.util.Map.Entry<String, float[]> pose : poses.entrySet()) {
            IBone bone = getBone(pose.getKey());
            float[] r = pose.getValue();
            bone.setRotationX((float) Math.toRadians(r[0]));
            bone.setRotationY((float) Math.toRadians(-r[1]));
            bone.setRotationZ((float) Math.toRadians(-r[2]));
        }
        if (drawing) {
            float t = poseTick;
            boolean airborne = com.scapeandrun.frostbite.client.ScoutEntrance.airborne(t);
            visible("leftSword", !airborne);
            visible("leftBladeIce", !airborne);
            visible("thrownSword", airborne);
            visible("thrownSwordIce", airborne);
            if (airborne) {
                double[] p = com.scapeandrun.frostbite.client.ScoutEntrance.sword(t);
                double[] local =
                        com.scapeandrun.frostbite.entity.ScoutSpace.boneTranslation(
                                0, p[0], p[1], p[2]);
                IBone sword = getBone("thrownSword");
                float angle = (float) (Math.PI * 2 * (t - 60) / 40);
                sword.setPositionX((float) local[0]);
                sword.setPositionY((float) local[1] + 6 * (float) Math.cos(angle));
                sword.setPositionZ((float) local[2] + 6 * (float) Math.sin(angle));
                sword.setRotationX(angle);
                sword.setRotationY(0);
                sword.setRotationZ(0);
            }
            getBone("leftSword")
                    .setRotationX(com.scapeandrun.frostbite.client.ScoutEntrance.spin(t));
            float cape = com.scapeandrun.frostbite.client.ScoutEntrance.cape(t);
            visible("entranceCape", cape > 0);
            getBone("entranceCape").setScaleY(Math.max(.001F, cape));
            for (String n : new String[] {"capeCenterIce", "capeLeftIce", "capeRightIce"}) {
                getBone(n)
                        .setRotationX(
                                -.12F
                                        - cape * .12F
                                        - (float) Math.sin((t - 174) * .10) * .08F * cape);
                getBone(n)
                        .setRotationY(
                                (n.equals("capeLeftIce") ? 1 : n.equals("capeRightIce") ? -1 : 0)
                                        * (1 - cape)
                                        * 1.25F);
            }
            for (String n :
                    new String[] {"capeCenterTailIce", "capeLeftTailIce", "capeRightTailIce"})
                getBone(n).setRotationX(-.12F + (float) Math.sin((t - 180) * .12) * .10F * cape);
        }
        if (scene == 0) {
            visible("entranceCape", true);
            getBone("entranceCape").setScaleY(1);
            float flutter =
                    (float) Math.sin((scout.ticksExisted + event.getPartialTick()) * .10F) * .035F;
            for (String n : new String[] {"capeCenterIce", "capeLeftIce", "capeRightIce"}) {
                getBone(n).setRotationX(-.24F + flutter);
                getBone(n).setRotationY(0);
            }
            for (String n :
                    new String[] {"capeCenterTailIce", "capeLeftTailIce", "capeRightTailIce"})
                getBone(n).setRotationX(-.12F - flutter * 2);
        }
        if (scout.isOverheating()) {
            float t = scout.getOverheatTick() + event.getPartialTick();
            float beat = t % 80;
            float drive =
                    com.scapeandrun.frostbite.client.ScoutBrawlPose.curve(
                            beat, 0, 0, 8, 0, 12, 1, 24, 1, 32, 0, 44, 0, 48, 1, 60, 1, 68, 0, 80,
                            0);
            getBone("root").setRotationX(.22F * drive);
            getBone("leftSwordArm").setRotationX(-.6F - drive * .75F);
            getBone("rightSwordArm").setRotationX(-.6F - drive * .75F);
            getBone("leftSwordArm").setRotationZ(.35F + drive * .15F);
            getBone("rightSwordArm").setRotationZ(-.35F - drive * .15F);
            getBone("clawArm").setRotationX(-.4F - drive * .6F);
            getBone("viceArm").setRotationX(-.4F - drive * .6F);

            getBone("leftWrist").setRotationZ((float) Math.sin(t * .8) * .025F);
            getBone("rightWrist").setRotationZ((float) Math.sin(t * .73 + 1) * .025F);
        }
        if (scene == 7) {
            animateDefeat(scout.getSceneTick() + event.getPartialTick());
            if (!brawl) {
                visible("leftWingIce", false);
                visible("rightWingIce", false);
            }
        } else {
            getBone("pilotLeftLeg").setRotationX(getBone("pilotLeftLeg").getRotationX() + 1.25F);
            getBone("pilotRightLeg").setRotationX(getBone("pilotRightLeg").getRotationX() + 1.25F);
        }
    }

    private void animateDefeat(float tick) {
        com.scapeandrun.frostbite.client.ScoutDefeatPose.apply(tick, this::getBone);
    }

    private void positionWorld(
            String name, net.minecraft.util.math.Vec3d point, EntityX20Scout scout, float partial) {
        net.minecraft.util.math.Vec3d body =
                new net.minecraft.util.math.Vec3d(
                        scout.lastTickPosX + (scout.posX - scout.lastTickPosX) * partial,
                        scout.lastTickPosY + (scout.posY - scout.lastTickPosY) * partial,
                        scout.lastTickPosZ + (scout.posZ - scout.lastTickPosZ) * partial);
        net.minecraft.util.math.Vec3d d = point.subtract(body);
        double yaw =
                scout.prevRenderYawOffset
                        + net.minecraft.util.math.MathHelper.wrapDegrees(
                                        scout.renderYawOffset - scout.prevRenderYawOffset)
                                * partial;
        double[] p =
                com.scapeandrun.frostbite.entity.ScoutSpace.boneTranslation(yaw, d.x, d.y, d.z);
        IBone bone = getBone(name);
        bone.setPositionX((float) p[0]);
        bone.setPositionY((float) p[1]);
        bone.setPositionZ((float) p[2]);
        bone.setRotationY((float) Math.toRadians(yaw));
    }

    private void animateHands(EntityX20Scout scout, float partial) {
        String[] digits = {"Index", "Middle", "Ring", "Little", "Thumb"};
        float tick = scout.getAttackTick() + partial;
        getBone("leftSword").setPositionY(-2F);
        getBone("rightSword").setPositionY(-2F);
        getBone("rightWrist").setRotationX(0);
        getBone("leftWrist")
                .setRotationX(
                        (float)
                                Math.toRadians(
                                        com.scapeandrun.frostbite.client.ScoutHandPose.wrist(
                                                scout.getAttack(), tick)));
        for (int i = 0; i < digits.length; i++)
            for (boolean right : new boolean[] {false, true})
                for (boolean tip : new boolean[] {false, true}) {
                    float[] pose =
                            com.scapeandrun.frostbite.client.ScoutHandPose.digit(
                                    scout.getAttack(), tick, i, tip, right);
                    IBone bone =
                            getBone((right ? "right" : "left") + digits[i] + (tip ? "Tip" : ""));
                    bone.setRotationX((float) Math.toRadians(pose[0]));
                    bone.setRotationY((float) Math.toRadians(-pose[1]));
                    bone.setRotationZ((float) Math.toRadians(-pose[2]));
                }
    }

    private void visible(String name, boolean visible) {
        try {
            IBone bone = getBone(name);
            if (bone != null) bone.setHidden(!visible);
        } catch (RuntimeException ignored) {
        }
    }
}
