package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;

public abstract class EntitySeaCreature extends EntityWaterMob {
    public enum Kind {
        CLAM,
        RAY,
        BELL,
        PRISM,
        FLOATY,
        MINNOW,
        BABY_BELL,
        GIANT
    }

    private static final DataParameter<Boolean> ANGRY =
            EntityDataManager.createKey(EntitySeaCreature.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> ACTION =
            EntityDataManager.createKey(EntitySeaCreature.class, DataSerializers.VARINT);
    private final Kind kind;
    private int wakeHits, cycle;
    private boolean slam, summoning;
    private Vec3d wander;

    protected EntitySeaCreature(World world, Kind kind) {
        super(world);
        this.kind = kind;
        boolean giant = kind == Kind.GIANT;
        setSize(
                giant ? 3.6F : kind == Kind.RAY ? 1.5F : .75F,
                giant ? 1.7F : kind == Kind.RAY ? .45F : .7F);
        setNoGravity(true);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(
                        giant
                                ? 150
                                : kind == Kind.MINNOW || kind == Kind.BABY_BELL
                                        ? 4
                                        : kind == Kind.CLAM ? 24 : 18);
        setHealth(getMaxHealth());
        experienceValue = giant ? 35 : 2;
    }

    public Kind kind() {
        return kind;
    }

    public boolean angry() {
        return dataManager.get(ANGRY);
    }

    public int action() {
        return dataManager.get(ACTION);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ANGRY, false);
        dataManager.register(ACTION, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.2);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(2);
    }

    public void provoke(EntityLivingBase attacker) {
        setRevengeTarget(attacker);
        dataManager.set(ANGRY, true);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        if (isEntityInvulnerable(source)) return false;
        if (!world.isRemote && source.getTrueSource() instanceof EntityLivingBase) {
            if (kind == Kind.GIANT && !angry()) {
                if (!SeaCombatRules.clamAwakens(++wakeHits)) {
                    playSound(net.minecraft.init.SoundEvents.BLOCK_STONE_HIT, .8F, .7F);
                    return false;
                }
                provoke((EntityLivingBase) source.getTrueSource());
                cycle = 0;
                return true;
            }
            if (kind != Kind.MINNOW && kind != Kind.BABY_BELL)
                provoke((EntityLivingBase) source.getTrueSource());
        }
        return super.attackEntityFrom(source, damage);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        EntityLivingBase target = getRevengeTarget();
        if (target != null
                && (!target.isEntityAlive()
                        || getDistanceSq(target) > 64 * 64
                        || target instanceof EntityPlayer
                                && ((EntityPlayer) target).capabilities.disableDamage))
            target = null;
        if (angry() && target == null) {
            dataManager.set(ANGRY, false);
            setRevengeTarget(null);
            cycle = 0;
            slam = false;
        }
        if (kind == Kind.GIANT) {
            giant(target);
            return;
        }
        if (kind == Kind.CLAM) {
            motionX *= .85;
            motionZ *= .85;
            motionY -= .035;
            if (target != null && ticksExisted % 45 == 0) {
                Vec3d direction =
                        target.getPositionVector().subtract(getPositionVector()).normalize();
                motionX = direction.x * .35;
                motionZ = direction.z * .35;
                motionY = .3;
                dataManager.set(ACTION, 15);
            }
            if (action() > 0) dataManager.set(ACTION, action() - 1);
            contact(target, 4);
            return;
        }
        if (!isInWater()) {
            motionY -= .04;
            return;
        }
        Vec3d goal;
        if (target != null) goal = target.getPositionEyes(1);
        else {
            if (wander == null
                    || ticksExisted % 70 == 0
                    || getPositionVector().squareDistanceTo(wander) < 1) {
                BlockPos candidate =
                        getPosition()
                                .add(
                                        rand.nextInt(11) - 5,
                                        rand.nextInt(5) - 2,
                                        rand.nextInt(11) - 5);
                if (world.getBlockState(candidate).getMaterial()
                        == net.minecraft.block.material.Material.WATER)
                    wander = new Vec3d(candidate).addVector(.5, .3, .5);
            }
            goal = wander == null ? getPositionVector() : wander;
        }
        Vec3d direction = goal.subtract(getPositionVector());
        double speed = target == null ? .045 : kind == Kind.RAY ? .22 : .12;
        if (direction.lengthSquared() > .2) {
            direction = direction.normalize().scale(speed);
            motionX += (direction.x - motionX) * .15;
            motionY += (direction.y - motionY) * .15;
            motionZ += (direction.z - motionZ) * .15;
            rotationYaw =
                    renderYawOffset = (float) -Math.toDegrees(Math.atan2(direction.x, direction.z));
        }
        contact(target, kind == Kind.BELL ? 3 : 4);
    }

    private void contact(EntityLivingBase target, float damage) {
        if (target != null
                && ticksExisted % 20 == 0
                && getEntityBoundingBox().grow(.2).intersects(target.getEntityBoundingBox()))
            target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
    }

    private void giant(EntityLivingBase target) {
        motionX *= .7;
        motionZ *= .7;
        motionY -= .045;
        if (target == null) {
            dataManager.set(ACTION, 0);
            return;
        }
        cycle++;
        dataManager.set(ACTION, cycle);
        if (cycle == 1) summoning = rand.nextInt(4) == 0;
        if (cycle == 25) {
            if (summoning) {
                for (int i = 0; i < 3; i++) {
                    Clam clam = new Clam(world);
                    clam.setPosition(posX + (i - 1) * 1.5, posY + .6, posZ + 1);
                    if (world.getCollisionBoxes(clam, clam.getEntityBoundingBox()).isEmpty()) {
                        clam.provoke(target);
                        world.spawnEntity(clam);
                    }
                }
            } else {
                for (int height = 7; height >= 3; height--) {
                    AxisAlignedBB box =
                            getEntityBoundingBox()
                                    .offset(
                                            target.posX - posX,
                                            target.posY + height - posY,
                                            target.posZ - posZ);
                    if (world.getCollisionBoxes(this, box).isEmpty()) {
                        setPositionAndUpdate(target.posX, target.posY + height, target.posZ);
                        motionY = 0;
                        break;
                    }
                }
            }
        }
        if (SeaCombatRules.clamSuspended(cycle, summoning)) motionY = 0;
        if (SeaCombatRules.clamDrops(cycle, summoning)) {
            motionY = -1.1;
            slam = true;
        }
        if (slam
                && (onGround
                        || collidedVertically
                        || getEntityBoundingBox()
                                .grow(.15)
                                .intersects(target.getEntityBoundingBox()))) {
            slam = false;
            for (EntityPlayer player :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, getEntityBoundingBox().grow(3, 1, 3)))
                if (!player.capabilities.disableDamage) {
                    player.attackEntityFrom(DamageSource.causeMobDamage(this), 10);
                    player.addVelocity((player.posX - posX) * .12, .3, (player.posZ - posZ) * .12);
                }
            playSound(net.minecraft.init.SoundEvents.ENTITY_IRONGOLEM_ATTACK, 1, .5F);
            if (world instanceof WorldServer)
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.WATER_BUBBLE,
                                posX,
                                posY + .2,
                                posZ,
                                45,
                                2,
                                .4,
                                2,
                                .1);
        }
        if (SeaCombatRules.clamRecovered(cycle)) {
            cycle = 0;
            slam = false;
        }
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        move(MoverType.SELF, motionX, motionY, motionZ);
        motionX *= .94;
        motionY *= .94;
        motionZ *= .94;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (world.getBlockState(getPosition()).getMaterial()
                        != net.minecraft.block.material.Material.WATER
                || !ExpeditionWorldGenerator.inSea(world, getPosition())
                || !world.checkNoEntityCollision(getEntityBoundingBox(), this)
                || !world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()) return false;
        return kind != Kind.GIANT
                || ScourgeLoot.Progress.get(world).killed()
                        && rand.nextInt(25) == 0
                        && world.getEntitiesWithinAABB(
                                        GiantClam.class, getEntityBoundingBox().grow(128))
                                .isEmpty();
    }

    @Override
    public int getMaxSpawnedInChunk() {
        return kind == Kind.GIANT ? 1 : 4;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (kind == Kind.CLAM || kind == Kind.GIANT) {
            if (rand.nextBoolean()) dropItem(SeaContent.WHITE_PEARL, 1);
            if (rand.nextInt(4) == 0) dropItem(SeaContent.BLACK_PEARL, 1);
            if (rand.nextInt(10) == 0) dropItem(SeaContent.PINK_PEARL, 1);
        }
        if (kind == Kind.PRISM && ScourgeLoot.Progress.get(world).killed())
            dropItem(ExpeditionContent.PRISM_SHARD, 1 + rand.nextInt(3));
        if (kind == Kind.GIANT) {
            entityDropItem(new ItemStack(ExpeditionContent.NAVYSTONE, 30 + rand.nextInt(11)), 0);
            if (rand.nextInt(3) == 0) dropItem(SeaContent.PENDANT, 1);
            if (rand.nextInt(3) == 0) dropItem(SeaContent.GIANT_PEARL, 1);
            if (ScourgeLoot.Progress.get(world).killed()
                    && ScourgeLoot.Progress.get(world).rescueKing()) {
                EntitySeaKing king = new EntitySeaKing(world);
                king.setPosition(posX, posY + .3, posZ);
                if (!world.spawnEntity(king)) ScourgeLoot.Progress.get(world).loseKing();
            }
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setInteger("WakeHits", wakeHits);
        tag.setBoolean("Provoked", angry());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        wakeHits = MathHelper.clamp(tag.getInteger("WakeHits"), 0, 5);
        dataManager.set(ANGRY, tag.getBoolean("Provoked"));
    }

    public static final class Clam extends EntitySeaCreature {
        public Clam(World w) {
            super(w, Kind.CLAM);
        }
    }

    public static final class Ray extends EntitySeaCreature {
        public Ray(World w) {
            super(w, Kind.RAY);
        }
    }

    public static final class GhostBell extends EntitySeaCreature {
        public GhostBell(World w) {
            super(w, Kind.BELL);
        }
    }

    public static final class PrismBack extends EntitySeaCreature {
        public PrismBack(World w) {
            super(w, Kind.PRISM);
        }
    }

    public static final class SeaFloaty extends EntitySeaCreature {
        public SeaFloaty(World w) {
            super(w, Kind.FLOATY);
        }
    }

    public static final class SeaMinnow extends EntitySeaCreature {
        public SeaMinnow(World w) {
            super(w, Kind.MINNOW);
        }
    }

    public static final class BabyGhostBell extends EntitySeaCreature {
        public BabyGhostBell(World w) {
            super(w, Kind.BABY_BELL);
        }
    }

    public static final class GiantClam extends EntitySeaCreature {
        public GiantClam(World w) {
            super(w, Kind.GIANT);
        }
    }
}
