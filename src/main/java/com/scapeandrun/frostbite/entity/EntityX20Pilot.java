package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAILeapAtTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.UUID;

public final class EntityX20Pilot extends EntityMob implements IAnimatable {
    private static final DataParameter<Integer> ATTACK =
            EntityDataManager.createKey(EntityX20Pilot.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACK_TICK =
            EntityDataManager.createKey(EntityX20Pilot.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SCOUT_ID =
            EntityDataManager.createKey(EntityX20Pilot.class, DataSerializers.VARINT);
    private final AnimationFactory factory = new AnimationFactory(this);
    private final BossInfoServer bossInfo =
            new BossInfoServer(
                    getDisplayName(), BossInfo.Color.PURPLE, BossInfo.Overlay.NOTCHED_10);
    private UUID scoutUuid;
    private int cooldown = 24;
    private boolean alternate;

    public EntityX20Pilot(World world) {
        super(world);
        setSize(0.72F, 2.05F);
        experienceValue = 95;
        stepHeight = 1.15F;
    }

    public EntityX20Pilot(World world, EntityX20Scout scout) {
        this(world);
        dataManager.set(SCOUT_ID, scout.getEntityId());
        scoutUuid = scout.getUniqueID();
        setCustomNameTag("X-20 Scout Pilot");
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, 0);
        dataManager.register(ATTACK_TICK, 0);
        dataManager.register(SCOUT_ID, -1);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(1, new EntityAILeapAtTarget(this, 0.42F));
        tasks.addTask(2, new EntityAIAttackMelee(this, 1.28D, false));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 40.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(220.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(20.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(10.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(16.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(52.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.72D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.34D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) {
            clientTrail();
            return;
        }
        bossInfo.setPercent(getHealth() / getMaxHealth());
        EntityX20Scout linkedScout = scout();
        if (linkedScout != null && linkedScout.isEntityAlive()) {
            if (linkedScout.getAttackTarget() != null
                    && linkedScout.getAttackTarget().isEntityAlive())
                setAttackTarget(linkedScout.getAttackTarget());
            else if (getAttackTarget() != null && getAttackTarget().isEntityAlive())
                linkedScout.setAttackTarget(getAttackTarget());
        }
        if (cooldown > 0) cooldown--;
        EntityLivingBase target = getAttackTarget();
        int attack = dataManager.get(ATTACK);
        if (attack != 0) {
            tickAttack(target, attack);
            return;
        }
        if (target == null || !target.isEntityAlive() || cooldown > 0) return;
        double d = getDistanceSq(target);
        if (d < 49.0D) begin(alternate ? 2 : 1);
        else if (d < 1024.0D) begin(3);
        alternate = !alternate;
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        if (!world.isRemote
                && entity instanceof EntityLivingBase
                && dataManager.get(ATTACK) == 0
                && cooldown <= 0) {
            begin(alternate ? 2 : 1);
            alternate = !alternate;
            return true;
        }
        return false;
    }

    private void begin(int attack) {
        dataManager.set(ATTACK, attack);
        dataManager.set(ATTACK_TICK, 0);
        navigator.clearPath();
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE,
                SoundCategory.HOSTILE,
                .8F,
                attack >= 3 ? 1.7F : 1.2F);
    }

    public void prepareRelay(@Nullable EntityLivingBase target) {
        if (world.isRemote) return;
        if (target != null) setAttackTarget(target);
        begin(4);
        cooldown = 75;
    }

    private void tickAttack(@Nullable EntityLivingBase target, int attack) {
        int tick = dataManager.get(ATTACK_TICK) + 1;
        dataManager.set(ATTACK_TICK, tick);
        if (target != null) {
            getLookHelper().setLookPositionWithEntity(target, 45F, 45F);
            if (attack == 1 && tick == 13) slash(target, false);
            if (attack == 2 && tick == 19) slash(target, true);
            if (attack == 3 && tick == 28) bladeWave(target);
        }
        int duration = attack == 1 ? 24 : attack == 2 ? 32 : attack == 4 ? 62 : 42;
        if (tick >= duration) {
            dataManager.set(ATTACK, 0);
            dataManager.set(ATTACK_TICK, 0);
            cooldown = attack >= 3 ? 35 : 18;
        }
    }

    private void slash(EntityLivingBase target, boolean heavy) {
        if (getDistanceSq(target) > (heavy ? 64D : 42D)) return;
        float damage = heavy ? 26F : 18F;
        if (target.attackEntityFrom(DamageSource.causeMobDamage(this), damage)) {
            Vec3d push = target.getPositionVector().subtract(getPositionVector()).normalize();
            target.addVelocity(
                    push.x * (heavy ? 1.1 : .55), heavy ? .52 : .2, push.z * (heavy ? 1.1 : .55));
            target.addPotionEffect(
                    new PotionEffect(MobEffects.SLOWNESS, heavy ? 70 : 30, heavy ? 2 : 0));
        }
        sparkBurst(target.getPositionVector().addVector(0, target.height * .5, 0), heavy ? 28 : 14);
    }

    private void bladeWave(EntityLivingBase target) {
        Vec3d start = getPositionVector().addVector(0, 1.05, 0),
                dir =
                        target.getPositionVector()
                                .addVector(0, target.height * .5, 0)
                                .subtract(start)
                                .normalize();
        AxisAlignedBB box = getEntityBoundingBox().expand(dir.x * 18, 3, dir.z * 18).grow(2);
        for (EntityLivingBase e : world.getEntitiesWithinAABB(EntityLivingBase.class, box)) {
            if (e == this || e == scout() || !e.isEntityAlive()) continue;
            Vec3d delta = e.getPositionVector().subtract(start);
            double along = delta.dotProduct(dir);
            if (along >= 0 && along <= 20 && delta.subtract(dir.scale(along)).lengthVector() < 2.2)
                e.attackEntityFrom(DamageSource.causeMobDamage(this).setProjectile(), 22F);
        }
        if (world instanceof WorldServer) {
            for (int i = 1; i <= 32; i++) {
                Vec3d p = start.add(dir.scale(i * .62));
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.END_ROD, p.x, p.y, p.z, 1, .08, .08, .08, .01);
            }
        }
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.HOSTILE,
                1.25F,
                .72F);
    }

    private void sparkBurst(Vec3d p, int count) {
        if (world instanceof WorldServer) {
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.FIREWORKS_SPARK,
                            p.x,
                            p.y,
                            p.z,
                            count,
                            .45,
                            .45,
                            .45,
                            .12);
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.END_ROD, p.x, p.y, p.z, count / 3, .3, .3, .3, .06);
        }
    }

    private void clientTrail() {
        int attack = dataManager.get(ATTACK), tick = dataManager.get(ATTACK_TICK);
        if (attack != 0 && tick > 8) {
            double a = Math.toRadians(-rotationYaw);
            for (int i = 0; i < 2; i++)
                world.spawnParticle(
                        EnumParticleTypes.REDSTONE,
                        posX - Math.sin(a) * .7 + (rand.nextDouble() - .5) * .12,
                        posY + 1.05 + (rand.nextDouble() - .5) * .15,
                        posZ + Math.cos(a) * .7 + (rand.nextDouble() - .5) * .12,
                        .1,
                        .9,
                        1.0);
        }
    }

    public Vec3d getBladePosition() {
        double a = Math.toRadians(-rotationYaw);
        return getPositionVector().addVector(-Math.sin(a) * 1.15D, 1.08D, Math.cos(a) * 1.15D);
    }

    @Nullable
    public EntityX20Scout scout() {
        Entity e = world.getEntityByID(dataManager.get(SCOUT_ID));
        if (e instanceof EntityX20Scout) return (EntityX20Scout) e;
        if (!world.isRemote && scoutUuid != null && world instanceof WorldServer) {
            e = ((WorldServer) world).getEntityFromUuid(scoutUuid);
            if (e instanceof EntityX20Scout) {
                dataManager.set(SCOUT_ID, e.getEntityId());
                return (EntityX20Scout) e;
            }
        }
        return null;
    }

    public int getAttack() {
        return dataManager.get(ATTACK);
    }

    public int getAttackTick() {
        return dataManager.get(ATTACK_TICK);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        entityDropItem(new ItemStack(ModContent.KX_GEAR, 1 + rand.nextInt(2)), 0);
        if (rand.nextInt(3) == 0) entityDropItem(new ItemStack(ModContent.KX20_BLADE), 0);
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        bossInfo.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bossInfo.removePlayer(p);
    }

    @Override
    public void setCustomNameTag(String n) {
        super.setCustomNameTag(n);
        bossInfo.setName(getDisplayName());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (scoutUuid != null) tag.setUniqueId("Scout", scoutUuid);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasUniqueId("Scout")) scoutUuid = tag.getUniqueId("Scout");
    }

    private <E extends IAnimatable> PlayState animation(AnimationEvent<E> event) {
        String name =
                dataManager.get(ATTACK) == 1
                        ? "slash"
                        : dataManager.get(ATTACK) == 2
                                ? "heavy_slash"
                                : dataManager.get(ATTACK) == 3
                                        ? "blade_wave"
                                        : dataManager.get(ATTACK) == 4
                                                ? "sync_relay"
                                                : Math.abs(motionX) + Math.abs(motionZ) > .04
                                                        ? "run"
                                                        : "idle";
        event.getController()
                .setAnimation(
                        new AnimationBuilder()
                                .addAnimation(
                                        "animation.x20_pilot." + name,
                                        dataManager.get(ATTACK) == 0));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData d) {
        d.addAnimationController(
                new AnimationController<EntityX20Pilot>(this, "x20_pilot", 2, this::animation));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
