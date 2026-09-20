package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;
import java.util.UUID;

public final class EntityScoutShard extends EntityLiving implements IAnimatable {
    private static final DataParameter<Integer> KIND =
            EntityDataManager.createKey(EntityScoutShard.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> AGE =
            EntityDataManager.createKey(EntityScoutShard.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> RETURNING =
            EntityDataManager.createKey(EntityScoutShard.class, DataSerializers.BOOLEAN);
    private UUID owner;
    private int slot;
    private int launchDelay = 20;
    private Vec3d velocity = Vec3d.ZERO, locked = Vec3d.ZERO, origin = Vec3d.ZERO;
    private final java.util.Set<UUID> struck = new java.util.HashSet<>();
    private final AnimationFactory factory = new AnimationFactory(this);

    public EntityScoutShard(World world) {
        super(world);
        setSize(.5F, .5F);
        setNoAI(true);
        setNoGravity(true);
        noClip = true;
    }

    public EntityScoutShard(World world, EntityX20Scout boss, Vec3d start, int kind, int slot) {
        this(world);
        owner = boss.getUniqueID();
        this.slot = slot;
        dataManager.set(KIND, kind);
        setPosition(start.x, start.y, start.z);
        if (kind == 10) dataManager.set(AGE, -slot * 7);
        double a = slot * 2.399963;
        velocity = new Vec3d(Math.cos(a) * .12, .08, Math.sin(a) * .12);
    }

    public static EntityScoutShard wedge(World world, EntityX20Scout boss, Vec3d start, int delay) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 4, 0);
        shard.launchDelay = delay;
        shard.velocity = Vec3d.ZERO;
        return shard;
    }

    public static EntityScoutShard overheatGlacier(
            World world, EntityX20Scout boss, Vec3d start, Vec3d landing, int slot) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 7, slot);
        shard.locked = landing;
        shard.velocity = new Vec3d((landing.x - start.x) / 40, .9, (landing.z - start.z) / 40);
        return shard;
    }

    public static EntityScoutShard scissors(
            World world, EntityX20Scout boss, Vec3d start, int slot) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 5, slot);
        shard.velocity = Vec3d.ZERO;
        return shard;
    }

    public static EntityScoutShard katanaPlatform(
            World world, EntityX20Scout boss, Vec3d start, int slot) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 8, slot);
        shard.origin = start;
        shard.velocity = Vec3d.ZERO;
        return shard;
    }

    public static EntityScoutShard surfboard(World world, EntityX20Scout boss, Vec3d start) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 9, 0);
        shard.origin = start;
        shard.velocity = Vec3d.ZERO;
        return shard;
    }

    public static EntityScoutShard ridingWedge(World world, EntityX20Scout boss, Vec3d start) {
        EntityScoutShard shard = new EntityScoutShard(world, boss, start, 6, 0);
        shard.velocity = Vec3d.ZERO;
        return shard;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(KIND, 0);
        dataManager.register(AGE, 0);
        dataManager.register(RETURNING, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(1);
    }

    public int kind() {
        return dataManager.get(KIND);
    }

    public int age() {
        return dataManager.get(AGE);
    }

    public boolean returning() {
        return dataManager.get(RETURNING);
    }

    @Override
    public void onLivingUpdate() {
        if (world.isRemote) {

            super.onLivingUpdate();
            return;
        }
        int age = age() + 1;
        dataManager.set(AGE, age);
        Entity entity = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
        if (!(entity instanceof EntityX20Scout) || !entity.isEntityAlive() || age > 280) {
            setDead();
            return;
        }
        EntityX20Scout boss = (EntityX20Scout) entity;
        EntityLivingBase target = boss.getAttackTarget();
        if (kind() == 10) {
            int active = age;
            if (active >= 0 && active < 24) {
                double radius = active * .42;
                for (EntityLivingBase victim :
                        world.getEntitiesWithinAABB(
                                EntityLivingBase.class, getEntityBoundingBox().grow(11, 3, 11))) {
                    if (victim == boss
                            || victim instanceof EntityScoutHardpoint
                            || victim instanceof EntityScoutShard
                            || victim instanceof EntityFrigidRobot
                            || struck.contains(victim.getUniqueID())) continue;
                    double dx = victim.posX - posX,
                            dz = victim.posZ - posZ,
                            distance = Math.sqrt(dx * dx + dz * dz);
                    if (Math.abs(distance - radius) < .8
                            && Math.abs(victim.posY - posY) < 2
                            && victim.attackEntityFrom(DamageSource.causeMobDamage(boss), 6))
                        struck.add(victim.getUniqueID());
                }
            }
            if (active > 26) setDead();
            return;
        }
        if (kind() == 8 || kind() == 9) {
            if (boss.getAttack() != ScoutCombatPattern.KATANA_CUTS || boss.isOverheating()) {
                setDead();
                return;
            }
            int tick = boss.getAttackTick();
            if (kind() == 9) {
                if (tick >= ScoutKatanaPlatforms.DISMOUNT) {
                    setDead();
                    return;
                }
                double u = Math.max(0, tick - ScoutKatanaPlatforms.BOARD), a = u * .022;
                Vec3d p = origin.addVector(Math.sin(a) * 2.5, u * .095, (1 - Math.cos(a)) * 2.5);
                setPosition(p.x, p.y, p.z);
                boss.setPosition(p.x, p.y + 1.05, p.z);
                boss.motionX = boss.motionY = boss.motionZ = 0;
                boss.setNoGravity(true);
                boss.fallDistance = 0;
                return;
            }
            int strike = ScoutKatanaPlatforms.strike(slot);
            if (tick < strike - 10) {
                Vec3d p = origin.addVector(0, (6 + slot * .8) * ScoutKatanaPlatforms.rise(age), 0);
                setPosition(p.x, p.y, p.z);
                return;
            }
            if (target == null || !target.isEntityAlive()) {
                setDead();
                return;
            }
            if (tick < strike) {
                locked = target.getPositionVector().addVector(0, .6, 0);
                Vec3d p =
                        getPositionVector()
                                .add(boss.getHammerHand().subtract(getPositionVector()).scale(.3));
                setPosition(p.x, p.y, p.z);
                return;
            }
            if (tick == strike) {
                velocity = locked.subtract(getPositionVector()).normalize().scale(1.65);
                com.exoarsenal.network.PacketScoutImpact.send(this, getPositionVector(), 0);
            }
            Vec3d from = getPositionVector(), to = from.add(velocity);
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            boolean hit =
                    target.getEntityBoundingBox().grow(1.1).contains(from)
                            || target.getEntityBoundingBox().grow(1.1).calculateIntercept(from, to)
                                    != null;
            if (wall != null || hit || to.distanceTo(locked) < 1.7 || tick >= strike + 42) {
                Vec3d point = wall == null ? to : wall.hitVec;
                setPosition(point.x, point.y, point.z);
                damage(boss, from, point, 12);
                Vec3d direction = new Vec3d(velocity.x, 0, velocity.z).normalize();
                for (int i = 0; i < 3; i++) {
                    Vec3d p = point.add(direction.scale(i * 3));
                    RayTraceResult floor =
                            world.rayTraceBlocks(
                                    p.addVector(0, 3, 0),
                                    p.addVector(0, -24, 0),
                                    false,
                                    true,
                                    false);
                    EntityScoutArena field = boss.arena();
                    if (field != null && field.raised())
                        p = new Vec3d(p.x, field.topAt(p.x, p.z) + .05, p.z);
                    else if (floor != null) p = floor.hitVec.addVector(0, .05, 0);
                    world.spawnEntity(new EntityScoutShard(world, boss, p, 10, i));
                }
                com.exoarsenal.network.PacketScoutImpact.send(this, point, 0);
                setDead();
            } else {
                setPosition(to.x, to.y, to.z);
                motionX = motionY = motionZ = 0;
            }
            return;
        }
        if (kind() == 7) {
            Vec3d from = getPositionVector();
            velocity = velocity.addVector(0, -.055, 0);
            Vec3d to = from.add(velocity);
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            EntityScoutArena arena = boss.arena();
            double floor = arena != null && arena.raised() ? arena.topAt(to.x, to.z) : locked.y;
            boolean landed = velocity.y < 0 && (to.y <= floor || wall != null);
            if (landed) {
                Vec3d hit = wall != null ? wall.hitVec : new Vec3d(to.x, floor, to.z);
                setPosition(hit.x, hit.y, hit.z);
                damage(boss, from, hit, 12);
                for (int i = 0; i < 3; i++)
                    world.spawnEntity(
                            new EntityScoutShard(
                                    world,
                                    boss,
                                    hit.addVector((i - 1) * .7, .8, 0),
                                    1,
                                    slot * 3 + i));
                com.exoarsenal.network.PacketScoutImpact.send(this, hit, 0);
                setDead();
            } else {
                setPosition(to.x, to.y, to.z);
                motionX = motionY = motionZ = 0;
            }
            return;
        }
        if (kind() == 6) {
            if (boss.getAttack() != ScoutCombatPattern.KATANA_CUTS) {
                setDead();
                return;
            }
            int tick = boss.getAttackTick();
            if (tick < 218) return;
            if (target == null || !target.isEntityAlive()) {
                setDead();
                return;
            }
            if (tick == 218) {
                locked = target.getPositionVector();
                velocity = locked.subtract(getPositionVector()).normalize().scale(.85);
            }
            if (tick < 240) {
                Vec3d before = getPositionVector();
                advance(false);
                boss.setPosition(posX, posY + 1.05, posZ);
                boss.motionX = boss.motionY = boss.motionZ = 0;
                boss.fallDistance = 0;
                boss.setNoGravity(true);
                boss.velocityChanged = true;
                damage(boss, before, getPositionVector(), 12);
            } else {
                Vec3d before = getPositionVector();
                advance(true);
                damage(boss, before, getPositionVector(), 16);
                if (isDead || tick >= 258) {
                    com.exoarsenal.network.PacketScoutImpact.send(this, getPositionVector(), 0);
                    setDead();
                }
            }
            return;
        }
        boolean recall = returning() || kind() == 3 || boss.recallingIce();
        dataManager.set(RETURNING, recall);
        if (recall) {
            Vec3d d = boss.getHammerHand().subtract(getPositionVector());
            if (d.lengthSquared() < 2) {
                setDead();
                return;
            }
            velocity = d.normalize().scale(Math.min(4, d.lengthVector()));
            advance(false);
            return;
        }
        if (kind() == 2) {
            if (age == 12 + slot * 2)
                damage(boss, getPositionVector(), getPositionVector().addVector(0, 2.5, 0), 12);
            if (age > 55 + slot * 2) setDead();
            return;
        }
        if (target == null || !target.isEntityAlive()) {
            setDead();
            return;
        }
        Vec3d aim = target.getPositionVector().addVector(0, target.height * .5, 0);
        if (kind() == 5) {
            int step = (age - 1) % 24;
            if (age > 120) {
                dataManager.set(RETURNING, true);
                return;
            }
            if (step < 9) {
                locked = aim;
                velocity = Vec3d.ZERO;
                rotationYaw = (float) Math.toDegrees(Math.atan2(posX - aim.x, aim.z - posZ));
                renderYawOffset = rotationYaw;
            }
            if (step == 9) {
                struck.clear();
                velocity = locked.subtract(getPositionVector()).normalize().scale(1.4);
            }
            if (step >= 9 && step < 17) {
                Vec3d before = getPositionVector();
                advance(false);
                damage(boss, before, getPositionVector(), 12);
            }
            if (step >= 17) {
                double angle = slot * Math.PI + (age / 24) * .8;
                Vec3d flank = aim.addVector(Math.cos(angle) * 6, 3, Math.sin(angle) * 6);
                velocity = flank.subtract(getPositionVector()).scale(.16);
                advance(false);
            }
            return;
        }
        if (kind() == 4) {
            if (age < launchDelay) return;
            if (age == launchDelay) {
                locked = aim;
                velocity = locked.subtract(getPositionVector()).normalize().scale(1.25);
            }
            Vec3d before = getPositionVector();
            advance(true);
            if (!isDead) damage(boss, before, getPositionVector(), 10);
            if (age > launchDelay + 45) setDead();
            return;
        }
        if (kind() == 0) {
            if (age < 20) {
                locked = aim;
                velocity = velocity.scale(.94);
            }
            if (age == 20) velocity = locked.subtract(getPositionVector()).normalize().scale(.7);
            if (age >= 42) velocity = velocity.scale(.65);
            if (age == 54) {
                for (int i = 0; i < ScoutCombatPattern.FRAGMENTS_PER_CHUNK; i++)
                    world.spawnEntity(
                            new EntityScoutShard(
                                    world,
                                    boss,
                                    getPositionVector().addVector((i - 1) * .35, .2, 0),
                                    1,
                                    slot * ScoutCombatPattern.FRAGMENTS_PER_CHUNK + i));
                com.exoarsenal.network.PacketScoutImpact.send(this, getPositionVector(), 0);
                setDead();
                return;
            }
        } else {
            if (age < 14) {
                locked = aim;
                velocity = velocity.scale(.96);
            } else {
                Vec3d desired = aim.subtract(getPositionVector()).normalize().scale(.95);

                velocity = velocity.scale(.92).add(desired.scale(.08));
            }
        }
        Vec3d before = getPositionVector();
        advance(true);
        if (!isDead && age >= (kind() == 0 ? 20 : 14))
            damage(boss, before, getPositionVector(), kind() == 0 ? 8 : 4);
    }

    private void advance(boolean collide) {
        Vec3d from = getPositionVector(), to = from.add(velocity);
        if (collide) {
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            if (wall != null) {

                if (kind() == 1 || kind() == 4 || kind() == 6) {
                    setPosition(wall.hitVec.x, wall.hitVec.y, wall.hitVec.z);
                    setDead();
                    return;
                }
                to = wall.hitVec;
                velocity = Vec3d.ZERO;
            }
        }
        setPosition(to.x, to.y, to.z);
        motionX = motionY = motionZ = 0;
        rotationYaw = (float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z));
        renderYawOffset = rotationYaw;
    }

    private void damage(EntityX20Scout boss, Vec3d from, Vec3d to, float amount) {
        double radius = kind() == 8 ? 1.8 : kind() == 6 ? 1.6 : kind() == 2 ? .7 : .4;
        for (EntityLivingBase victim :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        new net.minecraft.util.math.AxisAlignedBB(from, to).grow(radius))) {
            if (victim == boss
                    || victim instanceof EntityFrigidRobot
                    || victim instanceof EntityScoutShard
                    || victim instanceof EntityScoutHardpoint
                    || victim instanceof EntityX20Pilot
                    || struck.contains(victim.getUniqueID())) continue;
            if (!victim.getEntityBoundingBox().grow(radius).contains(from)
                    && victim.getEntityBoundingBox().grow(radius).calculateIntercept(from, to)
                            == null) continue;
            if (victim.attackEntityFrom(DamageSource.causeMobDamage(boss).setProjectile(), amount))
                struck.add(victim.getUniqueID());
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public int getBrightnessForRender() {
        return kind() == 0 && age() < 42 ? super.getBrightnessForRender() : 0xF000F0;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (owner != null) tag.setUniqueId("ScoutOwner", owner);
        tag.setInteger("Kind", kind());
        tag.setInteger("Age", age());
        tag.setInteger("Slot", slot);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasUniqueId("ScoutOwner")) owner = tag.getUniqueId("ScoutOwner");
        dataManager.set(KIND, tag.getInteger("Kind"));
        dataManager.set(AGE, tag.getInteger("Age"));
        slot = tag.getInteger("Slot");
        setDead();
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
