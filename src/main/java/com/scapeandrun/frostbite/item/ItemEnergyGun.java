package com.scapeandrun.frostbite.item;

import com.scapeandrun.frostbite.network.PacketRToolEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemEnergyGun extends Item implements IAnimatable {
    public enum Type {
        PROTOTYPE_SHOTGUN("Prototype Energy Shotgun", false, false, 250000, 900, 14, 22.0D, 0.0F),
        PROTOTYPE_CANNON("Prototype Energy Cannon", false, false, 400000, 4800, 34, 48.0D, 30.0F),
        PROTOTYPE_PISTOL("Prototype Energy Pistol", false, true, 150000, 260, 5, 34.0D, 8.0F),
        PROTOTYPE_MACHINEGUN(
                "Prototype Laser Machinegun", false, false, 300000, 130, 2, 38.0D, 4.5F),
        X10_SHOTGUN("X-10 Energy Shotgun", true, true, 1000000, 1600, 10, 30.0D, 0.0F),
        X10_CANNON("X-10 Energy Cannon", true, false, 1200000, 9000, 42, 64.0D, 55.0F),
        X10_MACHINEGUN("X-10 Energy Machinegun", true, false, 1000000, 210, 2, 48.0D, 6.5F),
        X10_NET_LAUNCHER("X-10 Energy Net Launcher", true, false, 800000, 2400, 24, 36.0D, 2.0F),
        KX_SHOTGUN("KX-20 Fluxuated Shotgun", true, false, 3000000, 2600, 7, 38.0D, 0.0F),
        KX_TRIBOW("KX-20 Fluxuated Tribow", true, false, 2800000, 1900, 5, 58.0D, 18.0F),
        KX_MINIGUN("KX-20 Fluxuated Minigun", true, false, 4200000, 260, 1, 58.0D, 9.0F),
        KX_RAILGUN("KX-20 Fluxuated Railgun", true, false, 4800000, 14500, 32, 92.0D, 68.0F);

        public final String displayName;
        public final boolean x10;
        public final boolean dual;
        public final int capacity;
        public final int energyCost;
        public final int cooldown;
        public final double range;
        public final float damage;

        Type(
                String displayName,
                boolean x10,
                boolean dual,
                int capacity,
                int energyCost,
                int cooldown,
                double range,
                float damage) {
            this.displayName = displayName;
            this.x10 = x10;
            this.dual = dual;
            this.capacity = capacity;
            this.energyCost = energyCost;
            this.cooldown = cooldown;
            this.range = range;
            this.damage = damage;
        }

        public boolean automatic() {
            return this == PROTOTYPE_MACHINEGUN || this == X10_MACHINEGUN || this == KX_MINIGUN;
        }

        public boolean shotgun() {
            return this == PROTOTYPE_SHOTGUN || this == X10_SHOTGUN || this == KX_SHOTGUN;
        }

        public boolean cannon() {
            return this == PROTOTYPE_CANNON || this == X10_CANNON;
        }

        public boolean kx() {
            return name().startsWith("KX_");
        }
    }

    private final Type type;
    private final AnimationFactory factory = new AnimationFactory(this);
    private final Map<AnimationController<?>, Integer> seenAnimations = new IdentityHashMap<>();

    public ItemEnergyGun(Type type) {
        this.type = type;
        setMaxStackSize(1);
    }

    public Type getType() {
        return type;
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    private static void animate(ItemStack stack, long tick, String action) {
        NBTTagCompound nbt = tag(stack);
        nbt.setString("GunAnimation", action);
        nbt.setLong("GunAnimationTick", tick);
        nbt.setInteger("GunAnimationSerial", nbt.getInteger("GunAnimationSerial") + 1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (player.getCooldownTracker().hasCooldown(this)
                || EnergyUtil.stored(stack) < type.energyCost) {
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        }
        if (type.automatic()) {
            tag(stack).setBoolean("GunFiring", true);
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        firePair(player, hand, stack);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private void firePair(EntityPlayer player, EnumHand hand, ItemStack stack) {
        if (player.world.isRemote) {
            animate(stack, player.world.getTotalWorldTime(), "fire");
            player.swingArm(hand);
        } else {
            fireServer(player, stack, hand);
        }

        if (type.dual) {
            EnumHand otherHand =
                    hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            ItemStack other = player.getHeldItem(otherHand);
            if (other.getItem() == this && EnergyUtil.stored(other) >= type.energyCost) {
                if (player.world.isRemote) animate(other, player.world.getTotalWorldTime(), "fire");
                else fireServer(player, other, otherHand);
            }
        }
        player.getCooldownTracker().setCooldown(this, type.cooldown);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (!(living instanceof EntityPlayer)
                || !type.automatic()
                || !tag(stack).getBoolean("GunFiring")) return;
        int used = getMaxItemUseDuration(stack) - count;
        if (used % type.cooldown != 0) return;
        EntityPlayer player = (EntityPlayer) living;
        if (EnergyUtil.stored(stack) < type.energyCost) {
            player.stopActiveHand();
            return;
        }
        if (player.world.isRemote) {
            animate(stack, player.world.getTotalWorldTime(), "automatic");
            player.swingArm(player.getActiveHand());
        } else fireServer(player, stack, player.getActiveHand());
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        tag(stack).setBoolean("GunFiring", false);
    }

    private void fireServer(EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (!EnergyUtil.drain(stack, type.energyCost, false)) return;
        animate(stack, player.world.getTotalWorldTime(), type.automatic() ? "automatic" : "fire");
        Vec3d look = player.getLookVec().normalize();
        Vec3d right = horizontalRight(look);
        Vec3d start =
                player.getPositionEyes(1.0F)
                        .add(look.scale(0.42D))
                        .add(right.scale(hand == EnumHand.MAIN_HAND ? 0.22D : -0.22D))
                        .addVector(0.0D, -0.12D, 0.0D);

        if (type == Type.KX_TRIBOW) fireTribow(player, start, look, right);
        else if (type == Type.KX_RAILGUN) fireRailgun(player, start, look);
        else if (type.shotgun()) fireShotgun(player, start, look, right);
        else if (type.cannon()) fireCannon(player, start, look);
        else if (type == Type.X10_NET_LAUNCHER) fireNet(player, start, look);
        else fireLaser(player, start, jitter(player, look, right), type.damage, type.range);

        player.world.playSound(
                null,
                player.posX,
                player.posY + 1.0D,
                player.posZ,
                type == Type.KX_RAILGUN
                        ? SoundEvents.ENTITY_LIGHTNING_THUNDER
                        : type.cannon()
                                ? SoundEvents.ENTITY_GENERIC_EXPLODE
                                : type.shotgun()
                                        ? SoundEvents.ENTITY_FIREWORK_BLAST
                                        : type == Type.X10_NET_LAUNCHER
                                                ? SoundEvents.ENTITY_SLIME_SQUISH
                                                : SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT,
                SoundCategory.PLAYERS,
                type.cannon() ? 0.85F : 0.62F,
                type.kx() ? 1.75F : type.x10 ? 1.45F : 1.15F);
    }

    private void fireTribow(EntityPlayer player, Vec3d start, Vec3d look, Vec3d right) {
        Vec3d up = right.crossProduct(look).normalize();
        List<Vec3d> ends = new ArrayList<>();
        double[][] spread = {{0, 0}, {0.026, 0.018}, {-0.026, -0.018}};
        for (double[] angle : spread) {
            Vec3d direction = look.add(right.scale(angle[0])).add(up.scale(angle[1])).normalize();
            Trace trace = trace(player, start, direction, type.range, null);
            ends.add(trace.end);
            if (trace.target != null) damage(player, trace.target, type.damage, 5);
        }
        PacketRToolEffect.send(player, PacketRToolEffect.GUN_KX_SPREAD, ends);
    }

    private void fireRailgun(EntityPlayer player, Vec3d start, Vec3d look) {
        Vec3d fullEnd = start.add(look.scale(type.range));
        RayTraceResult block = player.world.rayTraceBlocks(start, fullEnd, false, true, false);
        Vec3d end = block == null ? fullEnd : block.hitVec;
        List<EntityLivingBase> targets = candidates(player, start, look, type.range, 1.15D);
        targets.sort(
                java.util.Comparator.comparingDouble(
                        e -> start.squareDistanceTo(e.getPositionVector())));
        List<Vec3d> points = new ArrayList<>();
        int pierced = 0;
        for (EntityLivingBase target : targets) {
            if (target == player || !target.isEntityAlive()) continue;
            RayTraceResult intercept =
                    target.getEntityBoundingBox().grow(0.35D).calculateIntercept(start, end);
            if (intercept == null) continue;
            damage(player, target, type.damage * (1.0F + pierced * 0.08F), 8);
            points.add(intercept.hitVec);
            if (++pierced >= 10) break;
        }
        points.add(end);
        PacketRToolEffect.send(player, PacketRToolEffect.GUN_KX_RAIL, points);
    }

    private Vec3d jitter(EntityPlayer player, Vec3d look, Vec3d right) {
        if (!type.automatic()) return look;
        double phase =
                (player.world.getTotalWorldTime() * 37L + player.getEntityId() * 11L) % 17L - 8L;
        Vec3d up = right.crossProduct(look).normalize();
        double spread = type.x10 ? 0.007D : 0.014D;
        return look.add(right.scale(phase * spread))
                .add(up.scale(((phase * 7L) % 9L - 4L) * spread))
                .normalize();
    }

    private void fireLaser(
            EntityPlayer player, Vec3d start, Vec3d direction, float damage, double range) {
        Trace trace = trace(player, start, direction, range, null);
        if (trace.target != null) damage(player, trace.target, damage, type.x10 ? 3 : 2);
        PacketRToolEffect.send(
                player,
                type.kx()
                        ? PacketRToolEffect.GUN_KX
                        : type.x10 ? PacketRToolEffect.GUN_LASER_X10 : PacketRToolEffect.GUN_LASER,
                java.util.Collections.singletonList(trace.end));
    }

    private void fireShotgun(EntityPlayer player, Vec3d start, Vec3d look, Vec3d right) {
        Vec3d up = right.crossProduct(look).normalize();
        List<EntityLivingBase> candidates = candidates(player, start, look, type.range, 2.5D);
        List<Vec3d> ends = new ArrayList<>();
        double[][] spread =
                type.x10
                        ? new double[][] {
                            {0, 0},
                            {.045, 0},
                            {-.045, 0},
                            {0, .045},
                            {0, -.045},
                            {.038, .038},
                            {-.038, .038},
                            {.038, -.038},
                            {-.038, -.038}
                        }
                        : new double[][] {
                            {0, 0},
                            {.07, 0},
                            {-.07, 0},
                            {0, .065},
                            {0, -.065},
                            {.055, .05},
                            {-.055, .05}
                        };
        for (int i = 0; i < spread.length; i++) {
            Vec3d direction =
                    look.add(right.scale(spread[i][0])).add(up.scale(spread[i][1])).normalize();
            Trace trace = trace(player, start, direction, type.range, candidates);
            ends.add(trace.end);
            if (trace.target != null) {
                float damage = type.x10 ? (i == 0 ? 17.0F : 4.0F) : 3.4F;
                damage(player, trace.target, damage, type.x10 ? 4 : 3);
            }
        }
        if (type.x10 && !ends.isEmpty()) areaDamage(player, ends.get(0), 1.8D, 7.0F, 0.35D);
        PacketRToolEffect.send(
                player,
                type.kx()
                        ? PacketRToolEffect.GUN_KX_SPREAD
                        : type.x10
                                ? PacketRToolEffect.GUN_SHOTGUN_X10
                                : PacketRToolEffect.GUN_SHOTGUN,
                ends);
    }

    private void fireCannon(EntityPlayer player, Vec3d start, Vec3d look) {
        Trace trace = trace(player, start, look, type.range, null);
        if (trace.target != null) damage(player, trace.target, type.damage, type.x10 ? 7 : 5);
        areaDamage(
                player,
                trace.end,
                type.x10 ? 4.0D : 2.6D,
                type.x10 ? 30.0F : 15.0F,
                type.x10 ? 1.15D : 0.7D);
        PacketRToolEffect.send(
                player,
                type.x10 ? PacketRToolEffect.GUN_CANNON_X10 : PacketRToolEffect.GUN_CANNON,
                java.util.Collections.singletonList(trace.end));
    }

    private void fireNet(EntityPlayer player, Vec3d start, Vec3d look) {
        Trace trace = trace(player, start, look, type.range, null);
        if (trace.target != null) {
            damage(player, trace.target, type.damage, 0);
            trace.target.addPotionEffect(
                    new PotionEffect(MobEffects.SLOWNESS, 160, 10, false, true));
            trace.target.addPotionEffect(
                    new PotionEffect(MobEffects.WEAKNESS, 160, 3, false, true));
            trace.target.addPotionEffect(new PotionEffect(MobEffects.GLOWING, 160, 0, false, true));
            trace.target.addPotionEffect(
                    new PotionEffect(MobEffects.MINING_FATIGUE, 160, 4, false, true));
        }
        PacketRToolEffect.send(
                player, PacketRToolEffect.GUN_NET, java.util.Collections.singletonList(trace.end));
    }

    private void areaDamage(
            EntityPlayer player, Vec3d center, double radius, float damage, double knockback) {
        AxisAlignedBB box =
                new AxisAlignedBB(
                        center.x - radius,
                        center.y - radius,
                        center.z - radius,
                        center.x + radius,
                        center.y + radius,
                        center.z + radius);
        int affected = 0;
        for (EntityLivingBase target :
                player.world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            if (target == player || !target.isEntityAlive()) continue;
            Vec3d delta =
                    target.getPositionVector()
                            .addVector(0.0D, target.height * 0.5D, 0.0D)
                            .subtract(center);
            double distance = delta.lengthVector();
            if (distance > radius) continue;
            float scaled = (float) (damage * (1.0D - distance / (radius * 1.35D)));
            if (damage(player, target, Math.max(2.0F, scaled), type.x10 ? 5 : 3)
                    && knockback > 0.0D) {
                Vec3d direction = distance < 0.1D ? new Vec3d(0, 1, 0) : delta.normalize();
                double force = knockback * (1.0D - distance / radius);
                target.addVelocity(direction.x * force, 0.12D + force * 0.35D, direction.z * force);
            }
            if (++affected >= 16) break;
        }
    }

    private boolean damage(
            EntityPlayer player, EntityLivingBase target, float amount, int fireSeconds) {
        ResourceLocation id = EntityList.getKey(target);
        if (id != null && "srparasites".equals(id.getResourceDomain()))
            amount *= type.kx() ? 1.7F : type.x10 ? 1.35F : 1.2F;
        DamageSource source =
                DamageSource.causePlayerDamage(player).setProjectile().setFireDamage();
        boolean hit = target.attackEntityFrom(source, amount);
        if (hit && fireSeconds > 0) target.setFire(fireSeconds);
        return hit;
    }

    private Trace trace(
            EntityPlayer player,
            Vec3d start,
            Vec3d direction,
            double range,
            @Nullable List<EntityLivingBase> suppliedCandidates) {
        Vec3d fullEnd = start.add(direction.scale(range));
        RayTraceResult block = player.world.rayTraceBlocks(start, fullEnd, false, true, false);
        Vec3d end = block == null ? fullEnd : block.hitVec;
        double closest = start.squareDistanceTo(end);
        EntityLivingBase hit = null;
        List<EntityLivingBase> list =
                suppliedCandidates == null
                        ? candidates(player, start, direction, range, 1.0D)
                        : suppliedCandidates;
        for (EntityLivingBase target : list) {
            if (target == player || !target.isEntityAlive() || !target.canBeCollidedWith())
                continue;
            RayTraceResult intercept =
                    target.getEntityBoundingBox().grow(0.3D).calculateIntercept(start, end);
            if (intercept == null) continue;
            double distance = start.squareDistanceTo(intercept.hitVec);
            if (distance < closest) {
                closest = distance;
                end = intercept.hitVec;
                hit = target;
            }
        }
        return new Trace(end, hit);
    }

    private static List<EntityLivingBase> candidates(
            EntityPlayer player, Vec3d start, Vec3d direction, double range, double grow) {
        Vec3d end = start.add(direction.scale(range));
        AxisAlignedBB bounds =
                new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z).grow(grow);
        return player.world.getEntitiesWithinAABB(EntityLivingBase.class, bounds);
    }

    private static Vec3d horizontalRight(Vec3d look) {
        Vec3d right = new Vec3d(-look.z, 0.0D, look.x);
        return right.lengthSquared() < 0.001D ? new Vec3d(1, 0, 0) : right.normalize();
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : event.getExtraDataOfType(ItemStack.class).get(0);
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        String action = nbt == null ? "" : nbt.getString("GunAnimation");
        long animationAge =
                nbt == null || Minecraft.getMinecraft().world == null
                        ? Long.MAX_VALUE
                        : Minecraft.getMinecraft().world.getTotalWorldTime()
                                - nbt.getLong("GunAnimationTick");
        long animationWindow = "automatic".equals(action) ? 5L : 11L;
        if (!action.isEmpty()
                && Minecraft.getMinecraft().world != null
                && animationAge <= animationWindow) {
            int serial = nbt.getInteger("GunAnimationSerial");
            Integer seen = seenAnimations.get(event.getController());
            if (seen == null || seen != serial) {
                event.getController().markNeedsReload();
                seenAnimations.put(event.getController(), serial);
            }
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.energy_gun." + action, false));
            return PlayState.CONTINUE;
        }
        event.getController()
                .setAnimation(
                        new AnimationBuilder().addAnimation("animation.energy_gun.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return EnumAction.NONE;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemEnergyGun>(this, "energy_gun", 1, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(type.capacity, type.kx() ? 96000 : type.x10 ? 32000 : 12000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < type.capacity;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) type.capacity;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }

    private static final class Trace {
        final Vec3d end;
        final EntityLivingBase target;

        Trace(Vec3d end, @Nullable EntityLivingBase target) {
            this.end = end;
            this.target = target;
        }
    }
}
