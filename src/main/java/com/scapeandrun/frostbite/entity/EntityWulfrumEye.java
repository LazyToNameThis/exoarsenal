package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.*;
import java.util.UUID;

public abstract class EntityWulfrumEye extends EntityMob implements IAnimatable {
    private static final DataParameter<Integer>
            PARTNER = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT),
            ATTACK = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT),
            TIME = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> SOLO =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> COUNTER =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> TRANSFORM =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> UPGRADED =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> INTRO =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SURVIVOR =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> OVERCLOCKED =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float> SHIELD =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT);
    private static final DataParameter<Boolean> ANCHORED =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float>
            ANCHOR_X = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT),
            ANCHOR_Y = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT),
            ANCHOR_Z = EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT);

    public Vec3d chainAnchor() {
        return dataManager.get(ANCHORED)
                ? new Vec3d(
                        dataManager.get(ANCHOR_X),
                        dataManager.get(ANCHOR_Y),
                        dataManager.get(ANCHOR_Z))
                : null;
    }

    void chainAnchor(Vec3d point) {
        dataManager.set(ANCHORED, point != null);
        if (point != null) {
            dataManager.set(ANCHOR_X, (float) point.x);
            dataManager.set(ANCHOR_Y, (float) point.y);
            dataManager.set(ANCHOR_Z, (float) point.z);
        }
    }

    private final WulfrumDuet duet = new WulfrumDuet();
    private final WulfrumSurvivor survivorScore = new WulfrumSurvivor();
    private int recordedShotTick = -1;
    private Vec3d recordedShot = Vec3d.ZERO;
    private final java.util.List<WulfrumRecordedShot> recordedShots = new java.util.ArrayList<>();

    public void rememberShot(Vec3d end) {
        rememberBeam(pupil(), end, 12, 5, 4);
    }

    void rememberBeam(Vec3d from, Vec3d end, int warning, int life, float damage) {
        if (recordedShotTick != ticksExisted) recordedShots.clear();
        recordedShotTick = ticksExisted;
        recordedShot = end;
        if (recordedShots.size() < 24)
            recordedShots.add(new WulfrumRecordedShot(from, end, warning, life, damage));
    }

    java.util.List<WulfrumRecordedShot> recordedShots() {
        return recordedShotTick == ticksExisted
                ? new java.util.ArrayList<>(recordedShots)
                : java.util.Collections.emptyList();
    }

    void rememberNetwork(EntityWulfrumRay ray) {
        if (!ray.firing() || ray.damage() <= 0) return;
        if (recordedShotTick != ticksExisted) recordedShots.clear();
        recordedShotTick = ticksExisted;
        recordedShots.removeIf(s -> s.network == ray.getEntityId());
        if (recordedShots.size() < 32)
            recordedShots.add(
                    new WulfrumRecordedShot(
                            ray.getPositionVector(),
                            ray.end(),
                            0,
                            6,
                            ray.damage(),
                            ray.getEntityId(),
                            ray.radius(),
                            ray.winding(),
                            ray.windingPhase()));
    }

    public int recordedShotTick() {
        return recordedShotTick;
    }

    public Vec3d recordedShot() {
        return recordedShot;
    }

    private static final DataParameter<String> BAR_ID =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.STRING);
    private static final DataParameter<Float>
            SEER_HEALTH =
                    EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT),
            OBSERVER_HEALTH =
                    EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.FLOAT);
    private final AnimationFactory factory = new AnimationFactory(this);
    private final BossInfoServer bar =
            new BossInfoServer(
                    new TextComponentString("Wulfrum Eye"),
                    BossInfo.Color.GREEN,
                    BossInfo.Overlay.PROGRESS);
    private UUID partnerId;
    private boolean paired, balanced, expert;
    private int cursor, absent, parryCooldown;
    private Vec3d aim = Vec3d.ZERO, origin = Vec3d.ZERO, direction = new Vec3d(0, 0, 1);
    private Vec3d counterDirection = Vec3d.ZERO;
    private int activeShards;
    private float routeYaw;
    private boolean partnerDefeated;
    private int missingPartner;
    private Vec3d entrance = Vec3d.ZERO;

    public abstract boolean seer();

    public EntityWulfrumEye(World world) {
        super(world);
        setSize(3, 3);
        setNoGravity(true);
        setNoAI(true);
        isImmuneToFire = true;
        experienceValue = seer() ? 0 : 200;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {
        if (!world.isRemote) {
            move(MoverType.SELF, motionX, motionY, motionZ);
            fallDistance = 0;
        }
    }

    public static final class Seer extends EntityWulfrumEye {
        public Seer(World w) {
            super(w);
        }

        public boolean seer() {
            return true;
        }
    }

    public static final class Observer extends EntityWulfrumEye {
        public Observer(World w) {
            super(w);
        }

        public boolean seer() {
            return false;
        }
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PARTNER, -1);
        dataManager.register(ATTACK, 0);
        dataManager.register(TIME, 0);
        dataManager.register(SOLO, false);
        dataManager.register(COUNTER, 0);
        dataManager.register(TRANSFORM, 0);
        dataManager.register(UPGRADED, false);
        dataManager.register(INTRO, 0);
        dataManager.register(BAR_ID, "");
        dataManager.register(SEER_HEALTH, 1F);
        dataManager.register(OBSERVER_HEALTH, 1F);
        dataManager.register(SURVIVOR, 0);
        dataManager.register(OVERCLOCKED, false);
        dataManager.register(SHIELD, 0F);
        dataManager.register(ANCHORED, false);
        dataManager.register(ANCHOR_X, 0F);
        dataManager.register(ANCHOR_Y, 0F);
        dataManager.register(ANCHOR_Z, 0F);
    }

    public String bossBarId() {
        return dataManager.get(BAR_ID);
    }

    public float seerHealth() {
        return dataManager.get(SEER_HEALTH);
    }

    public float observerHealth() {
        return dataManager.get(OBSERVER_HEALTH);
    }

    public int introTick() {
        return dataManager.get(INTRO);
    }

    public int transformTick() {
        return dataManager.get(TRANSFORM);
    }

    public boolean upgraded() {
        return dataManager.get(UPGRADED);
    }

    public boolean changing() {
        EntityWulfrumEye other = partner();
        return introTick() > 0
                || survivorTick() > 0
                || transformTick() > 0
                || other != null && other.transformTick() > 0;
    }

    public int survivorTick() {
        return dataManager.get(SURVIVOR);
    }

    public boolean overclocked() {
        return dataManager.get(OVERCLOCKED);
    }

    public float energyShield() {
        return dataManager.get(SHIELD);
    }

    private void beginSurvivor() {
        if (!expert || overclocked() || survivorTick() > 0) return;
        dataManager.set(SURVIVOR, 1);
        dataManager.set(TRANSFORM, 0);
        dataManager.set(UPGRADED, true);
        dataManager.set(TIME, 0);
        motionX = motionY = motionZ = 0;
    }

    private void survivorTransition() {
        int t = survivorTick();
        motionX = motionY = motionZ = 0;
        if (t == 115) {
            for (EntityPlayer p : world.playerEntities)
                if (p.getDistanceSq(this) < 128 * 128)
                    p.sendMessage(
                            new TextComponentString(
                                    seer()
                                            ? "X-03 MISSING, DEPLETING EXCESS FUEL RESERVES"
                                            : "X-02 MISSING, DEPLETING EXCESS FUEL RESERVES"));
        }
        if (t >= 160) {
            setHealth(getMaxHealth());
            dataManager.set(SHIELD, 50F);
            dataManager.set(OVERCLOCKED, true);
            dataManager.set(SURVIVOR, 0);
        } else dataManager.set(SURVIVOR, t + 1);
    }

    public boolean chainAttached() {
        return !solo()
                && (coordinating()
                        ? dataManager.get(COORD_CHAIN)
                        : !(introTick() >= 188 && introTick() < 248)
                                && !SeerObserverPattern.detached(attack(), attackTick()));
    }

    public float balancedDamage(float amount) {
        return SeerObserverPattern.damage(amount, expert, world.getDifficulty().getDifficultyId());
    }

    public DamageSource contactDamage() {
        return new EntityDamageSource("mob", this) {
            @Override
            public boolean isDifficultyScaled() {
                return false;
            }
        };
    }

    public DamageSource projectileDamage(Entity projectile) {
        return new EntityDamageSourceIndirect("indirectMagic", projectile, this) {
            @Override
            public boolean isDifficultyScaled() {
                return false;
            }
        }.setProjectile().setMagicDamage();
    }

    public void releaseShard() {
        activeShards = Math.max(0, activeShards - 1);
    }

    public int counterTick() {
        return dataManager.get(COUNTER);
    }

    private void tickCounter() {
        if (counterTick() == 0) return;
        int t = counterTick() + 1;
        dataManager.set(COUNTER, t >= 27 ? 0 : t);
        if (t < 18) {
            motionX *= .5;
            motionY *= .5;
            motionZ *= .5;
        } else {
            face(pupil().add(counterDirection));
            motionX = counterDirection.x * 1.45;
            motionY = counterDirection.y * 1.45;
            motionZ = counterDirection.z * 1.45;
            bladeContact(10);
        }
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(seer() ? 220 : 260);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(6);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(80);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    public int attack() {
        return dataManager.get(ATTACK);
    }

    public int attackTick() {
        return dataManager.get(TIME);
    }

    public boolean solo() {
        return dataManager.get(SOLO);
    }

    private static final DataParameter<Boolean> COORDINATING =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> COORD_CHAIN =
            EntityDataManager.createKey(EntityWulfrumEye.class, DataSerializers.BOOLEAN);

    {
        dataManager.register(COORDINATING, false);
        dataManager.register(COORD_CHAIN, true);
    }

    void coordinationChain(boolean attached) {
        dataManager.set(COORD_CHAIN, attached);
    }

    void beginSurvivorCoordination() {
        survivorScore.beginCoordination();
    }

    EntityWulfrumAppendage coordinationSocket(int index) {
        return survivorScore.coordinateSocket(this, index);
    }

    public boolean coordinating() {
        return dataManager.get(COORDINATING);
    }

    void coordinationPose(int attack, int tick) {
        dataManager.set(COORDINATING, attack >= 0);
        if (attack >= 0) {
            dataManager.set(ATTACK, attack);
            dataManager.set(TIME, tick);
        } else dataManager.set(TIME, 0);
    }

    public EntityWulfrumEye partner() {
        Entity e =
                world.isRemote
                        ? world.getEntityByID(dataManager.get(PARTNER))
                        : partnerId == null
                                ? null
                                : ((WorldServer) world).getEntityFromUuid(partnerId);
        return e instanceof EntityWulfrumEye && e.isEntityAlive() ? (EntityWulfrumEye) e : null;
    }

    public Vec3d pupil() {
        return getPositionVector().addVector(0, 1.5, 0);
    }

    void glide(Vec3d to, double speed) {
        fly(to.addVector(0, -1.5, 0), speed);
    }

    void look(Vec3d to) {
        face(to);
    }

    boolean contact(float damage) {
        return damage > 0 && bodyContact(this, damage);
    }

    void slash(float damage) {
        bladeContact(damage);
    }

    void fragment(Vec3d from, Vec3d velocity, int wait, EntityLivingBase target, float damage) {
        shard(from, velocity, 0, wait, target, damage);
    }

    public void prepareEntrance() {
        if (world.isRemote || paired || introTick() > 0) return;
        entrance = getPositionVector();
        dataManager.set(INTRO, 1);
        placeEntrance(1);
    }

    EntityWulfrumEye prepareTrialPair(Vec3d anchor) {
        EntityWulfrumEye other = new Seer(world);
        paired = other.paired = true;
        partnerId = other.getUniqueID();
        other.partnerId = getUniqueID();
        entrance = other.entrance = anchor;
        dataManager.set(INTRO, 0);
        other.dataManager.set(INTRO, 0);
        other.setPosition(anchor.x - 8, anchor.y + 48, anchor.z);
        world.spawnEntity(other);
        return other;
    }

    private void pair() {
        Vec3d anchor = introTick() > 0 ? entrance : getPositionVector();
        EntityWulfrumEye other = seer() ? new Observer(world) : new Seer(world);
        other.paired = true;
        other.partnerId = getUniqueID();
        partnerId = other.getUniqueID();
        paired = true;
        entrance = other.entrance = anchor;
        dataManager.set(INTRO, 1);
        other.dataManager.set(INTRO, 1);
        placeEntrance(1);
        other.placeEntrance(1);
        world.spawnEntity(other);
    }

    private void placeEntrance(int tick) {
        double[] p = SeerObserverIntro.offset(tick, seer());
        setPosition(entrance.x + p[0], entrance.y + p[1], entrance.z + p[2]);
        motionX = motionY = motionZ = 0;
        noClip = tick < SeerObserverIntro.END;
        velocityChanged = true;
    }

    private void entranceTick(EntityWulfrumEye other) {
        int tick = introTick();
        placeEntrance(tick);
        other.placeEntrance(tick);
        face(entrance.addVector(0, 2, 12));
        other.face(entrance.addVector(0, 2, 12));
        EntityWulfrumEye sword = seer() ? this : other, gun = seer() ? other : this;
        if (tick == 188)
            for (int delay : new int[] {3, 6, 9, 12})
                world.spawnEntity(new EntityWulfrumEcho(sword, delay, 60, false).ceremonial());
        if (tick >= 188 && tick < 248 && tick % 4 == 0) {
            double angle = (tick - 188) * .22;
            Vec3d end = gun.pupil().addVector(Math.cos(angle) * 12, 24, Math.sin(angle) * 12);
            world.spawnEntity(new EntityWulfrumRay(world, gun, gun.pupil(), end, 0, 8, 0));
        }
        if (tick == 30 || tick == 94)
            world.playSound(
                    null,
                    entrance.x,
                    entrance.y + 3,
                    entrance.z,
                    tick == 30
                            ? SoundEvents.ENTITY_FIREWORK_LAUNCH
                            : SoundEvents.ENTITY_IRONGOLEM_ATTACK,
                    SoundCategory.HOSTILE,
                    1.3F,
                    tick == 30 ? .55F : .65F);
        if (tick < 94 && tick % 3 == 0) {
            for (EntityWulfrumEye eye : new EntityWulfrumEye[] {this, other})
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.END_ROD,
                                eye.posX,
                                eye.posY + 2.8,
                                eye.posZ,
                                4,
                                .25,
                                .7,
                                .25,
                                .03);
        }
        int next = tick >= SeerObserverIntro.END ? 0 : tick + 1;
        dataManager.set(INTRO, next);
        other.dataManager.set(INTRO, next);
        if (next == 0) {
            noClip = other.noClip = false;
        }
    }

    @Override
    public void onLivingUpdate() {
        if (EntityDraedon.arriving(this)) return;
        super.onLivingUpdate();
        setNoGravity(true);
        fallDistance = 0;
        if (world.isRemote || !isEntityAlive()) return;
        if (ticksExisted % 40 == 0) {
            activeShards = 0;
            for (Entity e : world.loadedEntityList)
                if (e instanceof EntityWulfrumShard
                        && !e.isDead
                        && ((EntityWulfrumShard) e).belongs(this)) activeShards++;
        }
        if (!balanced) {
            balanced = true;
            expert = com.scapeandrun.frostbite.world.FrostbiteWorldSettings.get(world).isExpert();
        }
        double maximum = (seer() ? 220 : 260) * (expert ? 1.5 : 1);
        if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getBaseValue() != maximum) {
            float fraction = getHealth() / getMaxHealth();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maximum);
            setHealth(getMaxHealth() * fraction);
        }
        if (!paired) pair();
        EntityWulfrumEye other = partner();
        dataManager.set(PARTNER, other == null ? -1 : other.getEntityId());
        dataManager.set(SOLO, other == null);
        if (other == null && !partnerDefeated) {
            motionX = motionY = motionZ = 0;
            if (++missingPartner > 100) setDead();
            return;
        }
        missingPartner = 0;
        dataManager.set(BAR_ID, bar.getUniqueId().toString());
        bar.setVisible(!seer() || other == null);
        dataManager.set(
                SEER_HEALTH,
                seer()
                        ? getHealth() / getMaxHealth()
                        : other == null ? 0 : other.getHealth() / other.getMaxHealth());
        dataManager.set(
                OBSERVER_HEALTH,
                !seer()
                        ? getHealth() / getMaxHealth()
                        : other == null ? 0 : other.getHealth() / other.getMaxHealth());
        bar.setName(new TextComponentString(SeerObserverIntro.NAME));
        bar.setPercent(getHealth() / getMaxHealth());
        if (survivorTick() > 0) {
            survivorTransition();
            return;
        }
        if (!upgraded() && getHealth() <= getMaxHealth() * .5F) {
            dataManager.set(UPGRADED, true);
            dataManager.set(TRANSFORM, 1);
            dataManager.set(COUNTER, 0);
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    SoundEvents.BLOCK_PISTON_EXTEND,
                    SoundCategory.HOSTILE,
                    1,
                    .65F);
        }
        if (parryCooldown > 0) parryCooldown--;
        if (DraedonCollaboration.control(this)) return;
        if (seer() && other != null) return;
        if (introTick() > 0 && other != null) {
            entranceTick(other);
            return;
        }
        EntityPlayer target = world.getNearestAttackablePlayer(this, 80, 40);
        if (target == null) {
            motionX *= .8;
            motionY *= .8;
            motionZ *= .8;
            if (++absent > 400) {
                if (other != null) other.setDead();
                setDead();
            }
            return;
        }
        absent = 0;
        setAttackTarget(target);
        if (other != null) other.setAttackTarget(target);
        EntityWulfrumEye sword = seer() ? this : other, handler = seer() ? null : this;
        if (transformTick() > 0 || other != null && other.transformTick() > 0) {
            transition(target);
            if (other != null) other.transition(target);
            dataManager.set(TIME, 0);
            if (other != null) other.dataManager.set(TIME, 0);
            return;
        }
        for (int substep = 0; substep < WulfrumCombatClock.STEPS; substep++) {
            int desired =
                    attackTick() == 0 || other == null
                            ? SeerObserverPattern.select(
                                    cursor,
                                    sword != null,
                                    handler != null,
                                    other == null
                                            ? getHealth() / getMaxHealth()
                                            : Math.min(
                                                    getHealth() / getMaxHealth(),
                                                    other.getHealth() / other.getMaxHealth()),
                                    expert,
                                    sword != null && sword.upgraded(),
                                    handler != null && handler.upgraded())
                            : attack();
            if (attackTick() == 0 && other != null && upgraded() == other.upgraded())
                desired = WulfrumDuetScore.select(cursor, expert, upgraded());
            if (other == null && overclocked())
                desired = WulfrumSurvivorScore.select(seer(), cursor);
            int t = attackTick() + 1;
            if (attack() != desired) {
                t = 1;
                dataManager.set(ATTACK, desired);
            }
            if (t == 1) {
                origin = target.getPositionVector();
                aim = target.getPositionVector().addVector(0, 1, 0);
                direction = aim.subtract(pupil()).normalize();
                routeYaw = target.rotationYaw;
            }
            dataManager.set(TIME, t);
            if (other != null) {
                other.dataManager.set(ATTACK, desired);
                other.dataManager.set(TIME, t);
            }
            if (desired >= 35) survivorScore.tick(this, target, desired, t);
            else if (desired >= 16) duet.tick(this, sword, handler, target, desired, t);
            else perform(desired, t, target, sword, handler);
            if (desired >= 16 && desired < 35 && duet.clockOverride() >= 0) {
                int next = duet.clockOverride();
                if (next >= SeerObserverPattern.duration(desired)) {
                    cursor++;
                    next = 0;
                }
                dataManager.set(TIME, next);
                if (other != null) other.dataManager.set(TIME, next);
                break;
            }
            if (sword != null) sword.tickCounter();
            if (other != null
                    && handler != null
                    && sword.getHealth() / sword.getMaxHealth() + .2F
                            < handler.getHealth() / handler.getMaxHealth()
                    && t % 70 == 30
                    && desired < 9
                    && desired != SeerObserverPattern.SYNCHRONIZATION) burst(handler, target, t, t);
            if (t >= SeerObserverPattern.duration(desired)) {
                cursor++;
                dataManager.set(TIME, 0);
            }
        }
    }

    private void transition(EntityLivingBase target) {
        int t = transformTick();
        if (t == 0) return;
        fly(target.getPositionVector().addVector(seer() ? -11 : 11, 5, 0), .75);
        EntityWulfrumEye other = partner();
        face(
                t > 30 && other != null
                        ? other.pupil()
                        : target.getPositionVector().addVector(0, 1, 0));
        dataManager.set(TRANSFORM, t >= SeerObserverPattern.TRANSFORM_TICKS ? 0 : t + 1);
        if (t == 30)
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    SoundEvents.BLOCK_IRON_DOOR_CLOSE,
                    SoundCategory.HOSTILE,
                    .8F,
                    .7F);
    }

    private void shard(
            Vec3d at, Vec3d velocity, int mode, int delay, EntityLivingBase target, float damage) {
        if (activeShards >= SeerObserverPattern.SHARD_CAP) return;
        EntityWulfrumShard p =
                new EntityWulfrumShard(this, at, velocity, mode, delay, target, damage);
        if (world.spawnEntity(p)) {
            activeShards++;
            p.counted();
        }
    }

    private void radial(Vec3d at, EntityLivingBase target, int count, float damage) {
        for (int i = 0; i < count; i++) {
            double y = 1 - 2 * (i + .5) / count, a = i * 2.39996323, r = Math.sqrt(1 - y * y);
            shard(
                    at,
                    new Vec3d(Math.cos(a) * r, y, Math.sin(a) * r).scale(.5),
                    2,
                    0,
                    target,
                    damage);
        }
    }

    private Vec3d route(int i, boolean invert) {
        double[] p = SeerObserverPattern.zigzag(i, invert);
        double a = Math.toRadians(routeYaw);
        return origin.addVector(
                p[0] * Math.cos(a) - p[2] * Math.sin(a),
                p[1],
                p[0] * Math.sin(a) + p[2] * Math.cos(a));
    }

    private boolean bodyContact(EntityWulfrumEye eye, float damage) {
        boolean hit = false;
        double radius = eye.seer() && eye.upgraded() ? 1.5 : 1;
        Vec3d a = new Vec3d(eye.prevPosX, eye.prevPosY + 1.5, eye.prevPosZ), b = eye.pupil();
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(a, b).grow(radius))) {
            if (p.isCreative() || p.isSpectator()) continue;
            AxisAlignedBB box = p.getEntityBoundingBox().grow(radius);
            if (box.contains(a) || box.calculateIntercept(a, b) != null) {
                hit = true;
                p.attackEntityFrom(eye.contactDamage(), balancedDamage(damage));
            }
        }
        return hit;
    }

    private void fly(Vec3d destination, double speed) {
        if (!changing()) speed *= expert ? 1.8 : 1.3;
        Vec3d d = destination.subtract(getPositionVector());
        if (d.lengthVector() > speed) d = d.normalize().scale(speed);
        motionX = d.x;
        motionY = d.y;
        motionZ = d.z;
        velocityChanged = true;
    }

    private void face(Vec3d point) {
        Vec3d d = point.subtract(pupil());
        rotationYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
        renderYawOffset = rotationYaw;
        rotationYawHead = rotationYaw;
        rotationPitch = (float) -Math.toDegrees(Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)));
    }

    private void lock(EntityLivingBase target) {
        aim = target.getPositionVector().addVector(0, 1, 0);
    }

    private void lunge(
            EntityWulfrumEye sword, EntityLivingBase target, int t, int start, float damage) {
        if (sword == null) return;
        if (t == start - 14) {
            lock(target);
            direction = aim.subtract(sword.pupil()).normalize();
            ray(sword.pupil(), aim.add(direction.scale(8)), 14, 1, 0);
        }
        if (t >= start && t < start + 10) {
            sword.face(sword.pupil().add(direction));
            sword.motionX = direction.x * 1.65;
            sword.motionY = direction.y * 1.65;
            sword.motionZ = direction.z * 1.65;
            sword.bladeContact(damage);
        }
    }

    private void bladeContact(float damage) {
        double reach =
                (attack() == SeerObserverPattern.SYNCHRONIZATION
                                ? 8.3
                                : attack() == SeerObserverPattern.PASSING
                                        ? 7.1
                                        : solo() ? 6.3 : 5.3)
                        * 8
                        / 7;
        Vec3d from = pupil(), to = from.add(getLookVec().scale(reach));
        if (upgraded() && seer() && (!overclocked() || WulfrumSurvivorScore.saw(attack()))) {
            bodyContact(this, damage);
            return;
        }
        for (EntityPlayer player :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, new AxisAlignedBB(from, to).grow(.6))) {
            if (player.isCreative() || player.isSpectator()) continue;
            if (player.getEntityBoundingBox().grow(.45).contains(from)
                    || player.getEntityBoundingBox().grow(.45).calculateIntercept(from, to) != null)
                player.attackEntityFrom(contactDamage(), balancedDamage(damage));
        }
    }

    private void ray(Vec3d from, Vec3d to, int warning, int active, float damage) {
        world.spawnEntity(new EntityWulfrumRay(world, this, from, to, warning, active, damage));
    }

    private void burst(EntityWulfrumEye handler, EntityLivingBase target, int t, int frame) {
        if (handler != null && t == frame)
            ray(handler.pupil(), target.getPositionVector().addVector(0, 1, 0), 20, 6, 7);
    }

    private void perform(
            int attack,
            int t,
            EntityLivingBase target,
            EntityWulfrumEye sword,
            EntityWulfrumEye handler) {
        if (sword != null) {
            sword.face(target.getPositionVector().addVector(0, 1, 0));
            sword.motionX *= .86;
            sword.motionY *= .86;
            sword.motionZ *= .86;
        }
        if (handler != null) {
            handler.face(target.getPositionVector().addVector(0, 1, 0));
            handler.fly(origin.addVector(10, 7, 8), .3);
        }
        if (attack >= SeerObserverPattern.SHARD_WHEEL) {
            advanced(attack, t, target, sword, handler);
            return;
        }
        switch (attack) {
            case SeerObserverPattern.FENCING:
            case SeerObserverPattern.SEER_ALONE:
                if (t < 24) sword.fly(target.getPositionVector().addVector(-7, 2, 0), .55);
                lunge(sword, target, t, 36, 10);
                lunge(sword, target, t, 94, 12);
                lunge(sword, target, t, 150, 12);
                if (t >= 56 && t < 70 || t >= 152 && t < 166) {
                    float cut = (t >= 152 ? t - 152 : t - 56) / 13F;
                    sword.rotationYaw += (cut - .5F) * 130;
                    sword.rotationPitch = t < 70 ? (cut - .5F) * 75 : 0;
                    sword.rotationYawHead = sword.renderYawOffset = sword.rotationYaw;
                    sword.bladeContact(9);
                }
                if (t >= 72 && t < 82) sword.fly(origin.addVector(-9, 4, -5), .7);
                if (attack == SeerObserverPattern.SEER_ALONE) {
                    lunge(sword, target, t, 65, 10);
                    lunge(sword, target, t, 118, 12);
                    if (t >= 98 && t < 112) {
                        sword.rotationYaw += (t - 98) * 26;
                        sword.renderYawOffset = sword.rotationYawHead = sword.rotationYaw;
                        sword.bladeContact(10);
                    }
                    if (t >= 126 && t < 136)
                        sword.fly(target.getPositionVector().addVector(0, 11, 0), 1.2);
                }
                burst(handler, target, t, 76);
                burst(handler, target, t, 164);
                break;
            case SeerObserverPattern.CROSSFIRE:
                if (t == 24 || t == 74) {
                    lock(target);
                    for (int i = 0; i < 4; i++) {
                        double a = i * Math.PI / 2;
                        Vec3d d = new Vec3d(Math.cos(a) * 13, 0, Math.sin(a) * 13),
                                offset = new Vec3d(-Math.sin(a) * 2, 0, Math.cos(a) * 2);
                        world.spawnEntity(
                                new EntityWulfrumRay(
                                                world,
                                                this,
                                                aim.add(d).add(offset),
                                                aim.subtract(d).add(offset),
                                                30,
                                                10,
                                                9)
                                        .track(target, 12));
                    }
                }
                lunge(sword, target, t, 62, 12);
                lunge(sword, target, t, 115, 12);
                break;
            case SeerObserverPattern.PASSING:
                if (t < 40) {
                    sword.fly(origin.addVector(-9, 3, 0), .7);
                    handler.fly(sword.getPositionVector().addVector(-4, 0, 0), .75);
                }
                if (t >= 40 && t < 54) handler.fly(sword.getPositionVector(), 1);
                lunge(sword, target, t, 58, 16);
                if (t > 80) sword.fly(origin.addVector(8, 5, 0), .65);
                break;
            case SeerObserverPattern.FLAIL:
                handler.fly(origin.addVector(0, 7, 0), .7);
                if (t < 190) {
                    double a = SeerObserverPattern.wheelAngle(t);
                    sword.fly(
                            handler.getPositionVector()
                                    .addVector(Math.cos(a) * 9, -1, Math.sin(a) * 9),
                            t < 60 ? .7 : 1.8);
                    sword.face(sword.pupil().add(sword.pupil().subtract(handler.pupil())));
                    if (t > 90) sword.bladeContact(12);
                    if (t >= 70 && t % 24 == 0)
                        for (int i = 0; i < 4; i++) {
                            double b = a + i * Math.PI / 2;
                            ray(
                                    handler.pupil(),
                                    handler.pupil()
                                            .addVector(Math.cos(b) * 24, 0, Math.sin(b) * 24),
                                    18,
                                    8,
                                    8);
                        }
                }
                lunge(sword, target, t, 204, 18);
                if (t >= 220 && t < 234) {
                    sword.rotationYaw += (t - 227) * 10;
                    sword.renderYawOffset = sword.rotationYawHead = sword.rotationYaw;
                    sword.bladeContact(13);
                }
                break;
            case SeerObserverPattern.RICOCHET:
                sword.fly(origin.addVector(-5, 3, 0), .45);
                handler.fly(origin.addVector(7, 7, -5), .45);
                for (int frame : new int[] {38, 76, 114}) {
                    if (t == frame - 18) ray(handler.pupil(), sword.pupil(), 18, 6, 0);
                    if (t == frame) {
                        ray(
                                sword.pupil(),
                                target.getPositionVector().addVector(0, 1, 0),
                                12,
                                8,
                                11);
                        world.playSound(
                                null,
                                sword.posX,
                                sword.posY,
                                sword.posZ,
                                SoundEvents.BLOCK_ANVIL_LAND,
                                SoundCategory.HOSTILE,
                                .6F,
                                1.6F);
                    }
                }
                if (t >= 148 && t < 188 && t % 5 == 0) {
                    double angle = (t - 168) * .025;
                    Vec3d d = target.getPositionVector().subtract(sword.pupil()).normalize();
                    Vec3d end =
                            sword.pupil()
                                    .addVector(
                                            (d.x * Math.cos(angle) - d.z * Math.sin(angle)) * 30,
                                            d.y * 30,
                                            (d.x * Math.sin(angle) + d.z * Math.cos(angle)) * 30);
                    ray(handler.pupil(), sword.pupil(), 0, 6, 0);
                    ray(sword.pupil(), end, 10, 6, 14);
                }
                break;
            case SeerObserverPattern.SLINGSHOT:
                for (int start : new int[] {42, 86, 130, 174}) {
                    if (t >= start - 32 && t < start - 14) {
                        Vec3d offset =
                                start == 42
                                        ? new Vec3d(-12, 3, 0)
                                        : start == 86
                                                ? new Vec3d(12, 3, 0)
                                                : start == 130
                                                        ? new Vec3d(-9, 7, 9)
                                                        : new Vec3d(0, 17, 0);
                        Vec3d anchor = target.getPositionVector().add(offset);
                        handler.fly(anchor.add(offset.normalize().scale(5)), 1.5);
                        sword.fly(anchor, 1.5);
                    }
                    lunge(sword, target, t, start, 16);
                }
                break;
            case SeerObserverPattern.SYNCHRONIZATION:
                if (t < 38) {
                    sword.fly(origin.addVector(-8, 3, 0), .5);
                    handler.fly(sword.getPositionVector().addVector(-5, 0, 0), .7);
                }
                if (t >= 38 && t < 260 && t % 8 == 0) ray(handler.pupil(), sword.pupil(), 0, 9, 0);
                if (t >= 38 && t < 200)
                    handler.fly(
                            sword.getPositionVector().subtract(sword.getLookVec().scale(5)), 1.3);
                lunge(sword, target, t, 64, 20);
                lunge(sword, target, t, 124, 20);
                if (t >= 90 && t < 110) {
                    sword.rotationYaw += (t - 100) * 8;
                    sword.renderYawOffset = sword.rotationYawHead = sword.rotationYaw;
                    sword.bladeContact(18);
                }
                if (t == 154)
                    ray(handler.pupil(), target.getPositionVector().addVector(0, 1, 0), 26, 16, 15);
                if (t == 180)
                    ray(sword.pupil(), target.getPositionVector().addVector(0, 1, 0), 18, 14, 16);
                if (t >= 200 && t < 260) {
                    double a = (t - 200) * .12;
                    sword.fly(
                            handler.getPositionVector()
                                    .addVector(Math.cos(a) * 8, 0, Math.sin(a) * 8),
                            1.5);
                    sword.bladeContact(12);
                }
                lunge(sword, target, t, 276, 18);
                if (t > 296) {
                    sword.motionX = sword.motionY = sword.motionZ = 0;
                    handler.motionX = handler.motionY = handler.motionZ = 0;
                }
                break;
            case SeerObserverPattern.OBSERVER_ALONE:
                if (t == 25 || t == 80)
                    for (int i = -2; i <= 2; i++) {
                        Vec3d p = target.getPositionVector().addVector(i * 3, 1, 0);
                        ray(p.addVector(0, 0, -15), p.addVector(0, 0, 15), 30, 14, 10);
                    }
                if (t >= 100 && t < 150 && t % 12 == 0) {
                    double a = (t - 100) * .08;
                    ray(
                            pupil(),
                            pupil().addVector(Math.cos(a) * 25, -3, Math.sin(a) * 25),
                            18,
                            10,
                            10);
                }
                if (t == 156)
                    ray(pupil(), target.getPositionVector().addVector(0, 1, 0), 32, 16, 18);
                lunge(this, target, t, 202, 14);
                break;
            default:
                break;
        }
    }

    private void advanced(
            int attack,
            int t,
            EntityLivingBase target,
            EntityWulfrumEye sword,
            EntityWulfrumEye handler) {
        switch (attack) {
            case SeerObserverPattern.SHARD_WHEEL:
                {
                    handler.fly(origin.addVector(0, 5, 0), .65);
                    double a = SeerObserverPattern.wheelAngle(t) * 1.35;
                    if (t < 144) {
                        sword.fly(
                                handler.getPositionVector()
                                        .addVector(Math.cos(a) * 10, -2, Math.sin(a) * 10),
                                t < 32 ? .6 : 2.5);
                        sword.face(sword.pupil().addVector(-Math.sin(a) * 4, 0, Math.cos(a) * 4));
                        if (t >= 40) bodyContact(sword, 9);
                        if (t >= 40 && t < 136 && t % 4 == 0) {
                            int ordinal = (t - 40) / 4;
                            shard(
                                    sword.pupil(),
                                    Vec3d.ZERO,
                                    0,
                                    SeerObserverPattern.shardRelease(ordinal) - t,
                                    target,
                                    5);
                        }
                    } else {
                        sword.fly(origin.addVector(-9, 5, 0), .55);
                        handler.fly(origin.addVector(9, 5, 0), .55);
                    }
                    break;
                }
            case SeerObserverPattern.ZIGZAG:
                {
                    if (t < 32) sword.fly(route(0, false), 1);
                    if (t == 28)
                        for (int delay : new int[] {6, 12, 18})
                            world.spawnEntity(new EntityWulfrumEcho(sword, delay, 212, false));
                    for (int pass = 0; pass < 2; pass++) {
                        int begin = 32 + pass * 100;
                        boolean inverted = pass == 1;
                        if (t == begin - 24)
                            for (int i = 0; i < 6; i++)
                                ray(
                                        route(inverted ? 6 - i : i, inverted).addVector(0, 1.5, 0),
                                        route(inverted ? 5 - i : i + 1, inverted)
                                                .addVector(0, 1.5, 0),
                                        24,
                                        1,
                                        0);
                        if (t >= begin && t < begin + 84) {
                            int line = (t - begin) / 14,
                                    local = (t - begin) % 14,
                                    index = inverted ? 5 - line : line + 1;
                            Vec3d end = route(index, inverted);
                            if (local < 9) {
                                sword.face(end.addVector(0, 1.5, 0));
                                sword.fly(end, 3.4);
                                bodyContact(sword, 8);
                            } else {
                                sword.motionX *= .35;
                                sword.motionY *= .35;
                                sword.motionZ *= .35;
                            }
                            if (local == 9) radial(sword.pupil(), target, 6, 4);
                        }
                        if (pass == 1 && t >= 116 && t < 132) sword.fly(route(6, true), 1.2);
                    }
                    if (t >= 216 && t < 240)
                        sword.fly(handler.getPositionVector().addVector(-3, 0, 0), 1.4);
                    if (t == 240) {
                        world.playSound(
                                null,
                                posX,
                                posY,
                                posZ,
                                SoundEvents.BLOCK_ANVIL_LAND,
                                SoundCategory.HOSTILE,
                                .6F,
                                1.2F);
                        radial(handler.pupil(), target, 6, 4);
                    }
                    if (t >= 248 && t < 304) {
                        handler.rotationYaw = (t - 248) * 18;
                        handler.renderYawOffset = handler.rotationYawHead = handler.rotationYaw;
                        if (t % 12 == 8) {
                            double a = (t - 248) * .16;
                            for (int i = 0; i < 3; i++) {
                                double b = a + i * Math.PI * 2 / 3;
                                ray(
                                        handler.pupil(),
                                        handler.pupil()
                                                .addVector(Math.cos(b) * 24, -4, Math.sin(b) * 24),
                                        18,
                                        5,
                                        6);
                            }
                        }
                    }
                    break;
                }
            case SeerObserverPattern.DOUBLE_PENDULUM:
                {
                    sword.fly(origin.addVector(0, 7, 0), .6);
                    double[] p = SeerObserverPattern.pendulum(t);
                    handler.fly(
                            sword.getPositionVector().addVector(p[0], p[1] - 7, p[2]),
                            t < 32 ? .6 : 2.2);
                    if (t == 32)
                        for (int delay : new int[] {10, 20, 30})
                            world.spawnEntity(new EntityWulfrumEcho(handler, delay, 164, true));
                    if (t >= 48 && t < 192 && t % 24 == 0) {
                        double angle = t * .11;
                        Vec3d d =
                                target.getPositionVector()
                                        .addVector(0, 1, 0)
                                        .subtract(handler.pupil())
                                        .normalize();
                        ray(
                                handler.pupil(),
                                handler.pupil()
                                        .addVector(
                                                d.x * 24 + Math.sin(angle) * 5,
                                                d.y * 24,
                                                d.z * 24 + Math.cos(angle) * 5),
                                22,
                                6,
                                7);
                    }
                    if (t >= 48 && t < 192) bodyContact(handler, 7);
                    break;
                }
            case SeerObserverPattern.CRASH_FLAIL:
                {
                    if (t < 42) {
                        handler.fly(origin.addVector(0, 4, -13), .75);
                        sword.fly(handler.getPositionVector().addVector(0, 2, -7), .85);
                    }
                    lunge(handler, target, t, 60, 10);
                    lunge(handler, target, t, 98, 10);
                    if (t >= 42 && t < 112) {
                        Vec3d behind =
                                new Vec3d(handler.motionX, handler.motionY, handler.motionZ)
                                        .normalize()
                                        .scale(-7);
                        sword.fly(
                                handler.getPositionVector()
                                        .add(behind)
                                        .addVector(Math.sin(t * .15) * 4, Math.cos(t * .11) * 2, 0),
                                1.8);
                        if (t >= 60) bodyContact(sword, 9);
                    }
                    if (t >= 112 && t < 146) handler.fly(origin.addVector(0, 5, 0), .65);
                    if (t >= 112 && t < 140)
                        sword.fly(handler.getPositionVector().addVector(0, 11, 0), 1.2);
                    if (t >= 140 && t < 146) sword.fly(handler.getPositionVector(), 2.2);
                    if (t >= 146 && t < 174) {
                        handler.fly(origin.addVector(0, .2, 0), 1.5);
                        sword.fly(origin.addVector(-6, 4, 0), 1);
                        if (t < 155) bodyContact(handler, 10);
                    }
                    if (t == 154) {
                        radial(handler.pupil(), target, 8, 5);
                        world.playSound(
                                null,
                                handler.posX,
                                handler.posY,
                                handler.posZ,
                                SoundEvents.BLOCK_ANVIL_LAND,
                                SoundCategory.HOSTILE,
                                .8F,
                                .6F);
                    }
                    if (t >= 174) {
                        handler.fly(origin.addVector(0, 4, 0), .23);
                        if (t == 180 || t == 198 || t == 216) {
                            Vec3d d =
                                    target.getPositionVector()
                                            .subtract(handler.pupil())
                                            .normalize();
                            for (int i = -4; i <= 4; i++) {
                                double a = i * .15;
                                Vec3d v =
                                        new Vec3d(
                                                (d.x * Math.cos(a) - d.z * Math.sin(a)) * .62,
                                                .35 + Math.abs(i) * .025,
                                                (d.x * Math.sin(a) + d.z * Math.cos(a)) * .62);
                                shard(handler.pupil(), v, 1, 0, target, 5);
                            }
                        }
                    }
                    break;
                }
            case SeerObserverPattern.SAW_ORBIT:
                {
                    if (t < 30) sword.fly(origin.addVector(-9, 2, 0), .6);
                    if (t >= 30 && t < 122) {
                        double a = (t - 30) * .09;
                        sword.fly(origin.addVector(Math.cos(a) * 7, 1.2, Math.sin(a) * 7), 1.1);
                        sword.face(origin.addVector(0, 1, 0));
                        bodyContact(sword, 8);
                        if (t % 18 == 0) shard(sword.pupil(), Vec3d.ZERO, 0, 28, target, 4);
                    }
                    if (t >= 122) sword.fly(origin.addVector(-9, 4, 0), .45);
                    break;
                }
            case SeerObserverPattern.SAW_DIVE:
                if (t < 26) sword.fly(target.getPositionVector().addVector(-7, 10, 0), .8);
                lunge(sword, target, t, 48, 11);
                if (t >= 64 && t < 86) sword.fly(target.getPositionVector().addVector(7, 9, 0), .8);
                lunge(sword, target, t, 108, 11);
                if (t == 60 || t == 120) radial(sword.pupil(), target, 8, 4);
                break;
            case SeerObserverPattern.MACHINEGUN:
                if (handler == null) break;
                handler.fly(origin.addVector(0, 5, -12), .35);
                if (t == 24 || t == 76 || t == 128) lock(target);
                if ((t >= 36 && t < 58 || t >= 88 && t < 110 || t >= 140 && t < 162)
                        && t % 6 == 0) {
                    double sweep = Math.sin((t % 52) * .17) * 3;
                    Vec3d muzzle = handler.pupil().add(handler.getLookVec().scale(3.4 * 8 / 7));
                    ray(muzzle, aim.addVector(sweep, 0, 0), 12, 3, 4);
                }
                if (sword != null) {
                    if (t < 100) sword.fly(origin.addVector(-9, 4, 2), .35);
                    lunge(sword, target, t, 124, 9);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (introTick() > 0 || survivorTick() > 0) return false;
        if (source.isFireDamage()
                || source == DamageSource.FALL
                || source.getTrueSource() instanceof EntityWulfrumEye) return false;
        if (!world.isRemote
                && attack() == 41
                && attackTick() >= 115
                && attackTick() < 150
                && amount > 0
                && source.getTrueSource() instanceof EntityLivingBase) survivorScore.punish();
        if (overclocked()
                && WulfrumSurvivorScore.parrying(attack(), attackTick())
                && source.getTrueSource() instanceof EntityLivingBase
                && parryCooldown == 0) {
            parryCooldown = 25;
            if (!world.isRemote) {
                survivorScore.counter();
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ITEM_SHIELD_BLOCK,
                        SoundCategory.HOSTILE,
                        1,
                        1.3F);
            }
            return false;
        }
        if (seer()
                && !upgraded()
                && SeerObserverPattern.guard(attack(), attackTick())
                && parryCooldown == 0
                && source.getImmediateSource() == source.getTrueSource()
                && source.getTrueSource() instanceof EntityLivingBase) {
            parryCooldown = 50;
            EntityLivingBase attacker = (EntityLivingBase) source.getTrueSource();
            face(attacker.getPositionVector().addVector(0, 1, 0));
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    SoundEvents.ITEM_SHIELD_BLOCK,
                    SoundCategory.HOSTILE,
                    1,
                    1.4F);
            if (!world.isRemote) {
                counterDirection =
                        attacker.getPositionVector()
                                .addVector(0, 1, 0)
                                .subtract(pupil())
                                .normalize();
                dataManager.set(COUNTER, 1);
                ray(pupil(), pupil().add(counterDirection.scale(12)), 18, 1, 0);
            }
            return false;
        }
        if (transformTick() > 0) amount *= .6F;
        if (attackTick() > SeerObserverPattern.duration(attack()) - 28) amount *= 1.2F;
        if (attack() == SeerObserverPattern.SYNCHRONIZATION && attackTick() > 296) amount *= 1.35F;
        if (energyShield() > 0 && !world.isRemote) {
            float absorbed = Math.min(amount, energyShield());
            dataManager.set(SHIELD, energyShield() - absorbed);
            amount -= absorbed;
            if (amount <= 0) return true;
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!world.isRemote) world.spawnEntity(new EntityWulfrumNova(this));
        EntityWulfrumEye other = partner();
        experienceValue = other == null ? 200 : 0;
        bar.setVisible(false);
        if (other != null) {
            other.partnerDefeated = true;
            other.dataManager.set(TIME, 0);
            other.dataManager.set(COUNTER, 0);
            other.dataManager.set(SOLO, true);
            other.beginSurvivor();
        }
        super.onDeath(source);
        if (!world.isRemote
                && other == null
                && partnerDefeated
                && world.getGameRules().getBoolean("doMobLoot")) {
            if (expert) {
                dropItem(com.scapeandrun.frostbite.registry.MachineRewards.SEER_BAG, 1);
                return;
            }
            WulfrumEncounterLoot reward = WulfrumEncounterLoot.roll(rand, expert);
            entityDropItem(
                    new net.minecraft.item.ItemStack(
                            com.scapeandrun.frostbite.expedition.WulfrumArsenal.SCRAP,
                            reward.scrap),
                    0);
            entityDropItem(
                    new net.minecraft.item.ItemStack(
                            com.scapeandrun.frostbite.expedition.ExpeditionContent.CORE,
                            reward.cores),
                    0);
            entityDropItem(
                    com.scapeandrun.frostbite.expedition.WulfrumArsenal.charged(
                            com.scapeandrun.frostbite.expedition.WulfrumArsenal.AWL),
                    0);
            entityDropItem(
                    com.scapeandrun.frostbite.expedition.WulfrumArsenal.charged(
                            com.scapeandrun.frostbite.expedition.WulfrumArsenal.LITTLE_BOY),
                    0);
            if (reward.heart)
                entityDropItem(
                        new net.minecraft.item.ItemStack(
                                com.scapeandrun.frostbite.expedition.WulfrumArsenal.HEART),
                        0);
        }
    }

    @Override
    public void setDead() {
        bar.setVisible(false);
        super.setDead();
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        bar.setVisible(!seer() || partnerDefeated);
        bar.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bar.removePlayer(p);
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
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setInteger("Survivor", survivorTick());
        n.setBoolean("Overclocked", overclocked());
        n.setFloat("WulfrumShield", energyShield());
        n.setBoolean("Paired", paired);
        if (partnerId != null) n.setUniqueId("Partner", partnerId);
        n.setBoolean("Balanced", balanced);
        n.setBoolean("Expert", expert);
        n.setInteger("Cursor", cursor);
        n.setBoolean("Upgraded", upgraded());
        n.setBoolean("PartnerDefeated", partnerDefeated);
        n.setInteger("Intro", introTick());
        n.setDouble("EntranceX", entrance.x);
        n.setDouble("EntranceY", entrance.y);
        n.setDouble("EntranceZ", entrance.z);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dataManager.set(SURVIVOR, Math.max(0, Math.min(160, n.getInteger("Survivor"))));
        dataManager.set(OVERCLOCKED, n.getBoolean("Overclocked"));
        dataManager.set(SHIELD, Math.max(0, Math.min(50, n.getFloat("WulfrumShield"))));
        paired = n.getBoolean("Paired");
        partnerId = n.hasUniqueId("Partner") ? n.getUniqueId("Partner") : null;
        balanced = n.getBoolean("Balanced");
        expert = n.getBoolean("Expert");
        cursor = n.getInteger("Cursor");
        dataManager.set(UPGRADED, n.getBoolean("Upgraded"));
        partnerDefeated = n.getBoolean("PartnerDefeated");
        dataManager.set(INTRO, Math.min(SeerObserverIntro.END, Math.max(0, n.getInteger("Intro"))));
        entrance =
                new Vec3d(
                        n.getDouble("EntranceX"),
                        n.getDouble("EntranceY"),
                        n.getDouble("EntranceZ"));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(AnimationData data) {}
}
