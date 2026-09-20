package com.exoarsenal.combat;

import com.exoarsenal.item.ItemRBlade;
import com.exoarsenal.item.ItemRTool;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;

final class MeleeAttackResolver {
    private static final int MAX_SWEEP_TARGETS = 3;
    private static final int BLADE_FIRE_SECONDS = 6;
    private static final float HEAVY_DAMAGE_MULTIPLIER = 1.55F;
    private static final float HEAVY_KNOCKBACK = 0.65F;
    private static final float LIGHT_KNOCKBACK = 0.2F;

    private MeleeAttackResolver() {}

    static void resolve(
            EntityPlayerMP player,
            WeaponDiscipline discipline,
            ItemStack weapon,
            Vec3d direction,
            float baseDamage,
            boolean heavy,
            Consumer<EntityPlayerMP> staggerPlayer) {
        Vec3d eye = player.getPositionEyes(1);
        double reach = discipline.reach;
        List<EntityLivingBase> targets =
                player.world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        player.getEntityBoundingBox().grow(reach),
                        target ->
                                target != player
                                        && target.isEntityAlive()
                                        && !player.isOnSameTeam(target));
        targets.sort(Comparator.comparingDouble(player::getDistanceSq));
        int hits = 0;
        for (EntityLivingBase target : targets) {
            if (target instanceof EntityPlayer && !player.canAttackPlayer((EntityPlayer) target))
                continue;
            Vec3d center = target.getPositionVector().addVector(0, target.height * 0.55, 0),
                    delta = center.subtract(eye);
            if (delta.lengthVector() > reach + target.width * 0.5
                    || delta.normalize().dotProduct(direction)
                            < Math.cos(Math.toRadians(discipline.arc * 0.5))
                    || !player.canEntityBeSeen(target)) continue;
            float damage =
                    (baseDamage
                                    + EnchantmentHelper.getModifierForCreature(
                                            weapon, target.getCreatureAttribute()))
                            * discipline.multiplier
                            * (heavy ? HEAVY_DAMAGE_MULTIPLIER : 1);
            if (target.attackEntityFrom(DamageSource.causePlayerDamage(player), damage)) {
                hits++;
                if (weapon.getItem() instanceof ItemRBlade) {
                    target.setFire(BLADE_FIRE_SECONDS);
                } else if (!(weapon.getItem() instanceof ItemRTool))
                    weapon.hitEntity(target, player);
                EnchantmentHelper.applyThornEnchantments(target, player);
                EnchantmentHelper.applyArthropodEnchantments(player, target);
                target.knockBack(
                        player,
                        heavy ? HEAVY_KNOCKBACK : LIGHT_KNOCKBACK,
                        -direction.x,
                        -direction.z);
                if (heavy
                        && (discipline == WeaponDiscipline.AXE
                                || discipline == WeaponDiscipline.HAMMER)
                        && target instanceof EntityPlayer)
                    ((EntityPlayer) target).disableShield(true);

                if (heavy && target.isNonBoss()) {
                    target.addPotionEffect(
                            new net.minecraft.potion.PotionEffect(
                                    net.minecraft.init.MobEffects.SLOWNESS, 12, 3));
                    if (target instanceof EntityPlayerMP)
                        staggerPlayer.accept((EntityPlayerMP) target);
                }
                if (discipline == WeaponDiscipline.RAPIER
                        || discipline == WeaponDiscipline.SCISSORS
                        || hits >= MAX_SWEEP_TARGETS) break;
            }
        }
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                hits > 0
                        ? SoundEvents.ENTITY_PLAYER_ATTACK_STRONG
                        : SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                0.65F,
                heavy ? 0.75F : 1.1F);
        player.resetCooldown();
    }
}
