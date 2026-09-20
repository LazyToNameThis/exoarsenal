package com.exoarsenal.entity;

import java.util.UUID;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public final class EntityPersonalMachine extends EntityLiving {
    private static final DataParameter<Boolean> MOUNT =
            EntityDataManager.createKey(EntityPersonalMachine.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ACTION =
            EntityDataManager.createKey(EntityPersonalMachine.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TARGET =
            EntityDataManager.createKey(EntityPersonalMachine.class, DataSerializers.VARINT);
    private UUID owner;
    private int cooldown, sequence, attackAge, lastHit, lastHurt;
    private Vec3d locked = Vec3d.ZERO;

    public EntityPersonalMachine(World world) {
        super(world);
        setNoAI(true);
        isImmuneToFire = true;
        setSize(.8F, .8F);
    }

    public EntityPersonalMachine(World world, EntityPlayer player, boolean mount) {
        this(world);
        owner = player.getUniqueID();
        dataManager.set(MOUNT, mount);
        setSize(mount ? 2.4F : .8F, mount ? 1.7F : .8F);
        setPosition(player.posX, player.posY + (mount ? 0 : 1.6), player.posZ);
        lastHit = player.getLastAttackedEntityTime();
        lastHurt = player.getRevengeTimer();
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(MOUNT, false);
        dataManager.register(ACTION, 0);
        dataManager.register(TARGET, -1);
    }

    public Entity beamTarget() {
        return world.getEntityByID(dataManager.get(TARGET));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(40);
    }

    public boolean mount() {
        return dataManager.get(MOUNT);
    }

    public int action() {
        return dataManager.get(ACTION);
    }

    public boolean ownedBy(EntityPlayer player) {
        return player.getUniqueID().equals(owner);
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (!ownedBy(player)) return false;
        if (!world.isRemote && mount() && !isBeingRidden()) player.startRiding(this);
        return true;
    }

    @Override
    public double getMountedYOffset() {
        return 1.6;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void fall(float distance, float multiplier) {}

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source.getTrueSource() instanceof EntityPlayer
                && ownedBy((EntityPlayer) source.getTrueSource())) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {}

    private boolean enemy(EntityLivingBase entity, EntityPlayer player) {
        return entity != null
                && entity != player
                && entity != this
                && entity.isEntityAlive()
                && !(entity instanceof EntityPlayer)
                && !(entity instanceof EntityPersonalMachine)
                && !(entity instanceof net.minecraft.entity.passive.EntityTameable
                        && ((net.minecraft.entity.passive.EntityTameable) entity).isTamed());
    }

    public void trigger(EntityPlayer player) {
        if (!ownedBy(player) || !mount() || cooldown > 0) return;
        locked = player.getLookVec();
        dataManager.set(ACTION, 1 + sequence++ % 3);
        attackAge = 0;
        cooldown = 60;
    }

    private void particles(Vec3d at, int count) {
        ((WorldServer) world)
                .spawnParticle(EnumParticleTypes.END_ROD, at.x, at.y, at.z, count, .3, .3, .3, .04);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        setSize(mount() ? 2.4F : .8F, mount() ? 1.7F : .8F);
        setNoGravity(!mount());
        if (world.isRemote) return;
        EntityPlayer player = owner == null ? null : world.getPlayerEntityByUUID(owner);
        if (player == null || !player.isEntityAlive()) {
            dataManager.set(ACTION, 0);
            return;
        }
        if (cooldown > 0) cooldown--;
        if (mount()) {
            Entity rider = getControllingPassenger();
            double speed = action() == 1 ? (attackAge >= 12 && attackAge < 26 ? 1.05 : 0) : .28;
            Vec3d heading = rider == player ? player.getLookVec() : Vec3d.ZERO;
            if (action() == 1) heading = locked;
            heading = new Vec3d(heading.x, 0, heading.z).normalize();
            double input = action() == 1 ? 1 : rider == player ? player.moveForward : 0;
            motionY = onGround ? 0 : Math.max(-.8, motionY - .08);
            move(MoverType.SELF, heading.x * speed * input, motionY, heading.z * speed * input);
            if (heading.lengthSquared() > .01)
                rotationYaw = (float) Math.toDegrees(Math.atan2(-heading.x, heading.z));
            if (action() > 0) {
                int t = ++attackAge;
                if (t < 12 && t % 3 == 0) particles(getPositionVector().addVector(0, 1.8, 0), 3);
                if (action() == 1 && t >= 12 && t < 26 && t % 7 == 0)
                    hitArea(player, getEntityBoundingBox().grow(1), 7);
                if (action() == 2 && t == 16) {
                    Vec3d from = getPositionVector().addVector(0, 1.8, 0),
                            to = from.add(locked.scale(24));
                    RayTraceResult block = world.rayTraceBlocks(from, to);
                    if (block != null) to = block.hitVec;
                    for (EntityLivingBase e :
                            world.getEntitiesWithinAABB(
                                    EntityLivingBase.class, new AxisAlignedBB(from, to).grow(1)))
                        if (enemy(e, player)
                                && e.getEntityBoundingBox().grow(.3).calculateIntercept(from, to)
                                        != null)
                            e.attackEntityFrom(DamageSource.causePlayerDamage(player), 10);
                    for (int i = 0; i < 24; i++)
                        particles(from.add(to.subtract(from).scale(i / 24D)), 1);
                }
                if (action() == 3 && t == 20) {
                    hitArea(player, getEntityBoundingBox().grow(5, 2, 5), 8);
                    for (int i = 0; i < 40; i++) {
                        double a = i * Math.PI / 20;
                        particles(
                                getPositionVector()
                                        .addVector(Math.cos(a) * 5, .25, Math.sin(a) * 5),
                                1);
                    }
                    world.playSound(
                            null,
                            getPosition(),
                            SoundEvents.ENTITY_IRONGOLEM_ATTACK,
                            SoundCategory.PLAYERS,
                            1,
                            .7F);
                }
                if (t >= 32) dataManager.set(ACTION, 0);
            }
        } else {
            if (player.getLastAttackedEntityTime() != lastHit) {
                lastHit = player.getLastAttackedEntityTime();
                if (enemy(player.getLastAttackedEntity(), player))
                    setAttackTarget(player.getLastAttackedEntity());
            }
            if (player.getRevengeTimer() != lastHurt) {
                lastHurt = player.getRevengeTimer();
                if (enemy(player.getRevengeTarget(), player))
                    setAttackTarget(player.getRevengeTarget());
            }
            EntityLivingBase target = getAttackTarget();
            if (!enemy(target, player) || target.getDistanceSq(player) > 32 * 32) {
                setAttackTarget(null);
                target = null;
            }
            dataManager.set(TARGET, target == null ? -1 : target.getEntityId());
            Vec3d goal =
                    target == null
                            ? player.getPositionVector()
                                    .addVector(
                                            Math.cos(getEntityId()) * 2,
                                            2.2,
                                            Math.sin(getEntityId()) * 2)
                            : target.getPositionVector().addVector(0, 2.5, 0);
            Vec3d delta = goal.subtract(getPositionVector());
            if (delta.lengthVector() > .35) delta = delta.normalize().scale(.35);
            move(MoverType.SELF, delta.x, delta.y, delta.z);
            rotationYaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
            if (getDistanceSq(player) > 48 * 48
                    && world.getCollisionBoxes(
                                    this,
                                    getEntityBoundingBox()
                                            .offset(
                                                    player.posX - posX,
                                                    player.posY + 2 - posY,
                                                    player.posZ - posZ))
                            .isEmpty()) setPosition(player.posX, player.posY + 2, player.posZ);
            if (target != null
                    && canEntityBeSeen(target)
                    && getDistanceSq(target) < 144
                    && cooldown == 0) {
                dataManager.set(ACTION, 2);
                if (++attackAge >= 16) {
                    target.attackEntityFrom(DamageSource.causeIndirectDamage(this, player), 4);
                    particles(target.getPositionEyes(1), 8);
                    cooldown = 30;
                    attackAge = 0;
                    dataManager.set(ACTION, 0);
                }
            } else {
                attackAge = 0;
                dataManager.set(ACTION, cooldown >= 24 && target != null ? 3 : 0);
            }
        }
        velocityChanged = true;
    }

    private void hitArea(EntityPlayer player, AxisAlignedBB area, float damage) {
        for (EntityLivingBase e : world.getEntitiesWithinAABB(EntityLivingBase.class, area))
            if (enemy(e, player) && canEntityBeSeen(e))
                e.attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
    }

    @Override
    public Entity getControllingPassenger() {
        return getPassengers().isEmpty() ? null : getPassengers().get(0);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        if (owner != null) n.setUniqueId("MachineOwner", owner);
        n.setBoolean("PersonalMount", mount());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        owner = n.hasUniqueId("MachineOwner") ? n.getUniqueId("MachineOwner") : null;
        dataManager.set(MOUNT, n.getBoolean("PersonalMount"));
    }
}
