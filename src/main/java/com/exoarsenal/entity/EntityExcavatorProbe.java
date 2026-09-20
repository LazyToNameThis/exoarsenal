package com.exoarsenal.entity;

import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class EntityExcavatorProbe extends EntityLiving {
    private static final DataParameter<Integer>
            PARENT =
                    EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.VARINT),
            MODE = EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.VARINT),
            INDEX = EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            X = EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.FLOAT),
            Y = EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.FLOAT),
            Z = EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.FLOAT);
    private int pulse;
    private boolean fired;
    private double encounterClock, simulationTicks;
    private static final DataParameter<Integer> COMBAT =
            EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> EXPERT =
            EntityDataManager.createKey(EntityExcavatorProbe.class, DataSerializers.BOOLEAN);
    private Vec3d aim = Vec3d.ZERO, direction = new Vec3d(0, 0, 1), flight = Vec3d.ZERO;
    private int retrievalWait;
    private Vec3d coordinatedPosition;
    private boolean coordinatedBlades;
    private int coordinatedDeployment, coordinatedFrame, coordinatedShot = 100;

    void coordinatedShot(int warning) {
        coordinatedShot = -warning;
    }

    void coordinate(Vec3d at, boolean blades) {
        coordinatedPosition = at;
        coordinatedBlades = blades;
    }

    void releaseCoordination() {
        coordinatedPosition = null;
    }

    public int combatTick() {
        return dataManager.get(COMBAT);
    }

    public boolean expertProbe() {
        return dataManager.get(EXPERT);
    }

    public EntityExcavatorProbe(World w) {
        super(w);
        setSize(.8F, .8F);
        setNoAI(true);
        setNoGravity(true);
        isImmuneToFire = true;
    }

    public EntityExcavatorProbe(EntityExcavator boss, int index) {
        this(boss.world);
        dataManager.set(PARENT, boss.getEntityId());
        dataManager.set(INDEX, index);
        setPosition(boss.posX, boss.posY + 2, boss.posZ);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PARENT, -1);
        dataManager.register(MODE, 0);
        dataManager.register(INDEX, 0);
        dataManager.register(X, 0F);
        dataManager.register(Y, 0F);
        dataManager.register(Z, 0F);
        dataManager.register(COMBAT, 0);
        dataManager.register(EXPERT, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(16);
    }

    public int mode() {
        return dataManager.get(MODE);
    }

    public int index() {
        return dataManager.get(INDEX);
    }

    public Vec3d mark() {
        return new Vec3d(dataManager.get(X), dataManager.get(Y), dataManager.get(Z));
    }

    private void mark(Vec3d p) {
        dataManager.set(X, (float) p.x);
        dataManager.set(Y, (float) p.y);
        dataManager.set(Z, (float) p.z);
    }

    public void scan() {
        if (index() < 6) dataManager.set(MODE, 1);
    }

    public void lock() {
        if (index() < 6) dataManager.set(MODE, 2);
    }

    public void recall() {
        dataManager.set(MODE, 4);
        dataManager.set(COMBAT, 0);
    }

    public void plant(Vec3d point) {
        mark(point);
        dataManager.set(MODE, 3);
    }

    public void surveySite(Vec3d point) {
        mark(point);
        dataManager.set(MODE, 5);
    }

    public void pull(Vec3d attachment) {
        mark(attachment);
        dataManager.set(MODE, 6);
    }

    public void fire(EntityExcavator boss) {
        if (isEntityAlive() && !fired) {
            boss.fireTurret(index(), mark(), 12, 16, 7);
            fired = true;
        }
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        motionX = motionY = motionZ = 0;
        if (world.isRemote) return;
        Entity e = world.getEntityByID(dataManager.get(PARENT));
        if (!(e instanceof EntityExcavator) || !e.isEntityAlive()) {
            setDead();
            return;
        }
        EntityExcavator boss = (EntityExcavator) e;
        EntityLivingBase target = boss.getAttackTarget();
        if (coordinatedPosition != null) {
            Vec3d from = getPositionVector(), delta = coordinatedPosition.subtract(from);
            if (delta.lengthVector() > 3) delta = delta.normalize().scale(3);
            direction = delta.lengthSquared() > .01 ? delta.normalize() : direction;
            dataManager.set(EXPERT, boss.expert());
            coordinatedDeployment =
                    Math.max(0, Math.min(10, coordinatedDeployment + (coordinatedBlades ? 1 : -1)));
            coordinatedFrame++;
            coordinatedShot++;
            int pose =
                    coordinatedDeployment > 0
                            ? ExcavatorProbeScore.blades(boss.expert())
                                    + (coordinatedDeployment < 10
                                            ? coordinatedDeployment
                                            : 26 + coordinatedFrame % 34)
                            : coordinatedShot >= 0 && coordinatedShot < 15
                                    ? 30 + coordinatedShot
                                    : 10;
            dataManager.set(COMBAT, pose);
            if (coordinatedDeployment == 10) strike(boss, from, from.add(delta), 5);
            setPosition(posX + delta.x, posY + delta.y, posZ + delta.z);
            rotationYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            rotationPitch = (float) -Math.toDegrees(Math.asin(direction.y));
            renderYawOffset = rotationYaw;
            velocityChanged = true;
            return;
        }
        if (target == null) return;
        encounterClock += boss.encounterRate();
        if (encounterClock < 1) return;
        encounterClock -= 1;
        simulationTicks++;
        if (mode() == 0) {
            combat(boss, target);
            return;
        }
        dataManager.set(COMBAT, 0);
        int count =
                boss.expert() && boss.expertAttack() == ExcavatorExpertScore.SURVEY
                        ? 4
                        : boss.attack() == ExcavatorClassicScore.Attack.SURVEYING_PROBES ? 3 : 6;
        double a = index() * Math.PI * 2 / count + simulationTicks * .012;
        Vec3d destination =
                target.getPositionVector().addVector(Math.cos(a) * 10, 5, Math.sin(a) * 10);
        if (mode() == 1)
            mark(target.getPositionVector().addVector(target.motionX * 8, 1, target.motionZ * 8));
        if (mode() == 0
                && boss.expert()
                && boss.deep()
                && boss.expertStage() == 0
                && ((int) simulationTicks + index() * 7) % 54 == 0) {
            Vec3d aim =
                    target.getPositionVector().addVector(target.motionX * 8, 1, target.motionZ * 8);
            boss.beam(getPositionVector(), aim, 12, 8, 5);
        }
        if (mode() == 2) destination = getPositionVector();
        if (mode() == 3) {
            destination = mark().addVector(0, -.25, 0);
            if (!fired
                    && boss.attackTick() >= 50
                    && boss.getPositionVector().squareDistanceTo(destination) < 64) {
                if (++pulse == 1)
                    boss.beam(
                            destination.addVector(0, .5, 0),
                            destination.addVector(0, 12, 0),
                            20,
                            14,
                            7);
                if (pulse >= 20) fired = true;
            }
        }
        if (mode() == 5) destination = mark().addVector(0, 5, 0);
        if (mode() == 6) destination = mark().addVector(0, 3, 0);
        if (mode() == 4)
            destination =
                    boss.attack() == ExcavatorClassicScore.Attack.FINAL_CHARGE
                            ? boss.drillTip()
                            : boss.segment(ExcavatorProbeScore.socketSegment(index()), 1);
        Vec3d delta = destination.subtract(getPositionVector());
        double speed = mode() == 4 ? 1.2 : .55;
        if (delta.lengthVector() > speed) delta = delta.normalize().scale(speed);
        setPosition(posX + delta.x, posY + delta.y, posZ + delta.z);
        velocityChanged = true;
        if (mode() == 4 && getPositionVector().squareDistanceTo(destination) < 1) setDead();
    }

    private void combat(EntityExcavator boss, EntityLivingBase target) {
        boolean expert = boss.expert();
        dataManager.set(EXPERT, expert);
        int t = combatTick() + 1;
        if (t > ExcavatorProbeScore.end(expert)) t = 1;
        dataManager.set(COMBAT, t);
        int q = t - ExcavatorProbeScore.blades(expert);
        Vec3d at = getPositionVector(), victim = target.getPositionVector().addVector(0, 1, 0);
        if (t == 1) {
            flight = Vec3d.ZERO;
            aim = victim;
            retrievalWait = 0;
        }
        if (q < 12) {
            Vec3d hover =
                    victim.addVector(Math.cos(index() * 2.4) * 8, 4, Math.sin(index() * 2.4) * 8);
            flight = flight.scale(.65).add(hover.subtract(at).normalize().scale(.22));
            direction = victim.subtract(at).normalize();
            mark(victim);
            if (ExcavatorProbeScore.shot(expert, t)) {
                aim = victim.addVector(target.motionX * 4, 0, target.motionZ * 4);
                boss.beam(at.add(direction.scale(.7)), aim, 10, 6, 4);
            }
            if (ExcavatorProbeScore.shot(expert, t - 10)) flight = direction.scale(-.85);
        } else if (q < 26) {
            if (q == 12) {
                aim = victim;
                mark(aim);
            }
            flight = flight.scale(.7);
        } else if (q < 60) {
            if (q == 26) direction = aim.subtract(at).normalize();
            flight = direction.scale(1.15);
            strike(boss, at, at.add(flight), 5);
        } else if (!expert) {
            flight = flight.scale(.8);
        } else if (q < 90) {
            flight = direction.scale(-.65);
        } else if (q < 112) {
            if (q == 90) {
                aim = victim;
                direction = aim.subtract(at).normalize();
                mark(at.add(direction.scale(1.2)));
            }
            Vec3d previous = mark(), next = previous.add(direction.scale(1.25));
            net.minecraft.util.math.RayTraceResult wall =
                    world.rayTraceBlocks(previous, next, false, true, false);
            if (wall != null) next = wall.hitVec;
            mark(next);
            strike(boss, previous, next, 5);
            flight = flight.scale(.7);
        } else if (q < 155) {
            Vec3d delta = mark().subtract(at);
            flight = delta.normalize().scale(Math.min(1.7, delta.lengthVector()));
            strike(boss, at, at.add(flight), 5);
            if (delta.lengthVector() < 1.1)
                dataManager.set(COMBAT, ExcavatorProbeScore.blades(true) + 155);
            else if (q == 154 && ++retrievalWait < 60) dataManager.set(COMBAT, t - 1);
        } else flight = flight.scale(.75);
        Vec3d next = at.add(flight);
        net.minecraft.util.math.RayTraceResult wall =
                world.rayTraceBlocks(at, next, false, true, false);
        if (wall != null) {
            next = wall.hitVec.subtract(flight.normalize().scale(.2));
            flight = Vec3d.ZERO;
        }
        setPosition(next.x, next.y, next.z);
        rotationYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
        rotationPitch = (float) -Math.toDegrees(Math.asin(direction.y));
        renderYawOffset = rotationYaw;
        rotationYawHead = rotationYaw;
        velocityChanged = true;
    }

    private void strike(EntityExcavator boss, Vec3d from, Vec3d to, float damage) {
        Vec3d side = new Vec3d(direction.z, 0, -direction.x).normalize();
        int q = combatTick() - ExcavatorProbeScore.blades(expertProbe());
        double sweep = q >= 26 && q < 60 ? Math.sin((q - 26) / 34D * Math.PI) * .9 : 0;
        for (int sign : new int[] {-1, 1}) {
            Vec3d a = from.add(side.scale(sign * .7)),
                    b = to.add(side.scale(sign * (.7 + sweep))).add(direction.scale(1.6));
            for (net.minecraft.entity.player.EntityPlayer p :
                    world.getEntitiesWithinAABB(
                            net.minecraft.entity.player.EntityPlayer.class,
                            new net.minecraft.util.math.AxisAlignedBB(a, b).grow(.35))) {
                if (p.isCreative() || p.isSpectator()) continue;
                net.minecraft.util.math.AxisAlignedBB box = p.getEntityBoundingBox().grow(.3);
                if (box.contains(a) || box.calculateIntercept(a, b) != null)
                    p.attackEntityFrom(
                            net.minecraft.util.DamageSource.causeMobDamage(boss), damage);
            }
        }
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void fall(float d, float m) {}

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        setDead();
    }
}
