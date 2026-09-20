package com.exoarsenal.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.exoarsenal.entity.EntityRBladeDisc;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemX10Blade extends ItemRBlade {
    private static final int RAPID_PULSE_TICKS = 6;
    private static final int RAPID_TARGET_LIMIT = 8;
    public static final int X10_CAPACITY = 1000000;
    public static final int SPIN_THROW = 0;
    public static final int RAPID_SLASHES = 1;
    public static final int CLEAVE = 2;
    public static final int KATANA = 0;
    public static final int SCISSORS = 1;
    public static final int GREATHAMMER = 2;
    public static final int RAPIER = 3;
    private static final UUID DAMAGE = UUID.fromString("198349b8-5026-4df7-b11c-0da4c0525b0f");
    private static final UUID SPEED = UUID.fromString("c2fa9a4e-b3a7-46c3-b2e5-8701093cfd7a");

    private static NBTTagCompound xTag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    public static int getAbility(ItemStack stack) {
        return Math.floorMod(xTag(stack).getInteger("X10Ability"), 3);
    }

    public static int getForm(ItemStack stack) {
        return Math.floorMod(xTag(stack).getInteger("X10Form"), 4);
    }

    public static void cycleAbility(ItemStack stack) {
        xTag(stack).setInteger("X10Ability", (getAbility(stack) + 1) % 3);
    }

    public static void cycleForm(ItemStack stack) {
        xTag(stack).setInteger("X10Form", (getForm(stack) + 1) % 4);
    }

    public static String abilityName(ItemStack stack) {
        return getAbility(stack) == RAPID_SLASHES
                ? "Rapid Slashes"
                : getAbility(stack) == CLEAVE ? "Cleave" : "Spin & Throw";
    }

    public static String formName(ItemStack stack) {
        switch (getForm(stack)) {
            case SCISSORS:
                return "Scissors";
            case GREATHAMMER:
                return "Greathammer";
            case RAPIER:
                return "Rapier";
            default:
                return "Katana";
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (EnergyUtil.stored(stack) < 500) return new ActionResult<>(EnumActionResult.FAIL, stack);
        xTag(stack).setBoolean("Active", true);
        if (getAbility(stack) == SPIN_THROW) {
            boolean started = ItemRBlade.beginSpin(player, hand);
            return new ActionResult<>(
                    started ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
        }
        if (getAbility(stack) == RAPID_SLASHES) {
            xTag(stack).setBoolean("X10Rapid", true);
            ItemRBlade.animate(stack, "x10_rapid", world.getTotalWorldTime());
            player.swingArm(hand);
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        long now = world.getTotalWorldTime();
        if (now < xTag(stack).getLong("X10CleaveReady")) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        xTag(stack).setLong("X10CleaveReady", now + 8L);
        if (world.isRemote) previewCleave(player, stack, hand);
        else cleave(player, stack, hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (isSpinning(stack)) {
            super.onUsingTick(stack, living, count);
            return;
        }
        if (!(living instanceof EntityPlayer) || !xTag(stack).getBoolean("X10Rapid")) return;
        EntityPlayer player = (EntityPlayer) living;
        if (living.world.isRemote) {
            if (count % RAPID_PULSE_TICKS == 0) player.swingArm(player.getActiveHand());
            return;
        }
        if (count % RAPID_PULSE_TICKS != 0) return;
        if (!EnergyUtil.drain(stack, 390, false)) {
            player.stopActiveHand();
            return;
        }
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        AxisAlignedBB sweep =
                player.getEntityBoundingBox()
                        .expand(look.x * 5.0D, look.y * 5.0D, look.z * 5.0D)
                        .grow(1.45D);
        long now = player.world.getTotalWorldTime();
        int hitCount = 0;
        int examined = 0;
        player.getEntityData().setBoolean("WastelandEnergySweep", true);
        try {
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(EntityLivingBase.class, sweep)) {
                if (target == player || !target.isEntityAlive()) continue;
                if (++examined > 36) break;
                Vec3d offset =
                        target.getPositionVector()
                                .addVector(0, target.height * 0.5D, 0)
                                .subtract(eye);
                double distanceSq = offset.lengthSquared();
                double forward = offset.dotProduct(look);
                if (distanceSq > 25.0D
                        || forward <= 0.0D
                        || forward * forward < distanceSq * 0.5184D) continue;
                if (now - target.getEntityData().getLong("ExoArsenalX10RapidHit") < 6L) continue;
                if (target.attackEntityFrom(
                        DamageSource.causePlayerDamage(player).setFireDamage(),
                        rapidDamage(stack) * 1.5F)) {
                    target.getEntityData().setLong("ExoArsenalX10RapidHit", now);
                    target.setFire(2);
                    if (++hitCount >= RAPID_TARGET_LIMIT) break;
                }
            }
        } finally {
            player.getEntityData().setBoolean("WastelandEnergySweep", false);
        }
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        if (xTag(stack).getBoolean("X10Rapid")) {
            xTag(stack).setBoolean("X10Rapid", false);
            return;
        }
        super.onPlayerStoppedUsing(stack, world, living, timeLeft);
    }

    private static float rapidDamage(ItemStack stack) {
        switch (getForm(stack)) {
            case SCISSORS:
                return 4.5F;
            case GREATHAMMER:
                return 7.5F;
            case RAPIER:
                return 4.0F;
            default:
                return 5.0F;
        }
    }

    private static void previewCleave(EntityPlayer player, ItemStack stack, EnumHand hand) {
        NBTTagCompound tag = xTag(stack);
        int stage = tag.getInteger("X10Cleave") % 3 + 1;
        tag.setInteger("X10Cleave", stage);
        ItemRBlade.animate(stack, "x10_cleave_" + stage, player.world.getTotalWorldTime());
        player.swingArm(hand);
    }

    private static void cleave(EntityPlayer player, ItemStack stack, EnumHand hand) {
        NBTTagCompound tag = xTag(stack);
        int stage = tag.getInteger("X10Cleave") % 3 + 1;
        int cost = stage == 3 ? 2200 : 1300;
        if (!EnergyUtil.drain(stack, cost, false)) return;
        tag.setInteger("X10Cleave", stage);
        ItemRBlade.animate(stack, "x10_cleave_" + stage, player.world.getTotalWorldTime());
        player.swingArm(hand);
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        double reach = getForm(stack) == GREATHAMMER ? 6.5D : 5.5D;
        float damage =
                getForm(stack) == GREATHAMMER ? 20.0F : getForm(stack) == RAPIER ? 11.0F : 15.0F;
        player.getEntityData().setBoolean("WastelandEnergySweep", true);
        int swept = 0;
        int examined = 0;
        try {
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(
                            EntityLivingBase.class, player.getEntityBoundingBox().grow(reach))) {
                if (target == player || !target.isEntityAlive()) continue;
                if (++examined > 48) break;
                Vec3d offset =
                        target.getPositionVector()
                                .addVector(0, target.height * 0.5D, 0)
                                .subtract(eye);
                double distanceSq = offset.lengthSquared();
                double forward = offset.dotProduct(look);
                if (distanceSq > reach * reach) continue;
                if (stage == 3
                        ? forward < -0.05D * Math.sqrt(distanceSq)
                        : forward <= 0.0D || forward * forward < distanceSq * 0.04D) continue;
                if (target.attackEntityFrom(
                        DamageSource.causePlayerDamage(player).setFireDamage(),
                        damage + (stage == 3 ? 5.0F : 0.0F))) {
                    target.setFire(4);
                    if (++swept >= 16) break;
                }
            }
        } finally {
            player.getEntityData().setBoolean("WastelandEnergySweep", false);
        }
        if (stage == 3) {
            Vec3d center = eye.add(look.scale(3.4D));
            int pushed = 0;
            int examinedPush = 0;
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(
                            EntityLivingBase.class,
                            new AxisAlignedBB(
                                    center.x - 3.0D,
                                    center.y - 3.0D,
                                    center.z - 3.0D,
                                    center.x + 3.0D,
                                    center.y + 3.0D,
                                    center.z + 3.0D))) {
                if (target == player || !target.isEntityAlive()) continue;
                if (++examinedPush > 32) break;
                Vec3d push = target.getPositionVector().subtract(center);
                double distanceSq = Math.max(0.25D, push.lengthSquared());
                if (distanceSq > 9.0D) continue;
                double strength = 0.9D * (1.0D - Math.sqrt(distanceSq) / 3.0D);
                Vec3d direction = push.normalize();
                target.addVelocity(
                        direction.x * strength, 0.18D + strength * 0.3D, direction.z * strength);
                if (++pushed >= 16) break;
            }
        }
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                1.0F,
                stage == 3 ? 0.72F : 1.05F + stage * 0.1F);
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(
            EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        if (slot != EntityEquipmentSlot.MAINHAND) return map;
        double damage, speed;
        switch (getForm(stack)) {
            case SCISSORS:
                damage = 13.0D;
                speed = -2.0D;
                break;
            case GREATHAMMER:
                damage = 19.0D;
                speed = -3.1D;
                break;
            case RAPIER:
                damage = 10.0D;
                speed = -1.0D;
                break;
            default:
                damage = 12.0D;
                speed = -1.55D;
        }
        map.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(DAMAGE, "X-10 blade damage", damage, 0));
        map.put(
                SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(SPEED, "X-10 blade speed", speed, 0));
        return map;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(X10_CAPACITY, 32000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < X10_CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) X10_CAPACITY;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }
}
