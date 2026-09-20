package com.exoarsenal.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.exoarsenal.entity.EntityAuroraField;
import com.exoarsenal.entity.EntityIceFragment;
import com.exoarsenal.network.PacketRToolEffect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ItemScoutWeapon extends Item implements IAnimatable {
    public enum Type {
        PINCER,
        RAILGUN,
        SMASHER,
        AURORA
    }

    private static final UUID DAMAGE = UUID.fromString("334f803c-0999-4750-9486-404630c5dc32");
    private static final UUID SPEED = UUID.fromString("998eb727-9198-4890-a8d6-7806c40101af");
    private final Type type;
    private final AnimationFactory factory = new AnimationFactory(this);
    private final Map<AnimationController<?>, Integer> seen = new IdentityHashMap<>();

    public ItemScoutWeapon(Type type) {
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

    private static void animate(ItemStack stack, World world, String action) {
        NBTTagCompound nbt = tag(stack);
        nbt.setString("ScoutAnimation", action);
        nbt.setBoolean("ScoutCharging", ScoutWeaponAnimation.charging(action));
        nbt.setLong("ScoutAnimationTick", world.getTotalWorldTime());
        nbt.setInteger("ScoutAnimationSerial", nbt.getInteger("ScoutAnimationSerial") + 1);
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (type == Type.RAILGUN) {
            if (player.isSneaking()) lockRailgun(world, player, stack);
            else fireRailgun(world, player, stack, hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (type == Type.AURORA) {
            castAurora(world, player, stack);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        animate(stack, world, type == Type.PINCER ? "crush_charge" : "slam_charge");
        player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack, World world, EntityLivingBase living, int timeLeft) {
        if (!(living instanceof EntityPlayer) || (type != Type.PINCER && type != Type.SMASHER))
            return;
        EntityPlayer player = (EntityPlayer) living;
        float charge = Math.min(1.0F, (getMaxItemUseDuration(stack) - timeLeft) / 40.0F);
        if (charge < 0.12F) {
            animate(stack, world, "idle");
            return;
        }
        animate(stack, world, type == Type.PINCER ? "crush" : "slam");
        if (!world.isRemote) chargedImpact(player, charge);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity holder, int slot, boolean selected) {

        if (world.isRemote
                || !stack.hasTagCompound()
                || !stack.getTagCompound().getBoolean("ScoutCharging")) return;
        boolean using =
                holder instanceof EntityLivingBase
                        && ((EntityLivingBase) holder).isHandActive()
                        && ((EntityLivingBase) holder).getActiveItemStack() == stack;
        if (!using) animate(stack, world, "idle");
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        animate(stack, attacker.world, type == Type.PINCER ? "snap" : "strike");
        if (attacker.world.isRemote || !(attacker instanceof EntityPlayer)) return true;
        EntityPlayer player = (EntityPlayer) attacker;
        if (type == Type.PINCER) {
            int combo = tag(stack).getInteger("PincerCombo") % 3 + 1;
            tag(stack).setInteger("PincerCombo", combo);
            if (combo == 3) frostShockwave(player, target.getPositionVector(), 4.2D, 7.0F);
            PotionEffect frozen = target.getActivePotionEffect(MobEffects.SLOWNESS);
            if (frozen != null && frozen.getAmplifier() >= 2) shatter(player, target);
        } else if (type == Type.SMASHER) {
            frostShockwave(player, target.getPositionVector(), 2.8D, 5.0F);
            if (isArmoredOrMechanical(target))
                target.attackEntityFrom(DamageSource.causePlayerDamage(player), 10.0F);
        }
        return true;
    }

    private void chargedImpact(EntityPlayer player, float charge) {
        double reach = type == Type.PINCER ? 4.5D : 6.5D;
        float damage = (type == Type.PINCER ? 16.0F : 24.0F) * (0.45F + charge * 0.55F);
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        Vec3d center = eye.add(look.scale(reach * 0.55D));
        int hit = 0;
        for (EntityLivingBase target :
                player.world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        new AxisAlignedBB(
                                center.x - reach,
                                center.y - 2.5D,
                                center.z - reach,
                                center.x + reach,
                                center.y + 2.5D,
                                center.z + reach))) {
            if (target == player || !target.isEntityAlive()) continue;
            Vec3d offset =
                    target.getPositionVector().addVector(0, target.height * 0.5D, 0).subtract(eye);
            if (offset.lengthSquared() > reach * reach
                    || offset.normalize().dotProduct(look) < 0.15D) continue;
            float dealt =
                    isArmoredOrMechanical(target) && type == Type.SMASHER ? damage * 1.5F : damage;
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), dealt);
            target.addPotionEffect(
                    new PotionEffect(
                            MobEffects.SLOWNESS,
                            (int) (50 + charge * 100),
                            charge >= 0.98F ? 10 : 3));
            if (charge >= 0.98F && target.width < 2.5F)
                target.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 45, 128));
            if (++hit >= 16) break;
        }
        frostShockwave(player, center, reach, damage * 0.28F);
        if (type == Type.SMASHER) spawnForwardFragments(player, eye, look, 7);
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.BLOCK_ANVIL_LAND,
                SoundCategory.PLAYERS,
                1.0F,
                type == Type.PINCER ? 0.75F : 0.52F);
    }

    private void lockRailgun(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote) return;
        EntityLivingBase best = null;
        double bestScore = Double.MAX_VALUE;
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, player.getEntityBoundingBox().grow(28.0D))) {
            if (target == player || !target.isEntityAlive()) continue;
            Vec3d offset =
                    target.getPositionVector().addVector(0, target.height * 0.5D, 0).subtract(eye);
            double distance = offset.lengthVector();
            if (distance > 28.0D
                    || offset.normalize().dotProduct(look) < 0.72D
                    || !player.canEntityBeSeen(target)) continue;
            double score = distance - offset.normalize().dotProduct(look) * 8.0D;
            if (score < bestScore) {
                best = target;
                bestScore = score;
            }
        }
        if (best != null) {
            tag(stack).setInteger("RailLock", best.getEntityId());
            tag(stack).setLong("RailLockUntil", world.getTotalWorldTime() + 100L);
            animate(stack, world, "lock");
        }
    }

    private void fireRailgun(World world, EntityPlayer player, ItemStack stack, EnumHand hand) {
        if (player.getCooldownTracker().hasCooldown(this) || EnergyUtil.stored(stack) < 250) return;
        player.getCooldownTracker().setCooldown(this, 40);
        animate(stack, world, "rail_fire");
        player.swingArm(hand);
        if (world.isRemote || !EnergyUtil.drain(stack, 250, false)) return;
        Vec3d start = player.getPositionEyes(1.0F).addVector(0, -0.1D, 0);
        Entity lockedEntity = world.getEntityByID(tag(stack).getInteger("RailLock"));
        boolean locked =
                lockedEntity instanceof EntityLivingBase
                        && lockedEntity.isEntityAlive()
                        && tag(stack).getLong("RailLockUntil") >= world.getTotalWorldTime();
        int focusId = tag(stack).getInteger("RailFocus");
        int focus = tag(stack).getInteger("RailFocusHits");
        Vec3d direction =
                locked
                        ? lockedEntity
                                .getPositionVector()
                                .addVector(0, lockedEntity.height * 0.55D, 0)
                                .subtract(start)
                                .normalize()
                        : player.getLookVec().normalize();
        Entity focusEntity = world.getEntityByID(focusId);
        if (!locked
                && focus > 0
                && focusEntity instanceof EntityLivingBase
                && focusEntity.isEntityAlive()
                && focusEntity.getDistance(player) <= 72.0F) {
            Vec3d focused =
                    focusEntity
                            .getPositionVector()
                            .addVector(0, focusEntity.height * 0.55D, 0)
                            .subtract(start)
                            .normalize();
            double assist = Math.min(0.40D, focus * 0.08D);
            direction = direction.scale(1.0D - assist).add(focused.scale(assist)).normalize();
        }
        Vec3d fullEnd = start.add(direction.scale(72.0D));
        RayTraceResult block = world.rayTraceBlocks(start, fullEnd, false, true, false);
        Vec3d end = block == null ? fullEnd : block.hitVec;
        List<RailHit> hits = new ArrayList<>();
        AxisAlignedBB bounds =
                new AxisAlignedBB(start.x, start.y, start.z, end.x, end.y, end.z).grow(1.4D);
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(EntityLivingBase.class, bounds)) {
            if (target == player || !target.isEntityAlive()) continue;
            RayTraceResult intercept =
                    target.getEntityBoundingBox().grow(0.42D).calculateIntercept(start, end);
            if (intercept != null)
                hits.add(new RailHit(target, start.squareDistanceTo(intercept.hitVec)));
        }
        hits.sort(Comparator.comparingDouble(value -> value.distance));
        if (hits.size() > 10) hits = new ArrayList<>(hits.subList(0, 10));
        float wastedMultiplier = 1.0F + (10 - hits.size()) * 0.10F;
        for (RailHit hit : hits) {
            if (focusId == hit.target.getEntityId()) focus = Math.min(5, focus + 1);
            else {
                focusId = hit.target.getEntityId();
                focus = 0;
            }
            hit.target.attackEntityFrom(
                    DamageSource.causePlayerDamage(player).setProjectile(),
                    18.0F * wastedMultiplier * (1.0F + focus * 0.12F));
            hit.target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 90, 4));
        }
        tag(stack).setInteger("RailFocus", focusId);
        tag(stack).setInteger("RailFocusHits", focus);
        List<Vec3d> path = new ArrayList<>();
        Vec3d side = direction.crossProduct(new Vec3d(0, 1, 0));
        if (side.lengthSquared() < 0.01D) side = new Vec3d(1, 0, 0);
        else side = side.normalize();
        for (int i = 1; i <= 10; i++) {
            double t = i / 10.0D;
            double curve = locked ? Math.sin(t * Math.PI) * 0.45D : 0.0D;
            path.add(start.add(end.subtract(start).scale(t)).add(side.scale(curve)));
        }
        PacketRToolEffect.send(player, PacketRToolEffect.SCOUT_RAILGUN, path);
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ENTITY_LIGHTNING_THUNDER,
                SoundCategory.PLAYERS,
                0.65F,
                1.65F);
    }

    private void castAurora(World world, EntityPlayer player, ItemStack stack) {
        if (world.isRemote || player.getCooldownTracker().hasCooldown(this)) return;
        for (EntityAuroraField field :
                world.getEntities(EntityAuroraField.class, value -> value.belongsTo(player))) {
            field.detonate(player);
            animate(stack, world, "aurora_detonate");
            player.getCooldownTracker().setCooldown(this, 30);
            return;
        }
        EntityAuroraField field = new EntityAuroraField(world, player);
        world.spawnEntity(field);
        animate(stack, world, "aurora_cast");
        player.getCooldownTracker().setCooldown(this, 20);
    }

    private static void frostShockwave(
            EntityPlayer player, Vec3d center, double radius, float damage) {
        int hit = 0;
        for (EntityLivingBase target :
                player.world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        new AxisAlignedBB(
                                center.x - radius,
                                center.y - 2.0D,
                                center.z - radius,
                                center.x + radius,
                                center.y + 2.0D,
                                center.z + radius))) {
            if (target == player
                    || !target.isEntityAlive()
                    || target.getDistanceSq(center.x, center.y, center.z) > radius * radius)
                continue;
            target.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
            target.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 65, 2));
            if (++hit >= 16) break;
        }
        if (player.world instanceof WorldServer)
            ((WorldServer) player.world)
                    .spawnParticle(
                            EnumParticleTypes.SNOW_SHOVEL,
                            center.x,
                            center.y + 0.35D,
                            center.z,
                            28,
                            radius * 0.45D,
                            0.25D,
                            radius * 0.45D,
                            0.08D);
    }

    private static void shatter(EntityPlayer player, EntityLivingBase frozen) {
        frozen.attackEntityFrom(DamageSource.causePlayerDamage(player), 8.0F);
        frostShockwave(player, frozen.getPositionVector(), 3.5D, 5.0F);
        Vec3d start = frozen.getPositionVector().addVector(0, frozen.height * 0.55D, 0);
        for (int i = 0; i < 8; i++) {
            double angle = i * Math.PI * 0.25D;
            Vec3d direction = new Vec3d(Math.cos(angle), 0.12D + (i & 1) * 0.12D, Math.sin(angle));
            player.world.spawnEntity(new EntityIceFragment(player.world, player, start, direction));
        }
    }

    private static void spawnForwardFragments(
            EntityPlayer player, Vec3d start, Vec3d look, int count) {
        Vec3d right = look.crossProduct(new Vec3d(0, 1, 0));
        if (right.lengthSquared() < 0.01D) right = new Vec3d(1, 0, 0);
        else right = right.normalize();
        Vec3d up = right.crossProduct(look).normalize();
        for (int i = 0; i < count; i++) {
            double across = (i - (count - 1) * 0.5D) * 0.075D;
            double rise = 0.02D + (i % 3) * 0.055D;
            Vec3d direction = look.add(right.scale(across)).add(up.scale(rise)).normalize();
            player.world.spawnEntity(
                    new EntityIceFragment(
                            player.world, player, start.add(look.scale(0.8D)), direction));
        }
    }

    private static boolean isArmoredOrMechanical(EntityLivingBase target) {
        ResourceLocation id = EntityList.getKey(target);
        String name = id == null ? "" : id.toString().toLowerCase(java.util.Locale.ROOT);
        return target.getTotalArmorValue() >= 8
                || name.contains("golem")
                || name.contains("robot")
                || name.contains("mech")
                || name.contains("scout");
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return type == Type.PINCER || type == Type.SMASHER ? EnumAction.BOW : EnumAction.NONE;
    }

    @Override
    public boolean isFull3D() {
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(
            EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        if (slot != EntityEquipmentSlot.MAINHAND) return map;
        double damage =
                type == Type.PINCER
                        ? 8.0D
                        : type == Type.SMASHER ? 23.0D : type == Type.AURORA ? 5.0D : 3.0D;
        double speed = type == Type.PINCER ? -1.0D : type == Type.SMASHER ? -3.45D : -2.4D;
        map.put(
                SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                new AttributeModifier(DAMAGE, "Scout weapon damage", damage, 0));
        map.put(
                SharedMonsterAttributes.ATTACK_SPEED.getName(),
                new AttributeModifier(SPEED, "Scout weapon speed", speed, 0));
        return map;
    }

    private <P extends IAnimatable> PlayState animation(AnimationEvent<P> event) {
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : event.getExtraDataOfType(ItemStack.class).get(0);
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        String action = nbt == null ? "" : nbt.getString("ScoutAnimation");
        if (!action.isEmpty()
                && Minecraft.getMinecraft().world != null
                && ScoutWeaponAnimation.playing(
                        action,
                        Minecraft.getMinecraft().world.getTotalWorldTime()
                                - nbt.getLong("ScoutAnimationTick"),
                        nbt.getBoolean("ScoutCharging"))) {
            int serial = nbt.getInteger("ScoutAnimationSerial");
            if (!java.util.Objects.equals(seen.get(event.getController()), serial)) {
                event.getController().markNeedsReload();
                seen.put(event.getController(), serial);
            }
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation(
                                            "animation.scout_weapon." + action,
                                            ScoutWeaponAnimation.charging(action)));
        } else
            event.getController()
                    .setAnimation(
                            new AnimationBuilder()
                                    .addAnimation("animation.scout_weapon.idle", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemScoutWeapon>(this, "scout_weapon", 2, this::animation));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return type == Type.RAILGUN ? EnergyUtil.provider(200000, 8000) : null;
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return type == Type.RAILGUN && EnergyUtil.stored(stack) < 200000;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return type == Type.RAILGUN ? 1.0D - EnergyUtil.stored(stack) / 200000.0D : 0.0D;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }

    private static final class RailHit {
        final EntityLivingBase target;
        final double distance;

        RailHit(EntityLivingBase target, double distance) {
            this.target = target;
            this.distance = distance;
        }
    }
}
