package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.combat.*;
import com.scapeandrun.frostbite.item.*;
import net.minecraft.client.model.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHandSide;

public final class CombatPlayerModel extends ModelPlayer {
    public CombatPlayerModel(boolean slim) {
        super(0, slim);
    }

    public void applyTo(ModelPlayer target, EntityPlayer player, float partial) {
        setModelAttributes(target);
        copyPose(target, this);
        float amount =
                player.prevLimbSwingAmount
                        + (player.limbSwingAmount - player.prevLimbSwingAmount) * partial;
        float limb = player.limbSwing - player.limbSwingAmount * (1 - partial);
        applyCombatPose(limb, amount, player.ticksExisted + partial, player);
        copyPose(this, target);
    }

    private static void copyPose(ModelPlayer from, ModelPlayer to) {
        copyModelAngles(from.bipedHead, to.bipedHead);
        copyModelAngles(from.bipedHeadwear, to.bipedHeadwear);
        copyModelAngles(from.bipedBody, to.bipedBody);
        copyModelAngles(from.bipedBodyWear, to.bipedBodyWear);
        copyModelAngles(from.bipedRightArm, to.bipedRightArm);
        copyModelAngles(from.bipedLeftArm, to.bipedLeftArm);
        copyModelAngles(from.bipedRightLeg, to.bipedRightLeg);
        copyModelAngles(from.bipedLeftLeg, to.bipedLeftLeg);
        copyModelAngles(from.bipedRightArmwear, to.bipedRightArmwear);
        copyModelAngles(from.bipedLeftArmwear, to.bipedLeftArmwear);
        copyModelAngles(from.bipedRightLegwear, to.bipedRightLegwear);
        copyModelAngles(from.bipedLeftLegwear, to.bipedLeftLegwear);
    }

    @Override
    public void render(
            Entity entity,
            float limb,
            float amount,
            float time,
            float yaw,
            float pitch,
            float scale) {
        PlayerSkinState.prepare(entity);
        super.render(entity, limb, amount, time, yaw, pitch, scale);
    }

    public static boolean gun(ItemStack s) {
        return s.getItem() instanceof ItemEnergyGun
                || s.getItem() instanceof ItemScoutWeapon
                        && ((ItemScoutWeapon) s.getItem()).getType()
                                == ItemScoutWeapon.Type.RAILGUN;
    }

    public static boolean braced(ItemStack s) {
        return !(s.getItem() instanceof ItemEnergyGun)
                || ((ItemEnergyGun) s.getItem()).getType() != ItemEnergyGun.Type.PROTOTYPE_PISTOL;
    }

    @Override
    public void setRotationAngles(
            float limb,
            float amount,
            float time,
            float yaw,
            float pitch,
            float scale,
            Entity entity) {
        bipedBody.rotationPointY = 0;
        super.setRotationAngles(limb, amount, time, yaw, pitch, scale, entity);
        applyCombatPose(limb, amount, time, entity);
    }

    private void applyCombatPose(float limb, float amount, float time, Entity entity) {
        if (!(entity instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) entity;
        if (com.scapeandrun.frostbite.event.RmorEventHandler.isDefenseForm(p)) return;
        PlayerLocomotion.apply(this, p, limb, amount);
        boolean right = p.getPrimaryHand() == EnumHandSide.RIGHT;
        ItemStack main = p.getHeldItemMainhand(), off = p.getHeldItemOffhand();
        if (gun(main)) hold(p, main, right, off.isEmpty() && braced(main), time);
        if (gun(off)) hold(p, off, !right, main.isEmpty() && braced(off), time);
        NBTTagCompound n = p.getEntityData();
        int action = n.getInteger("FrostCombatAction");
        int index = n.getInteger("FrostCombatProfile");
        float age =
                p.world.getTotalWorldTime()
                        - n.getLong("FrostCombatStart")
                        + (time - p.ticksExisted);
        boolean active = false;
        if (action > 0 && index >= 0 && index < WeaponDiscipline.values().length && age >= 0) {
            WeaponDiscipline d = WeaponDiscipline.values()[index];
            ModelRenderer arm = right ? bipedRightArm : bipedLeftArm;
            active =
                    age
                            < ((action == WeaponCombat.LIGHT || action == WeaponCombat.HEAVY)
                                    ? d.duration(action == WeaponCombat.HEAVY)
                                    : 18);
            if ((action == WeaponCombat.LIGHT || action == WeaponCombat.HEAVY)
                    && age <= d.duration(action == WeaponCombat.HEAVY)) {
                WeaponPose q =
                        WeaponPose.sample(
                                d,
                                age,
                                action == WeaponCombat.HEAVY,
                                n.getInteger("FrostCombatCombo"));
                float sign = right ? 1 : -1;
                arm.rotateAngleX = q.x;
                arm.rotateAngleY = q.y * sign;
                arm.rotateAngleZ = q.z * sign;
                bipedBody.rotateAngleY = q.body * sign;
                bipedBody.rotateAngleX = q.lean;
                bipedRightLeg.rotateAngleX = -q.lean * 1.5F;
                bipedLeftLeg.rotateAngleX = q.lean;
                bipedRightLeg.rotateAngleZ = 0.08F;
                bipedLeftLeg.rotateAngleZ = -0.08F;
                ModelRenderer support = right ? bipedLeftArm : bipedRightArm;
                if (off.isEmpty()
                        && (d == WeaponDiscipline.HAMMER
                                || d == WeaponDiscipline.AXE
                                || d == WeaponDiscipline.SAW)) {
                    support.rotateAngleX = q.x + 0.15F;
                    support.rotateAngleY = -0.35F * sign;
                    support.rotateAngleZ = -0.18F * sign;
                } else {
                    support.rotateAngleX = -0.6F;
                    support.rotateAngleZ = -0.22F * sign;
                }
            } else if (action == WeaponCombat.PARRY && age < 18) {
                arm.rotateAngleX = -1.65F;
                arm.rotateAngleY = (right ? 1 : -1) * 0.7F;
                arm.rotateAngleZ = (right ? 1 : -1) * 0.8F;
                bipedBody.rotateAngleY = (right ? 1 : -1) * -0.2F;
            } else if (action == WeaponCombat.DODGE && age < 16) {
                bipedBody.rotateAngleX = 0.6F;
                bipedRightArm.rotateAngleX = -0.7F;
                bipedLeftArm.rotateAngleX = -0.7F;
                bipedRightLeg.rotateAngleX = 0.9F;
                bipedLeftLeg.rotateAngleX = -1.0F;
            } else if (action == WeaponCombat.STAGGER && age < 16) {
                bipedBody.rotateAngleX = -0.25F;
                bipedRightArm.rotateAngleZ = 0.4F;
                bipedLeftArm.rotateAngleZ = -0.4F;
            }
        }
        float brace =
                Math.max(
                        ExcavatorParryEffects.brace(p, time - p.ticksExisted),
                        BrawlerParryVisual.brace(p, time - p.ticksExisted));
        float release = BrawlerParryVisual.release(p, time - p.ticksExisted);
        if (release > 0) {
            bipedBody.rotateAngleY -= release * .55F;
            bipedBody.rotateAngleX += release * .22F;
            bipedRightArm.rotateAngleX = -1.45F * release;
            bipedLeftArm.rotateAngleX = -1.3F * release;
            bipedRightLeg.rotateAngleX = -.55F * release;
            bipedLeftLeg.rotateAngleX = .65F * release;
        }
        if (brace > 0) {
            ModelRenderer weapon = right ? bipedRightArm : bipedLeftArm,
                    support = right ? bipedLeftArm : bipedRightArm;
            float sign = right ? 1 : -1;
            float struggle =
                    (float)
                                    Math.sin(
                                            (time
                                                            - p.getEntityData()
                                                                    .getLong("ExcavatorParryBrace")
                                                            + p.world.getTotalWorldTime()
                                                            - p.ticksExisted)
                                                    * 2.4F)
                            * .055F
                            * brace;
            weapon.rotateAngleX += (-1.7F - weapon.rotateAngleX) * brace;
            weapon.rotateAngleY += (sign * .65F - weapon.rotateAngleY) * brace;
            weapon.rotateAngleZ += (sign * .9F - weapon.rotateAngleZ) * brace;
            if (off.isEmpty()) {
                support.rotateAngleX += (-1.45F - support.rotateAngleX) * brace;
                support.rotateAngleY += (-sign * .5F - support.rotateAngleY) * brace;
                support.rotateAngleZ += (-sign * .45F - support.rotateAngleZ) * brace;
            }
            bipedBody.rotateAngleX += (-.16F - bipedBody.rotateAngleX) * brace;
            bipedBody.rotateAngleY += (-sign * .28F - bipedBody.rotateAngleY) * brace;
            bipedRightLeg.rotateAngleX += (-.35F - bipedRightLeg.rotateAngleX) * brace;
            bipedLeftLeg.rotateAngleX += (.42F - bipedLeftLeg.rotateAngleX) * brace;
            bipedRightLeg.rotateAngleZ = .12F * brace;
            bipedLeftLeg.rotateAngleZ = -.12F * brace;
            weapon.rotateAngleX += struggle;
            support.rotateAngleX -= struggle * .7F;
            bipedBody.rotateAngleX += Math.abs(struggle) * 1.4F;
            bipedBody.rotationPointY += brace * .65F;
            bipedHead.rotateAngleX = -.1F * brace;
        }
        for (com.scapeandrun.frostbite.entity.EntityBrawler boss :
                p.world.getEntitiesWithinAABB(
                        com.scapeandrun.frostbite.entity.EntityBrawler.class,
                        p.getEntityBoundingBox().grow(12))) {
            if (boss.martialVictim() != p.getEntityId()) continue;
            float struggle = (float) Math.sin(time * .8) * .12F;
            bipedRightArm.rotateAngleX = -1.6F + struggle;
            bipedLeftArm.rotateAngleX = -1.6F - struggle;
            bipedRightArm.rotateAngleZ = .35F;
            bipedLeftArm.rotateAngleZ = -.35F;
            bipedRightLeg.rotateAngleX = .8F + struggle;
            bipedLeftLeg.rotateAngleX = -.5F - struggle;
            bipedBody.rotateAngleX = .2F;
            bipedHead.rotateAngleX = -.3F;
            active = true;
            break;
        }
        if (active || brace > 0) {
            shoulder(bipedRightArm, -5);
            shoulder(bipedLeftArm, 5);
        }
        copyModelAngles(bipedHead, bipedHeadwear);
        copyModelAngles(bipedRightArm, bipedRightArmwear);
        copyModelAngles(bipedLeftArm, bipedLeftArmwear);
        copyModelAngles(bipedBody, bipedBodyWear);
        copyModelAngles(bipedRightLeg, bipedRightLegwear);
        copyModelAngles(bipedLeftLeg, bipedLeftLegwear);
    }

    private void shoulder(ModelRenderer arm, float x) {
        double y = bipedBody.rotateAngleY, lean = bipedBody.rotateAngleX;
        arm.rotationPointX = (float) (x * Math.cos(y));
        arm.rotationPointY = (float) (2 * Math.cos(lean) + x * Math.sin(y) * Math.sin(lean));
        arm.rotationPointZ = (float) (2 * Math.sin(lean) - x * Math.sin(y) * Math.cos(lean));
    }

    private void hold(EntityPlayer p, ItemStack stack, boolean right, boolean support, float time) {
        ModelRenderer trigger = right ? bipedRightArm : bipedLeftArm,
                other = right ? bipedLeftArm : bipedRightArm;
        float sign = right ? 1 : -1;
        boolean heavy =
                stack.getItem() instanceof ItemEnergyGun
                        && (((ItemEnergyGun) stack.getItem()).getType().cannon()
                                || ((ItemEnergyGun) stack.getItem()).getType().automatic());
        float age =
                stack.hasTagCompound()
                        ? p.world.getTotalWorldTime()
                                - stack.getTagCompound().getLong("GunAnimationTick")
                                + (time - p.ticksExisted)
                        : 99;
        float recoil = age >= 0 && age < 8 ? CombatMotion.recoil(age / 8) : 0;
        trigger.rotateAngleX =
                -1.48F + bipedHead.rotateAngleX + (heavy ? 0.16F : 0) - recoil * 0.12F;
        trigger.rotateAngleY = bipedHead.rotateAngleY - sign * 0.10F;
        trigger.rotateAngleZ = sign * 0.04F;
        if (support) {
            other.rotateAngleX =
                    -1.32F + bipedHead.rotateAngleX + (heavy ? 0.12F : 0) - recoil * 0.08F;
            other.rotateAngleY = bipedHead.rotateAngleY + sign * 0.48F;
            other.rotateAngleZ = -sign * 0.12F;
        }
    }
}
