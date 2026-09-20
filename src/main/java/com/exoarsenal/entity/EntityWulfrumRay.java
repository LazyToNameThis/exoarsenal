package com.exoarsenal.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public final class EntityWulfrumRay extends Entity implements IEntityAdditionalSpawnData {
    private static final DataParameter<Integer> AGE =
            EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            END_X = EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.FLOAT),
            END_Y = EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.FLOAT),
            END_Z = EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.FLOAT);
    private Vec3d end = Vec3d.ZERO;
    private int warning = 20, active = 6, owner = -1;
    private float damage;
    private float radius = .12F;
    private boolean helix;
    private static final DataParameter<Float> WINDING =
            EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.FLOAT);
    private boolean winding;
    private boolean coordinationPaused;

    public void coordinationPaused(boolean paused) {
        coordinationPaused = paused;
    }

    private Vec3d[] cachedCurve;
    private Vec3d cachedStart, cachedEnd;
    private float cachedPhase;
    private long cachedTime = Long.MIN_VALUE;

    public EntityWulfrumRay winding(float phase) {
        winding = true;
        dataManager.set(WINDING, phase);
        return this;
    }

    public boolean winding() {
        return winding;
    }

    public float windingPhase() {
        return dataManager.get(WINDING);
    }

    private Vec3d[] path;
    private static final DataParameter<NBTTagCompound> ROUTE =
            EntityDataManager.createKey(EntityWulfrumRay.class, DataSerializers.COMPOUND_TAG);
    private NBTTagCompound cachedRoute;
    private Entity originSocket;
    private Vec3d lockedTarget;

    public EntityWulfrumRay followOrigin(Entity socket, Vec3d target) {
        originSocket = socket;
        lockedTarget = target;
        return this;
    }

    public EntityWulfrumRay path(Vec3d[] points) {
        if (points.length < 2 || points.length > 64)
            throw new IllegalArgumentException("Cut path must contain 2..64 points");
        path = points.clone();
        setPosition(path[0].x, path[0].y, path[0].z);
        setEnd(path[path.length - 1]);
        NBTTagCompound route = new NBTTagCompound();
        route.setInteger("count", points.length);
        for (int i = 0; i < points.length; i++) {
            route.setDouble("x" + i, points[i].x);
            route.setDouble("y" + i, points[i].y);
            route.setDouble("z" + i, points[i].z);
        }
        dataManager.set(ROUTE, route);
        return this;
    }

    public Vec3d[] path() {
        if (world.isRemote) {
            NBTTagCompound route = dataManager.get(ROUTE);
            if (route != cachedRoute) {
                cachedRoute = route;
                int count = route.getInteger("count");
                if (count >= 2 && count <= 64) {
                    path = new Vec3d[count];
                    for (int i = 0; i < count; i++)
                        path[i] =
                                new Vec3d(
                                        route.getDouble("x" + i),
                                        route.getDouble("y" + i),
                                        route.getDouble("z" + i));
                }
            }
        }
        if (!winding) return path == null ? new Vec3d[] {getPositionVector(), end()} : path;
        Vec3d start = getPositionVector(), finish = end();
        float phase = windingPhase();
        if (cachedCurve != null
                && cachedTime == world.getTotalWorldTime()
                && start.equals(cachedStart)
                && finish.equals(cachedEnd)
                && phase == cachedPhase) return cachedCurve;
        Vec3d[] curve = WulfrumHelix.path(start, finish, phase);
        for (int i = 1; i < curve.length; i++) {
            RayTraceResult wall = world.rayTraceBlocks(curve[i - 1], curve[i], false, true, false);
            if (wall != null) {
                curve = java.util.Arrays.copyOf(curve, i + 1);
                curve[i] = wall.hitVec;
                break;
            }
        }
        cachedStart = start;
        cachedEnd = finish;
        cachedPhase = phase;
        cachedTime = world.getTotalWorldTime();
        cachedCurve = curve;
        return curve;
    }

    public EntityWulfrumRay charged(float size, boolean spiral) {
        radius = Math.max(.12F, Math.min(2, size));
        helix = spiral;
        return this;
    }

    public float radius() {
        return radius;
    }

    public boolean helix() {
        return helix;
    }

    public float damage() {
        return damage;
    }

    public void refresh() {
        if (!world.isRemote) dataManager.set(AGE, 0);
    }

    private EntityLivingBase tracking;
    private Vec3d previousTarget;
    private int trackingTicks;

    public EntityWulfrumRay(World w) {
        super(w);
        setSize(.1F, .1F);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityWulfrumRay(
            World w,
            EntityWulfrumEye eye,
            Vec3d from,
            Vec3d to,
            int warning,
            int active,
            float damage) {
        this(w);
        owner = eye.getEntityId();
        setPosition(from.x, from.y, from.z);
        RayTraceResult wall = w.rayTraceBlocks(from, to, false, true, false);
        setEnd(wall == null ? to : wall.hitVec);
        this.warning = WulfrumCombatClock.ticks(warning);
        this.active = WulfrumCombatClock.ticks(active);
        this.damage = damage;
    }

    public EntityWulfrumRay track(EntityLivingBase target, int ticks) {
        tracking = target;
        previousTarget = target.getPositionVector();
        trackingTicks = Math.min(ticks, warning - 10);
        return this;
    }

    private void setEnd(Vec3d p) {
        end = p;
        dataManager.set(END_X, (float) p.x);
        dataManager.set(END_Y, (float) p.y);
        dataManager.set(END_Z, (float) p.z);
    }

    public void endpoints(Vec3d from, Vec3d to) {
        if (world.isRemote) return;
        setPosition(from.x, from.y, from.z);
        RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
        setEnd(wall == null ? to : wall.hitVec);
        velocityChanged = true;
    }

    @Override
    protected void entityInit() {
        dataManager.register(AGE, 0);
        dataManager.register(END_X, 0F);
        dataManager.register(END_Y, 0F);
        dataManager.register(END_Z, 0F);
        dataManager.register(WINDING, 0F);
        dataManager.register(ROUTE, new NBTTagCompound());
    }

    public Vec3d end() {
        return world.isRemote
                ? new Vec3d(dataManager.get(END_X), dataManager.get(END_Y), dataManager.get(END_Z))
                : end;
    }

    public boolean firing() {
        return dataManager.get(AGE) >= warning;
    }

    public float charge() {
        return warning == 0 ? 1 : Math.min(1, dataManager.get(AGE) / (float) warning);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote || coordinationPaused) return;
        int age = dataManager.get(AGE) + 1;
        dataManager.set(AGE, age);
        Entity e = world.getEntityByID(owner);
        if (age > warning + active
                || !(e instanceof EntityWulfrumEye)
                || !e.isEntityAlive()
                || ((EntityWulfrumEye) e).changing()
                        && !(damage == 0 && ((EntityWulfrumEye) e).introTick() > 0)) {
            setDead();
            return;
        }
        if (originSocket != null) {
            if (originSocket.isDead) {
                setDead();
                return;
            }
            Vec3d socket = originSocket.getPositionVector();
            endpoints(
                    socket.add(lockedTarget.subtract(socket).normalize().scale(.6)), lockedTarget);
        }
        if (tracking != null && age <= trackingTicks && tracking.isEntityAlive()) {
            Vec3d next = tracking.getPositionVector(), delta = next.subtract(previousTarget);
            previousTarget = next;
            setPosition(posX + delta.x, posY + delta.y, posZ + delta.z);
            setEnd(end.add(delta));
        }
        if (age == warning && path == null) {
            RayTraceResult wall =
                    world.rayTraceBlocks(getPositionVector(), end, false, true, false);
            if (wall != null) setEnd(wall.hitVec);
        }
        if (age < warning || damage <= 0) return;
        Vec3d[] points = path();
        AxisAlignedBB bounds = new AxisAlignedBB(points[0], points[0]);
        for (Vec3d point : points) bounds = bounds.union(new AxisAlignedBB(point, point));
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(EntityPlayer.class, bounds.grow(radius + .13))) {
            if (p.isCreative() || p.isSpectator()) continue;
            AxisAlignedBB box = p.getEntityBoundingBox().grow(radius + .1);
            for (int i = 1; i < points.length; i++)
                if (box.contains(points[i - 1])
                        || box.calculateIntercept(points[i - 1], points[i]) != null) {
                    p.attackEntityFrom(
                            ((EntityWulfrumEye) e).projectileDamage(this),
                            ((EntityWulfrumEye) e).balancedDamage(damage));
                    break;
                }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf b) {
        b.writeDouble(end.x);
        b.writeDouble(end.y);
        b.writeDouble(end.z);
        b.writeInt(warning);
        b.writeInt(active);
        b.writeInt(owner);
        b.writeFloat(radius);
        b.writeBoolean(helix);
        b.writeBoolean(winding);
        b.writeFloat(windingPhase());
        b.writeInt(path == null ? 0 : path.length);
        if (path != null)
            for (Vec3d p : path) {
                b.writeDouble(p.x);
                b.writeDouble(p.y);
                b.writeDouble(p.z);
            }
    }

    @Override
    public void readSpawnData(ByteBuf b) {
        end = new Vec3d(b.readDouble(), b.readDouble(), b.readDouble());
        warning = b.readInt();
        active = b.readInt();
        owner = b.readInt();
        radius = b.readFloat();
        helix = b.readBoolean();
        winding = b.readBoolean();
        dataManager.set(WINDING, b.readFloat());
        int count = b.readInt();
        if (count < 0 || count > 64) throw new IllegalArgumentException("Invalid cut path");
        if (count > 0) {
            path = new Vec3d[count];
            for (int i = 0; i < count; i++)
                path[i] = new Vec3d(b.readDouble(), b.readDouble(), b.readDouble());
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }
}
