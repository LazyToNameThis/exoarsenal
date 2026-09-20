package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.entity.slime.KingSlimeAttackRules;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.UUID;

public class EntityKingSlimeSupport extends EntityMob {
    private static final DataParameter<Integer>
            KIND =
                    EntityDataManager.createKey(
                            EntityKingSlimeSupport.class, DataSerializers.VARINT),
            POSE =
                    EntityDataManager.createKey(
                            EntityKingSlimeSupport.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SPIKED =
            EntityDataManager.createKey(EntityKingSlimeSupport.class, DataSerializers.BOOLEAN);
    private UUID owner;
    private int timer, jumpTimer, chargeTicks;
    private Vec3d charge = Vec3d.ZERO;

    public EntityKingSlimeSupport(World w) {
        super(w);
        setNoAI(true);
        setSize(.8F, 1);
        experienceValue = 0;
    }

    public EntityKingSlimeSupport(EntityKingSlime boss, int kind) {
        this(boss.world);
        owner = boss.getUniqueID();
        dataManager.set(KIND, kind);
        dataManager.set(SPIKED, rand.nextBoolean());
        setHealth(getMaxHealth());
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(KIND, 0);
        dataManager.register(POSE, 0);
        dataManager.register(SPIKED, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(30);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    public int kind() {
        return dataManager.get(KIND);
    }

    public int pose() {
        return dataManager.get(POSE);
    }

    public boolean spiked() {
        return dataManager.get(SPIKED);
    }

    public boolean belongs(EntityKingSlime boss) {
        return boss.getUniqueID().equals(owner);
    }

    public EntityKingSlime boss() {
        Entity e =
                owner == null || world.isRemote
                        ? null
                        : ((WorldServer) world).getEntityFromUuid(owner);
        return e instanceof EntityKingSlime && !e.isDead ? (EntityKingSlime) e : null;
    }

    @Override
    public void onLivingUpdate() {
        setNoGravity(kind() == 0);
        noClip = kind() == 0;
        setSize(kind() == 1 ? .6F : .85F, kind() == 1 ? 1.8F : .9F);
        super.onLivingUpdate();
        if (world.isRemote) return;
        EntityKingSlime b = boss();
        if (b == null || !b.isEntityAlive()) {
            setDead();
            return;
        }
        timer++;
        EntityPlayer p = world.getNearestAttackablePlayer(this, 112, 64);
        if (p == null) return;
        rotationYaw = (float) Math.toDegrees(Math.atan2(posZ - p.posZ, posX - p.posX)) + 90;
        renderYawOffset = rotationYaw;
        if (b.state() == KingSlimeAttackRules.DEATH) {
            if (kind() != 1) {
                setDead();
                return;
            }
            int t = b.clock();
            Vec3d at = b.getPositionVector();
            if (t == 22) setPositionAndUpdate(at.x - 9, at.y + 1.3, at.z);
            if (t >= 23 && t < 38) {
                motionX = 1.2;
                motionY = 0;
                motionZ = 0;
                noClip = true;
                setNoGravity(true);
                dataManager.set(POSE, 3);
            } else {
                motionX *= .8;
                motionZ *= .8;
            }
            return;
        }
        if (kind() == 0) {
            jewel(b, p);
            return;
        }
        Vec3d d = new Vec3d(p.posX - posX, 0, p.posZ - posZ).normalize();
        if (kind() == 1) {
            if (getDistanceSq(p) > 900 && timer % 50 == 0) {
                Vec3d dest = b.safeGround(p.getPositionVector().add(d.scale(-12)), .6, 1.8);
                if (dest != null) {
                    setPositionAndUpdate(dest.x, dest.y, dest.z);
                    b.burst(10);
                }
            }
            if (onGround) {
                motionX = motionX * .7 + d.x * .17;
                motionZ = motionZ * .7 + d.z * .17;
                dataManager.set(POSE, 0);
                if (++jumpTimer >= 20 && getDistanceSq(p) > 36) {
                    jumpTimer = 0;
                    motionY = .75;
                    motionX = d.x * .65;
                    motionZ = d.z * .65;
                    chargeTicks = 8;
                    dataManager.set(POSE, 1);
                }
            }
            if (!onGround) {
                dataManager.set(POSE, motionY > 0 ? 1 : 2);
                if (chargeTicks > 0 && --chargeTicks == 0) {
                    int count = MathHelper.clamp((int) (getDistance(p) / 4), 2, 6);
                    for (int i = 0; i < count; i++) {
                        Vec3d aim =
                                p.getPositionVector()
                                        .addVector(0, 1, 0)
                                        .subtract(getPositionVector().addVector(0, 1, 0))
                                        .normalize();
                        double angle = (i / (double) (count - 1) - .5) * .72;
                        shot(aim.rotateYaw((float) angle).scale(.69), 1, false);
                    }
                }
            }

        } else {
            if (onGround && ++jumpTimer >= 18) {
                jumpTimer = 0;
                motionX = d.x * .38;
                motionZ = d.z * .38;
                motionY = .5;
            }
            if (spiked() && timer % 55 == 0 && getDistanceSq(p) < 225)
                for (int i = -1; i <= 1; i++)
                    shot(
                            p.getPositionVector()
                                    .addVector(0, 1, 0)
                                    .subtract(getPositionVector())
                                    .normalize()
                                    .rotateYaw(i * .2F)
                                    .scale(.5),
                            2,
                            true);
            if (getEntityBoundingBox().intersects(p.getEntityBoundingBox()))
                p.attackEntityFrom(DamageSource.causeMobDamage(this), 3);
        }
        velocityChanged = true;
    }

    private void jewel(EntityKingSlime b, EntityPlayer p) {
        int mode = b.expert() ? 0 : KingSlimeAttackRules.jewelMode(b.fraction(), timer);
        dataManager.set(POSE, mode);
        if (chargeTicks > 0) {
            motionX = charge.x;
            motionY = charge.y;
            motionZ = charge.z;
            chargeTicks--;
            if (getEntityBoundingBox().grow(.2).intersects(p.getEntityBoundingBox()))
                p.attackEntityFrom(DamageSource.causeMobDamage(this), 7);
        } else {
            Vec3d desired =
                    p.getPositionVector()
                            .addVector(
                                    Math.sin(timer * .025) * 2,
                                    10 + Math.sin(timer * .05) * .4,
                                    Math.cos(timer * .025) * 2);
            Vec3d v = desired.subtract(getPositionVector()).scale(.06);
            motionX = v.x;
            motionY = v.y;
            motionZ = v.z;
            boolean clear = getEntitySenses().canSee(p);
            int rate = b.expert() ? (clear ? 25 : 10) : 40;
            if (timer % rate == 0) {
                Vec3d aim =
                        p.getPositionVector()
                                .addVector(
                                        p.motionX * (clear ? 13 : 7),
                                        1 + p.motionY * 7,
                                        p.motionZ * (clear ? 13 : 7))
                                .subtract(getPositionVector())
                                .normalize();
                if (mode == 1) {
                    charge = aim.scale(1.2);
                    chargeTicks = 14;
                } else shot(aim.scale(b.expert() ? 1.03 : .85), 0, clear);
            }
        }
        velocityChanged = true;
    }

    private void shot(Vec3d velocity, int kind, boolean collide) {
        world.spawnEntity(
                new EntityKingSlimeShot(
                        this,
                        getPositionVector().addVector(0, this.kind() == 1 ? 1 : 0, 0),
                        velocity,
                        kind,
                        collide));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        EntityKingSlime b = boss();
        if (source == DamageSource.FALL
                || source == DamageSource.IN_WALL
                || kind() == 1
                || kind() == 0 && b != null && !b.expert()
                || source.getTrueSource() instanceof EntityKingSlimeSupport
                || source.getTrueSource() instanceof EntityKingSlime) return false;
        return super.attackEntityFrom(source, amount);
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {}

    @Override
    public void fall(float d, float m) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        if (owner != null) n.setUniqueId("KingOwner", owner);
        n.setInteger("KingKind", kind());
        n.setBoolean("KingSpiked", spiked());
        n.setInteger("KingTimer", timer);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        owner = n.hasUniqueId("KingOwner") ? n.getUniqueId("KingOwner") : null;
        dataManager.set(KIND, n.getInteger("KingKind"));
        dataManager.set(SPIKED, n.getBoolean("KingSpiked"));
        timer = n.getInteger("KingTimer");
    }
}
