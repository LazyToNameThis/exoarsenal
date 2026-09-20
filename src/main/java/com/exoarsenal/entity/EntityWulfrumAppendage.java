package com.exoarsenal.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;

public final class EntityWulfrumAppendage extends Entity {
    private static final DataParameter<Integer> OWNER =
            EntityDataManager.createKey(EntityWulfrumAppendage.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TETHER =
            EntityDataManager.createKey(EntityWulfrumAppendage.class, DataSerializers.VARINT);

    public void tether(Entity entity) {
        dataManager.set(TETHER, entity.getEntityId());
    }

    public Entity tether() {
        Entity entity = world.getEntityByID(dataManager.get(TETHER));
        return entity == null ? owner() : entity;
    }

    private static final DataParameter<Boolean> HOT =
            EntityDataManager.createKey(EntityWulfrumAppendage.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GHOST =
            EntityDataManager.createKey(EntityWulfrumAppendage.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer>
            CHARGE =
                    EntityDataManager.createKey(
                            EntityWulfrumAppendage.class, DataSerializers.VARINT),
            FIRE =
                    EntityDataManager.createKey(
                            EntityWulfrumAppendage.class, DataSerializers.VARINT);

    public void charge(int warning, int life) {
        dataManager.set(CHARGE, WulfrumCombatClock.ticks(warning));
        dataManager.set(FIRE, WulfrumCombatClock.ticks(life));
    }

    public int chargeTicks() {
        return dataManager.get(CHARGE);
    }

    public boolean firing() {
        return chargeTicks() == 0 && dataManager.get(FIRE) > 0;
    }

    private Vec3d routeFrom, routeTo;
    private Vec3d[] route;
    private int delay;

    public EntityWulfrumAppendage(World w) {
        super(w);
        setSize(.4F, .4F);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityWulfrumAppendage(EntityWulfrumEye e) {
        this(e.world);
        dataManager.set(OWNER, e.getEntityId());
        setPosition(e.posX, e.posY + 1.5, e.posZ);
    }

    public static EntityWulfrumAppendage replay(
            EntityWulfrumEye eye, Vec3d from, Vec3d to, int delay) {
        EntityWulfrumAppendage e = new EntityWulfrumAppendage(eye);
        e.routeFrom = from;
        e.routeTo = to;
        e.delay = WulfrumCombatClock.ticks(delay);
        e.setPosition(from.x, from.y, from.z);
        e.dataManager.set(GHOST, true);
        return e;
    }

    public boolean ghost() {
        return dataManager.get(GHOST);
    }

    public static EntityWulfrumAppendage replay(EntityWulfrumEye eye, Vec3d[] path, int delay) {
        EntityWulfrumAppendage e = replay(eye, path[0], path[path.length - 1], delay);
        e.route = path.clone();
        return e;
    }

    @Override
    protected void entityInit() {
        dataManager.register(OWNER, -1);
        dataManager.register(TETHER, -1);
        dataManager.register(HOT, false);
        dataManager.register(GHOST, false);
        dataManager.register(CHARGE, 0);
        dataManager.register(FIRE, 0);
    }

    public EntityWulfrumEye owner() {
        Entity e = world.getEntityByID(dataManager.get(OWNER));
        return e instanceof EntityWulfrumEye ? (EntityWulfrumEye) e : null;
    }

    public boolean hot() {
        return dataManager.get(HOT);
    }

    @Override
    public Vec3d getLookVec() {
        return getVectorForRotation(rotationPitch, rotationYaw);
    }

    public void pose(Vec3d p, Vec3d aim, boolean strike) {
        Vec3d old = getPositionVector();
        setPosition(p.x, p.y, p.z);
        Vec3d d = aim.subtract(p).normalize();
        rotationYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        rotationPitch = (float) -Math.toDegrees(Math.asin(d.y));
        dataManager.set(HOT, strike);
        velocityChanged = true;
        EntityWulfrumEye e = owner();
        if (strike && e != null && e.seer())
            for (EntityPlayer player :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, new AxisAlignedBB(old, p).grow(.7))) {
                if (player.isCreative() || player.isSpectator()) continue;
                AxisAlignedBB box = player.getEntityBoundingBox().grow(.6);
                if (box.contains(old) || box.calculateIntercept(old, p) != null)
                    player.attackEntityFrom(e.projectileDamage(this), e.balancedDamage(5));
            }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (!world.isRemote) {
            if (chargeTicks() > 0) dataManager.set(CHARGE, chargeTicks() - 1);
            else if (dataManager.get(FIRE) > 0) dataManager.set(FIRE, dataManager.get(FIRE) - 1);
            EntityWulfrumEye e = owner();
            if (e == null || !e.isEntityAlive() || !e.overclocked() || e.changing()) {
                setDead();
                return;
            }
            if (routeFrom != null) {
                if (route != null) {
                    int n = (ticksExisted - delay) * WulfrumCombatClock.STEPS;
                    if (n >= route.length) {
                        setDead();
                        return;
                    }
                    n = Math.max(0, n);
                    pose(route[n], route[Math.min(route.length - 1, n + 2)], ticksExisted >= delay);
                    return;
                }
                if (ticksExisted > delay + WulfrumCombatClock.ticks(18)) {
                    setDead();
                    return;
                }
                double f =
                        WulfrumSurvivorScore.ease(
                                (ticksExisted - delay) / (double) WulfrumCombatClock.ticks(16));
                pose(
                        routeFrom.add(routeTo.subtract(routeFrom).scale(f)),
                        routeTo,
                        ticksExisted >= delay);
            }
        }
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}
}
