package com.exoarsenal.entity;

import java.util.ArrayDeque;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class EntityWulfrumEcho extends Entity {
    private static final DataParameter<Integer> OWNER =
            EntityDataManager.createKey(EntityWulfrumEcho.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> READY =
            EntityDataManager.createKey(EntityWulfrumEcho.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> PHANTOM =
            EntityDataManager.createKey(EntityWulfrumEcho.class, DataSerializers.BOOLEAN);
    private Vec3d routeFrom, routeTo;
    private boolean snapshot;

    private static final class Frame {
        final Vec3d position;
        final float yaw, pitch;
        final java.util.List<WulfrumRecordedShot> shots;

        Frame(EntityWulfrumEye source) {
            position = source.getPositionVector();
            yaw = source.rotationYaw;
            pitch = source.rotationPitch;
            shots = source.recordedShots();
        }
    }

    private final ArrayDeque<Frame> history = new ArrayDeque<>();
    private int delay = 8, life = 160;
    private boolean lasers, ceremonial;

    public EntityWulfrumEcho ceremonial() {
        ceremonial = true;
        return this;
    }

    public static EntityWulfrumEcho snapshot(EntityWulfrumEye source, int life) {
        EntityWulfrumEcho echo =
                new EntityWulfrumEcho(source, 0, life * WulfrumCombatClock.STEPS, false);
        echo.snapshot = true;
        echo.ceremonial = true;
        echo.rotationYaw = source.rotationYaw;
        echo.rotationPitch = source.rotationPitch;
        echo.dataManager.set(READY, true);
        return echo;
    }

    private final java.util.Map<Integer, EntityWulfrumRay> network = new java.util.HashMap<>();

    public EntityWulfrumEcho(World w) {
        super(w);
        setSize(2, 2);
        noClip = true;
        setNoGravity(true);
    }

    public EntityWulfrumEcho(EntityWulfrumEye source, int delay, int life, boolean lasers) {
        this(source.world);
        dataManager.set(OWNER, source.getEntityId());
        this.delay = WulfrumCombatClock.ticks(delay);
        this.life = WulfrumCombatClock.ticks(life);
        this.lasers = lasers;
        setPosition(source.posX, source.posY, source.posZ);
    }

    public static EntityWulfrumEcho phantom(
            EntityWulfrumEye source, Vec3d from, Vec3d to, int delay, int life) {
        EntityWulfrumEcho e = new EntityWulfrumEcho(source, delay, life, false);
        e.routeFrom = from;
        e.routeTo = to;
        e.setPosition(from.x, from.y, from.z);
        e.dataManager.set(PHANTOM, true);
        return e;
    }

    public boolean phantom() {
        return dataManager.get(PHANTOM);
    }

    @Override
    protected void entityInit() {
        dataManager.register(OWNER, -1);
        dataManager.register(READY, false);
        dataManager.register(PHANTOM, false);
    }

    public EntityWulfrumEye source() {
        Entity e = world.getEntityByID(dataManager.get(OWNER));
        return e instanceof EntityWulfrumEye ? (EntityWulfrumEye) e : null;
    }

    public boolean ready() {
        return dataManager.get(READY);
    }

    @Override
    public Vec3d getLookVec() {
        return getVectorForRotation(rotationPitch, rotationYaw);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        EntityWulfrumEye source = source();
        if (source == null
                || !source.isEntityAlive()
                || source.changing() && !ceremonial
                || ticksExisted > life) {
            setDead();
            return;
        }
        if (snapshot) return;
        if (routeFrom != null) {
            dataManager.set(READY, true);
            Vec3d old = getPositionVector().addVector(0, 1.5, 0);
            double f =
                    WulfrumSurvivorScore.ease(
                            (ticksExisted - delay) / (double) WulfrumCombatClock.ticks(14));
            Vec3d p = routeFrom.add(routeTo.subtract(routeFrom).scale(f)),
                    d = routeTo.subtract(routeFrom).normalize();
            setPosition(p.x, p.y - 1.5, p.z);
            rotationYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
            rotationPitch = (float) -Math.toDegrees(Math.asin(d.y));
            if (ticksExisted >= delay) {
                for (EntityPlayer victim :
                        world.getEntitiesWithinAABB(
                                EntityPlayer.class, new AxisAlignedBB(old, p).grow(.8))) {
                    if (victim.isCreative() || victim.isSpectator()) continue;
                    AxisAlignedBB box = victim.getEntityBoundingBox().grow(.7);
                    if (box.contains(old) || box.calculateIntercept(old, p) != null) {
                        victim.attackEntityFrom(
                                source.projectileDamage(this), source.balancedDamage(5));
                        setDead();
                        break;
                    }
                }
                if (world.rayTraceBlocks(old, p, false, true, false) != null || f >= 1) setDead();
            }
            return;
        }
        history.addLast(new Frame(source));
        if (history.size() <= delay) return;
        Frame frame = history.removeFirst();
        Vec3d from = getPositionVector().addVector(0, 1.5, 0),
                to = frame.position.addVector(0, 1.5, 0);
        setPosition(frame.position.x, frame.position.y, frame.position.z);
        rotationYaw = frame.yaw;
        rotationPitch = frame.pitch;
        dataManager.set(READY, true);
        if (!lasers && !ceremonial)
            for (EntityPlayer p :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, new AxisAlignedBB(from, to).grow(.85))) {
                if (p.isCreative() || p.isSpectator()) continue;
                AxisAlignedBB box = p.getEntityBoundingBox().grow(.7);
                if (box.contains(from) || box.calculateIntercept(from, to) != null)
                    p.attackEntityFrom(source.projectileDamage(this), source.balancedDamage(3));
            }
        if (lasers && source.overclocked()) {
            java.util.Set<Integer> active = new java.util.HashSet<>();
            for (WulfrumRecordedShot shot : frame.shots) {
                if (shot.network < 0) {
                    world.spawnEntity(
                            new EntityWulfrumRay(
                                    world,
                                    source,
                                    shot.from,
                                    shot.to,
                                    shot.warning,
                                    shot.life,
                                    shot.damage));
                    continue;
                }
                active.add(shot.network);
                EntityWulfrumRay beam = network.get(shot.network);
                if (beam == null || beam.isDead) {
                    beam =
                            new EntityWulfrumRay(
                                            world, source, shot.from, shot.to, 0, 6, shot.damage)
                                    .charged(shot.radius, false);
                    if (shot.winding) beam.winding(shot.phase);
                    world.spawnEntity(beam);
                    network.put(shot.network, beam);
                }
                if (shot.winding) beam.winding(shot.phase);
                beam.endpoints(shot.from, shot.to);
                beam.refresh();
            }
            network.entrySet()
                    .removeIf(
                            entry -> {
                                if (active.contains(entry.getKey())) return false;
                                entry.getValue().setDead();
                                return true;
                            });
        } else if (lasers
                && !source.overclocked()
                && ticksExisted % 36 == delay % 36
                && source.getAttackTarget() != null) {
            Vec3d end = source.getAttackTarget().getPositionVector().addVector(0, 1, 0);
            world.spawnEntity(new EntityWulfrumRay(world, source, to, end, 22, 5, 4));
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }

    @Override
    public void setDead() {
        if (network != null) for (EntityWulfrumRay beam : network.values()) beam.setDead();
        super.setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}
}
