package com.exoarsenal.event;

import com.exoarsenal.item.EnergyUtil;
import com.exoarsenal.item.ItemRTool;
import com.exoarsenal.item.ItemRmorArmor;
import com.exoarsenal.network.PacketRToolEffect;
import com.exoarsenal.registry.ModContent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RmorEventHandler {
    public static final int STYLISH = 0;
    public static final int BERSERK = 1;
    public static final int TITANIC = 2;
    private static final String MODE = "WastelandRmorMode";
    private static final String ANIMATION = "WastelandRmorAnimation";
    private static final String ANIMATION_TICK = "WastelandRmorAnimationTick";
    private static final String ANIMATION_SERIAL = "WastelandRmorAnimationSerial";
    private static final String COMBO = "WastelandRmorCombo";
    private static final String BLADE_DEPLOYED = "WastelandRmorBladeDeployed";
    private static final String DEFENSE_FORM = "ExoArsenalRmorDefenseForm";
    private static final UUID DEFENSE_ARMOR_ID =
            UUID.fromString("1b6af611-5298-4975-b42d-5b34a3bc0ed2");
    private static final UUID DEFENSE_TOUGHNESS_ID =
            UUID.fromString("df57c32e-5036-451e-8d65-011a47b10cf0");
    private static final AttributeModifier DEFENSE_ARMOR =
            new AttributeModifier(DEFENSE_ARMOR_ID, "R-mor defense plating", 5.0D, 0)
                    .setSaved(false);
    private static final AttributeModifier DEFENSE_TOUGHNESS =
            new AttributeModifier(DEFENSE_TOUGHNESS_ID, "R-mor defense bracing", 4.0D, 0)
                    .setSaved(false);

    public static boolean hasFullSet(EntityLivingBase wearer) {
        return wearer != null
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                        == ModContent.RMOR_HELMET
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == ModContent.RMOR_CHESTPLATE
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                        == ModContent.RMOR_LEGGINGS
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()
                        == ModContent.RMOR_BOOTS;
    }

    public static boolean hasCanvasSet(EntityLivingBase wearer) {
        return wearer != null
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                        == ModContent.CANVAS_HELMET
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == ModContent.CANVAS_CHESTPLATE
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                        == ModContent.CANVAS_LEGGINGS
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()
                        == ModContent.CANVAS_BOOTS;
    }

    public static boolean hasX10Set(EntityLivingBase wearer) {
        return X10Systems.hasSet(wearer);
    }

    public static boolean hasPoweredSet(EntityLivingBase wearer) {
        return hasFullSet(wearer) || hasX10Set(wearer);
    }

    public static int getMode(EntityLivingBase wearer) {
        if (!(wearer instanceof EntityPlayer)) return STYLISH;
        ItemStack chest = wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.getItem() instanceof ItemRmorArmor && chest.hasTagCompound()) {
            return Math.floorMod(chest.getTagCompound().getInteger(MODE), 3);
        }
        return Math.floorMod(((EntityPlayer) wearer).getEntityData().getInteger(MODE), 3);
    }

    public static String getModeName(EntityLivingBase wearer) {
        switch (getMode(wearer)) {
            case BERSERK:
                return "berserk";
            case TITANIC:
                return "titanic";
            default:
                return "stylish";
        }
    }

    public static boolean isDefenseForm(EntityLivingBase wearer) {
        return wearer != null && hasPoweredSet(wearer) && state(wearer).getBoolean(DEFENSE_FORM);
    }

    public static void setDefenseForm(EntityPlayer player, boolean enabled) {
        if (!hasPoweredSet(player)) enabled = false;
        state(player).setBoolean(DEFENSE_FORM, enabled);
    }

    public static void toggleDefenseForm(EntityPlayer player) {
        if (!hasPoweredSet(player)) {
            player.sendStatusMessage(
                    new TextComponentTranslation("status.exoarsenal.rmor_incomplete"), true);
            return;
        }
        boolean enabling = !isDefenseForm(player);
        if (enabling && !EnergyUtil.drainArmor(player, 1500, false)) {
            player.sendStatusMessage(
                    new TextComponentTranslation("status.exoarsenal.rmor_defense_empty"), true);
            return;
        }
        setDefenseForm(player, enabling);
        state(player).setBoolean(BLADE_DEPLOYED, false);
        triggerAnimation(player, enabling ? "defense_transform_in" : "defense_transform_out");
        player.sendStatusMessage(
                new TextComponentTranslation(
                        enabling
                                ? "status.exoarsenal.rmor_defense_on"
                                : "status.exoarsenal.rmor_defense_off"),
                true);
    }

    public static void cycleMode(EntityPlayer player) {
        if (!hasPoweredSet(player)) {
            player.sendStatusMessage(
                    new TextComponentTranslation("status.exoarsenal.rmor_incomplete"), true);
            return;
        }
        int mode = (getMode(player) + 1) % 3;
        state(player).setInteger(MODE, mode);
        triggerAnimation(player, "shift_" + modeName(mode));
        player.sendStatusMessage(
                new TextComponentTranslation("status.exoarsenal.rmor_mode", getModeName(player)),
                true);
    }

    private static String modeName(int mode) {
        switch (mode) {
            case BERSERK:
                return "berserk";
            case TITANIC:
                return "titanic";
            default:
                return "stylish";
        }
    }

    private static NBTTagCompound state(EntityLivingBase wearer) {
        ItemStack chest = wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.hasTagCompound()) chest.setTagCompound(new NBTTagCompound());
        return chest.getTagCompound();
    }

    private static void triggerAnimation(EntityPlayer player, String animation) {
        NBTTagCompound tag = state(player);
        tag.setString(ANIMATION, animation);
        tag.setLong(ANIMATION_TICK, player.world.getTotalWorldTime());
        tag.setInteger(ANIMATION_SERIAL, tag.getInteger(ANIMATION_SERIAL) + 1);
    }

    public static String getAnimation(EntityLivingBase wearer) {
        if (wearer == null
                || !(wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        instanceof ItemRmorArmor)) return "";
        return state(wearer).getString(ANIMATION);
    }

    public static long getAnimationTick(EntityLivingBase wearer) {
        return wearer == null ? 0L : state(wearer).getLong(ANIMATION_TICK);
    }

    public static int getAnimationSerial(EntityLivingBase wearer) {
        return wearer == null ? 0 : state(wearer).getInteger(ANIMATION_SERIAL);
    }

    public static int getAnimationDuration(String animation) {
        if (animation.startsWith("defense_transform")) return 24;
        if (animation.startsWith("defense_fire")) return 10;
        if (animation.startsWith("x10_shotgun_windup")) return 12;
        if (animation.startsWith("x10_blaster")) return 10;
        if (animation.startsWith("x10_katana")) return 16;
        if ("defense_jump".equals(animation)) return 16;
        if (animation.startsWith("attack_titanic")) return 31;
        if (animation.startsWith("attack_berserk")) return 22;
        if (animation.startsWith("attack_stylish")) return 18;
        if (animation.startsWith("shift_")) return 16;
        if ("shield".equals(animation)) return 10;
        if ("dive_lock".equals(animation) || "dive_release".equals(animation)) return 14;
        if ("deploy".equals(animation) || "stow".equals(animation)) return 12;
        return 16;
    }

    public static boolean isBladeDeployed(EntityLivingBase wearer) {
        if (wearer == null) return false;
        if (isDefenseForm(wearer)) return false;
        ItemStack chest = wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!(chest.getItem() instanceof ItemRmorArmor)) return false;
        NBTTagCompound tag = state(wearer);
        return tag.hasKey(BLADE_DEPLOYED)
                ? tag.getBoolean(BLADE_DEPLOYED)
                : wearer.getHeldItemOffhand().isEmpty();
    }

    public static int getPowerLevel(EntityLivingBase wearer) {
        if (wearer == null) return 0;
        int stored = 0;
        int capacity = 0;
        for (EntityEquipmentSlot slot :
                new EntityEquipmentSlot[] {
                    EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST,
                    EntityEquipmentSlot.LEGS,
                    EntityEquipmentSlot.FEET
                }) {
            ItemStack stack = wearer.getItemStackFromSlot(slot);
            if (!EnergyUtil.isRmor(stack)) continue;
            stored += EnergyUtil.stored(stack);
            capacity += EnergyUtil.capacity(stack);
        }
        if (stored <= 0 || capacity <= 0) return 0;
        return stored * 2 < capacity ? 1 : 2;
    }

    @SubscribeEvent
    public void shield(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)
                || event.getSource() == DamageSource.OUT_OF_WORLD) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!hasPoweredSet(player)) return;
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        NBTTagCompound tag = chest.getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            chest.setTagCompound(tag);
        }
        long now = player.world.getTotalWorldTime();
        if (now - tag.getLong("ShieldLastHit") > 200L) tag.setInteger("ShieldHits", 5);
        int hits = tag.hasKey("ShieldHits") ? tag.getInteger("ShieldHits") : 5;
        if (hits > 0 && EnergyUtil.drainArmor(player, 100, false)) {
            tag.setInteger("ShieldHits", hits - 1);
            tag.setLong("ShieldLastHit", now);
            event.setCanceled(true);
            triggerAnimation(player, "shield");
            if (!player.world.isRemote)
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.shield", hits - 1), true);
        }
    }

    public static void defenseAction(EntityPlayer player, int action) {
        if (!isDefenseForm(player)) return;
        if (action == 0 || action == 1) fireDefenseWeapon(player, action);
        else if (action == 2) defenseJump(player);
    }

    public static int getDefenseWeapon(EntityLivingBase wearer) {
        return wearer == null ? 0 : Math.floorMod(state(wearer).getInteger("X10DefenseWeapon"), 3);
    }

    public static void cycleDefenseWeapon(EntityPlayer player) {
        if (!hasX10Set(player) || !isDefenseForm(player)) return;
        NBTTagCompound tag = state(player);
        int weapon = (getDefenseWeapon(player) + 1) % 3;
        tag.setInteger("X10DefenseWeapon", weapon);
        player.sendStatusMessage(
                new TextComponentTranslation(
                        "status.exoarsenal.x10_defense_weapon",
                        weapon == 1
                                ? "Energy Blasters"
                                : weapon == 2 ? "Energy Katana" : "Energy Shotguns"),
                true);
    }

    private static void fireDefenseWeapon(EntityPlayer player, int side) {
        if (!hasX10Set(player)) {
            fireShotgun(player, side);
            return;
        }
        int weapon = getDefenseWeapon(player);
        if (weapon == 0) queueX10Shotgun(player);
        else if (weapon == 1) fireBlaster(player, side);
        else fireKatana(player, side);
    }

    private static void queueX10Shotgun(EntityPlayer player) {
        NBTTagCompound tag = state(player);
        long now = player.world.getTotalWorldTime();
        if (tag.hasKey("X10ShotPending") && tag.getLong("X10ShotPending") > now) return;
        int side = tag.getInteger("X10ShotSide") & 1;
        tag.setInteger("X10ShotSide", side ^ 1);
        tag.setInteger("X10PendingSide", side);
        tag.setLong("X10ShotPending", now + 5L);
        triggerAnimation(
                player, side == 0 ? "x10_shotgun_windup_left" : "x10_shotgun_windup_right");
    }

    private static void resolveX10Shotgun(EntityPlayer player) {
        NBTTagCompound tag = state(player);
        long pending = tag.getLong("X10ShotPending");
        if (pending <= 0L || player.world.getTotalWorldTime() < pending) return;
        tag.removeTag("X10ShotPending");
        fireShotgun(player, tag.getInteger("X10PendingSide") & 1);
    }

    private static void fireBlaster(EntityPlayer player, int side) {
        NBTTagCompound tag = state(player);
        long now = player.world.getTotalWorldTime();
        if (now - tag.getLong("X10BlasterShot") < 3L || !EnergyUtil.drainArmor(player, 170, false))
            return;
        tag.setLong("X10BlasterShot", now);
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        EntityLivingBase best = null;
        double distance = 28.0D * 28.0D;
        for (EntityLivingBase target :
                player.world.getEntitiesWithinAABB(
                        EntityLivingBase.class, player.getEntityBoundingBox().grow(28))) {
            if (target == player || !target.isEntityAlive() || !player.canEntityBeSeen(target))
                continue;
            Vec3d delta =
                    target.getPositionVector().addVector(0, target.height * .55, 0).subtract(eye);
            if (delta.lengthSquared() < distance && delta.normalize().dotProduct(look) > .975) {
                best = target;
                distance = delta.lengthSquared();
            }
        }
        Vec3d end =
                best == null
                        ? eye.add(look.scale(28))
                        : best.getPositionVector().addVector(0, best.height * .55, 0);
        if (best != null) {
            best.attackEntityFrom(DamageSource.causePlayerDamage(player).setFireDamage(), 8.0F);
            best.setFire(2);
        }
        PacketRToolEffect.send(
                player, PacketRToolEffect.SHOTGUN, java.util.Collections.singletonList(end));
        triggerAnimation(player, side == 0 ? "x10_blaster_left" : "x10_blaster_right");
        player.world.playSound(
                null,
                player.posX,
                player.posY + 1,
                player.posZ,
                SoundEvents.ENTITY_FIREWORK_BLAST,
                SoundCategory.PLAYERS,
                .55F,
                1.9F);
    }

    private static void fireKatana(EntityPlayer player, int side) {
        NBTTagCompound tag = state(player);
        long now = player.world.getTotalWorldTime();
        if (now - tag.getLong("X10KatanaShot") < 9L || !EnergyUtil.drainArmor(player, 520, false))
            return;
        tag.setLong("X10KatanaShot", now);
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        player.getEntityData().setBoolean("WastelandEnergySweep", true);
        try {
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(
                            EntityLivingBase.class, player.getEntityBoundingBox().grow(5.5))) {
                if (target == player || !target.isEntityAlive()) continue;
                Vec3d delta =
                        target.getPositionVector()
                                .addVector(0, target.height * .5, 0)
                                .subtract(eye);
                if (delta.lengthSquared() <= 30.25D && delta.normalize().dotProduct(look) > .25D) {
                    target.attackEntityFrom(
                            DamageSource.causePlayerDamage(player).setFireDamage(), 14.0F);
                    target.setFire(4);
                }
            }
        } finally {
            player.getEntityData().setBoolean("WastelandEnergySweep", false);
        }
        triggerAnimation(player, side == 0 ? "x10_katana_left" : "x10_katana_right");
        player.world.playSound(
                null,
                player.posX,
                player.posY + 1,
                player.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                1.0F,
                1.2F);
    }

    private static void fireShotgun(EntityPlayer player, int side) {
        NBTTagCompound armor = state(player);
        long now = player.world.getTotalWorldTime();
        String cooldown = side == 0 ? "DefenseLeftShot" : "DefenseRightShot";
        if (now - armor.getLong(cooldown) < 8L || !EnergyUtil.drainArmor(player, 425, false))
            return;
        armor.setLong(cooldown, now);
        triggerAnimation(player, side == 0 ? "defense_fire_left" : "defense_fire_right");
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        Vec3d right = new Vec3d(-look.z, 0.0D, look.x);
        if (right.lengthSquared() < 0.001D) right = new Vec3d(1.0D, 0.0D, 0.0D);
        right = right.normalize();
        Vec3d muzzle =
                eye.add(right.scale(side == 0 ? -0.48D : 0.48D))
                        .add(look.scale(0.55D))
                        .addVector(0.0D, -0.35D, 0.0D);
        Vec3d end = eye.add(look.scale(15.0D));
        AxisAlignedBB coneBounds = player.getEntityBoundingBox().grow(15.0D);
        player.getEntityData().setBoolean("WastelandEnergySweep", true);
        try {
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(EntityLivingBase.class, coneBounds)) {
                if (target == player || !target.isEntityAlive()) continue;
                Vec3d center =
                        target.getPositionVector().addVector(0.0D, target.height * 0.5D, 0.0D);
                Vec3d offset = center.subtract(eye);
                double distance = offset.lengthVector();
                if (distance > 15.0D
                        || distance < 0.1D
                        || offset.normalize().dotProduct(look) < 0.93D
                        || !player.canEntityBeSeen(target)) continue;
                float damage = (float) Math.max(4.0D, 11.0D - distance * 0.38D);
                if (target.attackEntityFrom(
                        DamageSource.causePlayerDamage(player).setProjectile().setFireDamage(),
                        damage)) {
                    target.setFire(2);
                    end = center;
                }
            }
        } finally {
            player.getEntityData().setBoolean("WastelandEnergySweep", false);
        }
        List<Vec3d> rays = new ArrayList<>();
        rays.add(end);
        rays.add(end.add(right.scale(0.22D)));
        rays.add(end.add(right.scale(-0.22D)));
        PacketRToolEffect.send(player, PacketRToolEffect.SHOTGUN, rays);
        if (player.world instanceof WorldServer) {
            WorldServer server = (WorldServer) player.world;
            for (int i = 0; i < 9; i++) {
                double t = i / 8.0D;
                Vec3d point = muzzle.add(end.subtract(muzzle).scale(t));
                server.spawnParticle(
                        EnumParticleTypes.REDSTONE,
                        point.x,
                        point.y,
                        point.z,
                        2,
                        0.07D,
                        0.07D,
                        0.07D,
                        0.01D);
            }
        }
        player.world.playSound(
                null,
                player.posX,
                player.posY + 1.0D,
                player.posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.PLAYERS,
                0.52F,
                1.75F + side * 0.08F);
    }

    private static void defenseJump(EntityPlayer player) {
        NBTTagCompound data = player.getEntityData();
        if (player.onGround
                || data.getBoolean("ExoArsenalDefenseDoubleJump")
                || !EnergyUtil.drainArmor(player, 1200, false)) return;
        Vec3d look = player.getLookVec();
        player.motionY = 0.78D;
        player.motionX += look.x * 0.22D;
        player.motionZ += look.z * 0.22D;
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
        data.setBoolean("ExoArsenalDefenseDoubleJump", true);
        triggerAnimation(player, "defense_jump");
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_FIREWORK_LAUNCH,
                SoundCategory.PLAYERS,
                0.7F,
                0.72F);
        if (player.world instanceof WorldServer)
            ((WorldServer) player.world)
                    .spawnParticle(
                            EnumParticleTypes.FLAME,
                            player.posX,
                            player.posY + 0.25D,
                            player.posZ,
                            14,
                            0.45D,
                            0.15D,
                            0.45D,
                            0.03D);
    }

    @SubscribeEvent
    public void blockDefenseInteractions(PlayerInteractEvent event) {
        if (!event.getWorld().isRemote && isDefenseForm(event.getEntityPlayer())) {
            event.setCanceled(true);
            event.setCancellationResult(net.minecraft.util.EnumActionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void blockDefenseMelee(AttackEntityEvent event) {
        if (!event.getEntityPlayer().world.isRemote && isDefenseForm(event.getEntityPlayer()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public void wristBlade(LivingHurtEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        if (player.getEntityData().getBoolean("WastelandEnergySweep")) return;
        if (!hasPoweredSet(player) || !player.getHeldItemOffhand().isEmpty()) return;
        int mode = getMode(player);
        int cost = mode == TITANIC ? 800 : mode == BERSERK ? 500 : 300;
        if (!EnergyUtil.drainArmor(player, cost, false)) return;
        float bonus = mode == TITANIC ? 12F : mode == BERSERK ? 8F : 5F;
        event.setAmount(event.getAmount() + bonus);
        event.getEntityLiving().setFire(mode == TITANIC ? 6 : 3);
        NBTTagCompound data = state(player);
        int cycle = data.getInteger(COMBO) + 1;
        data.setInteger(COMBO, cycle);
        String animation;
        if (mode == STYLISH) {
            animation =
                    cycle % 2 == 0
                            ? "attack_stylish_lunge"
                            : ((cycle / 2) % 2 == 0
                                    ? "attack_stylish_slash"
                                    : "attack_stylish_vault");
        } else if (mode == BERSERK) {
            int phase = (cycle - 1) % 3;
            animation =
                    phase == 0
                            ? "attack_berserk_cleave_left"
                            : phase == 1
                                    ? "attack_berserk_cleave_right"
                                    : "attack_berserk_overhead";
        } else animation = cycle % 2 == 0 ? "attack_titanic_crush" : "attack_titanic_cleave";
        triggerAnimation(player, animation);
        if (player.world instanceof WorldServer) {
            WorldServer server = (WorldServer) player.world;
            EntityLivingBase target = event.getEntityLiving();
            server.spawnParticle(
                    EnumParticleTypes.SWEEP_ATTACK,
                    target.posX,
                    target.posY + target.height * 0.55D,
                    target.posZ,
                    1,
                    0.25D,
                    0.2D,
                    0.25D,
                    0.0D);
            server.spawnParticle(
                    EnumParticleTypes.REDSTONE,
                    target.posX,
                    target.posY + target.height * 0.5D,
                    target.posZ,
                    mode == TITANIC ? 6 : 4,
                    0.32D,
                    0.3D,
                    0.32D,
                    0.012D);
            server.spawnParticle(
                    EnumParticleTypes.FLAME,
                    target.posX,
                    target.posY + target.height * 0.45D,
                    target.posZ,
                    mode == TITANIC ? 3 : 2,
                    0.22D,
                    0.22D,
                    0.22D,
                    0.02D);
        }
        if (mode == STYLISH && cycle % 2 == 0) {
            double yaw = Math.toRadians(player.rotationYaw);
            player.motionX += -Math.sin(yaw) * 0.85D;
            player.motionZ += Math.cos(yaw) * 0.85D;
            player.motionY = Math.max(player.motionY, 0.18D);
            player.velocityChanged = true;
        } else if (mode == BERSERK)
            event.getEntityLiving()
                    .knockBack(
                            player,
                            1.2F,
                            player.posX - event.getEntityLiving().posX,
                            player.posZ - event.getEntityLiving().posZ);
    }

    @SubscribeEvent
    public void poweredMovement(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) return;
        ItemRTool.printerTick(player);
        boolean fullSet = hasPoweredSet(player);
        updateDefenseAttributes(player, fullSet && isDefenseForm(player));
        if (!fullSet) return;
        if (hasX10Set(player) && isDefenseForm(player)) resolveX10Shotgun(player);
        NBTTagCompound data = player.getEntityData();
        NBTTagCompound armorState = state(player);
        if (player.onGround) data.setBoolean("ExoArsenalDefenseDoubleJump", false);
        boolean bladeDeployed = !isDefenseForm(player) && player.getHeldItemOffhand().isEmpty();
        if (!armorState.hasKey(BLADE_DEPLOYED))
            armorState.setBoolean(BLADE_DEPLOYED, bladeDeployed);
        else if (armorState.getBoolean(BLADE_DEPLOYED) != bladeDeployed) {
            armorState.setBoolean(BLADE_DEPLOYED, bladeDeployed);
            triggerAnimation(player, bladeDeployed ? "deploy" : "stow");
        }
        boolean sneaking = player.isSneaking();
        if (player.isInWater() && sneaking && !data.getBoolean("WastelandWasSneaking")) {
            boolean enabled = !data.getBoolean("WastelandDiveBoots");
            data.setBoolean("WastelandDiveBoots", enabled);
            triggerAnimation(player, enabled ? "dive_lock" : "dive_release");
            player.sendStatusMessage(
                    new TextComponentTranslation(
                            enabled ? "status.exoarsenal.dive_on" : "status.exoarsenal.dive_off"),
                    true);
        }
        data.setBoolean("WastelandWasSneaking", sneaking);
        if (player.isInWater()
                && data.getBoolean("WastelandDiveBoots")
                && EnergyUtil.drainArmor(player, 2, false)) {
            player.motionY = sneaking ? -0.10D : Math.max(player.motionY, -0.02D);
            player.stepHeight = 1.0F;
            player.setAir(300);
            player.velocityChanged = true;
        } else player.stepHeight = 0.6F;
        if (getMode(player) == TITANIC && !isDefenseForm(player))
            player.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 10, 0, true, false));
    }

    private static void updateDefenseAttributes(EntityPlayer player, boolean enabled) {
        updateModifier(
                player.getEntityAttribute(SharedMonsterAttributes.ARMOR), DEFENSE_ARMOR, enabled);
        updateModifier(
                player.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS),
                DEFENSE_TOUGHNESS,
                enabled);
    }

    private static void updateModifier(
            IAttributeInstance attribute, AttributeModifier modifier, boolean enabled) {
        AttributeModifier current = attribute.getModifier(modifier.getID());
        if (enabled && current == null) attribute.applyModifier(modifier);
        else if (!enabled && current != null) attribute.removeModifier(current);
    }
}
