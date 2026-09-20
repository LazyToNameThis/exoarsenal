package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.registry.ModContent;
import com.scapeandrun.frostbite.entity.scout.ScoutAnimationRules;
import com.scapeandrun.frostbite.world.ModBiomes;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIHurtByTarget;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAISwimming;
import net.minecraft.entity.ai.EntityAIWanderAvoidWater;
import net.minecraft.entity.ai.EntityAIWatchClosest;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.BossInfo;
import net.minecraft.world.BossInfoServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public final class EntityX20Scout extends EntityMob implements IAnimatable {
    public static final int PHASE_HARDPOINTS = 1;
    public static final int PHASE_EXPOSED = 2;
    public static final int PHASE_DUO = 3;
    public static final int PHASE_BRAWL = 4;
    private static final DataParameter<Integer> CAPTURED =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> GRAB_MODE =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PROJECTILE_KIND =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> RICOCHETS =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ANCHOR_ISLAND =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            DROP_X = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT),
            DROP_Y = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT),
            DROP_Z = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private Vec3d grappleOrigin = Vec3d.ZERO;
    private Vec3d katanaStart = Vec3d.ZERO, katanaEnd = Vec3d.ZERO;
    private Vec3d expertOrigin = Vec3d.ZERO, expertAnchor = Vec3d.ZERO;
    private int expertIsland = 4, expertBounces;
    private int projectileAge;
    private boolean projectileReturning;

    public static final int ATTACK_NONE = 0;
    public static final int ATTACK_MINIGUN_LEFT = 1;
    public static final int ATTACK_MINIGUN_RIGHT = 2;
    public static final int ATTACK_LASER_LEFT = 3;
    public static final int ATTACK_LASER_RIGHT = 4;
    public static final int ATTACK_CLAW = 5;
    public static final int ATTACK_VICE = 6;
    public static final int ATTACK_STOMP = 7;
    public static final int ATTACK_OVERLOAD = 8;
    public static final int ATTACK_MORTAR = 9;
    public static final int ATTACK_SYNC_BEAM = 10;

    private static final int ALL_HARDPOINTS = (1 << EntityScoutHardpoint.COUNT) - 1;
    private static final DataParameter<Integer> ATTACK =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> ATTACK_TICK =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> MOVE_STATE =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> PHASE =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HARDPOINT_MASK =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Float> AIM_X =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> AIM_Y =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> AIM_Z =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);

    private final BossInfoServer bossInfo =
            new BossInfoServer(getDisplayName(), BossInfo.Color.BLUE, BossInfo.Overlay.NOTCHED_20);
    private final AnimationFactory factory = new AnimationFactory(this);
    private final int[] hardpointIds = new int[EntityScoutHardpoint.COUNT];
    private int attackCooldown = 35;
    private int rangeReelCooldown, outOfReachTicks;
    private int attackCursor;
    private int hardpointCheck;
    private boolean wasOnGround = true;
    private int landingTicks;
    private float lastMoveYaw;
    private UUID pilotUuid;
    private static final DataParameter<Boolean> HAMMER =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Boolean> GUARD_BROKEN =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> HAMMER_FLIGHT =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Float> HAMMER_X =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HAMMER_Y =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> HAMMER_Z =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);
    private Vec3d attackDirection = new Vec3d(0, 0, 1);
    private Vec3d hammerVelocity = Vec3d.ZERO;
    private final java.util.Set<Integer> contactVictims = new java.util.HashSet<>();
    private final java.util.Set<Integer> waveVictims = new java.util.HashSet<>();
    private Vec3d waveCenter = Vec3d.ZERO;
    private int waveAge = -1;
    private boolean leapLanded;
    private boolean introDone;
    private int rageLandings;
    private boolean phaseShotQueued, nextCannon;
    private static final DataParameter<Integer> WEAPON_FORM =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private boolean relayShattered;
    private boolean balanceApplied;
    private boolean difficultyChosen;
    private long amplifierUntil;
    private static final UUID AMPLIFIER_HEALTH =
            UUID.fromString("3a7d787a-ce58-48b9-b81c-d6765b7b2e7f");

    public boolean isAmplified() {
        return !world.isRemote && world.getTotalWorldTime() < amplifierUntil;
    }

    public void amplify() {
        amplifierUntil = world.getTotalWorldTime() + 50;
        if (getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).getModifier(AMPLIFIER_HEALTH)
                == null) {
            float fraction = getHealth() / getMaxHealth();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .applyModifier(
                            new net.minecraft.entity.ai.attributes.AttributeModifier(
                                    AMPLIFIER_HEALTH, "Frigid amplifier", .25, 1));
            setHealth(getMaxHealth() * fraction);
        }
    }

    private final ExpertScoutWaves expertWaves = new ExpertScoutWaves();
    private boolean expertIntermissionDone;

    public EntityScoutArena arena() {
        for (EntityScoutArena a :
                world.getEntitiesWithinAABB(
                        EntityScoutArena.class, getEntityBoundingBox().grow(96)))
            if (a.belongs(this)) return a;
        return null;
    }

    public Vec3d arenaCenter() {
        EntityScoutArena a = arena();
        return a == null ? getPositionVector() : a.getPositionVector();
    }

    private EntityScoutArena ensureArena() {
        EntityScoutArena a = arena();
        if (a == null) {
            EntityLivingBase target = getAttackTarget();
            Vec3d center = target == null ? getPositionVector() : target.getPositionVector();
            center = groundPoint(center);
            a = new EntityScoutArena(this, center);
            world.spawnEntity(a);
        }
        return a;
    }

    public void waveMemberDefeated(UUID id) {
        expertWaves.defeated(id);
    }

    private static final DataParameter<Boolean> EXPERT =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Integer> OVERHEAT =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private boolean master, overheatSpent;
    private DamageSource finalDamage = DamageSource.GENERIC;

    public boolean isOverheating() {
        return dataManager.get(OVERHEAT) >= 0;
    }

    public int getOverheatTick() {
        return dataManager.get(OVERHEAT);
    }

    private static final DataParameter<String> BOSS_BAR_ID =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.STRING);

    public boolean isExpert() {
        return dataManager.get(EXPERT);
    }

    public String getBossBarId() {
        return dataManager.get(BOSS_BAR_ID);
    }

    private static final DataParameter<Integer> SCENE =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SCENE_TICK =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> LAND_TICK =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private Vec3d previousHammerPosition;
    private static final DataParameter<Integer> WAVE_AGE =
            EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            WAVE_X = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT),
            WAVE_Y = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT),
            WAVE_Z = EntityDataManager.createKey(EntityX20Scout.class, DataSerializers.FLOAT);

    public EntityX20Scout(World world) {
        super(world);
        setSize(3.8F, 10.0F);
        isImmuneToFire = true;
        experienceValue = 180;
        stepHeight = 1.55F;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(EXPERT, false);
        dataManager.register(OVERHEAT, -1);
        dataManager.register(BOSS_BAR_ID, "");
        dataManager.register(ATTACK, ATTACK_NONE);
        dataManager.register(CAPTURED, -1);
        dataManager.register(GRAB_MODE, 0);
        dataManager.register(PROJECTILE_KIND, 0);
        dataManager.register(RICOCHETS, 0);
        dataManager.register(ANCHOR_ISLAND, 4);
        dataManager.register(DROP_X, 0F);
        dataManager.register(DROP_Y, 0F);
        dataManager.register(DROP_Z, 0F);
        dataManager.register(ATTACK_TICK, 0);
        dataManager.register(MOVE_STATE, 0);
        dataManager.register(PHASE, PHASE_EXPOSED);
        dataManager.register(HARDPOINT_MASK, ALL_HARDPOINTS);
        dataManager.register(AIM_X, 0F);
        dataManager.register(AIM_Y, 0F);
        dataManager.register(AIM_Z, 0F);
        dataManager.register(HAMMER, false);
        dataManager.register(GUARD_BROKEN, false);
        dataManager.register(HAMMER_FLIGHT, 0);
        dataManager.register(LAND_TICK, -1);
        dataManager.register(SCENE, 0);
        dataManager.register(SCENE_TICK, 0);
        dataManager.register(WEAPON_FORM, 0);
        dataManager.register(HAMMER_X, 0F);
        dataManager.register(HAMMER_Y, 0F);
        dataManager.register(HAMMER_Z, 0F);
        dataManager.register(WAVE_AGE, -1);
        dataManager.register(WAVE_X, 0F);
        dataManager.register(WAVE_Y, 0F);
        dataManager.register(WAVE_Z, 0F);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        tasks.addTask(4, new EntityAIWanderAvoidWater(this, 0.48D));
        tasks.addTask(5, new EntityAIWatchClosest(this, EntityPlayer.class, 52.0F));
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(10.5D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(11.0D);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(22.0D);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(56.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(0.96D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.19D);
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) {
            previousHammerPosition = getHammerPosition();
            clientEffects();
            return;
        }
        if (!isEntityAlive()) {
            releaseGrab();
            setNoGravity(false);
            return;
        }
        if (getBossBarId().isEmpty())
            dataManager.set(BOSS_BAR_ID, bossInfo.getUniqueId().toString());
        if (!isAmplified()
                && getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                                .getModifier(AMPLIFIER_HEALTH)
                        != null) {
            float fraction = getHealth() / getMaxHealth();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).removeModifier(AMPLIFIER_HEALTH);
            setHealth(getMaxHealth() * fraction);
        }
        if (isAmplified() && ticksExisted % 40 == 0) heal(2);
        if (isAmplified()
                && getScene() == 0
                && getAttack() != 0
                && getAttackTick() == 28
                && getAttackTarget() != null) {
            world.spawnEntity(EntityScoutShard.wedge(world, this, localOffset(-4, 4, 2), 18));
            world.spawnEntity(EntityScoutShard.wedge(world, this, localOffset(4, 4, 2), 24));
        }
        if (!balanceApplied) {
            balanceApplied = true;
            float healthFraction = getHealth() / Math.max(1, getMaxHealth());
            if (!difficultyChosen) {
                com.scapeandrun.frostbite.world.FrostbiteWorldSettings settings =
                        com.scapeandrun.frostbite.world.FrostbiteWorldSettings.get(world);
                dataManager.set(EXPERT, settings.isExpert());
                master = settings.isMaster();
                difficultyChosen = true;
            }
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .setBaseValue(isExpert() ? 450 : 300);
            getEntityAttribute(SharedMonsterAttributes.ARMOR)
                    .setBaseValue(isExpert() ? 12.075 : 10.5);
            setHealth(getMaxHealth() * healthFraction);
            if (getHealth() > getMaxHealth()) setHealth(getMaxHealth());
        }
        updateMovementState();
        tickHexWave();
        if (++hardpointCheck >= 20) {
            hardpointCheck = 0;
            ensureHardpoints();
        }
        updateBossBar();
        if (rangeReelCooldown > 0) rangeReelCooldown--;
        if (isOverheating()) {
            tickOverheat();
            return;
        }
        if (getScene() != 0) {
            tickScene();
            return;
        }
        if (!introDone && getAttackTarget() != null) {
            startIntro();
            return;
        }
        if (getPhase() == PHASE_EXPOSED
                && ScoutCombatPattern.phaseForHealth(getHealth(), getMaxHealth()) >= PHASE_DUO) {
            startScene(3);
            return;
        }
        if (getPhase() == PHASE_DUO
                && ScoutCombatPattern.phaseForHealth(getHealth(), getMaxHealth()) >= PHASE_BRAWL) {
            startScene(6);
            return;
        }
        if (attackCooldown > 0) attackCooldown--;

        EntityLivingBase target = getAttackTarget();
        if (target == null || !target.isEntityAlive()) {
            clearAttack();
            dataManager.set(HAMMER_FLIGHT, 0);
            return;
        }
        if (validVictim(target)
                && canEntityBeSeen(target)
                && ScoutRangeReel.outOfReach(getDistanceSq(target))) outOfReachTicks++;
        else outOfReachTicks = 0;
        if (getAttack() == ATTACK_NONE
                && ScoutRangeReel.ready(
                        getDistanceSq(target), outOfReachTicks, rangeReelCooldown)) {
            outOfReachTicks = 0;
            rangeReelCooldown = ScoutRangeReel.COOLDOWN;
            faceTarget(target);
            setAim(target.getPositionVector().addVector(0, target.height * .6, 0));
            beginAttack(ScoutCombatPattern.RANGE_REEL);
            return;
        }

        if (getAttack() == ATTACK_NONE || getAttackTick() < 10) {
            setAim(target.getPositionVector().addVector(0, target.height * 0.52D, 0));
            getLookHelper().setLookPositionWithEntity(target, 22.0F, 12.0F);
        }
        if (getAttack() != ATTACK_NONE) {
            tickAttack(target);
            return;
        }
        if (getPhase() == PHASE_BRAWL && arena() != null && getDistanceSq(target) > 49D) {
            navigator.clearPath();
            setNoGravity(true);
            Vec3d approach = target.getPositionVector().addVector(0, 1.5, 0);
            flyToward(approach, .48);
            faceTarget(target);
        } else if (getDistanceSq(target) > 49D) navigator.tryMoveToEntityLiving(target, 1.25D);
        else {
            navigator.clearPath();
            setNoGravity(false);
        }
        int planned =
                ScoutCombatPattern.plannedAttack(
                        isExpert(), getPhase(), attackCursor, phaseShotQueued, nextCannon);
        if (attackCooldown <= 0 && ScoutCombatPattern.canStart(planned, getDistanceSq(target))) {
            setNoGravity(false);
            commitAttackPlan(planned);
            beginAttack(planned);
        }
    }

    private void ensureHardpoints() {
        if (getPhase() == PHASE_BRAWL) return;
        List<EntityScoutHardpoint> nearby =
                world.getEntitiesWithinAABB(
                        EntityScoutHardpoint.class, getEntityBoundingBox().grow(12.0D));
        for (int kind = 0; kind < EntityScoutHardpoint.COUNT; kind++) {
            if (kind != EntityScoutHardpoint.MINIGUN_RIGHT) continue;
            if (!isHardpointAlive(kind)) continue;
            EntityScoutHardpoint existing = null;
            Entity byId = hardpointIds[kind] == 0 ? null : world.getEntityByID(hardpointIds[kind]);
            if (byId instanceof EntityScoutHardpoint && byId.isEntityAlive())
                existing = (EntityScoutHardpoint) byId;
            if (existing == null)
                for (EntityScoutHardpoint candidate : nearby)
                    if (candidate.getKind() == kind && candidate.belongsTo(this)) {
                        existing = candidate;
                        break;
                    }
            if (existing == null) {
                existing = new EntityScoutHardpoint(world, this, kind);
                world.spawnEntity(existing);
            }
            hardpointIds[kind] = existing.getEntityId();
        }
    }

    private void commitAttackPlan(int planned) {
        boolean queued = !isExpert() && getPhase() == PHASE_EXPOSED && phaseShotQueued;
        attackCursor =
                ScoutCombatPattern.advanceCursor(
                        isExpert(), getPhase(), attackCursor, phaseShotQueued);
        if (queued) nextCannon = !nextCannon;
        phaseShotQueued =
                !isExpert()
                        && getPhase() == PHASE_EXPOSED
                        && !queued
                        && (planned == ScoutCombatPattern.CROSS
                                || planned == ScoutCombatPattern.THROW);
    }

    public int getScene() {
        return dataManager.get(SCENE);
    }

    public int getSceneTick() {
        return dataManager.get(SCENE_TICK);
    }

    private void startScene(int scene) {
        if (scene == 3 && isExpert() && !expertIntermissionDone) scene = 8;
        phaseShotQueued = false;
        releaseGrab();
        setNoGravity(false);
        clearAttack();
        navigator.clearPath();
        motionX = motionZ = 0;
        dataManager.set(HAMMER_FLIGHT, 0);
        dataManager.set(HAMMER, false);
        dataManager.set(SCENE, scene);
        dataManager.set(SCENE_TICK, 0);
        if (scene == 8) {
            dataManager.set(PHASE, PHASE_DUO);
            formWeapon(0);
            ensureArena();
        }
        if (scene == 3 || scene == 6) {
            formWeapon(0);
            Vec3d p = groundPoint(localOffset(-4, 1, 3));
            dataManager.set(DROP_X, (float) p.x);
            dataManager.set(DROP_Y, (float) p.y);
            dataManager.set(DROP_Z, (float) p.z);
        }
    }

    public void prepareEntrance() {
        if (!world.isRemote && !introDone && getScene() == 0) startIntro();
    }

    private void startIntro() {
        introDone = true;
        startScene(1);

        if (world.getCollisionBoxes(this, getEntityBoundingBox().expand(0, 24, 0)).isEmpty()) {
            setPositionAndUpdate(posX, posY + 24, posZ);
            setNoGravity(true);
            motionY = 0;
        }
    }

    private void sceneImpact() {
        impact(getPositionVector().addVector(0, .15, 0), 32);
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                1.8F,
                .55F);
    }

    // Scout cutscenes
    private void tickScene() {
        navigator.clearPath();
        motionX = motionZ = 0;
        int scene = getScene(), tick = getSceneTick() + 1;
        dataManager.set(SCENE_TICK, tick);
        if (scene == 8) {
            if (tick == 35) {
                sceneImpact();
                startHexWave();
            }
            EntityScoutArena field = ensureArena();
            if (tick >= 48 && tick < 80) {
                setNoGravity(true);
                Vec3d perch = field.getPositionVector().addVector(0, 7, -18);
                Vec3d d = perch.subtract(getPositionVector()).scale(.16);
                motionX = d.x;
                motionY = d.y;
                motionZ = d.z;
                velocityChanged = true;
            }
            if (tick >= 80) {
                setNoGravity(true);
                motionY = 0;
            }
            if (tick >= 100 && expertWaves.tick(this)) {
                expertIntermissionDone = true;
                startScene(9);
                setNoGravity(true);
            }
        } else if (scene == 9) {

            EntityScoutArena field = ensureArena();
            Vec3d landing = groundPoint(field.getPositionVector().addVector(0, 0, -5));
            Vec3d destination =
                    landing.addVector(0, Math.sin(Math.min(1, tick / 44D) * Math.PI) * 7, 0);
            Vec3d d = destination.subtract(getPositionVector()).scale(.22);
            setNoGravity(true);
            motionX = d.x;
            motionY = d.y;
            motionZ = d.z;
            velocityChanged = true;
            if (tick >= 52) {
                setNoGravity(false);
                sceneImpact();
                startScene(3);
            }
        } else if (scene == 1) {
            if (tick < 50 && hasNoGravity()) motionY = 0;
            if (tick == 50) setNoGravity(false);
            if (tick >= 50 && onGround) {
                sceneImpact();
                startScene(2);
            } else if (tick > 240) {
                setNoGravity(false);
                startScene(2);
            }
        } else if (scene == 2) {
            if (tick == com.scapeandrun.frostbite.client.ScoutEntrance.DIALOGUE)
                say("Hello there. Why have you called me, and how did you get my controller?");
            if (tick == 64 || tick == 82)
                world.playSound(
                        null,
                        posX,
                        posY + 4,
                        posZ,
                        SoundEvents.BLOCK_PISTON_EXTEND,
                        SoundCategory.HOSTILE,
                        1.2F,
                        .7F);
            if (tick >= com.scapeandrun.frostbite.client.ScoutEntrance.END) {
                dataManager.set(SCENE, 0);
                attackCooldown = 15;
            }
        } else if (scene == 3) {
            if (tick < 40 && getAttackTarget() != null) faceTarget(getAttackTarget());
            if (tick == 24) impact(getDroppedPosition(), 20);
            EntityLivingBase target = getAttackTarget();
            if (tick == 48 && target != null) tryGrab(target, 11, 1);
            if (tick >= 48 && tick < 235 && getCaptured() != null) {
                holdByNeck();
            }
            if (tick == 80)
                say(
                        "You piss me the FUCK off. Who the hell do you think you are, summoning me, only to then attack me with no negotiation?");
            if (tick == 235) releaseGrab();
            if (tick >= 260) {
                dataManager.set(PHASE, PHASE_DUO);
                attackCursor = 0;
                phaseShotQueued = false;
                formWeapon(0);
                dataManager.set(SCENE, 0);
                attackCooldown = 20;
                bossInfo.setColor(BossInfo.Color.PURPLE);
                EntityScoutArena a = arena();
                if (a != null) a.dormant();
            }
        } else if (scene == 6) {
            if (tick == 25) {
                EntityScoutArena a = ensureArena();
                a.raise();
                setPositionAndUpdate(a.posX + 8, a.posY + .25, a.posZ);
            }
            if (tick == 24) {
                dataManager.set(PHASE, PHASE_BRAWL);
                impact(getDroppedPosition(), 24);
                for (int id : hardpointIds) {
                    Entity e = world.getEntityByID(id);
                    if (e != null) e.setDead();
                }
            }
            if (tick == 64 || tick == 88 || tick == 112) {
                sceneImpact();
                startHexWave();
            }
            if (tick == 130)
                say("Alright, you want to throw hands? Lets throw hands, then, bitch.");
            if (tick >= 230) {
                dataManager.set(SCENE, 0);
                attackCursor = 0;
                attackCooldown = 20;
                bossInfo.setColor(BossInfo.Color.RED);
                getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.28);
            }
        } else if (scene == 4) {
            if (tick > 8 && onGround) {
                rageLandings++;
                sceneImpact();
                startHexWave();
                startScene(5);
            } else if (tick > 140) {
                startScene(5);
                rageLandings++;
            }
        } else if (scene == 5 && tick >= 24) {
            if (rageLandings < 3) {
                startScene(4);
                motionY = .8;
            } else {
                dataManager.set(SCENE, 0);
                attackCooldown = 12;
                getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(.24);
            }
        }
    }

    private void beginAttack(int attack) {
        if (attack == ATTACK_NONE) return;
        dataManager.set(ATTACK, attack);
        dataManager.set(ATTACK_TICK, 0);
        navigator.clearPath();
        contactVictims.clear();
        attackDirection = getAim().subtract(getPositionVector());
        attackDirection = new Vec3d(attackDirection.x, 0, attackDirection.z).normalize();
        leapLanded = false;
        relayShattered = false;
        expertOrigin = getPositionVector();
        expertAnchor = getAim();
        expertBounces = 0;
        dataManager.set(RICOCHETS, 0);
        EntityScoutArena activeArena = arena();
        if (activeArena != null) expertIsland = activeArena.nearestIsland(getAim().x, getAim().z);
        dataManager.set(ANCHOR_ISLAND, expertIsland);
        dataManager.set(LAND_TICK, -1);
        int openingForm = ScoutCombatPattern.openingWeaponForm(attack);
        if (openingForm >= 0) {
            if (dataManager.get(WEAPON_FORM) != openingForm) formWeapon(openingForm);
            else {
                dataManager.set(HAMMER, openingForm == 1);
                dataManager.set(HAMMER_FLIGHT, 0);
            }
        }
        if (attack == ScoutCombatPattern.STUN) dataManager.set(GUARD_BROKEN, false);
        if (attack == ATTACK_SYNC_BEAM) {
            EntityX20Pilot pilot = getPilot();
            if (pilot != null) pilot.prepareRelay(getAttackTarget());
        }
        float pitch =
                attack == ATTACK_STOMP
                        ? 0.55F
                        : attack == ATTACK_OVERLOAD
                                ? 1.7F
                                : attack >= ATTACK_LASER_LEFT && attack <= ATTACK_LASER_RIGHT
                                        ? 1.35F
                                        : 0.82F;
        world.playSound(
                null,
                posX,
                posY + 1.5D,
                posZ,
                SoundEvents.BLOCK_PISTON_EXTEND,
                SoundCategory.HOSTILE,
                1.05F,
                pitch);
    }

    private void tickAttack(EntityLivingBase target) {
        int attack = getAttack(), tick = getAttackTick() + 1;
        dataManager.set(ATTACK_TICK, tick);
        if (attack >= ScoutCombatPattern.SLASH) {
            tickPattern(attack, tick, target);
            return;
        }
        if (attack == ATTACK_MINIGUN_LEFT || attack == ATTACK_MINIGUN_RIGHT) {
            if (tick == 22 || tick == 34) swordCut(target, attack == ATTACK_MINIGUN_RIGHT);
        } else if (attack == ATTACK_LASER_LEFT || attack == ATTACK_LASER_RIGHT) {
            if (tick == 38) laser(target, attack == ATTACK_LASER_RIGHT, false);
            if (tick >= 39 && tick <= 45 && tick % 2 == 1)
                laser(target, attack == ATTACK_LASER_RIGHT, true);
        } else if (attack == ATTACK_CLAW && tick == 25) claw(target);
        else if (attack == ATTACK_VICE) {
            if (tick == 31) viceGrab(target);
            if (tick == 42) viceSlam(target);
        } else if (attack == ATTACK_STOMP && tick == 34) stomp();
        else if (attack == ATTACK_OVERLOAD && tick == 51) overload(target);
        else if (attack == ATTACK_MORTAR && (tick == 24 || tick == 31 || tick == 38))
            mortar(target, tick);
        else if (attack == ATTACK_SYNC_BEAM && tick == 46) synchronizedRelay(target);
        if (tick >= duration(attack)) {
            clearAttack();
            attackCooldown = cooldown(attack);
        }
    }

    // Scout attack patterns
    private void tickPattern(int attack, int tick, EntityLivingBase target) {
        if (attack == ScoutCombatPattern.RANGE_REEL) {
            tickRangeReel(tick, target);
            return;
        }
        if (ScoutCombatPattern.extended(attack)) {
            tickExtendedExpert(attack, tick, target);
            return;
        }
        if (attack == ScoutCombatPattern.KATANA_CUTS
                || attack == ScoutCombatPattern.KATANA_GLACIER) {
            tickKatana(attack, tick, target);
            return;
        }
        if (attack >= ScoutCombatPattern.BRAWL_FLURRY) {
            tickBrawl(attack, tick, target);
            return;
        }
        navigator.clearPath();
        rotationYaw = (float) Math.toDegrees(Math.atan2(-attackDirection.x, attackDirection.z));
        renderYawOffset = rotationYaw;
        rotationYawHead = rotationYaw;
        if (attack != ScoutCombatPattern.CHARGE
                && attack != ScoutCombatPattern.LEAP
                && attack != ScoutCombatPattern.SWORD_RELAY
                && attack != ScoutCombatPattern.SCISSOR_CHOPS) {
            motionX *= .45;
            motionZ *= .45;
        }
        switch (attack) {
            case ScoutCombatPattern.SLASH:
                if (tick == 20) arcContact(9, .15, 18, 0);
                break;
            case ScoutCombatPattern.CROSS:
                if (tick == 22 || tick == 34) {
                    contactVictims.clear();
                    arcContact(9, .1, 18, tick == 22 ? -.25 : .25);
                }
                break;
            case ScoutCombatPattern.CHARGE:
                if (tick >= 20 && tick <= 38) {
                    motionX = attackDirection.x * .72;
                    motionZ = attackDirection.z * .72;
                    arcContact(4, .2, 20, 0);
                    if (collidedHorizontally) {
                        motionX = motionZ = 0;
                    }
                } else {
                    motionX *= .3;
                    motionZ *= .3;
                }
                break;
            case ScoutCombatPattern.SPIN:
                if (tick >= 22 && tick <= 46) {
                    double angle = (tick - 22) / 24D * Math.PI * 2;
                    arcContact(9, .72, 16, angle);
                }
                break;
            case ScoutCombatPattern.STUN:
                motionX = motionZ = 0;
                break;
            case ScoutCombatPattern.MORPH:
                if (tick == 16) {
                    dataManager.set(HAMMER, true);
                    dataManager.set(WEAPON_FORM, 1);
                }
                break;
            case ScoutCombatPattern.LEAP:
                if (tick == 16) {
                    motionX = -attackDirection.x * .25;
                    motionZ = -attackDirection.z * .25;
                    motionY = 1.45;
                    velocityChanged = true;
                }
                if (tick > 16 && tick < 38) setAim(target.getPositionVector());
                if (tick == 38) {
                    Vec3d dive = getAim().subtract(getPositionVector()).normalize().scale(2.2);
                    attackDirection = new Vec3d(dive.x, 0, dive.z).normalize();
                    motionX = dive.x;
                    motionY = Math.min(-.9, dive.y);
                    motionZ = dive.z;
                    velocityChanged = true;
                }
                if (tick > 38 && onGround && !leapLanded) {
                    leapLanded = true;
                    dataManager.set(LAND_TICK, tick);
                    motionX = motionZ = 0;
                    startHexWave();
                    arcContact(5, -1, 26, 0);
                }
                break;
            case ScoutCombatPattern.SWEEP_CW:
            case ScoutCombatPattern.SWEEP_CCW:
                if (tick >= 21 && tick <= 31) {
                    double angle =
                            (-1.2 + (tick - 21) * .24)
                                    * (attack == ScoutCombatPattern.SWEEP_CW ? 1 : -1);
                    arcContact(10, .82, 24, angle);
                }
                break;
            case ScoutCombatPattern.BASH:
                if (tick == 18) arcContact(6, .4, 22, 0);
                break;
            case ScoutCombatPattern.THROW:
                tickHammer(tick);
                break;
            case ScoutCombatPattern.MINIGUN:
                if (tick >= 24 && tick <= 48 && tick % 4 == 0) firePhaseGun(false);
                break;
            case ScoutCombatPattern.CANNON:
                if (tick == 36) firePhaseGun(true);
                break;
            case ScoutCombatPattern.VICE_SNAP:
                if (tick == 20 || tick == 30) {
                    contactVictims.clear();
                    arcContact(7, .5, 12, 0);
                }
                break;
            case ScoutCombatPattern.VICE_CRUSH:
                if (tick == 32) {
                    contactVictims.clear();
                    arcContact(6, .65, 26, 0);
                }
                break;
            case ScoutCombatPattern.SWORD_RELAY:
                tickSwordRelay(tick, target);
                break;
            case ScoutCombatPattern.HAMMER_RELAY:
                tickHammerRelay(tick, target);
                break;
            case ScoutCombatPattern.SCISSOR_SWEEP:
                if (tick >= 24 && tick <= 36) arcContact(10, .65, 22, -1.3 + (tick - 24) * .22);
                break;
            case ScoutCombatPattern.SCISSOR_CHOPS:
                int chop = (tick - 12) % 32;
                if (tick >= 12 && tick < 96) {
                    if (chop == 0) {
                        setAim(target.getPositionVector());
                        Vec3d d = getAim().subtract(getPositionVector()).normalize();
                        attackDirection = new Vec3d(d.x, 0, d.z).normalize();
                        contactVictims.clear();
                    }
                    if (chop < 8) {
                        motionX = attackDirection.x * .85;
                        motionZ = attackDirection.z * .85;
                    } else {
                        motionX *= .4;
                        motionZ *= .4;
                    }
                    if (chop == 8) arcContact(7, .5, 24, 0);
                }
                break;
            case ScoutCombatPattern.SCISSOR_HUNT:
                tickScissorHunt(tick, target);
                break;
            default:
                break;
        }
        int end =
                attack == ScoutCombatPattern.STUN
                        ? ScoutCombatPattern.stunEnd(isGuardBroken())
                        : ScoutCombatPattern.duration(attack);
        if (isExpert()
                && getPhase() == PHASE_DUO
                && tick == ScoutCombatPattern.offhandContact(attack)) {
            contactVictims.clear();
            arcContact(9, .2, 16, .25);
        }
        if (attack == ScoutCombatPattern.LEAP && !leapLanded && tick < 140) return;
        if (attack == ScoutCombatPattern.LEAP
                && leapLanded
                && tick - dataManager.get(LAND_TICK) < 28) return;
        if (tick >= end) {
            if (attack == ScoutCombatPattern.STUN) dataManager.set(GUARD_BROKEN, false);
            if (attack == ScoutCombatPattern.THROW) {
                dataManager.set(HAMMER_FLIGHT, 0);
                dataManager.set(HAMMER, false);
                dataManager.set(WEAPON_FORM, 0);
            }
            clearAttack();
            attackCooldown =
                    attack == ScoutCombatPattern.SPIN || attack == ScoutCombatPattern.STUN
                            ? 0
                            : isExpert() ? 6 : 10;
        }
    }

    public EntityLivingBase getCaptured() {
        Entity e = world.getEntityByID(dataManager.get(CAPTURED));
        return e instanceof EntityLivingBase && e.isEntityAlive() ? (EntityLivingBase) e : null;
    }

    public int getGrabMode() {
        return dataManager.get(GRAB_MODE);
    }

    public int getProjectileKind() {
        return dataManager.get(PROJECTILE_KIND);
    }

    public int getRicochets() {
        return dataManager.get(RICOCHETS);
    }

    public Vec3d getViceSocket(boolean right, float partial) {
        double[] p =
                ScoutRigMath.vice(
                        getScene(),
                        getAttack(),
                        (getScene() != 0 ? getSceneTick() : getAttackTick()) + partial,
                        right);
        if (world.isRemote) {
            double yaw =
                    prevRenderYawOffset
                            + net.minecraft.util.math.MathHelper.wrapDegrees(
                                            renderYawOffset - prevRenderYawOffset)
                                    * partial;
            double[] offset = ScoutSpace.offset(yaw, p[0], p[1], p[2]);
            return new Vec3d(
                    lastTickPosX + (posX - lastTickPosX) * partial + offset[0],
                    lastTickPosY + (posY - lastTickPosY) * partial + offset[1],
                    lastTickPosZ + (posZ - lastTickPosZ) * partial + offset[2]);
        }
        return localOffset(p[0], p[1], p[2]);
    }

    public Vec3d getViceAnchor(boolean right, float partial) {
        EntityScoutArena field = arena();
        if (field == null) return getViceSocket(right, partial);
        int island = dataManager.get(ANCHOR_ISLAND);

        if (island == 4) island = 0;
        int anchored = right ? 8 - island : island;
        double edgeX = -Math.signum(ScoutArenaLayout.x(anchored)) * 2.5,
                edgeZ = -Math.signum(ScoutArenaLayout.z(anchored)) * 2.5;
        return field.islandPoint(anchored, edgeX, .9, edgeZ, partial);
    }

    public Vec3d getDetachedVice(boolean right, float partial) {
        Vec3d from = getViceSocket(right, partial), to = getViceAnchor(right, partial);
        double t = ScoutViceMotion.extension(getAttack(), getAttackTick() + partial, right);
        return from.add(to.subtract(from).scale(t));
    }

    public Vec3d getDroppedPosition() {
        return new Vec3d(dataManager.get(DROP_X), dataManager.get(DROP_Y), dataManager.get(DROP_Z));
    }

    private void say(String text) {
        for (EntityPlayer player : world.playerEntities)
            if (getDistanceSq(player) < 96 * 96)
                player.sendMessage(
                        new net.minecraft.util.text.TextComponentString(
                                "\u00a7bTundra Trekker\u00a7f: " + text));
    }

    private boolean tryGrab(EntityLivingBase target, double range, int mode) {
        if (!validVictim(target)
                || getDistanceSq(target) > range * range
                || !canEntityBeSeen(target)
                || target.isRiding()) return false;
        for (EntityX20Scout other :
                world.getEntitiesWithinAABB(
                        EntityX20Scout.class, target.getEntityBoundingBox().grow(48)))
            if (other != this && other.getCaptured() == target) return false;
        dataManager.set(CAPTURED, target.getEntityId());
        dataManager.set(GRAB_MODE, mode);
        grappleOrigin = target.getPositionVector();
        return true;
    }

    private void tickRangeReel(int tick, EntityLivingBase target) {
        navigator.clearPath();
        motionX *= .35;
        motionZ *= .35;
        if (tick < ScoutRangeReel.WINDUP) {
            faceTarget(target);
            setAim(target.getPositionVector().addVector(0, target.height * .6, 0));
        }
        if (tick == ScoutRangeReel.WINDUP) {
            dataManager.set(PROJECTILE_KIND, 3);
            projectileReturning = false;
            Vec3d socket = getViceSocket(true, 0);
            setHammerPosition(socket);
            hammerVelocity = getAim().subtract(socket).normalize().scale(1.8);
        }
        EntityLivingBase held = getCaptured();
        if (held == null && getProjectileKind() == 3) {
            Vec3d from = getHammerPosition(), to;
            if (tick > ScoutRangeReel.OUTBOUND_END) projectileReturning = true;
            if (projectileReturning) {
                Vec3d home = getViceSocket(true, 0), delta = home.subtract(from);
                to = from.add(delta.normalize().scale(Math.min(2.4, delta.lengthVector())));
                if (delta.lengthVector() < 1) dataManager.set(PROJECTILE_KIND, 0);
            } else {
                to = from.add(hammerVelocity);
                RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
                if (wall != null) {
                    to = wall.hitVec;
                    projectileReturning = true;
                }
                AxisAlignedBB body = target.getEntityBoundingBox().grow(.7);
                if ((body.contains(from) || body.calculateIntercept(from, to) != null)
                        && tryGrab(target, 56, 4)) {
                    say("Don't think you can escape, buddy. This is a fair fight.");
                    projectileReturning = true;
                    EntityScoutArena field = arena();
                    Vec3d landing = groundPoint(localOffset(0, 1, 5));
                    if (field != null && field.raised())
                        landing =
                                field.islandPosition(field.nearestIsland(landing.x, landing.z), 0)
                                        .addVector(0, .1, 0);
                    expertAnchor = landing;
                }
            }
            setHammerPosition(to);
        }
        held = getCaptured();
        if (held != null) {
            if (tick < ScoutRangeReel.SLAM_START) {
                Vec3d socket = getViceSocket(true, 0);
                holdAt(socket.addVector(0, -held.height * .55, 0), 4);
            } else holdAt(expertAnchor, 4);
            held = getCaptured();
            if (held != null)
                setHammerPosition(held.getPositionVector().addVector(0, held.height * .6, 0));
            if (tick == ScoutRangeReel.SLAM) {
                if (held != null && held.getPositionVector().squareDistanceTo(expertAnchor) < 9) {
                    held.attackEntityFrom(DamageSource.causeMobDamage(this), 16);
                    impact(expertAnchor, 24);
                    startWaveAt(expertAnchor);
                }
                releaseGrab();
                projectileReturning = true;
            }
        }
        if (tick >= ScoutRangeReel.END) {
            clearAttack();
            attackCooldown = 24;
        }
    }

    private void releaseGrab() {
        EntityLivingBase held = getCaptured();
        if (held != null) {
            held.fallDistance = 0;
            held.velocityChanged = true;
        }
        dataManager.set(CAPTURED, -1);
        dataManager.set(GRAB_MODE, 0);
    }

    private void holdAt(Vec3d destination, int mode) {
        EntityLivingBase held = getCaptured();
        if (held == null
                || held.world != world
                || getDistanceSq(held) > 64 * 64
                || (held instanceof EntityPlayer && ((EntityPlayer) held).isSpectator())) {
            releaseGrab();
            return;
        }
        Vec3d delta = destination.subtract(held.getPositionVector());

        if (delta.lengthVector() > 1.5) delta = delta.normalize().scale(1.5);
        held.move(net.minecraft.entity.MoverType.SELF, delta.x, delta.y, delta.z);
        held.motionX = held.motionY = held.motionZ = 0;
        held.fallDistance = 0;
        held.velocityChanged = true;
        held.setPositionAndUpdate(held.posX, held.posY, held.posZ);
        dataManager.set(GRAB_MODE, mode);
    }

    private Vec3d groundPoint(Vec3d p) {
        EntityScoutArena field = arena();
        if (field != null
                && field.raised()
                && Math.abs(p.x - field.posX) <= field.half(0)
                && Math.abs(p.z - field.posZ) <= field.half(0))
            return new Vec3d(p.x, field.topAt(p.x, p.z) + .08, p.z);
        RayTraceResult hit =
                world.rayTraceBlocks(
                        p.addVector(0, 8, 0), p.addVector(0, -32, 0), false, true, false);
        return hit == null ? p : hit.hitVec.addVector(0, .08, 0);
    }

    public Vec3d getGripPosition(float partial) {
        float tick = (getScene() != 0 ? getSceneTick() : getAttackTick()) + partial;
        double[] p = ScoutRigMath.hand(getScene(), getAttack(), tick);
        return localOffset(p[0], p[1], p[2]);
    }

    private void holdByNeck() {
        EntityLivingBase held = getCaptured();
        if (held != null) holdAt(getGripPosition(0).addVector(0, -held.getEyeHeight() * .86, 0), 1);
    }

    private void faceTarget(EntityLivingBase target) {
        Vec3d d = target.getPositionVector().subtract(getPositionVector());
        attackDirection = new Vec3d(d.x, 0, d.z).normalize();
        rotationYaw = (float) Math.toDegrees(Math.atan2(-attackDirection.x, attackDirection.z));
        renderYawOffset = rotationYaw;
        rotationYawHead = rotationYaw;
    }

    private void flyToward(Vec3d p, double speed) {
        Vec3d d = p.subtract(getPositionVector());
        if (d.lengthVector() > speed) d = d.normalize().scale(speed);
        motionX = d.x;
        motionY = d.y;
        motionZ = d.z;
        velocityChanged = true;
    }

    private void brawlImpact(float damage) {
        contactVictims.clear();
        arcContact(7, -1, damage, 0);
        startHexWave();
        sceneImpact();
    }

    private void launchBrawlProjectile(int kind, EntityLivingBase target) {
        dataManager.set(PROJECTILE_KIND, kind);
        projectileAge = 0;
        projectileReturning = false;
        contactVictims.clear();
        Vec3d from = localOffset(kind == 4 ? 5 : -5, kind == 3 ? 3 : 5, 2);
        setHammerPosition(from);
        setAim(target.getPositionVector().addVector(0, target.height * .6, 0));
        hammerVelocity = getAim().subtract(from).normalize().scale(kind == 2 ? .85 : 1.05);
        if (kind == 2
                && net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(world, this)) {
            net.minecraft.util.math.BlockPos base =
                    new net.minecraft.util.math.BlockPos(localOffset(-3, -.1, 2));
            for (int x = -1; x <= 1; x++)
                for (int z = -1; z <= 1; z++) {
                    net.minecraft.util.math.BlockPos p = base.add(x, 0, z);
                    net.minecraft.block.state.IBlockState state = world.getBlockState(p);
                    if (!state.getMaterial().isLiquid()
                            && state.getBlockHardness(world, p) >= 0
                            && state.getBlockHardness(world, p) <= 5
                            && world.getTileEntity(p) == null) world.destroyBlock(p, false);
                }
        }
    }

    private void tickBrawlProjectile(EntityLivingBase target) {
        int kind = getProjectileKind();
        if (kind == 0) return;
        projectileAge++;
        Vec3d from = getHammerPosition(),
                home = localOffset(kind == 4 ? 5 : -5, kind == 3 ? 3 : 5, 2),
                to;
        if (projectileReturning) {
            Vec3d d = home.subtract(from);
            if (d.lengthVector() < 1.5) {
                dataManager.set(PROJECTILE_KIND, 0);
                return;
            }
            to = from.add(d.normalize().scale(1.5));
        } else {
            if (kind != 2 && projectileAge < 16) {
                Vec3d desired =
                        target.getPositionVector()
                                .addVector(0, target.height * .6, 0)
                                .subtract(from)
                                .normalize()
                                .scale(1.2);
                hammerVelocity = hammerVelocity.scale(.85).add(desired.scale(.15));
            }
            if (kind == 2) hammerVelocity = hammerVelocity.addVector(0, -.025, 0);
            to = from.add(hammerVelocity);
            RayTraceResult block = world.rayTraceBlocks(from, to, false, true, false);
            if (block != null) {
                to = block.hitVec;
                projectileReturning = true;
                impact(to, 20);
                if (kind == 2) {
                    dataManager.set(PROJECTILE_KIND, 0);
                    return;
                }
            }
            for (EntityLivingBase victim :
                    world.getEntitiesWithinAABB(
                            EntityLivingBase.class,
                            new AxisAlignedBB(from, to).grow(kind == 2 ? 1.4 : .8))) {
                if (!validVictim(victim) || contactVictims.contains(victim.getEntityId())) continue;
                if (victim.getEntityBoundingBox().grow(.7).calculateIntercept(from, to) == null
                        && !victim.getEntityBoundingBox().grow(.7).contains(from)) continue;
                contactVictims.add(victim.getEntityId());
                victim.attackEntityFrom(DamageSource.causeMobDamage(this), kind == 2 ? 18 : 10);
                if (kind == 3 && tryGrab(victim, 32, 4))
                    grappleOrigin = groundPoint(localOffset(0, 1, 5));
                projectileReturning = true;
                impact(victim.getPositionVector().addVector(0, 1, 0), 14);
                if (kind == 2) dataManager.set(PROJECTILE_KIND, 0);
                break;
            }
            if (projectileAge >= 36) {
                projectileReturning = true;
                if (kind == 2) dataManager.set(PROJECTILE_KIND, 0);
            }
        }
        setHammerPosition(to);
        if (projectileAge > 80) dataManager.set(PROJECTILE_KIND, 0);
    }

    private void tickBrawl(int attack, int tick, EntityLivingBase target) {
        navigator.clearPath();
        if (tick < 12) faceTarget(target);
        tickBrawlProjectile(target);
        if (attack == ScoutCombatPattern.BRAWL_FLURRY) {
            if (tick == 18 || tick == 30 || tick == 42 || tick == 56) {
                contactVictims.clear();
                arcContact(9, .35, tick == 56 ? 12 : 8, tick == 18 ? .15 : tick == 30 ? -.15 : 0);
            }
            if (tick == 62) {
                motionY = .9;
                velocityChanged = true;
            }
            if (tick == 78) motionY = -1.4;
            if (tick >= 78 && tick < 96 && onGround && !leapLanded) {
                leapLanded = true;
                brawlImpact(16);
            }
            if (tick == 96) tryGrab(target, 10, 1);
            if (tick >= 96 && tick < 112) holdByNeck();
            if (tick == 112 && getCaptured() != null) {
                grappleOrigin = getCaptured().getPositionVector();
                setNoGravity(true);
            }
            if (tick >= 112 && tick < 132 && getCaptured() != null) {
                holdAt(grappleOrigin.addVector(0, (tick - 112) * .45, 0), 2);
                flyToward(grappleOrigin.addVector(-2, (tick - 112) * .45 - 2, 0), .9);
            }
            if (tick == 132 && getCaptured() != null) {
                EntityLivingBase held = getCaptured();
                grappleOrigin = held.getPositionVector();
                held.attackEntityFrom(DamageSource.causeMobDamage(this), 12);
            }
            if (tick >= 132 && tick < 156 && getCaptured() != null) {
                Vec3d path = grappleOrigin.add(attackDirection.scale((tick - 132) * .8));
                holdAt(path.addVector(0, Math.sin((tick - 132) / 24D * Math.PI) * 2, 0), 2);
                flyToward(path.add(attackDirection.scale(4)).addVector(0, 2, 0), 1.8);
            }
            if (tick == 156 && getCaptured() != null) {
                grappleOrigin = getCaptured().getPositionVector();
                setAim(groundPoint(grappleOrigin));
            }
            if (tick >= 156 && tick <= 172 && getCaptured() != null) {
                double a = (tick - 156) / 16D;
                holdAt(grappleOrigin.scale(1 - a).add(getAim().scale(a)), 3);
                if (tick == 172) {
                    EntityLivingBase held = getCaptured();
                    if (held != null) held.attackEntityFrom(DamageSource.causeMobDamage(this), 20);
                    impact(getAim(), 32);
                    startWaveAt(getAim());
                    releaseGrab();
                }
            }
            if (tick >= 174) {
                setNoGravity(false);
                motionX *= .5;
                motionZ *= .5;
            }
        } else if (attack == ScoutCombatPattern.BRAWL_SIEGE) {
            if (tick == 20 || tick == 38) {
                contactVictims.clear();
                arcContact(9, .2, 14, 0);
            }
            if (tick == 48) {
                motionY = 1.0;
                velocityChanged = true;
            }
            if (tick == 64) motionY = -1.6;
            if (tick >= 64 && tick < 88 && onGround && !leapLanded) {
                leapLanded = true;
                brawlImpact(22);
                for (int i = 0; i < 8; i++) {
                    double a = i * Math.PI / 4;
                    world.spawnEntity(
                            new EntityScoutShard(
                                    world,
                                    this,
                                    groundPoint(
                                            getPositionVector()
                                                    .addVector(
                                                            Math.cos(a) * 6, 1, Math.sin(a) * 6)),
                                    2,
                                    i));
                }
            }
            if (tick == 104) launchBrawlProjectile(2, target);
            if (tick == 146) launchBrawlProjectile(1, target);
            if (tick == 174) {
                setNoGravity(true);
                leapLanded = false;
            }
            if (tick >= 174 && tick < 252) {
                flyToward(groundPoint(getPositionVector()).addVector(0, 9, 0), .45);
                if (tick == 184 || tick == 210 || tick == 236)
                    launchBrawlProjectile(tick == 210 ? 4 : 1, target);
            }
            if (tick == 254) {
                setNoGravity(false);
                motionY = -1.8;
            }
            if (tick >= 254 && onGround && !leapLanded) {
                leapLanded = true;
                brawlImpact(24);
            }
        } else {
            if (tick == 28) launchBrawlProjectile(3, target);
            if (tick >= 48 && tick <= 128 && getCaptured() != null) {
                double cycle = Math.min(4, (tick - 44) / 20D), angle = cycle * Math.PI;
                Vec3d side = localOffset(Math.cos(angle) * 6, 1 + Math.abs(Math.sin(angle)) * 6, 5);
                holdAt(groundPoint(side).addVector(0, Math.abs(Math.sin(angle)) * 6, 0), 4);
                if (tick == 64 || tick == 84 || tick == 104 || tick == 124) {
                    EntityLivingBase held = getCaptured();
                    if (held != null) {
                        Vec3d p = groundPoint(held.getPositionVector());
                        holdAt(p, 4);
                        held.attackEntityFrom(DamageSource.causeMobDamage(this), 12);
                        impact(p, 24);
                        startWaveAt(p);
                    }
                }
            }
            if (tick == 130) releaseGrab();
        }
        if (tick >= ScoutCombatPattern.duration(attack)) {
            clearAttack();
            attackCooldown = 24;
        }
    }

    private void startWaveAt(Vec3d p) {
        startHexWave();
        waveCenter = p;
        dataManager.set(WAVE_X, (float) p.x);
        dataManager.set(WAVE_Y, (float) p.y);
        dataManager.set(WAVE_Z, (float) p.z);
    }

    private void expertSpiral(Vec3d center, int arms, int count, double spacing) {
        for (int i = 0; i < count; i++)
            for (int arm = 0; arm < arms; arm++) {
                double angle = i * .62 + arm * Math.PI * 2 / arms, r = 2 + i * spacing;
                world.spawnEntity(
                        new EntityScoutShard(
                                world,
                                this,
                                groundPoint(
                                        center.addVector(
                                                Math.cos(angle) * r, 1, Math.sin(angle) * r)),
                                2,
                                i));
            }
    }

    private void expertCut(Vec3d from, Vec3d to, int delay) {
        world.spawnEntity(new EntityScoutCut(world, this, from, to, delay));
    }

    private void expertHit(float damage, double arc) {
        contactVictims.clear();
        arcContact(9, arc, damage, 0);
    }

    public void onExpertParry() {
        if (world.isRemote || getAttack() != ScoutCombatPattern.EXPERT_BOXING) return;
        int tick = getAttackTick();
        for (int heavy : new int[] {44, 80, 100, 150, 188})
            if (Math.abs(tick - heavy) <= 2) {
                clearAttack();
                attackCooldown = 36;
                motionX = motionZ = 0;
                world.playSound(
                        null,
                        posX,
                        posY + 4,
                        posZ,
                        SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.HOSTILE,
                        .65F,
                        1.4F);
                return;
            }
    }

    private void expertLaunch(EntityLivingBase target, double speed) {
        setAim(target.getPositionVector().addVector(0, 1, 0));
        hammerVelocity = getAim().subtract(getHammerHand()).normalize().scale(speed);
        setHammerPosition(getHammerHand());
        dataManager.set(HAMMER_FLIGHT, 1);
        contactVictims.clear();
    }

    private void expertRecall(int remaining) {
        Vec3d d = getHammerHand().subtract(getHammerPosition());
        moveThrown(d.scale(1D / Math.max(1, remaining)), false);
        dataManager.set(HAMMER_FLIGHT, 3);
    }

    private void expertPlatform(int style) {
        EntityScoutArena a = arena();
        if (a != null) a.disturb(style, expertIsland);
    }

    private Vec3d expertDeck() {
        EntityScoutArena a = arena();
        return a == null ? groundPoint(expertAnchor) : a.islandPosition(expertIsland, 0);
    }

    private void tickExtendedExpert(int attack, int t, EntityLivingBase target) {
        navigator.clearPath();
        if (t < 14) faceTarget(target);
        if (attack >= ScoutCombatPattern.EXPERT_AERIAL) {
            tickExpertBrawl(attack, t, target);
        } else
            switch (attack) {
                case ScoutCombatPattern.SWORD_UPROOT:
                    if (t == 20) expertLaunch(target, 1.4);
                    if (t >= 20 && t < 44) moveThrown(hammerVelocity, true);
                    if (t == 44) {
                        swordShatter();
                        spawnRecall(getHammerPosition(), 12);
                    }
                    if (t == 76) {
                        formWeapon(0);
                        setAim(target.getPositionVector());
                        motionY = 1.3;
                        velocityChanged = true;
                    }
                    if (t == 96) {
                        Vec3d d = getAim().subtract(getPositionVector()).normalize().scale(1.6);
                        motionX = d.x;
                        motionZ = d.z;
                        motionY = -1.7;
                    }
                    if (t == 112) {
                        expertAnchor = groundPoint(getPositionVector());
                        setHammerPosition(expertAnchor);
                        dataManager.set(HAMMER_FLIGHT, 2);
                        startWaveAt(expertAnchor);
                        expertHit(22, -1);
                    }
                    if (t == 140 || t == 172 || t == 204) {
                        startWaveAt(expertAnchor);
                        expertSpiral(expertAnchor, 3, 8, .8);
                        impact(expertAnchor, 20);
                    }
                    if (t == 204) {
                        formWeapon(0);
                        motionX = -attackDirection.x * .6;
                        motionZ = -attackDirection.z * .6;
                    }
                    if (t == 228) {
                        expertHit(18, -.2);
                        Vec3d side = new Vec3d(-attackDirection.z, 0, attackDirection.x);
                        expertCut(
                                getPositionVector().add(side.scale(-10)).addVector(0, 1, 0),
                                getPositionVector().add(side.scale(10)).addVector(0, 1, 0),
                                18);
                    }
                    if (t == 244) expertLaunch(target, 1.6);
                    if (t >= 244 && t < 262) moveThrown(hammerVelocity, true);
                    if (t >= 262 && t <= 280) expertRecall(281 - t);
                    if (t == 281) formWeapon(0);
                    break;
                case ScoutCombatPattern.KATANA_CHAIN:
                    if (t >= 24 && t < 132) {
                        int step = (t - 24) % 36;
                        if (step == 0) {
                            faceTarget(target);
                            expertOrigin = getPositionVector();
                            expertAnchor = target.getPositionVector().add(attackDirection.scale(8));
                            setAim(expertAnchor);
                            setHammerPosition(expertAnchor);
                        }
                        if (step >= 12 && step < 20) {
                            setNoGravity(true);
                            flyToward(expertAnchor, 2.4);
                            expertHit(9, .1);
                        }
                        if (step == 20) {
                            motionX = motionZ = 0;
                            setNoGravity(false);
                            expertCut(
                                    expertOrigin.addVector(0, 1, 0),
                                    getPositionVector().addVector(0, 1, 0),
                                    14);
                        }
                    }
                    if (t == 142) {
                        Vec3d d = expertAnchor.subtract(getPositionVector());
                        for (int i = 1; i <= 9; i++) {
                            Vec3d c = getPositionVector().add(d.scale(i / 10D));
                            Vec3d cross = new Vec3d(-d.z, 0, d.x).normalize().scale(4);
                            expertCut(
                                    c.subtract(cross).addVector(0, 1, 0),
                                    c.add(cross).addVector(0, 1, 0),
                                    24 + i * 2);
                        }
                    }
                    break;
                case ScoutCombatPattern.SCISSOR_WALLS:
                    if (t >= 20 && t < 212 && (t - 20) % 24 == 0) {
                        setAim(target.getPositionVector().addVector(0, 1, 0));
                        world.spawnEntity(
                                EntityScoutCut.crushingWalls(
                                        world, this, getAim(), rand.nextInt(4), 20));
                    }
                    if (t == 216) {
                        setHammerPosition(getHammerHand());
                        dataManager.set(HAMMER_FLIGHT, 4);
                        world.spawnEntity(
                                EntityScoutShard.scissors(
                                        world,
                                        this,
                                        target.getPositionVector().addVector(-6, 5, 0),
                                        0));
                        world.spawnEntity(
                                EntityScoutShard.scissors(
                                        world,
                                        this,
                                        target.getPositionVector().addVector(6, 5, 0),
                                        1));
                    }
                    if (t >= 216 && t < 284) {
                        int step = (t - 216) % 22;
                        if (step == 0) {
                            setAim(target.getPositionVector().addVector(0, 1, 0));
                            hammerVelocity =
                                    getAim().subtract(getHammerPosition()).normalize().scale(1.8);
                            contactVictims.clear();
                        }
                        if (step >= 10 && step < 17) moveThrown(hammerVelocity, true);
                        if (step == 12) {
                            faceTarget(target);
                            expertHit(12, .3);
                        }
                    }
                    if (t >= 284 && t < 300) expertRecall(300 - t);
                    if (t == 300) formWeapon(2);
                    break;
                case ScoutCombatPattern.SCISSOR_TRENCH:
                    if (t == 24) {
                        faceTarget(target);
                        expertOrigin = getPositionVector();
                    }
                    if (t >= 32 && t < 76) {
                        motionX = attackDirection.x * .36;
                        motionZ = attackDirection.z * .36;
                        if (t % 6 == 0) {
                            Vec3d side =
                                    new Vec3d(-attackDirection.z, 0, attackDirection.x).scale(3.5);
                            world.spawnEntity(
                                    new EntityScoutShard(
                                            world,
                                            this,
                                            groundPoint(getPositionVector().add(side)),
                                            2,
                                            0));
                            world.spawnEntity(
                                    new EntityScoutShard(
                                            world,
                                            this,
                                            groundPoint(getPositionVector().subtract(side)),
                                            2,
                                            0));
                        }
                    }
                    if (t == 76) {
                        motionX = motionZ = 0;
                        expertCut(
                                expertOrigin.addVector(0, .8, 0),
                                getPositionVector().addVector(0, .8, 0),
                                20);
                    }
                    if (t == 110) {
                        setHammerPosition(target.getPositionVector().addVector(0, 8, 0));
                        dataManager.set(HAMMER_FLIGHT, 4);
                    }
                    if (t >= 120 && t < 240) {
                        int step = (t - 120) % 24;
                        if (step == 0) {
                            setAim(target.getPositionVector().addVector(0, 1, 0));
                            hammerVelocity =
                                    getAim().subtract(getHammerPosition()).normalize().scale(1.5);
                            contactVictims.clear();
                        }
                        if (step >= 10 && step < 18) moveThrown(hammerVelocity, true);
                        if (step == 20) {
                            faceTarget(target);
                            expertHit(10, .4);
                        }
                    }
                    if (t >= 240 && t < 262) expertRecall(262 - t);
                    if (t == 262) formWeapon(2);
                    break;
                case ScoutCombatPattern.HAMMER_RICOCHET:
                    if (t == 18) {
                        setHammerPosition(getHammerHand());
                        dataManager.set(HAMMER_FLIGHT, 1);
                        hammerVelocity = new Vec3d(0, 1.4, 0);
                    }
                    if (t >= 18 && t < 42) moveThrown(hammerVelocity, false);
                    if (t == 48 || t == 70 || t == 96) {
                        faceTarget(target);
                        expertHit(t == 96 ? 15 : 9, .25);
                    }
                    if (t == 62) tryGrab(target, 7, 1);
                    if (t >= 62 && t < 72) holdByNeck();
                    if (t == 72) releaseGrab();
                    if (t >= 100 && t < 126) moveThrown(new Vec3d(0, -1.3, 0), false);
                    if (t == 126) {
                        expertLaunch(target, 1.8);
                        impact(getHammerHand(), 22);
                    }
                    if (t >= 126 && t < 210) {
                        Vec3d from = getHammerPosition(), next = from.add(hammerVelocity);
                        RayTraceResult wall = world.rayTraceBlocks(from, next, false, true, false);
                        boolean boundary =
                                Math.abs(next.x - expertOrigin.x) > 18
                                        || Math.abs(next.z - expertOrigin.z) > 18;
                        if ((wall != null || boundary) && expertBounces < 3) {
                            if (wall != null && wall.sideHit != null) {
                                switch (wall.sideHit.getAxis()) {
                                    case X:
                                        hammerVelocity =
                                                new Vec3d(
                                                        -hammerVelocity.x,
                                                        hammerVelocity.y,
                                                        hammerVelocity.z);
                                        break;
                                    case Z:
                                        hammerVelocity =
                                                new Vec3d(
                                                        hammerVelocity.x,
                                                        hammerVelocity.y,
                                                        -hammerVelocity.z);
                                        break;
                                    default:
                                        hammerVelocity =
                                                new Vec3d(
                                                        hammerVelocity.x,
                                                        -hammerVelocity.y,
                                                        hammerVelocity.z);
                                }
                            } else
                                hammerVelocity =
                                        new Vec3d(
                                                -hammerVelocity.x,
                                                hammerVelocity.y,
                                                -hammerVelocity.z);
                            expertBounces++;
                            dataManager.set(RICOCHETS, expertBounces);
                            contactVictims.clear();
                            impact(from, 24);
                            expertSpiral(groundPoint(from), 1, 4, .5);
                        }
                        moveThrown(hammerVelocity, true);
                    }
                    if (t >= 210 && t < 228) {
                        expertRecall(228 - t);
                        flyToward(getHammerPosition().addVector(0, -4, 0), .5);
                    }
                    if (t == 228) {
                        formWeapon(1);
                        motionX = hammerVelocity.x * .3;
                        motionZ = hammerVelocity.z * .3;
                    }
                    if (t >= 228 && t < 246) {
                        motionX *= .88;
                        motionZ *= .88;
                    }
                    if (t == 250) {
                        expertHit(28, -.5);
                        startHexWave();
                    }
                    break;
                case ScoutCombatPattern.HAMMER_TORNADO:
                    if (t >= 20 && t < 132) {
                        int contact = t == 40 ? 1 : t == 72 ? 2 : t == 98 ? 3 : t == 120 ? 4 : 0;
                        rotationYaw += Math.min(22, 5 + (t - 20) * .15);
                        renderYawOffset = rotationYaw;
                        if (contact > 0) {
                            expertHit(12 + contact * 2, -1);
                            startHexWave();
                        }
                    }
                    if (t == 140) {
                        faceTarget(target);
                        expertLaunch(target, 1.9);
                    }
                    if (t >= 140 && t < 162) moveThrown(hammerVelocity, true);
                    if (t >= 162 && t < 186) {
                        dataManager.set(HAMMER_FLIGHT, 3);
                        flyToward(getHammerPosition().addVector(0, -3, 0), 1.3);
                    }
                    if (t == 186) {
                        formWeapon(1);
                        motionY = 1;
                        setAim(target.getPositionVector());
                    }
                    if (t == 204) {
                        flyToward(getAim(), 2);
                        motionY = -1.6;
                    }
                    if (t == 218) {
                        expertHit(30, -1);
                        startHexWave();
                        expertSpiral(groundPoint(getPositionVector()), 6, 5, 1.2);
                    }
                    break;
            }
        if (t >= ScoutCombatPattern.duration(attack)) {
            dataManager.set(HAMMER_FLIGHT, 0);
            clearAttack();
            attackCooldown = 28;
        }
    }

    private void tickExpertBrawl(int attack, int t, EntityLivingBase target) {
        tickBrawlProjectile(target);
        switch (attack) {
            case ScoutCombatPattern.EXPERT_AERIAL:
                if (t < 62) {
                    if (t < 12) flyToward(target.getPositionVector(), .55);
                    if (t == 18 || t == 30 || t == 42 || t == 56) expertHit(t == 56 ? 16 : 10, .3);
                }
                if (t == 62) {
                    faceTarget(target);
                    tryGrab(target, 9, 1);
                    setNoGravity(true);
                    expertOrigin = getPositionVector();
                }
                if (t >= 62 && t < 92) {
                    if (getCaptured() != null) {
                        flyToward(expertOrigin.addVector(0, 13, 0), .65);
                        holdByNeck();
                    } else
                        flyToward(
                                expertOrigin.add(attackDirection.scale(8)).addVector(0, 7, 0), .9);
                }
                if (t == 92 && getCaptured() != null)
                    grappleOrigin = getCaptured().getPositionVector();
                if (t >= 92 && t < 106 && getCaptured() != null) {

                    double u = (t - 92) / 14D;
                    Vec3d p = grappleOrigin.addVector(0, 7 * (2 * u - u * u), 0);
                    holdAt(p, 2);
                    flyToward(p.add(attackDirection.scale(-3)).addVector(0, -2, 0), 1.1);
                }
                if (t == 106 && getCaptured() != null) {
                    grappleOrigin = getCaptured().getPositionVector();
                    getCaptured().attackEntityFrom(DamageSource.causeMobDamage(this), 8);
                }
                if (t >= 106 && t < 124 && getCaptured() != null) {
                    Vec3d p = grappleOrigin.add(attackDirection.scale((t - 106) * .48));
                    holdAt(p, 2);
                    flyToward(p.add(attackDirection.scale(4)).addVector(0, 1, 0), 1.5);
                }
                if (t == 124) {
                    EntityScoutArena field = arena();
                    if (field != null && getCaptured() != null) {
                        int landing = field.nearestIsland(target.posX, target.posZ);
                        if (landing == expertIsland)
                            landing = expertIsland + (expertIsland % 3 == 2 ? -1 : 1);
                        expertIsland = landing;
                        expertAnchor = field.islandPosition(landing, 0).addVector(0, .08, 0);
                    } else expertAnchor = groundPoint(target.getPositionVector());
                    setAim(expertAnchor);
                }
                if (t == 124 && getCaptured() != null) {
                    grappleOrigin = getCaptured().getPositionVector();
                    faceTarget(getCaptured());
                    getCaptured().attackEntityFrom(DamageSource.causeMobDamage(this), 10);
                }
                if (t >= 124 && t < 150) {
                    flyToward(expertAnchor.addVector(0, 1, 0), 1.4);
                    if (getCaptured() != null) {
                        double u = (t - 124) / 25D;
                        holdAt(
                                grappleOrigin.add(
                                        expertAnchor.subtract(grappleOrigin).scale(u * u)),
                                3);
                    }
                }
                if (t == 150) {
                    if (getCaptured() != null)
                        getCaptured().attackEntityFrom(DamageSource.causeMobDamage(this), 20);
                    releaseGrab();
                    expertHit(22, -1);
                    startWaveAt(expertAnchor);
                    expertPlatform(1);
                }
                if (t == 158 || t == 166 || t == 174)
                    startWaveAt(expertAnchor.addVector(t == 158 ? 2 : -2, 0, t == 174 ? 2 : -2));
                if (t >= 180) setNoGravity(false);
                break;
            case ScoutCombatPattern.EXPERT_PENDULUM:
                if (t == 20) {
                    setNoGravity(true);
                    expertAnchor = expertDeck().addVector(0, 2, 0);
                    setAim(expertAnchor);
                }
                if (t >= 28 && t < 132) {
                    double angle = (t - 28) * Math.PI / 52;
                    Vec3d anchor =
                            t < 92
                                    ? getViceAnchor(false, 0).add(getViceAnchor(true, 0)).scale(.5)
                                    : getViceAnchor(false, 0);
                    Vec3d p =
                            anchor.addVector(
                                    Math.sin(angle) * 8,
                                    -4 - Math.cos(angle) * 3,
                                    Math.cos(angle) * 5);
                    flyToward(p, 1.2);
                    if (t == 50 || t == 100) {
                        faceTarget(target);
                        expertHit(15, -.3);
                    }
                }
                if (t == 136) launchBrawlProjectile(3, target);
                if (t >= 162 && t < 196) {
                    if (getCaptured() != null) {
                        flyToward(expertDeck().addVector(0, 1, 0), 1.1);
                        holdByNeck();
                    } else {
                        faceTarget(target);
                        flyToward(target.getPositionVector(), 1.2);
                    }
                }
                if (t == 196) {
                    expertHit(23, .1);
                    releaseGrab();
                    startWaveAt(expertDeck());
                    expertPlatform(1);
                }
                if (t >= 210) setNoGravity(false);
                break;
            case ScoutCombatPattern.EXPERT_BOXING:
                if (t == 18 || t == 28 || t == 44 || t == 62 || t == 80 || t == 100 || t == 116
                        || t == 128 || t == 140 || t == 150) {
                    faceTarget(target);
                    expertHit(t == 44 || t == 80 || t == 150 ? 17 : 8, t == 62 ? -.1 : .4);
                }
                if (t == 174) {
                    faceTarget(target);
                    setAim(target.getPositionVector());
                }
                if (t == 188) {
                    expertHit(28, -.1);
                    if (getAttack() != attack) return;
                    Vec3d side = new Vec3d(-attackDirection.z, 0, attackDirection.x);
                    Vec3d c = getPositionVector().add(attackDirection.scale(8)).addVector(0, 1, 0);
                    expertCut(c.subtract(side.scale(10)), c.add(side.scale(10)), 12);
                    expertCut(
                            c.subtract(attackDirection.scale(10)),
                            c.add(attackDirection.scale(10)),
                            12);
                    motionX = -attackDirection.x * .8;
                    motionZ = -attackDirection.z * .8;
                }
                if (t >= 196) {
                    setNoGravity(true);
                    motionY = .12;
                }
                break;
            case ScoutCombatPattern.EXPERT_DIVE:
                if (t == 16) setNoGravity(true);
                if (t >= 16 && t < 44) flyToward(expertDeck().addVector(0, 16, 0), .8);
                if (t == 44 || t == 84 || t == 124) {
                    faceTarget(target);
                    setAim(target.getPositionVector());
                    expertAnchor = getAim();
                }
                if (t >= 50 && t < 70 || t >= 90 && t < 110 || t >= 130 && t < 148)
                    flyToward(expertAnchor, 1.3);
                if (t == 62) expertHit(14, .3);
                if (t == 96) launchBrawlProjectile(3, target);
                if (t == 144) {
                    expertHit(22, -1);
                    expertPlatform(1);
                    EntityScoutArena field = arena();
                    if (field != null) field.splitIsland(expertIsland);
                    startWaveAt(expertAnchor);
                    releaseGrab();
                }
                if (t >= 150 && t < 182) flyToward(expertDeck().addVector(0, 19, 0), 1);
                if (t == 182) {
                    setAim(expertDeck());
                    expertAnchor = getAim();
                }
                if (t >= 194 && t < 210) flyToward(expertAnchor, 1.8);
                if (t == 210) {
                    expertHit(30, -1);
                    expertPlatform(1);
                    startWaveAt(expertAnchor);
                    expertSpiral(expertAnchor, 6, 4, 1.2);
                    setNoGravity(false);
                }
                break;
            case ScoutCombatPattern.EXPERT_WEB:
                if (t == 22) {
                    setNoGravity(true);
                    setAim(expertDeck());
                }
                if (t >= 22 && t < 62) flyToward(expertDeck().addVector(0, 10, 0), .5);
                if (t == 64) {
                    Vec3d c = expertDeck().addVector(0, 1, 0);
                    expertCut(c.addVector(-10, 0, -3), c.addVector(10, 0, 3), 20);
                }
                if (t == 80) {
                    EntityScoutArena field = arena();
                    if (field != null) field.disturb(5, 8 - (expertIsland == 4 ? 0 : expertIsland));
                }
                if (t == 92) {
                    Vec3d c = expertDeck().addVector(0, 1, 0);
                    expertCut(c.addVector(0, -2, -7), c.addVector(0, 9, 7), 20);
                }
                if (t == 116) expertPlatform(4);
                if (t == 132) {
                    expertAnchor = target.getPositionVector();
                    setAim(expertAnchor);
                }
                if (t >= 144 && t < 160) flyToward(expertAnchor, 1.6);
                if (t == 154) expertHit(22, .2);
                if (t == 180) {
                    expertPlatform(2);
                    setAim(expertDeck());
                }
                if (t >= 180 && t < 210) flyToward(expertDeck().addVector(-6, 10, -6), .65);
                if (t >= 230) setNoGravity(false);
                break;
            case ScoutCombatPattern.EXPERT_FREEFALL:
                if (t == 24) {
                    expertPlatform(3);
                    setNoGravity(true);
                    setAim(expertDeck());
                }
                if (t >= 24 && t < 80) {
                    double a = (t - 24) * .14;
                    flyToward(expertDeck().addVector(Math.cos(a) * 5, 2, Math.sin(a) * 5), 1.1);
                    if (t == 40 || t == 58) {
                        faceTarget(target);
                        expertHit(14, .15);
                    }
                    if (t == 66) launchBrawlProjectile(3, target);
                }
                if (t >= 80 && t < 108) {
                    releaseGrab();
                    flyToward(expertDeck().addVector(0, -5, 0), 1.5);
                }
                if (t >= 108 && t < 140) flyToward(expertDeck().addVector(0, 10, 0), 1.2);
                if (t == 148) {
                    faceTarget(target);
                    setAim(target.getPositionVector());
                }
                if (t >= 148 && t < 170)
                    flyToward(target.getPositionVector().addVector(0, 1, 0), 1.2);
                if (t == 170) {
                    tryGrab(target, 8, 1);
                    expertAnchor = expertDeck();
                    setAim(expertAnchor);
                }
                if (t >= 170 && t < 202) {
                    if (getCaptured() != null) holdByNeck();
                    flyToward(expertAnchor.addVector(0, Math.max(0, 18 - (t - 170) * .6), 0), 1.4);
                }
                if (t == 202) {
                    boolean caught = getCaptured() != null;
                    if (caught)
                        getCaptured().attackEntityFrom(DamageSource.causeMobDamage(this), 28);
                    releaseGrab();
                    expertPlatform(1);
                    startWaveAt(expertAnchor);
                    expertSpiral(expertAnchor, 3, 9, .8);
                    expertHit(26, -1);
                    setNoGravity(false);
                }
                break;
        }
    }

    private void arcContact(double range, double dot, float damage, double angle) {
        Vec3d forward =
                new Vec3d(
                        attackDirection.x * Math.cos(angle) - attackDirection.z * Math.sin(angle),
                        0,
                        attackDirection.x * Math.sin(angle) + attackDirection.z * Math.cos(angle));
        for (EntityLivingBase victim :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(range, 2, range))) {
            if (!validVictim(victim) || contactVictims.contains(victim.getEntityId())) continue;
            Vec3d delta = victim.getPositionVector().subtract(getPositionVector());
            Vec3d flat = new Vec3d(delta.x, 0, delta.z);
            if (flat.lengthSquared() > range * range
                    || flat.normalize().dotProduct(forward) < dot
                    || !canEntityBeSeen(victim)) continue;
            contactVictims.add(victim.getEntityId());
            if (victim.attackEntityFrom(DamageSource.causeMobDamage(this), damage)) {
                Vec3d push = flat.normalize();
                victim.addVelocity(push.x * .6, .15, push.z * .6);
                impact(victim.getPositionVector().addVector(0, 1, 0), 6);
            }
        }
    }

    public Vec3d getPhaseMuzzle(boolean cannon) {
        return localOffset(cannon ? 4.8 : -4.8, 3.5, 2.8);
    }

    public int getWeaponForm() {
        return dataManager.get(WEAPON_FORM);
    }

    public boolean recallingIce() {
        return getAttack() == ScoutCombatPattern.HAMMER_RELAY && getAttackTick() >= 210;
    }

    private void formWeapon(int form) {
        dataManager.set(WEAPON_FORM, form);
        dataManager.set(HAMMER, form == 1);
        dataManager.set(HAMMER_FLIGHT, 0);
        impact(getHammerHand(), 24);
    }

    private void moveThrown(Vec3d step, boolean strike) {
        Vec3d from = getHammerPosition(), to = from.add(step);
        RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
        if (strike && wall != null) to = wall.hitVec;
        if (strike) hammerContact(from, to);
        setHammerPosition(to);
    }

    private void spawnRecall(Vec3d center, int count) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2 / count;
            world.spawnEntity(
                    new EntityScoutShard(
                            world, this, center.addVector(Math.cos(a), .5, Math.sin(a)), 3, i));
        }
    }

    private void swordShatter() {
        if (relayShattered) return;
        relayShattered = true;
        dataManager.set(HAMMER_FLIGHT, 6);
        Vec3d p = getHammerPosition();
        impact(p, 32);

        for (int i = 0; i < 12; i++) {
            double a = i * Math.PI / 2, radius = 2 + (i / 4) * 2;
            Vec3d point = p.addVector(Math.cos(a) * radius, 3, Math.sin(a) * radius);
            RayTraceResult ground =
                    world.rayTraceBlocks(point, point.addVector(0, -12, 0), false, true, false);
            if (ground != null)
                world.spawnEntity(new EntityScoutShard(world, this, ground.hitVec, 2, i));
        }
    }

    private void tickKatana(int attack, int tick, EntityLivingBase target) {
        navigator.clearPath();
        boolean cuts = attack == ScoutCombatPattern.KATANA_CUTS;
        if (cuts && tick >= 30 && tick < 110) {
            int pass = (tick - 30) / 16, step = (tick - 30) % 16;
            if (pass < 5) {
                if (step == 0) {
                    faceTarget(target);
                    katanaStart = getPositionVector().addVector(0, 1.2, 0);
                    Vec3d d = target.getPositionVector().subtract(getPositionVector());
                    d = new Vec3d(d.x, 0, d.z).normalize();
                    katanaEnd = target.getPositionVector().add(d.scale(9));
                    setNoGravity(true);
                }
                if (step < 6) flyToward(katanaEnd, 3.2);
                else {
                    motionX *= .2;
                    motionZ *= .2;
                }
                if (step == 6) {
                    world.spawnEntity(
                            new EntityScoutCut(
                                    world,
                                    this,
                                    katanaStart,
                                    getPositionVector().addVector(0, 1.2, 0),
                                    130 - tick));
                    setNoGravity(false);
                }
            }
        } else {
            motionX *= .35;
            motionZ *= .35;
        }
        if (cuts && tick == 130) {
            world.playSound(
                    null,
                    posX,
                    posY + 5,
                    posZ,
                    SoundEvents.BLOCK_GLASS_BREAK,
                    SoundCategory.HOSTILE,
                    1.5F,
                    .6F);
            expertOrigin = getPositionVector();
        }
        if (cuts) {
            for (int i = 0; i < ScoutKatanaPlatforms.COUNT; i++)
                if (tick == ScoutKatanaPlatforms.toss(i)) {
                    double angle = i * Math.PI * 2 / 9;
                    Vec3d origin =
                            groundPoint(
                                    expertOrigin.addVector(
                                            Math.cos(angle) * 6, 0, Math.sin(angle) * 6));
                    world.spawnEntity(EntityScoutShard.katanaPlatform(world, this, origin, i));
                    impact(origin, 8);
                }
            if (tick == ScoutKatanaPlatforms.BOARD) {
                setNoGravity(true);
                world.spawnEntity(
                        EntityScoutShard.surfboard(
                                world, this, getPositionVector().addVector(0, -1.05, 0)));
            }
            if (tick >= ScoutKatanaPlatforms.BOARD && tick < ScoutKatanaPlatforms.DISMOUNT) {
                setNoGravity(true);
                faceTarget(target);
            }
        }
        if (!cuts && (tick == 34 || tick == 98 || tick == 142)) {
            faceTarget(target);
            int count = tick == 34 ? 5 : tick == 98 ? 10 : 4;
            for (int i = 0; i < count; i++) {
                double a = i * Math.PI * 2 / count;
                world.spawnEntity(
                        EntityScoutShard.wedge(
                                world,
                                this,
                                getPositionVector()
                                        .addVector(
                                                Math.cos(a) * 5, 8 + (i % 2) * 2, Math.sin(a) * 5),
                                18 + i * 2));
            }
        }
        int launch = cuts ? ScoutKatanaPlatforms.DISMOUNT : 164;
        if (tick == launch) {
            setNoGravity(false);
            setAim(target.getPositionVector());
            motionY = 1.35;
            velocityChanged = true;
        }
        if (tick > launch && tick < launch + 18) setAim(target.getPositionVector());
        if (tick == launch + 18) {
            Vec3d d = getAim().subtract(getPositionVector()).normalize().scale(2.5);
            motionX = d.x;
            motionZ = d.z;
            motionY = Math.min(-1, d.y);
            velocityChanged = true;
        }
        if (tick > launch + 18 && !leapLanded && onGround) {
            leapLanded = true;
            dataManager.set(LAND_TICK, tick);
            contactVictims.clear();
            arcContact(7, -1, 24, 0);
            startHexWave();
            for (int i = 0; i < 18; i++) {
                double a = i * .7, r = 2 + i * .65;
                Vec3d p =
                        groundPoint(
                                getPositionVector().addVector(Math.cos(a) * r, 1, Math.sin(a) * r));
                world.spawnEntity(new EntityScoutShard(world, this, p, 2, i));
            }
        }
        if (tick >= ScoutCombatPattern.duration(attack)
                && (!leapLanded
                        ? tick > ScoutCombatPattern.duration(attack) + 80
                        : tick - dataManager.get(LAND_TICK) > 24)) {
            formWeapon(0);
            clearAttack();
            attackCooldown = 25;
        }
    }

    private void tickSwordRelay(int tick, EntityLivingBase target) {
        if (tick == 22) {
            setHammerPosition(getHammerHand().add(attackDirection.scale(3)));
            dataManager.set(HAMMER_FLIGHT, 4);
        }
        if (tick >= 34 && tick < 44) {
            Vec3d reach = getHammerPosition().subtract(getHammerHand());
            Vec3d flat = new Vec3d(reach.x, 0, reach.z);
            double speed = Math.min(.8, Math.max(0, flat.lengthVector() - .8));
            flat = flat.normalize();
            motionX = flat.x * speed;
            motionZ = flat.z * speed;
        } else {
            motionX *= .4;
            motionZ *= .4;
        }
        if (tick == 46) {
            setAim(target.getPositionVector().addVector(0, .8, 0));
            hammerVelocity = getAim().subtract(getHammerPosition()).normalize().scale(2.1);
            dataManager.set(HAMMER_FLIGHT, 1);
            impact(getHammerPosition(), 10);
        }
        if (tick >= 46 && tick < 76 && !relayShattered) {
            Vec3d before = getHammerPosition();
            moveThrown(hammerVelocity, true);
            if (!contactVictims.isEmpty()
                    || getHammerPosition().squareDistanceTo(getAim()) < .16
                    || getHammerPosition().squareDistanceTo(before) < .01
                    || tick == 75) swordShatter();
        }
        if (tick == 102) spawnRecall(getHammerPosition(), 12);
        if (tick == 136) formWeapon(1);
    }

    private void tickHammerRelay(int tick, EntityLivingBase target) {
        if (tick == 22) {
            setHammerPosition(getHammerHand());
            setAim(target.getPositionVector().addVector(0, 1, 0));
            hammerVelocity = getAim().subtract(getHammerHand()).normalize().scale(1.5);
            dataManager.set(HAMMER_FLIGHT, 1);
        }
        if (tick >= 22 && tick < 52) moveThrown(hammerVelocity, true);
        if (tick == 52) {
            dataManager.set(HAMMER_FLIGHT, 3);
            contactVictims.clear();
        }
        if (tick >= 52 && tick < 72) {
            Vec3d delta = getHammerHand().subtract(getHammerPosition());
            setHammerPosition(getHammerPosition().add(delta.scale(1D / (72 - tick))));
        }
        if (tick == 72) {
            setHammerPosition(getHammerHand());
            setAim(target.getPositionVector().addVector(0, 1, 0));
            hammerVelocity = getAim().subtract(getHammerHand()).normalize().scale(2);
            dataManager.set(HAMMER_FLIGHT, 5);
            impact(getHammerHand(), 20);
            contactVictims.clear();
        }
        if (tick >= 72 && tick < 88) moveThrown(hammerVelocity, true);
        if (tick == 88) {
            dataManager.set(HAMMER_FLIGHT, 6);
            impact(getHammerPosition(), 32);
            for (int i = 0; i < ScoutCombatPattern.CHUNKS; i++) {
                double a = i * Math.PI * 2 / ScoutCombatPattern.CHUNKS;
                world.spawnEntity(
                        new EntityScoutShard(
                                world,
                                this,
                                getHammerPosition()
                                        .addVector(
                                                Math.cos(a) * 2,
                                                .5 + (i % 3) * .5,
                                                Math.sin(a) * 2),
                                0,
                                i));
            }
        }
        if (tick == 246) formWeapon(2);
    }

    private void tickScissorHunt(int tick, EntityLivingBase target) {
        if (tick == 16) {
            setHammerPosition(getHammerHand().addVector(0, 2, 0));
            dataManager.set(HAMMER_FLIGHT, 4);
        }
        if (tick >= 20 && tick < 32) setAim(target.getPositionVector().addVector(0, 1, 0));
        if (tick >= 32 && tick < 144) {
            int dash = (tick - 32) % 24;
            if (dash == 16) setAim(target.getPositionVector().addVector(0, 1, 0));
            if (dash == 0) {
                hammerVelocity = getAim().subtract(getHammerPosition()).normalize().scale(1.6);
                contactVictims.clear();
            }
            if (dash < 12) moveThrown(hammerVelocity, true);
        }
        if (tick == 148) {
            com.scapeandrun.frostbite.network.PacketScoutImpact.send(this, getHammerPosition(), 3);
            dataManager.set(HAMMER_FLIGHT, 6);
            spawnRecall(getHammerPosition(), 12);
        }
        if (tick == 172) formWeapon(0);
    }

    private void firePhaseGun(boolean cannon) {
        Vec3d from = getPhaseMuzzle(cannon), direction = getAim().subtract(from).normalize();
        Vec3d to = traceEnd(from, direction, 40);
        EntityLivingBase hit = null;
        double distance = from.distanceTo(to);
        for (EntityLivingBase candidate :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, new AxisAlignedBB(from, to).grow(.5))) {
            if (!validVictim(candidate)) continue;
            RayTraceResult intercept =
                    candidate.getEntityBoundingBox().grow(.2).calculateIntercept(from, to);
            if (intercept != null && from.distanceTo(intercept.hitVec) < distance) {
                hit = candidate;
                distance = from.distanceTo(intercept.hitVec);
            }
        }
        impact(from, 6);
        if (hit != null && hit.attackEntityFrom(DamageSource.causeMobDamage(this), cannon ? 24 : 5))
            impact(from.add(direction.scale(distance)), cannon ? 28 : 6);
        world.playSound(
                null,
                from.x,
                from.y,
                from.z,
                SoundEvents.ENTITY_BLAZE_SHOOT,
                SoundCategory.HOSTILE,
                cannon ? 1.6F : .7F,
                cannon ? .5F : 1.4F);
    }

    private boolean validVictim(EntityLivingBase e) {
        return e != this
                && !(e instanceof EntityFrigidRobot)
                && !(e instanceof EntityScoutHardpoint)
                && !(e instanceof EntityScoutShard)
                && !(e instanceof EntityX20Pilot)
                && e.isEntityAlive()
                && (!(e instanceof EntityPlayer)
                        || (!((EntityPlayer) e).isSpectator() && !((EntityPlayer) e).isCreative()));
    }

    public boolean isHammerForm() {
        return dataManager.get(HAMMER);
    }

    public boolean isGuardBroken() {
        return dataManager.get(GUARD_BROKEN);
    }

    public boolean isGuarding() {
        return getPhase() != PHASE_BRAWL
                && getAttack() != ScoutCombatPattern.STUN
                && !isGuardBroken();
    }

    public boolean hitShield(DamageSource source, float amount) {
        if (world.isRemote
                || amount <= 0
                || source.getImmediateSource() == null
                || source.isExplosion()) return false;
        if (ScoutCombatPattern.canBreakGuard(getAttack(), getAttackTick(), isGuardBroken())) {
            dataManager.set(GUARD_BROKEN, true);
            world.playSound(
                    null,
                    posX,
                    posY + 4,
                    posZ,
                    SoundEvents.BLOCK_GLASS_BREAK,
                    SoundCategory.HOSTILE,
                    1.8F,
                    .65F);
            com.scapeandrun.frostbite.network.PacketScoutImpact.send(
                    this, getHardpointPosition(EntityScoutHardpoint.MINIGUN_RIGHT), 2);
            return true;
        }
        world.playSound(
                null,
                posX,
                posY + 4,
                posZ,
                SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.HOSTILE,
                .7F,
                .65F);
        return false;
    }

    private void startHexWave() {
        waveCenter = getPositionVector();
        waveAge = 0;
        waveVictims.clear();
        com.scapeandrun.frostbite.network.PacketScoutImpact.send(
                this, waveCenter.addVector(0, .2, 0), 1);
        dataManager.set(WAVE_X, (float) waveCenter.x);
        dataManager.set(WAVE_Y, (float) waveCenter.y);
        dataManager.set(WAVE_Z, (float) waveCenter.z);
        dataManager.set(WAVE_AGE, 0);
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                1.5F,
                .5F);
    }

    private void tickHexWave() {
        if (waveAge < 0) return;
        double inner = waveAge * .55;
        waveAge++;
        double outer = waveAge * .55;
        dataManager.set(WAVE_AGE, waveAge);
        for (EntityLivingBase victim :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class,
                        new AxisAlignedBB(waveCenter, waveCenter).grow(14, 2, 14))) {
            if (!validVictim(victim) || waveVictims.contains(victim.getEntityId())) continue;
            double radius =
                    ScoutCombatPattern.hexRadius(
                            victim.posX - waveCenter.x, victim.posZ - waveCenter.z);
            if (radius < inner - .65
                    || radius > outer + .65
                    || victim.posY > waveCenter.y + 1.2
                    || victim.posY + victim.height < waveCenter.y
                    || !canEntityBeSeen(victim)) continue;
            waveVictims.add(victim.getEntityId());
            if (victim.attackEntityFrom(DamageSource.causeMobDamage(this), 18F)) {
                victim.addVelocity(0, .35, 0);
                impact(victim.getPositionVector().addVector(0, .5, 0), 12);
            }
        }
        if (waveAge >= 22) {
            waveAge = -1;
            dataManager.set(WAVE_AGE, -1);
        }
    }

    public int getWaveAge() {
        return dataManager.get(WAVE_AGE);
    }

    public Vec3d getWaveCenter() {
        return new Vec3d(dataManager.get(WAVE_X), dataManager.get(WAVE_Y), dataManager.get(WAVE_Z));
    }

    public int getHammerFlight() {
        return dataManager.get(HAMMER_FLIGHT);
    }

    public Vec3d getHammerPosition() {
        return new Vec3d(
                dataManager.get(HAMMER_X), dataManager.get(HAMMER_Y), dataManager.get(HAMMER_Z));
    }

    public Vec3d getHammerPosition(float partial) {
        Vec3d current = getHammerPosition();
        return previousHammerPosition == null
                ? current
                : previousHammerPosition.add(
                        current.subtract(previousHammerPosition)
                                .scale(Math.max(0, Math.min(1, partial))));
    }

    private void setHammerPosition(Vec3d p) {
        dataManager.set(HAMMER_X, (float) p.x);
        dataManager.set(HAMMER_Y, (float) p.y);
        dataManager.set(HAMMER_Z, (float) p.z);
    }

    public Vec3d getHammerHand() {
        return getGripPosition(0);
    }

    private void tickHammer(int tick) {
        if (tick == 22) {
            Vec3d start = getHammerHand(), delta = getAim().subtract(start);
            double travel = Math.max(1, delta.lengthVector() / 1.6);
            setHammerPosition(start);
            hammerVelocity =
                    new Vec3d(
                            delta.x / travel,
                            ScoutSpace.launchY(delta.y, travel),
                            delta.z / travel);
            dataManager.set(HAMMER_FLIGHT, 1);
        }
        int flight = getHammerFlight();
        if (tick == 76 && flight != 0) {
            dataManager.set(HAMMER_FLIGHT, 3);
            flight = 3;
            contactVictims.clear();
        }
        if (flight == 1) {
            Vec3d from = getHammerPosition(), to = from.add(hammerVelocity);
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            if (wall != null) {
                to = wall.hitVec;
                dataManager.set(HAMMER_FLIGHT, 2);
                impact(to, 18);
                world.playSound(
                        null,
                        to.x,
                        to.y,
                        to.z,
                        SoundEvents.BLOCK_ANVIL_LAND,
                        SoundCategory.HOSTILE,
                        1,
                        .55F);
            }
            hammerContact(from, to);
            setHammerPosition(to);
            hammerVelocity = hammerVelocity.addVector(0, -.035, 0);
        } else if (flight == 3) {
            Vec3d from = getHammerPosition(), delta = getHammerHand().subtract(from);
            double speed = 1.6 + (tick - 76) * .07;
            if (delta.lengthVector() <= speed) {
                setHammerPosition(getHammerHand());
                dataManager.set(HAMMER_FLIGHT, 0);
            } else {
                Vec3d to = from.add(delta.normalize().scale(speed));
                hammerContact(from, to);
                setHammerPosition(to);
            }
        }
    }

    private void hammerContact(Vec3d from, Vec3d to) {
        for (EntityLivingBase victim :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, new AxisAlignedBB(from, to).grow(1.4))) {
            if (!validVictim(victim) || contactVictims.contains(victim.getEntityId())) continue;
            if (!victim.getEntityBoundingBox().grow(1.1).contains(from)
                    && victim.getEntityBoundingBox().grow(1.1).calculateIntercept(from, to) == null)
                continue;
            contactVictims.add(victim.getEntityId());
            victim.attackEntityFrom(DamageSource.causeMobDamage(this).setProjectile(), 25F);
            impact(victim.getPositionVector(), 8);
        }
    }

    private void swordCut(EntityLivingBase target, boolean right) {

        Vec3d forward = getLookVec();
        for (EntityLivingBase victim :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(6, 1, 6))) {
            if (victim == this
                    || victim instanceof EntityScoutHardpoint
                    || victim instanceof EntityX20Pilot) continue;
            Vec3d delta = victim.getPositionVector().subtract(getPositionVector());
            if (delta.lengthSquared() > 81
                    || delta.normalize().dotProduct(forward) < 0.15
                    || !canEntityBeSeen(victim)) continue;
            if (victim.attackEntityFrom(DamageSource.causeMobDamage(this), 18F)) {
                victim.addVelocity(
                        (right ? -1 : 1) * forward.z * .3, .12, (right ? 1 : -1) * forward.x * .3);
                impact(victim.getPositionVector().addVector(0, victim.height * .5, 0), 8);
            }
        }
        world.playSound(
                null,
                posX,
                posY + 4,
                posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.HOSTILE,
                1.5F,
                .65F);
    }

    private void laser(EntityLivingBase target, boolean right, boolean residual) {
        Vec3d muzzle =
                getMuzzlePosition(
                        right ? EntityScoutHardpoint.LASER_RIGHT : EntityScoutHardpoint.LASER_LEFT);
        Vec3d direction = getAim().subtract(muzzle).normalize();
        Vec3d end = traceEnd(muzzle, direction, 64.0D);
        if (target.getEntityBoundingBox().grow(1.15D).calculateIntercept(muzzle, end) != null) {
            target.attackEntityFrom(
                    DamageSource.causeMobDamage(this).setProjectile(), residual ? 5F : 24F);
        }
        beamParticles(
                muzzle,
                end,
                EnumParticleTypes.END_ROD,
                residual ? 16 : 42,
                residual ? .025D : .07D);
        if (!residual)
            world.playSound(
                    null,
                    muzzle.x,
                    muzzle.y,
                    muzzle.z,
                    SoundEvents.ENTITY_LIGHTNING_THUNDER,
                    SoundCategory.HOSTILE,
                    1.0F,
                    1.55F);
    }

    private void synchronizedRelay(EntityLivingBase target) {
        EntityX20Pilot pilot = getPilot();
        if (pilot == null || !pilot.isEntityAlive()) {
            overload(target);
            return;
        }
        Vec3d source = getReactorEmitterPosition();
        Vec3d relay = pilot.getBladePosition();
        Vec3d direction = getAim().subtract(relay).normalize();
        Vec3d end = traceEnd(relay, direction, 64.0D);
        if (target.getEntityBoundingBox().grow(1.25D).calculateIntercept(relay, end) != null) {
            target.attackEntityFrom(
                    DamageSource.causeMobDamage(this).setProjectile().setMagicDamage(), 31.0F);
        }
        beamParticles(source, relay, EnumParticleTypes.END_ROD, 26, .045D);
        beamParticles(relay, end, EnumParticleTypes.FIREWORKS_SPARK, 44, .055D);
        world.playSound(
                null,
                relay.x,
                relay.y,
                relay.z,
                SoundEvents.ENTITY_LIGHTNING_THUNDER,
                SoundCategory.HOSTILE,
                1.15F,
                1.72F);
    }

    private void claw(EntityLivingBase target) {
        if (getDistanceSq(target) > 90D || !canEntityBeSeen(target)) return;
        if (target.attackEntityFrom(DamageSource.causeMobDamage(this), 24F)) {
            Vec3d push = target.getPositionVector().subtract(getPositionVector()).normalize();
            target.addVelocity(push.x * .85D - push.z * .32D, .34D, push.z * .85D + push.x * .32D);
        }
        impact(target.getPositionVector().addVector(0, target.height * .45D, 0), 30);
    }

    private void viceGrab(EntityLivingBase target) {
        if (getDistanceSq(target) > 110D || !canEntityBeSeen(target)) return;
        Vec3d pull = getPositionVector().subtract(target.getPositionVector()).normalize();
        target.attackEntityFrom(DamageSource.causeMobDamage(this), 13F);
        target.addVelocity(pull.x * 1.15D, .28D, pull.z * 1.15D);
    }

    private void viceSlam(EntityLivingBase target) {
        if (getDistanceSq(target) > 64D) return;
        target.attackEntityFrom(DamageSource.causeMobDamage(this), 29F);
        target.addVelocity(0, -.9D, 0);
        impact(target.getPositionVector(), 36);
    }

    private void stomp() {
        AxisAlignedBB box = getEntityBoundingBox().grow(10D, 3D, 10D);
        for (EntityPlayer player : world.getEntitiesWithinAABB(EntityPlayer.class, box)) {
            double distance = getDistance(player);
            if (distance > 10D) continue;
            Vec3d push = player.getPositionVector().subtract(getPositionVector()).normalize();
            player.attackEntityFrom(
                    DamageSource.causeMobDamage(this), (float) Math.max(8D, 27D - distance * 1.6D));
            player.addVelocity(push.x * 1.25D, .55D, push.z * 1.25D);
        }
        impact(getPositionVector().addVector(0, .15, 0), 70);
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                1.3F,
                .55F);
    }

    private void overload(EntityLivingBase target) {
        Vec3d center = getPositionVector().addVector(0, 2.1, 0);
        for (EntityPlayer player :
                world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(18D))) {
            double d = getDistance(player);
            if (d > 18D) continue;
            player.attackEntityFrom(
                    DamageSource.causeMobDamage(this).setMagicDamage(),
                    (float) Math.max(7D, 25D - d));
            beamParticles(
                    center,
                    player.getPositionVector().addVector(0, 1, 0),
                    EnumParticleTypes.END_ROD,
                    20,
                    .04);
        }
        impact(center, 90);
    }

    private void mortar(EntityLivingBase target, int tick) {
        EntityScoutBomb bomb = new EntityScoutBomb(world, this);
        double side = tick == 31 ? 0 : (tick == 24 ? -1.3 : 1.3);
        Vec3d launch = localOffset(side, 3.3, -.8);
        bomb.setPosition(launch.x, launch.y, launch.z);
        double dx = target.posX - bomb.posX,
                dz = target.posZ - bomb.posZ,
                h = Math.sqrt(dx * dx + dz * dz);
        bomb.shoot(dx, target.posY - bomb.posY + h * .18, dz, .78F, 2.0F);
        world.spawnEntity(bomb);
        world.playSound(
                null,
                launch.x,
                launch.y,
                launch.z,
                SoundEvents.ENTITY_FIREWORK_LAUNCH,
                SoundCategory.HOSTILE,
                .85F,
                .72F);
    }

    private Vec3d traceEnd(Vec3d start, Vec3d direction, double range) {
        Vec3d end = start.add(direction.scale(range));
        RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
        return result == null ? end : result.hitVec;
    }

    private void beamParticles(
            Vec3d start, Vec3d end, EnumParticleTypes particle, int steps, double spread) {
        if (!(world instanceof WorldServer)) return;
        WorldServer server = (WorldServer) world;
        Vec3d beam = end.subtract(start);
        for (int i = 0; i <= steps; i++) {
            Vec3d p = start.add(beam.scale(i / (double) steps));
            server.spawnParticle(
                    particle, p.x, p.y, p.z, 1, spread, spread, spread, .01, new int[0]);
        }
    }

    private void impact(Vec3d p, int count) {
        com.scapeandrun.frostbite.network.PacketScoutImpact.send(this, p, count >= 24 ? 1 : 0);
    }

    public void onHardpointHit(EntityScoutHardpoint hardpoint, float amount) {
        if (world instanceof WorldServer && rand.nextFloat() < .75F) {
            Vec3d p = hardpoint.getPositionVector().addVector(0, hardpoint.height * .5, 0);
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.FIREWORKS_SPARK,
                            p.x,
                            p.y,
                            p.z,
                            Math.min(12, 2 + (int) amount),
                            .25,
                            .25,
                            .25,
                            .11,
                            new int[0]);
        }
        updateBossBar();
    }

    public void onHardpointDestroyed(int kind) {
        if (world.isRemote) return;
        int mask = getHardpointMask() & ~(1 << kind);
        dataManager.set(HARDPOINT_MASK, mask);
        clearAttack();
        attackCooldown = 32;
        impact(getHardpointPosition(kind), 55);
        world.playSound(
                null,
                posX,
                posY + 2,
                posZ,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.HOSTILE,
                1.05F,
                .8F);
        if (mask == 0) {
            dataManager.set(PHASE, PHASE_EXPOSED);
            bossInfo.setColor(BossInfo.Color.YELLOW);
            bossInfo.setOverlay(BossInfo.Overlay.NOTCHED_10);
            attackCooldown = 70;
            world.playSound(
                    null,
                    posX,
                    posY + 2,
                    posZ,
                    SoundEvents.ENTITY_WITHER_BREAK_BLOCK,
                    SoundCategory.HOSTILE,
                    1.3F,
                    .55F);
            impact(getPositionVector().addVector(0, 2.2, 0), 110);
        }
    }

    private void enterDuoPhase() {
        dataManager.set(PHASE, PHASE_DUO);
        bossInfo.setColor(BossInfo.Color.PURPLE);
        attackCooldown = 75;
        impact(getPositionVector().addVector(0, 3.0, 0), 140);
        world.playSound(
                null,
                posX,
                posY + 3,
                posZ,
                SoundEvents.BLOCK_GLASS_BREAK,
                SoundCategory.HOSTILE,
                1.4F,
                .68F);
        EntityX20Pilot pilot = new EntityX20Pilot(world, this);
        Vec3d exit = localOffset(0, 3.7, .2);
        pilot.setPosition(exit.x, exit.y, exit.z);
        pilot.motionY = .65D;
        pilot.motionX = -Math.sin(Math.toRadians(rotationYaw)) * .7D;
        pilot.motionZ = Math.cos(Math.toRadians(rotationYaw)) * .7D;
        pilot.setAttackTarget(getAttackTarget());
        world.spawnEntity(pilot);
        pilotUuid = pilot.getUniqueID();
    }

    @Override
    public void fall(float distance, float multiplier) {}

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (isOverheating() && source != DamageSource.OUT_OF_WORLD) return false;
        if (source.isFireDamage() || source == DamageSource.FALL) return false;
        if (getScene() != 0 && source != DamageSource.OUT_OF_WORLD) return false;
        Vec3d origin = source.getDamageLocation();
        if (isGuarding()
                && origin != null
                && source != DamageSource.OUT_OF_WORLD
                && !source.isUnblockable()) {
            Vec3d delta = origin.subtract(getPositionVector());
            delta = new Vec3d(delta.x, 0, delta.z).normalize();
            Vec3d facing = getLookVec();
            facing = new Vec3d(facing.x, 0, facing.z).normalize();
            if (delta.dotProduct(facing) > .35) {
                if (!world.isRemote)
                    world.playSound(
                            null,
                            posX,
                            posY + 4,
                            posZ,
                            SoundEvents.ITEM_SHIELD_BLOCK,
                            SoundCategory.HOSTILE,
                            .8F,
                            .6F);
                return false;
            }
        }
        return super.attackEntityFrom(source, amount);
    }

    @Override
    public boolean attackEntityAsMob(Entity entity) {
        return false;
    }

    private void updateBossBar() {
        bossInfo.setPercent(
                isOverheating()
                        ? ScoutOverheat.remaining(getOverheatTick())
                        : getHealth() / getMaxHealth());
    }

    private void tickOverheat() {
        int t = getOverheatTick() + 1;
        dataManager.set(OVERHEAT, t);
        navigator.clearPath();
        if (finalDamage.getTrueSource() instanceof EntityPlayer) recentlyHit = 100;
        if (t >= ScoutOverheat.DURATION) {
            dataManager.set(OVERHEAT, -1);
            setHealth(0);
            onDeath(finalDamage);
            return;
        }
        EntityLivingBase target = getAttackTarget();
        if (target == null || !target.isEntityAlive())
            target = world.getNearestAttackablePlayer(this, 56, 32);
        if (target != null) {
            setAttackTarget(target);
            if (t % 80 == 4 || t % 80 == 40) {
                setAim(target.getPositionVector().addVector(0, 1, 0));
                faceTarget(target);
                attackDirection = getAim().subtract(getPositionVector()).normalize();
            }
            if (ScoutOverheat.dash(t)) {
                setNoGravity(true);
                motionX = attackDirection.x * 1.35;
                motionY = attackDirection.y * .65;
                motionZ = attackDirection.z * 1.35;
                if (t % 6 == 0) {
                    contactVictims.clear();
                    arcContact(6, .1, 10, 0);
                }
            } else {
                motionX *= .7;
                motionZ *= .7;
                setNoGravity(false);
            }
            if (ScoutOverheat.volley(t)) {
                setAim(target.getPositionVector().addVector(0, 1, 0));
                firePhaseGun(t % 40 == 0);
                EntityScoutBomb bomb = new EntityScoutBomb(world, this);
                Vec3d p = localOffset(t % 20 == 0 ? -2 : 2, 5, 1);
                bomb.setPosition(p.x, p.y, p.z);
                Vec3d d = getAim().subtract(p);
                bomb.shoot(d.x, d.y + d.lengthVector() * .12, d.z, 1.05F, 1);
                world.spawnEntity(bomb);
            }
            if (ScoutOverheat.glacier(t)) {
                for (int i = 0; i < 2; i++)
                    world.spawnEntity(
                            EntityScoutShard.overheatGlacier(
                                    world,
                                    this,
                                    localOffset(i == 0 ? -2 : 2, 5, 0),
                                    target.getPositionVector().addVector(i == 0 ? -2 : 2, 0, 0),
                                    i));
            }
        }
        if (t % 4 == 0 && world instanceof WorldServer) {
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.FLAME, posX, posY + 5, posZ, 8, 1.4, 2, 1.4, .06);
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.SMOKE_LARGE, posX, posY + 7, posZ, 4, 1, 1, 1, .035);
        }
        if (t % 40 == 32) {
            world.newExplosion(this, posX, posY + 2, posZ, 2.5F, false, false);
            for (EntityLivingBase victim :
                    world.getEntitiesWithinAABB(
                            EntityLivingBase.class, getEntityBoundingBox().grow(3)))
                if (validVictim(victim)) victim.setFire(3);
        }
        updateMovementState();
        updateBossBar();
    }

    private void updateMovementState() {
        boolean landed = onGround && !wasOnGround;
        if (landed) {
            landingTicks = 10;
            world.playSound(
                    null,
                    posX,
                    posY,
                    posZ,
                    SoundEvents.ENTITY_IRONGOLEM_STEP,
                    SoundCategory.HOSTILE,
                    1.2F,
                    .52F);
        }
        int state;
        if (landingTicks > 0) {
            landingTicks--;
            state = 5;
        } else if (!onGround) {
            state = motionY > .075 ? 3 : 4;
        } else {
            double horizontal = motionX * motionX + motionZ * motionZ;
            float turn = net.minecraft.util.math.MathHelper.wrapDegrees(rotationYaw - lastMoveYaw);

            double runThreshold = dataManager.get(MOVE_STATE) == 1 ? .018 : .025;
            if (horizontal > runThreshold) state = 1;
            else if (horizontal > .0004) state = 2;
            else if (turn > 2.2) state = 7;
            else if (turn < -2.2) state = 6;
            else state = 0;
        }
        if (dataManager.get(MOVE_STATE) != state) dataManager.set(MOVE_STATE, state);
        wasOnGround = onGround;
        lastMoveYaw = rotationYaw;
    }

    private void clientEffects() {
        int attack = getAttack(), tick = getAttackTick();
        if (getPhase() == PHASE_HARDPOINTS && ticksExisted % 2 == 0) {
            double a = rand.nextDouble() * Math.PI * 2, r = 3.25 + rand.nextDouble() * .2;
            world.spawnParticle(
                    EnumParticleTypes.REDSTONE,
                    posX + Math.cos(a) * r,
                    posY + .7 + rand.nextDouble() * 3.0,
                    posZ + Math.sin(a) * r,
                    .1,
                    .75,
                    1.0);
        }
        if ((attack == ATTACK_LASER_LEFT
                        || attack == ATTACK_LASER_RIGHT
                        || attack == ATTACK_SYNC_BEAM)
                && tick < 46) {
            Vec3d p =
                    getMuzzlePosition(
                            attack == ATTACK_LASER_RIGHT
                                    ? EntityScoutHardpoint.LASER_RIGHT
                                    : EntityScoutHardpoint.LASER_LEFT);
            for (int i = 0; i < 2; i++)
                world.spawnParticle(
                        EnumParticleTypes.END_ROD,
                        p.x + (rand.nextDouble() - .5) * .3,
                        p.y + (rand.nextDouble() - .5) * .3,
                        p.z + (rand.nextDouble() - .5) * .3,
                        0,
                        0,
                        0);
        }
    }

    public Vec3d getHardpointPosition(int kind) {
        switch (kind) {
            case EntityScoutHardpoint.MINIGUN_LEFT:
                return localOffset(-7.125, 6.181, 1.6);
            case EntityScoutHardpoint.MINIGUN_RIGHT:
                return getAttack() == ScoutCombatPattern.STUN
                        ? localOffset(6.925, 4.681, 1.4)
                        : localOffset(5.925, 5.081, 1.4);
            case EntityScoutHardpoint.LASER_LEFT:
                return localOffset(-5.525, 7.981, 0);
            case EntityScoutHardpoint.LASER_RIGHT:
                return localOffset(5.525, 7.981, 0);
            case EntityScoutHardpoint.CLAW:
                return getDetachedVice(false, 0);
            default:
                return getDetachedVice(true, 0);
        }
    }

    public Vec3d getMuzzlePosition(int kind) {
        Vec3d base = getHardpointPosition(kind);
        double forward =
                kind == EntityScoutHardpoint.LASER_LEFT || kind == EntityScoutHardpoint.LASER_RIGHT
                        ? 1.7
                        : 1.55;
        double yaw = Math.toRadians(-rotationYaw);
        return base.addVector(
                -Math.sin(yaw) * forward,
                kind >= EntityScoutHardpoint.LASER_LEFT && kind <= EntityScoutHardpoint.LASER_RIGHT
                        ? .12
                        : 0,
                Math.cos(yaw) * forward);
    }

    public Vec3d getReactorEmitterPosition() {
        return localOffset(0, 5.05, 1.05);
    }

    private Vec3d localOffset(double side, double up, double forward) {
        double[] offset = ScoutSpace.offset(rotationYaw, side, up, forward);
        return getPositionVector().addVector(offset[0], offset[1], offset[2]);
    }

    @Nullable
    public EntityX20Pilot getPilot() {
        if (!world.isRemote && pilotUuid != null && world instanceof WorldServer) {
            Entity entity = ((WorldServer) world).getEntityFromUuid(pilotUuid);
            if (entity instanceof EntityX20Pilot && entity.isEntityAlive())
                return (EntityX20Pilot) entity;
        }
        for (Entity entity : world.loadedEntityList)
            if (entity instanceof EntityX20Pilot
                    && entity.isEntityAlive()
                    && ((EntityX20Pilot) entity).scout() == this) return (EntityX20Pilot) entity;
        return null;
    }

    private void setAim(Vec3d p) {
        dataManager.set(AIM_X, (float) p.x);
        dataManager.set(AIM_Y, (float) p.y);
        dataManager.set(AIM_Z, (float) p.z);
    }

    public Vec3d getAim() {
        return new Vec3d(dataManager.get(AIM_X), dataManager.get(AIM_Y), dataManager.get(AIM_Z));
    }

    private static int kindForAttack(int attack) {
        switch (attack) {
            case ATTACK_MINIGUN_LEFT:
                return EntityScoutHardpoint.MINIGUN_LEFT;
            case ATTACK_MINIGUN_RIGHT:
                return EntityScoutHardpoint.MINIGUN_RIGHT;
            case ATTACK_LASER_LEFT:
                return EntityScoutHardpoint.LASER_LEFT;
            case ATTACK_LASER_RIGHT:
                return EntityScoutHardpoint.LASER_RIGHT;
            case ATTACK_CLAW:
                return EntityScoutHardpoint.CLAW;
            case ATTACK_VICE:
                return EntityScoutHardpoint.VICE;
            default:
                return -1;
        }
    }

    private static int duration(int attack) {
        switch (attack) {
            case ATTACK_MINIGUN_LEFT:
            case ATTACK_MINIGUN_RIGHT:
                return 52;
            case ATTACK_LASER_LEFT:
            case ATTACK_LASER_RIGHT:
                return 56;
            case ATTACK_CLAW:
                return 39;
            case ATTACK_VICE:
                return 54;
            case ATTACK_STOMP:
                return 52;
            case ATTACK_OVERLOAD:
                return 65;
            case ATTACK_MORTAR:
                return 52;
            case ATTACK_SYNC_BEAM:
                return 62;
            default:
                return 1;
        }
    }

    private static int cooldown(int attack) {
        return attack == ATTACK_STOMP
                ? 42
                : attack == ATTACK_OVERLOAD
                        ? 58
                        : attack == ATTACK_MORTAR ? 34 : attack == ATTACK_SYNC_BEAM ? 48 : 22;
    }

    private void clearAttack() {
        releaseGrab();
        setNoGravity(false);
        dataManager.set(PROJECTILE_KIND, 0);
        if (getAttack() != ATTACK_NONE) dataManager.set(ATTACK, ATTACK_NONE);
        dataManager.set(ATTACK_TICK, 0);
    }

    public int getAttack() {
        return dataManager.get(ATTACK);
    }

    public int getAttackTick() {
        return dataManager.get(ATTACK_TICK);
    }

    public int getPhase() {
        return dataManager.get(PHASE);
    }

    public int getHardpointMask() {
        return dataManager.get(HARDPOINT_MASK);
    }

    public boolean isHardpointAlive(int kind) {
        return kind >= 0 && (getHardpointMask() & (1 << kind)) != 0;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (isExpert()) {
            dropItem(ModContent.SCOUT_TREASURE_BAG, 1);
            return;
        }
        entityDropItem(new ItemStack(ModContent.MACHINED_CORE, 2), 0);
        entityDropItem(new ItemStack(ModContent.FRIGID_METAL, 8 + rand.nextInt(7)), 0);
        entityDropItem(new ItemStack(ModContent.KX_GEAR, 2 + rand.nextInt(3)), 0);
        switch (rand.nextInt(4)) {
            case 0:
                dropItem(ModContent.SCOUT_PINCER, 1);
                break;
            case 1:
                dropItem(ModContent.MODIFIED_RAILGUN, 1);
                break;
            case 2:
                dropItem(ModContent.GLACIER_SMASHER, 1);
                break;
            default:
                dropItem(ModContent.AURORA_BOREALIS, 1);
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.getTotalWorldTime() / 24000L >= 100L
                && world.getBiome(getPosition()) == ModBiomes.FROZEN_HILLS
                && world.getEntitiesWithinAABB(
                                EntityX20Scout.class, getEntityBoundingBox().grow(192D))
                        .isEmpty()
                && rand.nextInt(60) == 0
                && super.getCanSpawnHere();
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public boolean canDespawn() {
        return false;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        bossInfo.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bossInfo.removePlayer(p);
    }

    @Override
    public void setCustomNameTag(String n) {
        super.setCustomNameTag(n);
        bossInfo.setName(getDisplayName());
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("ScoutIntroDone", introDone);
        tag.setInteger("ScoutPhase", getPhase());
        tag.setInteger("HardpointMask", getHardpointMask());
        tag.setBoolean("ScoutExpert", isExpert());
        tag.setBoolean("ScoutMaster", master);
        tag.setBoolean("ScoutOverheatSpent", overheatSpent);
        tag.setInteger("ScoutOverheat", getOverheatTick());
        tag.setBoolean("ScoutIntermissionDone", expertIntermissionDone);
        tag.setInteger("ScoutAttackCursor", attackCursor);
        tag.setBoolean("ScoutQueuedVice", phaseShotQueued);
        tag.setBoolean("ScoutNextViceSnap", nextCannon);
        tag.setTag("ScoutExpertWaves", expertWaves.save());
        if (getScene() == 8) tag.setInteger("ScoutWaveSceneTick", getSceneTick());
        if (getScene() == 3 || getScene() == 6 || getScene() == 9) {
            tag.setInteger("ScoutTransition", getScene());
            tag.setInteger("ScoutTransitionTick", getSceneTick());
        }
        if (getScene() == 7) tag.setInteger("ScoutDefeatTick", getSceneTick());
        Vec3d p = getDroppedPosition();
        tag.setDouble("ScoutDropX", p.x);
        tag.setDouble("ScoutDropY", p.y);
        tag.setDouble("ScoutDropZ", p.z);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        introDone = !tag.hasKey("ScoutIntroDone") || tag.getBoolean("ScoutIntroDone");
        setNoGravity(false);
        difficultyChosen = tag.hasKey("ScoutExpert");
        dataManager.set(EXPERT, tag.getBoolean("ScoutExpert"));
        master = tag.getBoolean("ScoutMaster");
        overheatSpent = tag.getBoolean("ScoutOverheatSpent");
        dataManager.set(
                OVERHEAT,
                tag.hasKey("ScoutOverheat")
                        ? Math.max(-1, Math.min(399, tag.getInteger("ScoutOverheat")))
                        : -1);
        expertIntermissionDone = tag.getBoolean("ScoutIntermissionDone");
        expertWaves.load(tag.getCompoundTag("ScoutExpertWaves"));
        if (tag.hasKey("ScoutWaveSceneTick")) {
            dataManager.set(SCENE, 8);
            dataManager.set(SCENE_TICK, tag.getInteger("ScoutWaveSceneTick"));
            setNoGravity(true);
        }
        int transition = tag.getInteger("ScoutTransition");
        if (transition == 3 || transition == 6 || transition == 9) {
            dataManager.set(SCENE, transition);
            dataManager.set(SCENE_TICK, Math.max(0, tag.getInteger("ScoutTransitionTick")));
            setNoGravity(transition == 9);
        }
        if (tag.hasKey("ScoutDefeatTick")) {
            dataManager.set(SCENE, 7);
            dataManager.set(
                    SCENE_TICK,
                    Math.max(
                            0,
                            Math.min(
                                    com.scapeandrun.frostbite.client.ScoutDefeatPose.END - 1,
                                    tag.getInteger("ScoutDefeatTick"))));
            setHealth(0);
            bossInfo.setVisible(false);
        }
        dataManager.set(
                PHASE,
                Math.min(PHASE_BRAWL, Math.max(PHASE_EXPOSED, tag.getInteger("ScoutPhase"))));
        dataManager.set(HARDPOINT_MASK, ALL_HARDPOINTS);
        attackCursor =
                Math.floorMod(
                        tag.getInteger("ScoutAttackCursor"),
                        ScoutCombatPattern.sequenceLength(isExpert(), getPhase()));
        phaseShotQueued =
                !isExpert() && getPhase() == PHASE_EXPOSED && tag.getBoolean("ScoutQueuedVice");
        nextCannon = tag.getBoolean("ScoutNextViceSnap");
        dataManager.set(
                DROP_X, (float) (tag.hasKey("ScoutDropX") ? tag.getDouble("ScoutDropX") : posX));
        dataManager.set(
                DROP_Y, (float) (tag.hasKey("ScoutDropY") ? tag.getDouble("ScoutDropY") : posY));
        dataManager.set(
                DROP_Z, (float) (tag.hasKey("ScoutDropZ") ? tag.getDouble("ScoutDropZ") : posZ));
        if (getPhase() >= PHASE_DUO)
            getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .setBaseValue(getPhase() == PHASE_BRAWL ? .28 : .24);
    }

    @Override
    public void setDead() {
        if (!world.isRemote) {
            releaseGrab();
            expertWaves.clear(this);
            EntityScoutArena field = arena();
            if (field != null) field.finish();
            for (int id : hardpointIds) {
                Entity e = world.getEntityByID(id);
                if (e != null) e.setDead();
            }
        }
        super.setDead();
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!world.isRemote && master && !overheatSpent && source != DamageSource.OUT_OF_WORLD) {
            overheatSpent = true;
            finalDamage = source;
            setHealth(1);
            deathTime = 0;
            releaseGrab();
            clearAttack();
            dataManager.set(SCENE, 0);
            dataManager.set(SCENE_TICK, 0);
            dataManager.set(PHASE, PHASE_BRAWL);
            dataManager.set(HAMMER_FLIGHT, 0);
            dataManager.set(OVERHEAT, 0);
            bossInfo.setColor(BossInfo.Color.RED);
            bossInfo.setVisible(true);
            return;
        }
        if (!world.isRemote) {
            startScene(7);
            bossInfo.setVisible(false);
        }
        setNoGravity(false);
        super.onDeath(source);
    }

    @Override
    protected void onDeathUpdate() {

        motionX = motionZ = 0;
        if (!world.isRemote) {
            dataManager.set(SCENE, 7);
            int t = getSceneTick() + 1;
            dataManager.set(SCENE_TICK, t);
            if (t == 40)
                world.playSound(
                        null,
                        posX,
                        posY + 7,
                        posZ,
                        SoundEvents.BLOCK_GLASS_BREAK,
                        SoundCategory.HOSTILE,
                        2,
                        .65F);
            if (t == 40 && world instanceof WorldServer) {
                Vec3d glass = localOffset(0, 8, 1);
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.BLOCK_CRACK,
                                glass.x,
                                glass.y,
                                glass.z,
                                48,
                                .7,
                                .8,
                                .25,
                                .2,
                                net.minecraft.block.Block.getStateId(
                                        net.minecraft.init.Blocks.GLASS.getDefaultState()));
            }
            if (t == 70 || t == 100 || t == 156)
                world.playSound(
                        null,
                        posX,
                        posY,
                        posZ,
                        SoundEvents.ENTITY_IRONGOLEM_DEATH,
                        SoundCategory.HOSTILE,
                        2,
                        .6F);
            if (t == 166) say("Well");
            if (t == 245) say("Shit..");
            if (t == 325)
                say(
                        "We will meet again, and I will bring my newest mech, straight out of the assembly line. I am curious of your true power.");
            if (t == 520)
                say("We will be rivals, for all eternity, til' the end of time, Farewell.");
            if (world instanceof WorldServer && t < 175 && t % 4 == 0)
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.SMOKE_LARGE,
                                posX,
                                posY + (t < 120 ? 7 : 1),
                                posZ,
                                5,
                                1,
                                .5,
                                1,
                                .025);
            if (world instanceof WorldServer
                    && t >= com.scapeandrun.frostbite.client.ScoutDefeatPose.ESCAPE_START
                    && t % 2 == 0) {
                float y = com.scapeandrun.frostbite.client.ScoutDefeatPose.pilotHeight(t);
                float z = com.scapeandrun.frostbite.client.ScoutDefeatPose.pilotDepth(t);
                float x = com.scapeandrun.frostbite.client.ScoutDefeatPose.pilotSide(t);
                Vec3d exhaust = localOffset(x * 2.35 / 16, (y - 2) * 2.35 / 16, -z * 2.35 / 16);
                ((WorldServer) world)
                        .spawnParticle(
                                EnumParticleTypes.FLAME,
                                exhaust.x,
                                exhaust.y,
                                exhaust.z,
                                4,
                                .12,
                                .15,
                                .12,
                                .02);
            }
            if (t >= com.scapeandrun.frostbite.client.ScoutDefeatPose.END) {
                deathTime = 19;
                super.onDeathUpdate();
            }
        }
    }

    private <E extends IAnimatable> PlayState animation(AnimationEvent<E> event) {
        int scene = getScene();
        int attack = getAttack();
        int tick = getAttackTick();
        int movement = dataManager.get(MOVE_STATE);
        boolean landed = dataManager.get(LAND_TICK) >= 0;
        String clip = ScoutAnimationRules.clip(scene, attack, tick, movement, landed);
        boolean loop = ScoutAnimationRules.loops(scene, attack, tick, movement, landed);
        event.getController()
                .setAnimation(
                        new AnimationBuilder().addAnimation("animation.x20_scout." + clip, loop));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<EntityX20Scout>(this, "x20_scout", 2, this::animation));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }
}
