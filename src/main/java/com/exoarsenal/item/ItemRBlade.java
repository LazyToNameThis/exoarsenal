package com.exoarsenal.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.exoarsenal.event.RmorEventHandler;
import com.exoarsenal.entity.EntityRBladeDisc;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class ItemRBlade extends ItemSword implements IAnimatable {
    public static final int CAPACITY = 250000;
    public static final int ATTACK_COST = 600;
    private static final int AIR_ATTACK_COST = 90;
    private static final int SPIN_PULSE_COST = 180;
    private final AnimationFactory factory = new AnimationFactory(this);
    private final Map<AnimationController<?>, Integer> seenAnimations = new IdentityHashMap<>();

    public ItemRBlade() {
        super(Item.ToolMaterial.DIAMOND);
        setMaxStackSize(1);
    }

    public static boolean isActive(ItemStack stack) {
        return stack.hasTagCompound()
                && stack.getTagCompound().getBoolean("Active")
                && !stack.getTagCompound().getBoolean("Thrown")
                && EnergyUtil.stored(stack) >= ATTACK_COST;
    }

    public static boolean isSpinning(ItemStack stack) {
        return stack.hasTagCompound() && stack.getTagCompound().getBoolean("Spinning");
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    public static void animate(ItemStack stack, String animation, long worldTick) {
        NBTTagCompound nbt = tag(stack);
        nbt.setString("Animation", animation);
        nbt.setLong("AnimationTick", worldTick);
        nbt.setInteger("AnimationSerial", nbt.getInteger("AnimationSerial") + 1);
    }

    public static int animationDuration(String animation) {
        if ("activate".equals(animation)) return 15;
        if ("deactivate".equals(animation)) return 12;
        if (animation.startsWith("attack_titanic")) return 28;
        if (animation.startsWith("attack_berserk")) return 20;
        if (animation.startsWith("attack_stylish")) return 16;
        if ("spin_start".equals(animation)) return 8;
        if ("throw".equals(animation) || "catch".equals(animation)) return 14;
        return 18;
    }

    public static String getCurrentAnimation(ItemStack stack, long worldTick) {
        if (!(stack.getItem() instanceof ItemRBlade) || !stack.hasTagCompound()) return "";
        NBTTagCompound nbt = stack.getTagCompound();
        if (stack.getItem() instanceof ItemX10Blade && nbt.getBoolean("X10Rapid"))
            return "x10_rapid";
        String action = nbt.getString("Animation");
        if (action.isEmpty()
                || worldTick - nbt.getLong("AnimationTick") > animationDuration(action)) return "";
        return action;
    }

    public static long getAnimationTick(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getLong("AnimationTick") : 0L;
    }

    public static int getAnimationSerial(ItemStack stack) {
        return stack.hasTagCompound() ? stack.getTagCompound().getInteger("AnimationSerial") : 0;
    }

    public static boolean beginSpin(EntityPlayer player, EnumHand hand) {
        return beginSpin(player, hand, false);
    }

    public static boolean beginSpinFromInput(EntityPlayer player, EnumHand hand) {
        return beginSpin(player, hand, true);
    }

    private static boolean beginSpin(
            EntityPlayer player, EnumHand hand, boolean authoritativeInput) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemRBlade) || EnergyUtil.stored(stack) < ATTACK_COST)
            return false;
        NBTTagCompound nbt = tag(stack);
        if (nbt.getBoolean("Thrown")) return false;
        if (!authoritativeInput && !isActive(stack)) return false;
        if (isSpinning(stack)) {
            player.setActiveHand(hand);
            return true;
        }

        if (authoritativeInput) nbt.setBoolean("Active", true);
        nbt.setBoolean("Spinning", true);
        animate(stack, "spin_start", player.world.getTotalWorldTime());
        player.setActiveHand(hand);
        return true;
    }

    public static boolean attackAir(EntityPlayer player, EnumHand hand, boolean consumeEnergy) {
        ItemStack stack = player.getHeldItem(hand);
        if (!(stack.getItem() instanceof ItemRBlade) || !isActive(stack) || isSpinning(stack))
            return false;
        NBTTagCompound nbt = tag(stack);
        long now = player.world.getTotalWorldTime();
        if (now - nbt.getLong("AirAttackTick") < 4L) return false;
        if (consumeEnergy && !EnergyUtil.drain(stack, AIR_ATTACK_COST, false)) return false;
        nbt.setLong("AirAttackTick", now);
        triggerAttack(stack, player);
        player.swingArm(hand);
        if (!player.world.isRemote)
            player.world.playSound(
                    null,
                    player.posX,
                    player.posY + 1.0D,
                    player.posZ,
                    SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                    SoundCategory.PLAYERS,
                    0.72F,
                    1.35F);
        return true;
    }

    private static int triggerAttack(ItemStack stack, EntityLivingBase attacker) {
        NBTTagCompound nbt = tag(stack);
        int combo = nbt.getInteger("AttackCycle") + 1;
        nbt.setInteger("AttackCycle", combo);
        String mode = RmorEventHandler.getModeName(attacker);
        int variation = "stylish".equals(mode) ? ((combo - 1) % 3) + 1 : ((combo - 1) % 2) + 1;
        int pattern =
                "berserk".equals(mode)
                        ? 3 + variation - 1
                        : "titanic".equals(mode) ? 5 + variation - 1 : variation - 1;
        animate(stack, "attack_" + mode + "_" + variation, attacker.world.getTotalWorldTime());
        return pattern;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (isSpinning(stack)) {
            player.setActiveHand(hand);
            return new ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
        }
        boolean activating = !isActive(stack);
        if (activating && EnergyUtil.stored(stack) < ATTACK_COST) {
            if (!world.isRemote)
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.rblade_empty"), true);
            return new ActionResult<>(net.minecraft.util.EnumActionResult.FAIL, stack);
        }
        tag(stack).setBoolean("Active", activating);
        animate(stack, activating ? "activate" : "deactivate", world.getTotalWorldTime());
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.PLAYERS,
                0.7F,
                activating ? 1.65F : 0.8F);
        return new ActionResult<>(net.minecraft.util.EnumActionResult.SUCCESS, stack);
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
    public void onUsingTick(ItemStack stack, EntityLivingBase living, int count) {
        if (!(living instanceof EntityPlayer) || !isSpinning(stack)) return;
        EntityPlayer player = (EntityPlayer) living;
        if (player.world.isRemote) {
            if ((player.ticksExisted & 1) == 0) spinParticles(player);
            return;
        }

        if (count % 6 != 0) return;
        if (!EnergyUtil.drain(stack, SPIN_PULSE_COST, false)) {
            player.stopActiveHand();
            return;
        }

        Vec3d look = player.getLookVec().normalize();
        Vec3d center =
                player.getPositionEyes(1.0F).add(look.scale(2.0D)).addVector(0.0D, -0.15D, 0.0D);
        AxisAlignedBB sweep =
                new AxisAlignedBB(
                        center.x - 2.1D,
                        center.y - 2.1D,
                        center.z - 2.1D,
                        center.x + 2.1D,
                        center.y + 2.1D,
                        center.z + 2.1D);
        player.getEntityData().setBoolean("WastelandEnergySweep", true);
        try {
            int examined = 0;
            int hit = 0;
            for (EntityLivingBase target :
                    player.world.getEntitiesWithinAABB(EntityLivingBase.class, sweep)) {
                if (target == player || !target.isEntityAlive()) continue;
                if (++examined > 32) break;
                Vec3d offset =
                        target.getPositionVector()
                                .addVector(0.0D, target.height * 0.5D, 0.0D)
                                .subtract(player.getPositionEyes(1.0F));
                double distanceSq = offset.lengthSquared();
                double forward = offset.dotProduct(look);
                if (distanceSq > 16.0D
                        || forward <= 0.0D
                        || forward * forward < distanceSq * 0.0225D) continue;
                if (target.attackEntityFrom(
                        DamageSource.causePlayerDamage(player).setFireDamage(), 3.75F)) {
                    target.setFire(2);
                    if (++hit >= 12) break;
                }
            }
        } finally {
            player.getEntityData().setBoolean("WastelandEnergySweep", false);
        }
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        if (!(living instanceof EntityPlayer) || !isSpinning(stack)) return;
        EntityPlayer player = (EntityPlayer) living;
        tag(stack).setBoolean("Spinning", false);
        if (world.isRemote
                || tag(stack).getBoolean("Thrown")
                || EnergyUtil.stored(stack) < ATTACK_COST) return;
        tag(stack).setBoolean("Thrown", true);
        animate(stack, "throw", world.getTotalWorldTime());
        world.spawnEntity(
                new EntityRBladeDisc(world, player, stack.getItem() instanceof ItemX10Blade));
        world.playSound(
                null,
                player.posX,
                player.posY + 1.0D,
                player.posZ,
                SoundEvents.ENTITY_ENDERDRAGON_FLAP,
                SoundCategory.PLAYERS,
                0.55F,
                1.75F);
    }

    private static void spinParticles(EntityPlayer player) {
        Vec3d look = player.getLookVec().normalize();
        Vec3d right = new Vec3d(-look.z, 0.0D, look.x);
        if (right.lengthSquared() < 0.001D) right = new Vec3d(1.0D, 0.0D, 0.0D);
        right = right.normalize();
        Vec3d up = right.crossProduct(look).normalize();
        Vec3d center =
                player.getPositionEyes(1.0F).add(look.scale(1.65D)).addVector(0.0D, 0.12D, 0.0D);
        double phase = (player.ticksExisted * 1.55D) % (Math.PI * 2.0D);
        for (int i = 0; i < 2; i++) {
            double angle = phase + i * Math.PI;
            Vec3d point =
                    center.add(right.scale(Math.cos(angle) * 1.32D))
                            .add(up.scale(Math.sin(angle) * 1.32D));
            player.world.spawnParticle(
                    EnumParticleTypes.REDSTONE, point.x, point.y, point.z, 1.0D, 0.08D, 0.02D);
            if ((player.ticksExisted + i) % 4 == 0)
                player.world.spawnParticle(
                        EnumParticleTypes.FLAME,
                        point.x,
                        point.y,
                        point.z,
                        look.x * 0.03D,
                        look.y * 0.03D,
                        look.z * 0.03D);
        }
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (!isActive(stack) || !EnergyUtil.drain(stack, ATTACK_COST, false)) return false;
        target.setFire(6);
        triggerAttack(stack, attacker);
        if (attacker.world instanceof WorldServer) {
            WorldServer server = (WorldServer) attacker.world;
            server.spawnParticle(
                    EnumParticleTypes.REDSTONE,
                    target.posX,
                    target.posY + target.height * 0.55D,
                    target.posZ,
                    4,
                    0.28D,
                    0.3D,
                    0.28D,
                    0.01D);
            server.spawnParticle(
                    EnumParticleTypes.FLAME,
                    target.posX,
                    target.posY + target.height * 0.5D,
                    target.posZ,
                    2,
                    0.2D,
                    0.22D,
                    0.2D,
                    0.02D);
        }
        return true;
    }

    @Override
    public Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier>
            getAttributeModifiers(EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, net.minecraft.entity.ai.attributes.AttributeModifier> map =
                HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND) {
            double damage = isActive(stack) ? 10.0D : 0.0D;
            map.put(
                    SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new net.minecraft.entity.ai.attributes.AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER, "Prototype R-Blade damage", damage, 0));
            map.put(
                    SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new net.minecraft.entity.ai.attributes.AttributeModifier(
                            ATTACK_SPEED_MODIFIER, "Prototype R-Blade speed", -2.15D, 0));
        }
        return map;
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : event.getExtraDataOfType(ItemStack.class).get(0);
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        if (stack.getItem() instanceof ItemX10Blade && nbt != null && nbt.getBoolean("X10Rapid")) {
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.rblade.x10_rapid", true));
            return PlayState.CONTINUE;
        }
        String action =
                Minecraft.getMinecraft().world == null
                        ? ""
                        : getCurrentAnimation(
                                stack, Minecraft.getMinecraft().world.getTotalWorldTime());
        if (!action.isEmpty()) {
            int serial = nbt.getInteger("AnimationSerial");
            Integer seen = seenAnimations.get(event.getController());
            if (seen == null || seen != serial) {
                event.getController().markNeedsReload();
                seenAnimations.put(event.getController(), serial);
            }
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.rblade." + action, false));
            return PlayState.CONTINUE;
        }
        if (isSpinning(stack)) {
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.rblade.spin_loop", true));
            return PlayState.CONTINUE;
        }
        event.getController()
                .setAnimation(
                        new AnimationBuilder()
                                .addAnimation(
                                        isActive(stack)
                                                ? "animation.rblade.active"
                                                : "animation.rblade.inactive",
                                        true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemRBlade>(this, "rblade", 2, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(CAPACITY, 8000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) CAPACITY;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }
}
