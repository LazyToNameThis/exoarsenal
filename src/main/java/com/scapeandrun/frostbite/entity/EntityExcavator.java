package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.world.ExcavatorTerrain;
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
import java.util.*;
import static com.scapeandrun.frostbite.entity.ExcavatorClassicScore.*;

public final class EntityExcavator extends EntityMob {
    private static final DataParameter<Integer>
            ATTACK = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            TIME = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            TRANSITION = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> DEEP =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> EXPERT =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> OVERBORE_SHIELD =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer>
            EXPERT_ATTACK =
                    EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            EXPERT_STAGE =
                    EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private static final DataParameter<Float> RATE =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> DRILL_ANGLE =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> PARRY_COUNT =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private final ExcavatorExpertFight expertFight = new ExcavatorExpertFight();
    private static final DataParameter<Integer> INTRO =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private boolean introDone;
    private static final DataParameter<Integer>
            FINALE = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            FINALE_TIME =
                    EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            BROKEN = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            MASH = EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private Vec3d finaleOrigin = Vec3d.ZERO, finaleDirection = new Vec3d(1, 0, 0);
    private Vec3d finaleRetreatStart = Vec3d.ZERO;
    private DamageSource finaleSource = DamageSource.GENERIC;
    private long lastMash = -100;
    private static final DataParameter<Integer> OPTIONAL_RECOIL =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);
    private Vec3d parryOrigin = Vec3d.ZERO, parryDirection = new Vec3d(1, 0, 0);

    public int optionalRecoil() {
        return dataManager.get(OPTIONAL_RECOIL);
    }

    private static final DataParameter<Boolean> COORDINATION_PARRY =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.BOOLEAN);

    {
        dataManager.register(COORDINATION_PARRY, false);
    }

    void coordinationParry(boolean value) {
        dataManager.set(COORDINATION_PARRY, value);
    }

    private static final DataParameter<Boolean> COORDINATION_STALL =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.BOOLEAN);

    {
        dataManager.register(COORDINATION_STALL, false);
    }

    void coordinationStall(boolean value) {
        dataManager.set(COORDINATION_STALL, value);
    }

    private static final DataParameter<Integer>
            COORDINATION_POSE =
                    EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT),
            COORDINATION_TIME =
                    EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);

    {
        dataManager.register(COORDINATION_POSE, -1);
        dataManager.register(COORDINATION_TIME, 0);
    }

    void coordinationPose(int pattern, int time) {
        dataManager.set(COORDINATION_POSE, pattern);
        dataManager.set(COORDINATION_TIME, time);
    }

    public ExcavatorActuation actuation(float partial) {
        int pattern = dataManager.get(COORDINATION_POSE);
        return pattern >= 0
                ? ExcavatorActuation.coordinated(
                        WulfrumCoordinationScore.Pattern.values()[pattern],
                        dataManager.get(COORDINATION_TIME) + partial,
                        dataManager.get(COORDINATION_STALL))
                : expert()
                        ? ExcavatorActuation.expert(
                                expertAttack(), expertStage(), attackTick() + partial)
                        : ExcavatorActuation.classic(attack(), attackTick() + partial);
    }

    public boolean chargeParryWindow() {
        if (dataManager.get(COORDINATION_PARRY)) return true;
        if (expert()
                || finale() > 0
                || introTick() != 0
                || transition() > 0
                || optionalRecoil() > 0) return false;
        int t = attackTick();
        switch (attack()) {
            case TURRET_SWEEP:
                return t >= 185 && t < 213;
            case FAULT_LINE:
                return t >= 130 && t < 164;
            case BORE_RICOCHET:
                return t >= 40 && t < 135;
            case FINAL_CHARGE:
                return t >= 110 && t < 140;
            default:
                return false;
        }
    }

    public int finale() {
        return dataManager.get(FINALE);
    }

    public int finaleTick() {
        return dataManager.get(FINALE_TIME);
    }

    public int brokenSegments() {
        return dataManager.get(BROKEN);
    }

    public int mash() {
        return dataManager.get(MASH);
    }

    public int introTick() {
        return dataManager.get(INTRO);
    }

    void prepareTrialEntrance() {
        introDone = true;
        initialized = false;
        dataManager.set(INTRO, -1);
        getEntityData().setBoolean("TrialOpeningPending", true);
    }

    public void prepareEntrance() {
        if (world.isRemote || introDone || introTick() != 0) return;
        center = ground(getPositionVector());
        dataManager.set(INTRO, 1);
        setPosition(center.x - 60, center.y - 7, center.z);
        prevPosX = lastTickPosX = posX;
        prevPosY = lastTickPosY = posY;
        prevPosZ = lastTickPosZ = posZ;
        spine.reset(getPositionVector().addVector(0, 1.5, 0));
    }

    boolean finishOpening(EntityPlayer p) {
        if (introTick() != -1) return false;
        dataManager.set(INTRO, 0);
        if (!dataManager.get(EXPERT)) {
            dataManager.set(EXPERT_STAGE, 0);
            dataManager.set(EXPERT_ATTACK, -1);
            dataManager.set(RATE, 1F);
            begin(p);
            return true;
        }
        return false;
    }

    private boolean difficultyLoaded;
    private final ExcavatorSpine spine = new ExcavatorSpine();
    private final EntityExcavatorSegment[] body = new EntityExcavatorSegment[ExcavatorSpine.COUNT];
    private final EntityExcavatorProbe[] probes = new EntityExcavatorProbe[8];
    private static final DataParameter<Integer> PROBES_OUT =
            EntityDataManager.createKey(EntityExcavator.class, DataSerializers.VARINT);

    public boolean probeOut(int index) {
        return (dataManager.get(PROBES_OUT) & (1 << index)) != 0;
    }

    private final EntityExcavatorPayload[] chunks = new EntityExcavatorPayload[3];
    private final List<EntityExcavatorPayload> hazards = new ArrayList<>();
    private final List<Vec3d> trench = new ArrayList<>();
    private final Vec3d[] sites = new Vec3d[6];
    private final BossInfoServer bar =
            new BossInfoServer(
                    new TextComponentString("X-04 \"Excavator\""),
                    BossInfo.Color.GREEN,
                    BossInfo.Overlay.PROGRESS);
    private Vec3d center = Vec3d.ZERO,
            locked = Vec3d.ZERO,
            velocity = Vec3d.ZERO,
            goal = Vec3d.ZERO,
            heading = new Vec3d(1, 0, 0),
            facingOverride;
    private int cursor,
            absent,
            mode,
            redirects,
            processingSite,
            processingStart,
            redirectCooldown,
            pendingChunk;
    private boolean initialized, embedded, coreFired;
    private List<Cue> score = Collections.emptyList();

    public EntityExcavator(World w) {
        super(w);
        dataManager.register(FINALE, 0);
        dataManager.register(FINALE_TIME, 0);
        dataManager.register(BROKEN, 0);
        dataManager.register(MASH, 0);
        dataManager.register(INTRO, 0);
        dataManager.register(PROBES_OUT, 0);
        setSize(3, 3);
        setNoAI(true);
        noClip = true;
        setNoGravity(true);
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        experienceValue = 180;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, 0);
        dataManager.register(TIME, 0);
        dataManager.register(TRANSITION, 0);
        dataManager.register(DEEP, false);
        dataManager.register(EXPERT, false);
        dataManager.register(OVERBORE_SHIELD, false);
        dataManager.register(EXPERT_ATTACK, -1);
        dataManager.register(EXPERT_STAGE, 0);
        dataManager.register(RATE, 1F);
        dataManager.register(PARRY_COUNT, 0);
        dataManager.register(DRILL_ANGLE, 0F);
        dataManager.register(OPTIONAL_RECOIL, 0);
    }

    public int parryCount() {
        return dataManager.get(PARRY_COUNT);
    }

    void parryCount(int count) {
        dataManager.set(PARRY_COUNT, count);
    }

    public boolean overboreShield() {
        return dataManager.get(OVERBORE_SHIELD);
    }

    void overboreShield(boolean active) {
        dataManager.set(OVERBORE_SHIELD, active);
    }

    public boolean expert() {
        return dataManager.get(EXPERT) || introTick() == -1;
    }

    public int expertAttack() {
        return dataManager.get(EXPERT_ATTACK);
    }

    public int expertStage() {
        return dataManager.get(EXPERT_STAGE);
    }

    public float encounterRate() {
        return dataManager.get(RATE);
    }

    private float drillSpeed() {
        if (dataManager.get(COORDINATION_STALL)) return ticksExisted % 16 < 3 ? 3 : 0;
        if (optionalRecoil() > 0) return optionalRecoil() < 26 ? 6 : 0;
        float rpm = deep() ? 38 : 20;
        if (expert()) {
            if (expertAttack() == ExcavatorExpertScore.FEEDBACK && expertStage() == 0) rpm = 6;
            if (expertStage() == 1) rpm = Math.max(2, Math.min(48, (attackTick() - 25) * .9F));
            if (expertStage() == 6 || expertStage() == 11) rpm = attackTick() % 30 < 3 ? 2 : 0;
        } else {
            if (attack() == Attack.INDUSTRIAL_PROCESSING && attackTick() >= 300)
                rpm = attackTick() >= 355 && attackTick() < 365 ? 8 : attackTick() % 20 < 2 ? 1 : 0;
            if (failing()) rpm *= .7F + .3F * (float) Math.sin(ticksExisted * .24);
        }
        return rpm;
    }

    public float drillRotation(float partial) {
        return dataManager.get(DRILL_ANGLE)
                + partial
                        * (finale() > 0
                                ? (finale() == 2 ? 90 : 15)
                                : drillSpeed() * encounterRate());
    }

    void expertState(int attack, int stage, int tick, double rate) {
        dataManager.set(EXPERT_ATTACK, attack);
        dataManager.set(EXPERT_STAGE, stage);
        dataManager.set(TIME, tick);
        dataManager.set(RATE, (float) rate);
    }

    void drive(Vec3d v) {
        velocity = v;
    }

    void guide(Vec3d p, double speed) {
        steer(p, speed);
    }

    Vec3d surface(Vec3d p) {
        return ground(p);
    }

    void brake() {
        velocity = velocity.scale(.78);
    }

    void face(Vec3d direction) {
        facingOverride = direction;
    }

    void deploy(int count) {
        launchProbes(count);
    }

    EntityExcavatorProbe probe(int i) {
        return probes[i];
    }

    void dock() {
        recall();
    }

    EntityExcavatorPayload lift(Vec3d p, float radius, int life) {
        EntityExcavatorPayload piece = chunk(p, radius, life);
        cutDisc(p, (int) radius);
        return piece;
    }

    void bulge(Vec3d p, float radius, int life, double height) {
        EntityExcavatorPayload piece = chunk(p, radius, life);
        piece.bulge(height);
    }

    List<EntityExcavatorPayload> liftSectionedIsland(Vec3d p) {
        List<EntityExcavatorPayload> pieces = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            EntityExcavatorPayload piece = chunk(p, 10, 240);
            piece.section(i);
            pieces.add(piece);
        }
        cutDisc(p, 10);
        return pieces;
    }

    void fault(Vec3d a, Vec3d b, int delay, float damage) {
        spawn(new EntityExcavatorPayload(this, 3, a, b, delay, delay + 16, .3F, damage));
    }

    void clearEffects() {
        for (EntityExcavatorPayload h : hazards) h.setDead();
        hazards.clear();
    }

    public void onExpertParry(EntityPlayer player) {
        if (DraedonCollaboration.parried(this, player)) return;
        if (expert()) expertFight.parried(this, player);
    }

    public boolean tryTutorialParry(EntityPlayer player) {
        if (!world.isRemote && chargeParryWindow() && mode == 8 && !embedded) {
            Vec3d contact = player.getPositionVector().addVector(0, 1, 0),
                    toward = drillTip().subtract(contact);
            if (toward.lengthSquared() < 30
                    && player.getLookVec().dotProduct(toward.normalize()) > .15
                    && velocity.dotProduct(contact.subtract(getPositionVector())) > 0) {
                parryOrigin = getPositionVector();
                parryDirection = new Vec3d(heading.x, 0, heading.z).normalize();
                dataManager.set(OPTIONAL_RECOIL, 1);
                velocity = Vec3d.ZERO;
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.HOSTILE,
                        1.5F,
                        1.5F);
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.CRIT,
                                contact.x,
                                contact.y,
                                contact.z,
                                28,
                                .3,
                                .3,
                                .3,
                                .25);
                return true;
            }
        }
        if (!world.isRemote
                && finale() == 4
                && player == getAttackTarget()
                && getDistanceSq(player) < 144) {
            long now = world.getTotalWorldTime();
            if (now - lastMash >= 3) {
                lastMash = now;
                dataManager.set(MASH, Math.min(24, mash() + 1));
            }
            return true;
        }
        return !world.isRemote && finale() == 0 && expertFight.tutorialParry(this, player);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(720);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    public Attack attack() {
        return Attack.values()[MathHelper.clamp(dataManager.get(ATTACK), 0, 10)];
    }

    public int attackTick() {
        return dataManager.get(TIME);
    }

    public boolean deep() {
        return dataManager.get(DEEP);
    }

    public int transition() {
        return dataManager.get(TRANSITION);
    }

    public boolean failing() {
        return ExcavatorClassicScore.failing(getHealth() / getMaxHealth());
    }

    public Vec3d segment(int i, float partial) {
        return spine.point(MathHelper.clamp(i, 0, 17), partial);
    }

    EntityExcavatorProbe coordinationProbe(int index) {
        if (index < 0 || index >= probes.length)
            throw new IllegalArgumentException("Invalid probe socket");
        if (!alive(probes[index])) {
            probes[index] = new EntityExcavatorProbe(this, index);
            Vec3d at = segment(ExcavatorProbeScore.socketSegment(index), 1);
            probes[index].setPosition(at.x, at.y, at.z);
            world.spawnEntity(probes[index]);
        }
        return probes[index];
    }

    private void updateSpine() {
        Vec3d head = getPositionVector().addVector(0, 1.5, 0);
        if (dataManager.get(COORDINATION_POSE)
                        == WulfrumCoordinationScore.Pattern.attack_52.ordinal()
                && dataManager.get(COORDINATION_TIME) <= 245) {
            spine.eclipse(
                    head,
                    WulfrumCoordinationGeometry.alignedAngle(
                            dataManager.get(COORDINATION_TIME), .045, Math.PI / 2));
            return;
        }
        double yaw = Math.toRadians(rotationYaw), pitch = Math.toRadians(rotationPitch);
        Vec3d forward =
                new Vec3d(
                        Math.sin(yaw) * Math.cos(pitch),
                        -Math.sin(pitch),
                        Math.cos(yaw) * Math.cos(pitch));
        if (expert()
                && parryCount() > 0
                && (expertStage() == 4 && attackTick() >= 16 || expertStage() == 6))
            spine.coil(
                    head,
                    new Vec3d(forward.x, 0, forward.z).normalize(),
                    expertStage() == 4
                            ? Math.min(1, (attackTick() - 16) / 12D)
                            : Math.max(0, 1 - (attackTick() - 15) / 25D));
        else if (expert()
                && expertAttack() == ExcavatorExpertScore.ACCIDENT
                && expertStage() == 0
                && attackTick() > 75)
            spine.coil(head, forward, Math.min(1, (attackTick() - 75) / 25D));
        else if (expert() && (expertStage() == 3 || expertStage() == 4))
            spine.impact(
                    head,
                    forward,
                    attackTick() + (expertStage() == 4 ? ExcavatorParryMotion.HITSTOP : 0));
        else spine.update(head);
    }

    public Vec3d drillTip() {
        double y = Math.toRadians(rotationYaw), p = Math.toRadians(rotationPitch);
        ExcavatorActuation pose =
                expert()
                        ? ExcavatorActuation.expert(expertAttack(), expertStage(), attackTick())
                        : ExcavatorActuation.classic(attack(), attackTick());
        double length =
                3.6
                        + pose.drill * .1
                        + (expert()
                                ? ExcavatorParryMotion.drillExtension(
                                                deep(), expertStage(), attackTick())
                                        * .1
                                : 0);
        return getPositionVector()
                .addVector(
                        Math.sin(y) * Math.cos(p) * length,
                        1.5 - Math.sin(p) * length,
                        Math.cos(y) * Math.cos(p) * length);
    }

    public Vec3d turret(int index) {
        EntityExcavatorSegment part = body[2 + Math.floorMod(index, 6) * 2];
        return part == null ? getPositionVector().addVector(0, 2, 0) : part.muzzle();
    }

    public void fireTurret(int index, Vec3d target, int warning, int life, float damage) {
        EntityExcavatorSegment part = body[2 + Math.floorMod(index, 6) * 2];
        if (part != null) {
            part.aim(target);
            part.firingIn(warning);
        }
        beam(turret(index), target, warning, life, damage);
    }

    Vec3d ground(Vec3d p) {
        BlockPos column = new BlockPos(p.x, 64, p.z);

        double height =
                world.isBlockLoaded(column)
                        ? world.getTopSolidOrLiquidBlock(column).getY()
                        : (center == Vec3d.ZERO ? posY : center.y);
        return new Vec3d(p.x, height, p.z);
    }

    private Vec3d circle(double angle, double radius, double y) {
        return center.addVector(Math.cos(angle) * radius, y, Math.sin(angle) * radius);
    }

    private void steer(Vec3d p, double speed) {
        Vec3d d = p.subtract(getPositionVector());
        Vec3d wanted = d.lengthVector() < speed ? d : d.normalize().scale(speed);
        velocity = velocity.scale(.55).add(wanted.scale(.45));
    }

    private void begin(EntityPlayer p) {
        center = ground(p.getPositionVector());
        locked = p.getPositionVector().addVector(0, 1, 0);
        Attack next = select(cursor++, getHealth() / getMaxHealth());
        dataManager.set(ATTACK, next.ordinal());
        dataManager.set(TIME, 0);
        score = timeline(next);
        mode = 0;
        embedded = false;
        coreFired = false;
        redirects = 0;
        trench.clear();
        goal = center;
        for (int i = 0; i < 6; i++) sites[i] = ground(circle(i * Math.PI / 3, 13, 0));
    }

    @Override
    public void onLivingUpdate() {
        if (EntityDraedon.arriving(this)) {
            updateSpine();
            return;
        }
        super.onLivingUpdate();
        noClip = true;
        motionX = motionY = motionZ = 0;
        if (world.isRemote) {
            updateSpine();
            return;
        }
        if (!isEntityAlive()) return;
        if (finale() > 0) {
            tickFinale();
            return;
        }
        int probesOut = 0;
        for (int i = 0; i < probes.length; i++) if (alive(probes[i])) probesOut |= 1 << i;
        dataManager.set(PROBES_OUT, probesOut);
        EntityPlayer player =
                getAttackTarget() instanceof EntityPlayer ? (EntityPlayer) getAttackTarget() : null;
        if (player == null
                || !player.isEntityAlive()
                || player.isCreative()
                || player.isSpectator()) player = world.getNearestAttackablePlayer(this, 128, 96);
        setAttackTarget(player);

        if (player == null && (!introDone || introTick() == -1))
            player = world.getClosestPlayer(posX, posY, posZ, 160, false);
        if (player == null) {
            if (++absent > 200) setDead();
            return;
        }
        absent = 0;
        dataManager.set(
                DRILL_ANGLE, (dataManager.get(DRILL_ANGLE) + drillSpeed() * encounterRate()) % 360);
        if (optionalRecoil() > 0) {
            int r = optionalRecoil();
            double u = Math.min(1, r / 26D);
            Vec3d at = parryOrigin.subtract(parryDirection.scale(20 * ExcavatorActuation.ease(u)));
            double floor = ground(at).y;
            at =
                    new Vec3d(
                            at.x,
                            parryOrigin.y
                                    + (floor - parryOrigin.y) * ExcavatorActuation.ease(u)
                                    + Math.sin(u * Math.PI) * 5,
                            at.z);
            setPosition(at.x, at.y, at.z);
            rotationPitch = (float) (-35 * Math.sin(u * Math.PI));
            spine.coil(
                    at.addVector(0, 1.5, 0),
                    parryDirection,
                    ExcavatorActuation.ease(r / 26D)
                            * (1 - ExcavatorActuation.ease((r - 42) / 14D)));
            velocityChanged = true;
            if (r == 26) parryLanding(player, ground(at), parryDirection);
            dataManager.set(OPTIONAL_RECOIL, r >= 56 ? 0 : r + 1);
            if (r >= 56) begin(player);
            return;
        }
        if (!difficultyLoaded) {
            dataManager.set(
                    EXPERT,
                    com.scapeandrun.frostbite.world.FrostbiteWorldSettings.get(world).isExpert());
            difficultyLoaded = true;
        }

        if (getEntityData().getBoolean("TrialOpeningPending")) {
            getEntityData().setBoolean("TrialOpeningPending", false);
            initialized = true;
            dataManager.set(INTRO, -1);
            expertFight.opening(this, player);
            return;
        }
        if (!introDone) {
            int t = introTick() + 1;
            dataManager.set(INTRO, t);
            if (t == 1) {
                center = ground(player.getPositionVector());
                setPosition(center.x - 60, center.y - 7, center.z);
                spine.reset(getPositionVector().addVector(0, 1.5, 0));
            }
            Vec3d
                    at =
                            center.addVector(
                                    ExcavatorEntranceMotion.x(t),
                                    ExcavatorEntranceMotion.y(t),
                                    ExcavatorEntranceMotion.z(t)),
                    delta = at.subtract(getPositionVector());
            setPosition(at.x, at.y, at.z);
            rotationYaw = (float) Math.toDegrees(Math.atan2(delta.x, delta.z));
            rotationPitch =
                    (float) -Math.toDegrees(Math.atan2(delta.y, Math.hypot(delta.x, delta.z)));
            updateSpine();
            for (int i = 1; i < body.length; i++)
                if (body[i] == null || body[i].isDead) {
                    body[i] = new EntityExcavatorSegment(this, i);
                    world.spawnEntity(body[i]);
                }
            if (t % 8 == 0 && at.y < ground(at).y + 3) dust(ground(at), 18);
            if (t == 18 || t == 78 || t == 140) {
                Vec3d impact = ground(at);
                spawn(new EntityExcavatorPayload(this, 1, impact, impact, 0, 12, .5F, 0));
                dust(impact, 40);
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.BLOCK_GRAVEL_BREAK,
                        SoundCategory.HOSTILE,
                        2,
                        .55F);
            }
            if (t >= 150) {
                introDone = true;
                initialized = true;
                dataManager.set(INTRO, -1);
                expertFight.opening(this, player);
            }
            return;
        }
        if (DraedonCollaboration.control(this)) {
            velocity = Vec3d.ZERO;
            updateSpine();
            contact();
            bar.setPercent(getHealth() / getMaxHealth());
            initialized = false;
            return;
        }
        if (!initialized) {
            initialized = true;
            spine.reset(getPositionVector().addVector(0, 1.5, 0));
            if (expert()) expertFight.begin(this, player, deep());
            else begin(player);
        }
        if (introTick() == 0 && !deep() && getHealth() <= getMaxHealth() * .5F) {
            dataManager.set(DEEP, true);
            dataManager.set(TRANSITION, 1);
            cursor = 0;
            recall();
        }
        if (transition() > 0) {
            int t = transition();
            dataManager.set(RATE, 1F);
            velocity = velocity.scale(.8);
            int length = expert() ? 160 : 100;
            if (expert()) {
                expertFight.transition(this, player, t);
            } else {
                if (t > 65) steer(center.addVector(0, -15, 0), 1.5);
                if (t == 80) shock(center, 12);
            }
            dataManager.set(TRANSITION, t >= length ? 0 : t + 1);
            if (t >= length) {
                if (expert()) expertFight.begin(this, player, true);
                else begin(player);
            }
        } else if (expert()) expertFight.tick(this, player);
        else {
            int t = attackTick() + 1;
            dataManager.set(TIME, t);
            for (Cue cue : score) if (cue.tick == t) execute(cue, player);
            movePattern(player, t);
            if (t >= attack().duration) begin(player);
        }
        Vec3d next = getPositionVector().add(velocity.scale(encounterRate()));
        setPosition(next.x, next.y, next.z);
        velocityChanged = true;
        Vec3d facing = facingOverride == null ? velocity : facingOverride;
        facingOverride = null;
        if (facing.lengthSquared() > .0001) {
            rotationYaw = (float) Math.toDegrees(Math.atan2(facing.x, facing.z));
            rotationPitch =
                    (float)
                            -Math.toDegrees(
                                    Math.atan2(
                                            facing.y,
                                            Math.sqrt(facing.x * facing.x + facing.z * facing.z)));
        }
        renderYawOffset = rotationYaw;
        updateSpine();
        for (int i = 1; i < body.length; i++)
            if (body[i] == null || body[i].isDead) {
                body[i] = new EntityExcavatorSegment(this, i);
                world.spawnEntity(body[i]);
            }
        hazards.removeIf(e -> e.isDead);
        if (transition() == 0 && (!expert() || !expertFight.parrySequence())) contact();
        bar.setPercent(getHealth() / getMaxHealth());
        if (expert() && ticksExisted % 100 == 0)
            ExcavatorTerrain.get(world).retain(getUniqueID(), world.getTotalWorldTime() + 600);
    }

    private void execute(Cue cue, EntityPlayer p) {
        Vec3d aim = p.getPositionVector().addVector(0, 1, 0);
        switch (cue.action) {
            case BURROW:
                mode = 1;
                break;
            case SURFACE_CRACK:
                warning(
                        ground(getPositionVector()),
                        ground(getPositionVector()).addVector(1, 0, 1),
                        20);
                break;
            case CIRCLE:
                mode = 2;
                break;
            case LOCK_TARGET:
                locked = aim;
                heading = locked.subtract(getPositionVector()).normalize();
                if (attack() == Attack.SEISMIC_BORE) mode = 1;
                break;
            case ERUPT:
                goal = locked.addVector(0, 16, 0);
                mode = 3;
                shock(ground(locked), 10);
                debris(ground(locked), 12);
                break;
            case DIVE:
                goal = ground(locked.addVector(12, 0, 0)).addVector(0, -8, 0);
                mode = 3;
                break;
            case SHOCKWAVE:
                shock(ground(getPositionVector()), 12);
                break;
            case TRENCH_STRAIGHT:
            case TRENCH_DIAGONAL:
            case TRENCH_CURVED:
                int pass =
                        cue.action == Action.TRENCH_STRAIGHT
                                ? 0
                                : cue.action == Action.TRENCH_DIAGONAL ? 1 : 2;
                heading = new Vec3d(Math.cos(pass * .8), 0, Math.sin(pass * .8));
                goal = center.subtract(heading.scale(23));
                mode = 10;
                trench.clear();
                break;
            case TRENCH_BURST:
                for (int i = 0; i < trench.size(); i += 3)
                    warning(
                            trench.get(i),
                            trench.get(Math.min(i + 3, trench.size() - 1)),
                            10 + i / 2);
                mode = 0;
                break;
            case LAUNCH_PROBES:
                launchProbes(cue.index);
                break;
            case SCAN:
                for (EntityExcavatorProbe probe : probes) if (alive(probe)) probe.scan();
                break;
            case LOCK_PROBES:
                for (EntityExcavatorProbe probe : probes) if (alive(probe)) probe.lock();
                break;
            case FIRE_RECORDED_SHOTS:
                if (alive(probes[cue.index])) probes[cue.index].fire(this);
                break;
            case RECALL_PROBES:
            case RECONNECT:
                recall();
                break;
            case LIFT_CHUNK:
                Vec3d at = ground(circle(cue.index * 2.1, 7, 0));
                chunks[cue.index] = chunk(at, 2, 230);
                cutDisc(at, 2);
                break;
            case AIM_CHUNK:
                if (alive(chunks[cue.index]))
                    fireTurret(cue.index, chunks[cue.index].getPositionVector(), 10, 8, 0);
                break;
            case SHOOT_CHUNK:
                if (alive(chunks[cue.index])) {
                    Vec3d impact = chunks[cue.index].getPositionVector();
                    chunks[cue.index].shatter();
                    world.playSound(
                            null,
                            impact.x,
                            impact.y,
                            impact.z,
                            SoundEvents.ENTITY_GENERIC_EXPLODE,
                            SoundCategory.HOSTILE,
                            .9F,
                            1.3F);
                }
                break;
            case DRILL_LAUNCH_CHUNK:
                if (alive(chunks[cue.index])) {
                    goal = chunks[cue.index].getPositionVector();
                    locked = aim;
                    pendingChunk = cue.index;
                    mode = 12;
                }
                break;
            case ANCHOR:
                mode = 4;
                goal =
                        ground(
                                        attack() == Attack.TURRET_SWEEP
                                                ? center.addVector(0, 0, -18)
                                                : getPositionVector())
                                .addVector(0, .5, 0);
                break;
            case HORIZONTAL_SWEEP:
                mode = 5;
                break;
            case CROSSING_SWEEPS:
                mode = 6;
                break;
            case CLOSE_CORRIDOR:
                mode = 7;
                locked = aim;
                break;
            case RETRACT:
                mode = 0;
                break;
            case CHARGE:
                mode = 8;
                heading = locked.subtract(getPositionVector()).normalize();
                if (attack() == Attack.FAULT_LINE) {
                    Vec3d d = aim.subtract(getPositionVector());
                    heading = new Vec3d(d.x, 0, d.z).normalize();
                }
                break;
            case CUT_PLATFORM:
                locked = ground(aim);
                for (int i = 0; i < 12; i++)
                    warning(
                            locked.addVector(
                                    Math.cos(i * Math.PI / 6) * 4,
                                    0,
                                    Math.sin(i * Math.PI / 6) * 4),
                            locked.addVector(
                                    Math.cos((i + 1) * Math.PI / 6) * 4,
                                    0,
                                    Math.sin((i + 1) * Math.PI / 6) * 4),
                            55);
                break;
            case LIFT_PLATFORM:
                chunks[0] = chunk(locked, 4, 160);
                cutDisc(locked, 4);
                mode = 13;
                break;
            case SHATTER_PLATFORM:
                if (alive(chunks[0])) chunks[0].shatter();
                goal = ground(locked).addVector(0, 1, 0);
                mode = 3;
                break;
            case UPWARD_FIRE:
                upwardFire(aim);
                break;
            case FAULT_WARNING:
                warning(sites[cue.index * 2], center, 105 - cue.tick);
                break;
            case FAULT_ERUPTION:
                locked = center;
                goal = center.addVector(0, 12, 0);
                mode = 3;
                debris(center, 16);
                shock(center, 15);
                break;
            case PLANT_PROBES:
                for (int i = 0; i < 6; i++) if (alive(probes[i])) probes[i].plant(sites[i]);
                break;
            case ARM_PROBE:
                mode = 14;
                break;
            case REDIRECT_RIGHT:
            case REDIRECT_DIAGONAL:
                break;
            case TUNNEL_RELOCATE:
                mode = 15;
                goal = center.addVector(-18, -8, 0);
                break;
            case MARK_SITES:
                for (int i = 0; i < 6; i++) {
                    warning(sites[i], sites[i].addVector(0, .1, 1), 35);
                    if (alive(probes[i])) probes[i].surveySite(sites[i]);
                }
                break;
            case PROCESS_SITE:
                locked = cue.index == 5 ? center : sites[cue.index];
                processingSite = cue.index;
                processingStart = attackTick();
                goal = locked.addVector(0, -7, 0);
                mode = 16;
                break;
            case FAKE_ERUPTION:
                debris(sites[4], 12);
                mode = 1;
                break;
            case METEOR_BORE:
                goal = center.addVector(0, -3, 0);
                mode = 17;
                break;
            case DRILL_STALL:
                mode = 18;
                velocity = Vec3d.ZERO;
                break;
            case DRILL_CLICK:
            case DRILL_SPUTTER:
            case DRILL_RESTART:
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        net.minecraft.init.SoundEvents.BLOCK_PISTON_EXTEND,
                        SoundCategory.HOSTILE,
                        1,
                        cue.action == Action.DRILL_RESTART ? 1.5F : .5F);
                break;
            case RETREAT:
                goal = center.addVector(-40, 7, 0);
                mode = 3;
                break;
            case FEED_DRILL:
                mode = 19;
                break;
            case EMBED_ON_MISS:
                break;
            case RECOVER:
                mode = 0;
                recall();
                break;
        }
    }

    private void movePattern(EntityPlayer p, int t) {
        double speed = failing() ? 1.7 : 1.2;
        Vec3d aim = p.getPositionVector().addVector(0, 1, 0);
        if (mode == 0) steer(ground(circle(t * .018, 18, 0)).addVector(0, -4, 0), .85);
        if (mode == 1)
            steer(
                    ground(attack() == Attack.SEISMIC_BORE && t >= 85 ? locked : aim)
                            .addVector(0, -7, 0),
                    speed);
        if (mode == 2) steer(circle((t - 20) * Math.PI * 2 / 65, 12, -6), speed);
        if (mode == 3 || mode == 12 || mode == 15) steer(goal, 1.8);
        if (mode == 12
                && alive(chunks[pendingChunk])
                && getPositionVector().squareDistanceTo(chunks[pendingChunk].getPositionVector())
                        < 30) {
            EntityExcavatorPayload chunk = chunks[pendingChunk];
            chunk.launch(locked.subtract(chunk.getPositionVector()).normalize().scale(1.5));
            mode = 0;
        }
        if (mode == 16) {
            int beat = t - processingStart;
            if (beat < 10) steer(locked.addVector(0, -7, 0), 3.5);
            else if (beat < 23 || processingSite == 5)
                steer(
                        locked.addVector(
                                processingSite == 3 ? 18 : 0,
                                processingSite == 5 ? 28 : processingSite == 3 ? 1 : 13,
                                0),
                        3);
            else steer(locked.addVector(0, -8, 0), 2.5);
            if (beat == 10) {
                shock(locked, 7);
                debris(locked, 8);
                if (processingSite == 1) fireTurret(0, locked.addVector(0, 7, 0), 10, 14, 5);
                if (processingSite == 2 && alive(probes[2]))
                    beam(probes[2].getPositionVector(), aim, 12, 14, 6);
            }
        }
        if (attack() == Attack.SEISMIC_BORE && t >= 120 && t < 160) {
            double f = (t - 120) / 40D;
            steer(locked.addVector(f * 15, 2 + Math.sin(f * Math.PI) * 16, 0), 2);
        }
        if (mode == 4) steer(goal, .5);
        if (mode >= 5 && mode <= 7) {
            velocity = velocity.scale(.7);
            if (t % 4 == 0) {
                double a = mode == 5 ? (t - 30) * .035 : mode == 6 ? (t - 75) * .04 : 0;
                for (int i = 0; i < 2; i++) {
                    Vec3d to;
                    if (mode == 7) {
                        double gap = Math.max(1, 7 - (t - 120) * .11);
                        to = locked.addVector((i == 0 ? -1 : 1) * gap, 0, 16);
                    } else
                        to =
                                aim.addVector(
                                        Math.sin(a + i * Math.PI) * 14,
                                        mode == 6 ? Math.cos(a + i * Math.PI) * 9 : 0,
                                        0);
                    fireTurret(i, to, 6, 6, 6);
                }
            }
        }
        if (mode == 8) {
            if (embedded) {
                velocity = velocity.scale(.4);
                return;
            }
            if (attack() == Attack.FINAL_CHARGE && t > 132)
                heading = new Vec3d(heading.x, -.35, heading.z).normalize();
            velocity = heading.scale(attack() == Attack.FINAL_CHARGE ? 3.5 : 2.1);
            Vec3d next = getPositionVector().add(velocity);
            boolean wall =
                    world.rayTraceBlocks(
                                    getPositionVector().addVector(0, 1, 0),
                                    next.addVector(0, 1, 0),
                                    false,
                                    true,
                                    false)
                            != null;
            if (redirectCooldown > 0) redirectCooldown--;
            if (attack() == Attack.BORE_RICOCHET && wall && redirectCooldown == 0) {
                redirects++;
                redirectCooldown = 10;
                heading =
                        redirects == 1
                                ? new Vec3d(-heading.z, 0, heading.x).normalize()
                                : new Vec3d(heading.z, .25, -heading.x).normalize();
                beam(getPositionVector(), getPositionVector().add(heading.scale(25)), 8, 18, 5);
                if (redirects >= 3) {
                    mode = 15;
                    goal = center.addVector(-18, -6, 0);
                }
            }
            if (attack() == Attack.FINAL_CHARGE && (wall || t >= 140)) {
                embedded = true;
                if (wall)
                    setPosition(
                            posX + heading.x * 1.8, posY + heading.y * 1.8, posZ + heading.z * 1.8);
                velocity = Vec3d.ZERO;
                shock(ground(getPositionVector()), 8);
            }
        }
        if (mode == 10) {
            if (getPositionVector().squareDistanceTo(goal) > 9 && trench.isEmpty()) steer(goal, 2);
            else {
                heading = attackTick() >= 180 ? heading.rotateYaw(.035F) : heading;
                Vec3d at = ground(getPositionVector());
                velocity = heading.scale(1.6);
                velocity = new Vec3d(velocity.x, (at.y - posY) * .4, velocity.z);
                if (t % 2 == 0) {
                    trench.add(at);
                    for (int x = -1; x <= 1; x++)
                        for (int z = -1; z <= 1; z++)
                            for (int y = 1; y <= 2; y++)
                                ExcavatorTerrain.get(world)
                                        .cut(this, new BlockPos(at).add(x, -y, z));
                }
            }
        }
        if (attack() == Attack.STRIP_MINE)
            for (int i = 0; i < chunks.length; i++) {
                EntityExcavatorPayload c = chunks[i];
                if (alive(c) && t < 150)
                    c.place(
                            new Vec3d(
                                    c.posX,
                                    ground(c.getPositionVector()).y
                                            - 1.2
                                            + 8 * ExcavatorActuation.ease((t - 25 - i * 15) / 35D),
                                    c.posZ));
            }
        if (mode == 13 && alive(chunks[0])) {
            chunks[0].place(chunks[0].getPositionVector().addVector(0, .3, 0));
            steer(chunks[0].getPositionVector().addVector(0, -5, 0), 1.4);
        }
        if (attack() == Attack.CORE_SAMPLE
                && t > 135
                && p.motionY < 0
                && p.posY < ground(p.getPositionVector()).y + 8) upwardFire(aim);
        if (mode == 14) {
            int index = MathHelper.clamp((t - 50) / 25, 0, 5);
            steer(sites[index].addVector(0, -3, 0), 1.7);
        }
        if (mode == 17) {
            steer(goal, 3.2);
            if (posY <= center.y + 2) {
                shock(center, 22);
                for (int i = 0; i < 6; i++)
                    warning(center, circle(i * Math.PI / 3, 24, 0), 8 + i * 2);
                debris(center, 20);
                mode = 18;
                velocity = Vec3d.ZERO;
            }
        }
        if (mode == 18 || mode == 19) velocity = velocity.scale(.6);
        if (posY < ground(getPositionVector()).y && t % 6 == 0) {
            Vec3d g = ground(getPositionVector());
            warning(g, g.addVector(1, 0, 0), 8);
            dust(g, 4);
            if (t % 12 == 0) chunk(g, 1, 12);
        }
    }

    private static boolean alive(Entity e) {
        return e != null && !e.isDead;
    }

    private void upwardFire(Vec3d aim) {
        if (coreFired) return;
        coreFired = true;
        for (int i = 0; i < 6; i++)
            fireTurret(i, aim.addVector(Math.cos(i) * 2, 8, Math.sin(i) * 2), 8, 12, 6);
    }

    private void launchProbes(int count) {
        for (int i = 0; i < probes.length; i++) {
            if (i >= 6 && alive(probes[i]) && probes[i].mode() == 0) continue;
            if (alive(probes[i])) probes[i].setDead();
            probes[i] = null;
            if (i < count || i >= 6) {
                probes[i] = new EntityExcavatorProbe(this, i);
                Vec3d socket =
                        segment(ExcavatorProbeScore.socketSegment(i), 1)
                                .addVector(ExcavatorProbeScore.socketSide(i) * 1.5, 0, 0);
                probes[i].setPosition(socket.x, socket.y, socket.z);
                world.spawnEntity(probes[i]);
            }
        }
    }

    private void recall() {
        for (EntityExcavatorProbe probe : probes)
            if (alive(probe) && (probe.index() < 6 || transition() > 0)) probe.recall();
    }

    private void spawn(EntityExcavatorPayload h) {
        if (hazards.size() < 160 && world.spawnEntity(h)) hazards.add(h);
    }

    public void beam(Vec3d from, Vec3d to, int warning, int life, float damage) {
        spawn(new EntityExcavatorPayload(this, 0, from, to, warning, warning + life, .22F, damage));
    }

    void laserWall(Vec3d a, Vec3d b, int warning, int life) {
        spawn(
                new EntityExcavatorPayload(
                        this,
                        EntityExcavatorPayload.LASER_WALL,
                        a,
                        b,
                        warning,
                        warning + life,
                        6,
                        7));
    }

    void surveyLine(Vec3d a, Vec3d b, int life) {
        spawn(
                new EntityExcavatorPayload(
                        this, EntityExcavatorPayload.SURVEY_LINE, a, b, 0, life, .04F, 0));
    }

    EntityExcavatorPayload tether(Vec3d a, Vec3d b) {
        EntityExcavatorPayload tether =
                new EntityExcavatorPayload(
                        this, EntityExcavatorPayload.TETHER, a, b, 20, 800, .12F, 4);
        spawn(tether);
        return tether;
    }

    public void shock(Vec3d at, int radius) {
        spawn(new EntityExcavatorPayload(this, 1, at, at, 4, 4 + radius * 2, .5F, 8));
    }

    void parryLanding(EntityPlayer player, Vec3d at, Vec3d direction) {
        com.scapeandrun.frostbite.network.PacketExcavatorParry.send(this, player, at, direction, 1);
        dust(at, 42);
        world.playSound(
                null,
                at.x,
                at.y,
                at.z,
                net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                2.5F,
                .55F);
        world.playSound(
                null,
                at.x,
                at.y,
                at.z,
                net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                SoundCategory.HOSTILE,
                2,
                .5F);
        if (player.isEntityAlive()
                && !player.isCreative()
                && !player.isSpectator()
                && player.getDistanceSq(at.x, at.y, at.z) < 6400) {
            Vec3d away = player.getPositionVector().subtract(at);
            away = new Vec3d(away.x, 0, away.z).normalize();
            player.addVelocity(away.x * .5, .1, away.z * .5);
            player.velocityChanged = true;
        }
    }

    private void warning(Vec3d a, Vec3d b, int delay) {
        spawn(new EntityExcavatorPayload(this, 3, a, b, delay, delay + 12, .3F, 6));
    }

    private EntityExcavatorPayload chunk(Vec3d at, float radius, int life) {
        EntityExcavatorPayload c =
                new EntityExcavatorPayload(
                        this, 2, at.addVector(0, -1.2, 0), at, 0, life, radius, 9);
        spawn(c);
        return c;
    }

    public void debris(Vec3d at, int count) {
        for (int i = 0; i < count; i++) {
            double a = i * 2.399963;
            EntityExcavatorPayload c = new EntityExcavatorPayload(this, 4, at, at, 4, 65, .3F, 4);
            c.launch(new Vec3d(Math.cos(a) * .5, .55 + (i % 3) * .12, Math.sin(a) * .5));
            spawn(c);
        }
        dust(at, count * 2);
    }

    void dust(Vec3d at, int count) {
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.BLOCK_DUST,
                        at.x,
                        at.y,
                        at.z,
                        count,
                        2,
                        .4,
                        2,
                        .12,
                        net.minecraft.block.Block.getStateId(
                                world.getBlockState(new BlockPos(at).down())));
        if (count >= 20)
            for (int layer = 0; layer < 4; layer++)
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.CLOUD,
                                at.x,
                                at.y + .4 + layer * .7,
                                at.z,
                                Math.min(16, count / 2),
                                2.8 - layer * .5,
                                .3,
                                2.8 - layer * .5,
                                .04 + layer * .012);
    }

    private void cutDisc(Vec3d at, int radius) {
        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++)
                if (x * x + z * z <= radius * radius)
                    ExcavatorTerrain.get(world).cut(this, new BlockPos(at).add(x, -1, z));
    }

    private void contact() {
        if (velocity.lengthVector() < .3) return;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(34))) {
            if (p.isCreative() || p.isSpectator()) continue;
            for (int i = 0; i < 18; i++) {
                AxisAlignedBB box = p.getEntityBoundingBox().grow(i == 0 ? 1.5 : 1.1);
                if (box.contains(segment(i, 1))
                        || box.calculateIntercept(segment(i, 0), segment(i, 1)) != null) {
                    p.attackEntityFrom(DamageSource.causeMobDamage(this), i == 0 ? 10 : 5);
                    break;
                }
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource s, float amount) {
        if (finale() > 0) return false;
        if ((!introDone || introTick() == -1 && parryCount() == 0)
                && s != DamageSource.OUT_OF_WORLD) return false;
        if (s == DamageSource.IN_WALL
                || s == DamageSource.FALL
                || s.isFireDamage()
                || s.getTrueSource() == this) return false;
        if (overboreShield()) amount *= .5F;
        if (mode == 18 || embedded || expert() && expertStage() == 6) amount *= 1.5F;
        return super.attackEntityFrom(s, amount);
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!world.isRemote && finale() == 0) {
            setHealth(1);
            deathTime = 0;
            finaleSource = source;
            finaleOrigin = ground(getPositionVector());
            EntityLivingBase target = getAttackTarget();
            Vec3d direction =
                    target == null
                            ? new Vec3d(1, 0, 0)
                            : target.getPositionVector().subtract(getPositionVector());
            finaleDirection = new Vec3d(direction.x, 0, direction.z).normalize();
            if (finaleDirection.lengthSquared() < .01) finaleDirection = new Vec3d(1, 0, 0);
            clearEffects();
            recall();
            dataManager.set(RATE, 1F);
            dataManager.set(FINALE, dataManager.get(EXPERT) ? 2 : 1);
            dataManager.set(FINALE_TIME, 0);
            return;
        }
        super.onDeath(source);
    }

    private void finaleStage(int stage) {
        dataManager.set(FINALE, stage);
        dataManager.set(FINALE_TIME, 0);
        finaleOrigin = getPositionVector();
    }

    private void finalePosition(Vec3d at, Vec3d forward) {
        setPosition(at.x, at.y, at.z);
        rotationYaw = (float) Math.toDegrees(Math.atan2(forward.x, forward.z));
        rotationPitch =
                (float) -Math.toDegrees(Math.atan2(forward.y, Math.hypot(forward.x, forward.z)));
        renderYawOffset = rotationYaw;
        Vec3d head = at.addVector(0, 1.5, 0);
        if (finale() == 5) spine.coil(head, forward, ExcavatorActuation.ease(finaleTick() / 25D));
        else if (finale() == 1 && finaleTick() > 35 && finaleTick() < 150)
            spine.coil(head, forward, .45 + .3 * Math.sin(finaleTick() * .1));
        else if (finale() == 4) spine.impact(head, forward, finaleTick() % 30);
        else spine.update(head);
        velocityChanged = true;
    }

    private void finaleExplosion(Vec3d at, int count) {
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.EXPLOSION_LARGE, at.x, at.y, at.z, 2, .6, .6, .6, 0);
        ((WorldServer) world)
                .spawnParticle(
                        EnumParticleTypes.SMOKE_LARGE, at.x, at.y, at.z, count, 1, 1, 1, .08);
        world.playSound(
                null,
                at.x,
                at.y,
                at.z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                1.2F,
                .75F);
    }

    private void tickFinale() {
        int t = finaleTick() + 1;
        dataManager.set(FINALE_TIME, t);
        motionX = motionY = motionZ = 0;
        dataManager.set(
                DRILL_ANGLE, (dataManager.get(DRILL_ANGLE) + (finale() == 2 ? 90 : 15)) % 360);
        hazards.removeIf(entity -> entity.isDead);
        EntityPlayer player =
                getAttackTarget() instanceof EntityPlayer ? (EntityPlayer) getAttackTarget() : null;
        if (player == null || !player.isEntityAlive() || player.isSpectator()) {
            player = world.getClosestPlayer(posX, posY, posZ, 160, false);
            setAttackTarget(player);
        }
        if (finale() == 1) {
            if (t <= 35)
                finalePosition(finaleOrigin.addVector(0, -t * .22, 0), new Vec3d(0, -1, 0));
            else if (t <= 125) {
                double u = (t - 35) / 90D, a = u * Math.PI * 6;
                Vec3d forward = finaleDirection.scale(Math.cos(a)).addVector(0, Math.sin(a), 0);
                finalePosition(
                        finaleOrigin
                                .add(finaleDirection.scale(u * 22))
                                .addVector(0, 12 + Math.sin(u * Math.PI) * 25, 0),
                        forward);
                rotationYaw =
                        (float) Math.toDegrees(Math.atan2(finaleDirection.x, finaleDirection.z));
                rotationPitch = (float) (-1080 * u);
            } else if (t <= 150) {
                Vec3d landing = ground(finaleOrigin.add(finaleDirection.scale(22)));
                finalePosition(
                        landing.addVector(0, 12 * (1 - (t - 125) / 25D), 0), finaleDirection);
                if (t == 150) {
                    finaleExplosion(landing, 40);
                    dust(landing, 60);
                }
            } else if ((t - 150) % 6 == 0) {
                int index = (t - 150) / 6 - 1;
                if (index < 18) {
                    finaleExplosion(segment(index, 1), 12);
                    dataManager.set(BROKEN, index + 1);
                    if (index > 0 && body[index] != null) body[index].setDead();
                } else finishFinale();
            }
        } else if (finale() == 2) {
            bar.setName(new TextComponentString("X-04 \"Excavator\" — BURNOUT"));
            bar.setPercent(Math.max(0, 1 - t / 600F));
            if (player != null) burnout(player, t);
            if (t >= 600) {
                clearEffects();
                finaleStage(3);
            }
        } else if (finale() == 3) {
            if (player == null) return;
            Vec3d aim = player.getPositionVector().addVector(0, 1, 0),
                    direction = aim.subtract(getPositionVector()).normalize();
            if (getPositionVector().distanceTo(aim) > 7) {
                Vec3d step =
                        direction.scale(Math.min(2.5, getPositionVector().distanceTo(aim) - 6));
                finalePosition(getPositionVector().add(step), direction);
            } else {
                finaleDirection = new Vec3d(direction.x, 0, direction.z).normalize();
                if (finaleDirection.lengthSquared() < .01) finaleDirection = new Vec3d(1, 0, 0);
                finalePosition(
                        player.getPositionVector()
                                .subtract(finaleDirection.scale(4.5))
                                .addVector(0, -.25, 0),
                        finaleDirection);
                finaleStage(4);
            }
        } else if (finale() == 4) {
            if (player == null) return;
            if (getDistanceSq(player) > 144) {
                finaleStage(3);
                return;
            }
            Vec3d contact = player.getPositionVector().addVector(0, 1.2, 0);
            if (t % 16 == 1)
                com.scapeandrun.frostbite.network.PacketExcavatorParry.send(
                        this, player, contact, finaleDirection, 0);
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.CRIT,
                            contact.x,
                            contact.y,
                            contact.z,
                            4,
                            .2,
                            .25,
                            .2,
                            .2);
            finalePosition(
                    finaleOrigin.add(
                            finaleDirection.scale(Math.sin(t * .65) * .06 - mash() * .025)),
                    finaleDirection);
            if (mash() >= 24) {
                finaleStage(5);
                bar.setPercent(0);
            }
        } else if (finale() == 5) {
            double u = Math.min(1, t / 80D);
            Vec3d at =
                    finaleOrigin
                            .subtract(finaleDirection.scale(u * 35))
                            .addVector(0, Math.sin(u * Math.PI) * 28, 0);
            if (t <= 80)
                finalePosition(
                        at,
                        finaleDirection
                                .scale(Math.cos(u * Math.PI * 2))
                                .addVector(0, Math.sin(u * Math.PI * 2), 0));
            if (t == 80) {
                Vec3d ground = ground(at);
                finalePosition(ground, finaleDirection);
                dataManager.set(BROKEN, 18);
                finaleExplosion(ground, 60);
            }
            if (t >= 80 && t < 120 && t % 4 == 0) {
                Vec3d atGround = getPositionVector();
                int age = t - 80;
                double height = age * .45;
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.SMOKE_LARGE,
                                atGround.x,
                                atGround.y + height,
                                atGround.z,
                                12,
                                1.2,
                                .8,
                                1.2,
                                .02);
                for (int i = 0; i < 12; i++) {
                    double a = i * Math.PI / 6, r = 2 + age * .15;
                    ((WorldServer) world)
                            .spawnParticle(
                                    EnumParticleTypes.SMOKE_LARGE,
                                    atGround.x + Math.cos(a) * r,
                                    atGround.y + height + 2,
                                    atGround.z + Math.sin(a) * r,
                                    2,
                                    .6,
                                    .6,
                                    .6,
                                    .02);
                }
            }
            if (t >= 250) finishFinale();
        }
        if (!isDead)
            for (int i = Math.max(1, brokenSegments()); i < body.length; i++)
                if (body[i] == null || body[i].isDead) {
                    body[i] = new EntityExcavatorSegment(this, i);
                    world.spawnEntity(body[i]);
                }
    }

    private void burnout(EntityPlayer player, int t) {
        Vec3d at = getPositionVector(), goal;
        if (t <= 200) {
            int beat = (t - 1) % 50;
            if (beat == 0) {
                locked = ground(player.getPositionVector());
                finaleRetreatStart = at;
                for (int i = 0; i < 3; i++) {
                    double a = i * Math.PI * 2 / 3;
                    fault(locked.addVector(Math.cos(a) * 14, 0, Math.sin(a) * 14), locked, 25, 9);
                }
            }
            if (beat < 25) {
                goal = locked.addVector(0, -8, 0);
                if (beat % 5 == 0) dust(ground(at), 12);
            } else if (beat < 36) {
                goal = locked.addVector(0, 14, 0);
                if (beat == 25) {
                    shock(locked, 12);
                    debris(locked, 8);
                    finaleExplosion(locked, 12);
                }
            } else goal = locked.addVector(12, -7, 0);
        } else if (t <= 400) {
            int beat = t - 201;
            if (beat == 0) {
                locked = ground(player.getPositionVector());
                deploy(6);
            }
            double angle = beat * .035;
            goal =
                    locked.addVector(
                            Math.cos(angle) * 19,
                            3 + Math.sin(angle * 2) * 2,
                            Math.sin(angle) * 19);
            if (beat % 24 == 0) {
                Vec3d aim = player.getPositionVector().addVector(0, 1, 0);
                fireTurret(beat / 24, aim, 16, 8, 9);
                fireTurret(beat / 24 + 3, aim.addVector(3, 0, -3), 16, 8, 9);
            }
            if (beat % 50 == 25) {
                Vec3d impact = ground(player.getPositionVector());
                fault(impact.addVector(-12, 0, -12), impact.addVector(12, 0, 12), 20, 9);
                fault(impact.addVector(-12, 0, 12), impact.addVector(12, 0, -12), 20, 9);
            }
        } else {
            int beat = t - 401;
            if (beat == 0) {
                recall();
                locked = ground(player.getPositionVector());
            }
            if (beat < 60) {
                goal =
                        locked.addVector(
                                Math.cos(beat * .08) * 10,
                                8 + beat * .45,
                                Math.sin(beat * .08) * 10);
                if (beat % 15 == 0) fireTurret(beat / 15, player.getPositionVector(), 20, 8, 8);
            } else if (beat < 90) {
                goal = locked.addVector(0, 35, 0);
                if (beat == 60) {
                    locked = ground(player.getPositionVector());
                    for (int i = 0; i < 6; i++) {
                        double a = i * Math.PI / 3;
                        fault(
                                locked,
                                locked.addVector(Math.cos(a) * 24, 0, Math.sin(a) * 24),
                                30,
                                12);
                    }
                }
            } else if (beat < 105) {
                goal = locked.addVector(0, -2, 0);
            } else if (beat < 145) {
                goal = locked.addVector(0, -2, 0);
                if (beat == 105) {
                    finaleExplosion(locked, 45);
                    dust(locked, 60);
                    debris(locked, 12);
                    shock(locked, 22);
                }
                if (beat == 117 || beat == 129) shock(locked, 18);
            } else {
                goal = ground(player.getPositionVector()).addVector(-22, -5, 0);
                if (beat % 8 == 0) dust(ground(at), 16);
            }
        }
        Vec3d delta = goal.subtract(at);
        double speed = t > 490 && t < 506 ? 4.2 : 2.1;
        Vec3d step = delta.lengthVector() > speed ? delta.normalize().scale(speed) : delta;
        if (step.lengthSquared() > .001) {
            setPosition(at.x + step.x, at.y + step.y, at.z + step.z);
            rotationYaw = (float) Math.toDegrees(Math.atan2(step.x, step.z));
            rotationPitch = (float) -Math.toDegrees(Math.atan2(step.y, Math.hypot(step.x, step.z)));
            renderYawOffset = rotationYaw;
            updateSpine();
            velocityChanged = true;
        }
        if (getEntityBoundingBox().grow(.5).intersects(player.getEntityBoundingBox()))
            player.attackEntityFrom(DamageSource.causeMobDamage(this), 10);
    }

    private void finishFinale() {
        setHealth(0);
        super.onDeath(finaleSource);
        setDead();
    }

    @Override
    public void fall(float d, float m) {}

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        bar.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bar.removePlayer(p);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (dataManager.get(EXPERT)) {
            dropItem(com.scapeandrun.frostbite.registry.MachineRewards.EXCAVATOR_BAG, 1);
            return;
        }
        ExcavatorLoot loot = ExcavatorLoot.roll(rand);
        dropItem(com.scapeandrun.frostbite.expedition.ExpeditionContent.SCRAP, loot.scrap);
        dropItem(com.scapeandrun.frostbite.expedition.ExpeditionContent.CORE, loot.cores);
        dropItem(com.scapeandrun.frostbite.registry.MachineRewards.PROBE, loot.probes);
    }

    @Override
    public void setDead() {
        if (!world.isRemote) {
            ExcavatorTerrain.get(world).release(getUniqueID());
            for (Entity e : body) if (e != null) e.setDead();
            for (Entity e : probes) if (e != null) e.setDead();
            for (Entity e : hazards) e.setDead();
        }
        bar.setVisible(false);
        super.setDead();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        introDone = n.getBoolean("IntroDone");
        cursor = n.getInteger("ExcavatorCursor");
        dataManager.set(DEEP, n.getBoolean("Deep"));
        difficultyLoaded = n.hasKey("Expert");
        dataManager.set(EXPERT, n.getBoolean("Expert"));
        dataManager.set(OVERBORE_SHIELD, n.getBoolean("OverboreShield"));
        initialized = false;
        dataManager.set(FINALE, MathHelper.clamp(n.getInteger("Finale"), 0, 5));
        dataManager.set(FINALE_TIME, n.getInteger("FinaleTime"));
        dataManager.set(BROKEN, n.getInteger("BrokenSegments"));
        dataManager.set(MASH, n.getInteger("ClashProgress"));
        finaleOrigin =
                new Vec3d(n.getDouble("FinaleX"), n.getDouble("FinaleY"), n.getDouble("FinaleZ"));
        finaleDirection =
                new Vec3d(
                        n.getDouble("FinaleDX"), n.getDouble("FinaleDY"), n.getDouble("FinaleDZ"));
        if (finaleDirection.lengthSquared() < .01) finaleDirection = new Vec3d(1, 0, 0);
        finaleRetreatStart =
                new Vec3d(
                        n.getDouble("RetreatX"), n.getDouble("RetreatY"), n.getDouble("RetreatZ"));
        locked =
                new Vec3d(
                        n.getDouble("FinaleLockX"),
                        n.getDouble("FinaleLockY"),
                        n.getDouble("FinaleLockZ"));
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setBoolean("IntroDone", introDone);
        n.setInteger("ExcavatorCursor", cursor);
        n.setBoolean("Deep", deep());
        n.setBoolean("Expert", dataManager.get(EXPERT));
        n.setBoolean("OverboreShield", overboreShield());
        n.setInteger("Finale", finale());
        n.setInteger("FinaleTime", finaleTick());
        n.setInteger("BrokenSegments", brokenSegments());
        n.setInteger("ClashProgress", mash());
        n.setDouble("FinaleX", finaleOrigin.x);
        n.setDouble("FinaleY", finaleOrigin.y);
        n.setDouble("FinaleZ", finaleOrigin.z);
        n.setDouble("FinaleDX", finaleDirection.x);
        n.setDouble("FinaleDY", finaleDirection.y);
        n.setDouble("FinaleDZ", finaleDirection.z);
        n.setDouble("RetreatX", finaleRetreatStart.x);
        n.setDouble("RetreatY", finaleRetreatStart.y);
        n.setDouble("RetreatZ", finaleRetreatStart.z);
        n.setDouble("FinaleLockX", locked.x);
        n.setDouble("FinaleLockY", locked.y);
        n.setDouble("FinaleLockZ", locked.z);
    }
}
