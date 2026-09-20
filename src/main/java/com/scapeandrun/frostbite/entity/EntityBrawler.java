package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;

public final class EntityBrawler extends EntityLiving implements IEntityMultiPart {
    private static final DataParameter<Integer>
            PHASE = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            CLOCK = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            ATTACK = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            TRANSITION = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            A = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            B = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            C = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            D = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT);
    private final MultiPartEntityPart[] arms = new MultiPartEntityPart[4];
    private final int[] hurt = new int[4];
    private final EntityBrawlerArm[] targets = new EntityBrawlerArm[4];
    private static final DataParameter<Integer> COUNTER_CONTACT =
            EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);
    private static final DataParameter<String> BAR_ID =
            EntityDataManager.createKey(EntityBrawler.class, DataSerializers.STRING);
    private final BossInfoServer bar =
            new BossInfoServer(
                    new TextComponentString("X-05 \"Brawler\""),
                    BossInfo.Color.GREEN,
                    BossInfo.Overlay.PROGRESS);
    private final BrawlerCombat combat = new BrawlerCombat(this);
    private final BrawlerAerialCombat aerial = new BrawlerAerialCombat(this);
    private final BrawlerLimiterCombat limiter = new BrawlerLimiterCombat(this);
    private final BrawlerIntro opening = new BrawlerIntro(this);
    private final BrawlerMartialCombat martial = new BrawlerMartialCombat(this);
    private static final DataParameter<Integer>
            MARTIAL = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            M_REACTION = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            M_TIME = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            M_VICTIM = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            M_BLOWN = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);

    {
        dataManager.register(MARTIAL, -1);
        dataManager.register(M_REACTION, 0);
        dataManager.register(M_TIME, 0);
        dataManager.register(M_VICTIM, -1);
        dataManager.register(M_BLOWN, -1);
    }

    public boolean bipedal() {
        return expert() && phase() == 3;
    }

    public int martialAttack() {
        return dataManager.get(MARTIAL);
    }

    public int martialVictim() {
        return dataManager.get(M_VICTIM);
    }

    public int martialBlownArm() {
        return dataManager.get(M_BLOWN);
    }

    void martialVictim(int id) {
        dataManager.set(M_VICTIM, id);
    }

    void martialBlownArm(int arm) {
        dataManager.set(M_BLOWN, arm);
    }

    void martialReaction(int kind, int tick) {
        dataManager.set(M_REACTION, kind);
        dataManager.set(M_TIME, tick);
    }

    void advanceMartial() {
        dataManager.set(
                MARTIAL, (martialAttack() + 1) % BrawlerMartialScore.Attack.values().length);
        dataManager.set(CLOCK, 0);
    }

    void beginMartialCoordination() {
        martial.reset();
        clearCoordinationHands();
    }

    void coordinationMartial(BrawlerMartialScore.Attack attack, int tick) {
        dataManager.set(MARTIAL, attack.ordinal());
        dataManager.set(CLOCK, Math.max(0, tick));
    }

    void endMartialCoordination() {
        martial.reset();
        clearCoordinationHands();
        dataManager.set(CLOCK, 0);
    }

    public BrawlerMartialPose martialPose(float partial) {
        return BrawlerMartialPose.sample(
                BrawlerMartialScore.decode(Math.max(0, martialAttack())),
                transition() > 0 ? 0 : clock() + partial,
                dataManager.get(M_REACTION),
                dataManager.get(M_TIME) + partial);
    }

    public double bodyHeight(float partial) {
        if (!bipedal()) return 1.4;
        BrawlerMartialPose p = martialPose(partial);
        double height = 4.55 - p.crouch - 2.5 * Math.abs(Math.sin(Math.toRadians(p.pitch)));
        return transition() > 0
                ? 1.4 + (height - 1.4) * BrawlerScore.smooth((75 - transition() + partial) / 45D)
                : height;
    }

    public Vec3d footWorld(int side, float partial) {
        return localToWorld(martialPose(partial).foot(side));
    }

    private static final DataParameter<Integer>
            LIMITER = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            ENGINES = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            INTRO = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Integer>
            AERIAL = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            AIR_RECOVERY = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean> EXPERT =
            EntityDataManager.createKey(EntityBrawler.class, DataSerializers.BOOLEAN);
    private static final DataParameter<Float>
            AIM_X = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            AIM_Y = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            AIM_Z = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> SHIELD =
            EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT);
    private boolean parryContact;
    private Vec3d martialContact;
    private int contactArm = -1;
    private Vec3d clashStand;
    private static final DataParameter<Integer>
            CLASH = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            CLASH_PLAYER = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            CLASH_ARM = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT),
            SHIELD_HITS = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            FIST_X = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            FIST_Y = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT),
            FIST_Z = EntityDataManager.createKey(EntityBrawler.class, DataSerializers.FLOAT);
    private int stun, parries;
    private static final DataParameter<net.minecraft.nbt.NBTTagCompound> COORD_HANDS =
            EntityDataManager.createKey(EntityBrawler.class, DataSerializers.COMPOUND_TAG);

    {
        dataManager.register(COORD_HANDS, new net.minecraft.nbt.NBTTagCompound());
    }

    void clearCoordinationHands() {
        dataManager.set(COORD_HANDS, new net.minecraft.nbt.NBTTagCompound());
    }

    void coordinationHand(int arm, Vec3d world, float grip) {
        Vec3d d =
                world.subtract(getPositionVector().addVector(0, bodyHeight(0), 0))
                        .rotateYaw((float) Math.toRadians(rotationYaw));
        double angle = Math.toRadians(-flightRoll(0));
        Vec3d local =
                new Vec3d(
                        d.x * Math.cos(angle) - d.y * Math.sin(angle),
                        d.x * Math.sin(angle) + d.y * Math.cos(angle),
                        d.z);
        if (bipedal()) {
            BrawlerMartialPose p = martialPose(0);
            local =
                    BrawlerMartialPose.rotateX(local, -p.pitch)
                            .rotateYaw((float) Math.toRadians(-p.yaw));
        }
        net.minecraft.nbt.NBTTagCompound n = dataManager.get(COORD_HANDS).copy();
        n.setDouble("x" + arm, local.x);
        n.setDouble("y" + arm, local.y);
        n.setDouble("z" + arm, local.z);
        n.setFloat("g" + arm, MathHelper.clamp(grip, 0, 1));
        dataManager.set(COORD_HANDS, n);
    }

    public boolean coordinatedHand(int arm) {
        return dataManager.get(COORD_HANDS).hasKey("x" + arm);
    }

    public float coordinationGrip(int arm) {
        return coordinatedHand(arm) ? dataManager.get(COORD_HANDS).getFloat("g" + arm) : 1;
    }

    public float weaponGrip(int arm, float partial) {
        if (coordinatedHand(arm)) return coordinationGrip(arm);
        if (clashTick() > 0 || introTick() > 0) return 1;
        if (aerialAttack() >= 0)
            return (float)
                    MathHelper.clamp(.72 + Math.max(0, hand(arm, partial).z - .5) * .1, 0, 1);
        return MathHelper.clamp(BrawlerScore.grip(pattern(), arm, clock() + partial), 0, 1);
    }

    void coordinationAnimation(WulfrumCoordinationScore.Pattern pattern, int tick) {
        NBTTagCompound n = dataManager.get(COORD_HANDS).copy();
        for (int arm = 0; arm < 4; arm++) {
            for (String axis : new String[] {"x", "y", "z"})
                if (n.hasKey(axis + arm)) n.setDouble("p" + axis + arm, n.getDouble(axis + arm));
            n.removeTag("wx" + arm);
            n.removeTag("wy" + arm);
            n.removeTag("wz" + arm);
        }
        n.setInteger("pattern", pattern.ordinal());
        n.setInteger("tick", tick);
        dataManager.set(COORD_HANDS, n);
    }

    public boolean coordinating() {
        return dataManager.get(COORD_HANDS).hasKey("pattern");
    }

    public double[] coordinationWrist(int arm, float partial) {
        NBTTagCompound n = dataManager.get(COORD_HANDS);
        if (n.hasKey("wx" + arm))
            return new double[] {
                n.getDouble("wx" + arm), n.getDouble("wy" + arm), n.getDouble("wz" + arm)
            };
        return WulfrumPairPose.wrist(
                WulfrumCoordinationScore.Pattern.values()[n.getInteger("pattern")],
                arm,
                n.getInteger("tick") + partial);
    }

    void coordinationWrist(int arm, Vec3d direction, double roll) {
        Vec3d d = direction.normalize().rotateYaw((float) Math.toRadians(rotationYaw));
        double a = Math.toRadians(-flightRoll(0));
        Vec3d local =
                new Vec3d(
                        d.x * Math.cos(a) - d.y * Math.sin(a),
                        d.x * Math.sin(a) + d.y * Math.cos(a),
                        d.z);
        NBTTagCompound n = dataManager.get(COORD_HANDS).copy();
        n.setDouble("wx" + arm, -Math.toDegrees(Math.atan2(local.y, Math.hypot(local.x, local.z))));
        n.setDouble("wy" + arm, Math.toDegrees(Math.atan2(local.x, local.z)));
        n.setDouble("wz" + arm, roll);
        dataManager.set(COORD_HANDS, n);
    }

    {
        dataManager.register(LIMITER, -1);
        dataManager.register(ENGINES, 1);
        dataManager.register(INTRO, 1);
    }

    public int limiterAttack() {
        return dataManager.get(LIMITER);
    }

    public int engineMask() {
        return dataManager.get(ENGINES);
    }

    void engineMask(int mask) {
        dataManager.set(ENGINES, mask);
    }

    public int introTick() {
        return dataManager.get(INTRO);
    }

    void introTick(int tick) {
        dataManager.set(INTRO, tick);
        dataManager.set(TRANSITION, 0);
        if (tick == 0) getEntityData().setBoolean("BrawlerIntroFinished", true);
    }

    void advanceLimiter() {
        dataManager.set(
                LIMITER, (limiterAttack() + 1) % BrawlerLimiterScore.Attack.values().length);
        dataManager.set(CLOCK, 0);
    }

    void introFistTarget(Vec3d target) {
        Vec3d d =
                target.subtract(getPositionVector().addVector(0, 1.4, 0))
                        .rotateYaw((float) Math.toRadians(rotationYaw));
        dataManager.set(FIST_X, (float) d.x);
        dataManager.set(FIST_Y, (float) d.y);
        dataManager.set(FIST_Z, (float) d.z);
    }

    public boolean tryIntroParry(EntityPlayer player) {
        if (world.isRemote
                || introTick() < 148
                || introTick() > 160
                || player.getDistanceSq(this) > 1024
                || clashTick() > 0) return false;
        dataManager.set(INTRO, 171);
        contactArm = 0;
        parryContact = true;
        try {
            parried(player);
        } finally {
            parryContact = false;
            contactArm = -1;
        }
        return true;
    }

    public EntityBrawler(World w) {
        super(w);
        dataManager.register(AERIAL, -1);
        dataManager.register(AIR_RECOVERY, 0);
        dataManager.register(EXPERT, false);
        dataManager.register(AIM_X, 0F);
        dataManager.register(AIM_Y, 0F);
        dataManager.register(AIM_Z, 4F);
        setSize(2.8F, 2.8F);
        setNoAI(true);
        setNoGravity(true);
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        for (int i = 0; i < 4; i++) {
            final int index = i;
            arms[i] =
                    new MultiPartEntityPart(this, "weapon_" + i, 1.7F, 1.7F) {
                        @Override
                        public boolean canBeCollidedWith() {
                            return EntityBrawler.this.isEntityAlive()
                                    && phase() == 1
                                    && armHealth(index) > 0;
                        }
                    };
        }
    }

    public boolean expert() {
        return dataManager.get(EXPERT);
    }

    public int aerialAttack() {
        return dataManager.get(AERIAL);
    }

    public int aerialRecovery() {
        return dataManager.get(AIR_RECOVERY);
    }

    void aerialRecovery(int ticks) {
        dataManager.set(AIR_RECOVERY, ticks);
    }

    public double flightRoll(float partial) {
        if (clashTick() > 0) return 0;
        if (coordinating()) {
            NBTTagCompound n = dataManager.get(COORD_HANDS);
            return WulfrumPairPose.bank(
                    WulfrumCoordinationScore.Pattern.values()[n.getInteger("pattern")],
                    n.getInteger("tick") + partial);
        }
        if (introTick() > 50 && introTick() < 96) return (introTick() + partial - 51) * 360 / 45;
        if (limiterAttack() >= 0)
            return aerialRecovery() > 0
                    ? Math.sin(aerialRecovery() * .25) * 70
                    : Math.sin((clock() + partial) * .09) * 22;
        if (phase() != 1) return 0;
        return aerialAttack() < 0
                ? Math.sin((ticksExisted + partial) * .055) * 4
                : BrawlerAerialScore.roll(
                                BrawlerAerialScore.decode(aerialAttack()), clock() + partial)
                        + (aerialRecovery() > 0 ? Math.sin(aerialRecovery() * .22) * 24 : 0);
    }

    public double jetThrust(float partial) {
        if (coordinating()) {
            if (phase() == 2 && (engineMask() & 1) == 0) return 0;
            NBTTagCompound n = dataManager.get(COORD_HANDS);
            return WulfrumPairPose.thrust(
                    WulfrumCoordinationScore.Pattern.values()[n.getInteger("pattern")],
                    n.getInteger("tick") + partial);
        }
        if (limiterAttack() >= 0)
            return (engineMask() & 1) == 0 ? 0 : aerialRecovery() > 0 ? .2 : 1.35;
        return aerialAttack() < 0
                ? .5
                : BrawlerAerialScore.thrust(
                        BrawlerAerialScore.decode(aerialAttack()), clock() + partial);
    }

    int liveArms() {
        int live = 0;
        for (int i = 0; i < 4; i++) if (armHealth(i) > 0) live |= 1 << i;
        return live;
    }

    void advanceAerial() {
        boolean critical = false;
        for (int i = 0; i < 4; i++) critical |= armHealth(i) > 0 && armHealth(i) <= 20;
        dataManager.set(
                AERIAL, BrawlerAerialScore.next(aerialAttack(), liveArms(), critical).ordinal());
        dataManager.set(CLOCK, 0);
    }

    void sacrificeArm(int arm) {
        if (arm >= 0 && arm < 4) {
            dataManager.set(key(arm), 0F);
            burst(localToWorld(hand(arm, 0)), 45);
        }
    }

    void aerialAim(Vec3d target) {
        Vec3d d =
                target.subtract(getPositionVector().addVector(0, 1.4, 0))
                        .rotateYaw((float) Math.toRadians(rotationYaw));
        double angle = Math.toRadians(-flightRoll(0)),
                x = d.x * Math.cos(angle) - d.y * Math.sin(angle),
                y = d.x * Math.sin(angle) + d.y * Math.cos(angle);
        dataManager.set(AIM_X, (float) MathHelper.clamp(x, -7, 7));
        dataManager.set(AIM_Y, (float) MathHelper.clamp(y, -9, 7));
        dataManager.set(AIM_Z, (float) MathHelper.clamp(d.z, 1, 8));
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PHASE, 1);
        dataManager.register(CLOCK, 0);
        dataManager.register(ATTACK, 0);
        dataManager.register(TRANSITION, 100);
        dataManager.register(A, 100F);
        dataManager.register(B, 100F);
        dataManager.register(C, 100F);
        dataManager.register(D, 100F);
        dataManager.register(SHIELD, 0F);
        dataManager.register(CLASH, 0);
        dataManager.register(CLASH_PLAYER, -1);
        dataManager.register(CLASH_ARM, -1);
        dataManager.register(SHIELD_HITS, 0);
        dataManager.register(FIST_X, 0F);
        dataManager.register(FIST_Y, 0F);
        dataManager.register(FIST_Z, 0F);
        dataManager.register(COUNTER_CONTACT, -1);
        dataManager.register(BAR_ID, "");
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(400);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    private DataParameter<Float> key(int i) {
        return i == 0 ? A : i == 1 ? B : i == 2 ? C : D;
    }

    public String bossBarId() {
        return dataManager.get(BAR_ID);
    }

    public float armHealth(int i) {
        return dataManager.get(key(i));
    }

    public int phase() {
        return dataManager.get(PHASE);
    }

    public int clock() {
        return dataManager.get(CLOCK);
    }

    public int attack() {
        return dataManager.get(ATTACK);
    }

    public int transition() {
        return dataManager.get(TRANSITION);
    }

    public boolean shielded() {
        return phase() == 1
                || phase() == 3 && !bipedal() && shieldHits() < BrawlerClash.SHIELD_HITS;
    }

    public int shieldHits() {
        return dataManager.get(SHIELD_HITS);
    }

    public int clashTick() {
        return dataManager.get(CLASH);
    }

    public int clashPlayer() {
        return dataManager.get(CLASH_PLAYER);
    }

    public int clashArm() {
        return dataManager.get(CLASH_ARM);
    }

    public float totalHealth() {
        return getHealth() + armHealth(0) + armHealth(1) + armHealth(2) + armHealth(3);
    }

    void collaborationPose(int tick, int contact) {
        dataManager.set(ATTACK, BrawlerScore.Pattern.PISTON.ordinal());
        dataManager.set(CLOCK, Math.max(1, Math.min(44, 28 + tick - contact)));
    }

    @Override
    public World getWorld() {
        return world;
    }

    @Override
    public Entity[] getParts() {
        return null;
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
    public void fall(float distance, float multiplier) {}

    @Override
    public void travel(float strafe, float vertical, float forward) {}

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        dataManager.set(BAR_ID, bar.getUniqueId().toString());
        bar.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bar.removePlayer(p);
    }

    @Override
    public boolean attackEntityFrom(DamageSource s, float amount) {
        if (s == DamageSource.OUT_OF_WORLD) return super.attackEntityFrom(s, amount);
        if (shielded() || transition() > 0 || s.isFireDamage() || s == DamageSource.FALL)
            return false;
        if (!world.isRemote && (bipedal() ? martial.counter(s) : combat.counter())) {
            burst(getPositionVector(), 12);
            return false;
        }
        float shield = dataManager.get(SHIELD);
        if (shield > 0) {
            dataManager.set(SHIELD, Math.max(0, shield - amount));
            amount = Math.max(0, amount - shield);
        }
        return amount > 0 && super.attackEntityFrom(s, amount);
    }

    @Override
    public boolean attackEntityFromPart(
            MultiPartEntityPart part, DamageSource source, float amount) {
        for (int i = 0; i < 4; i++) if (arms[i] == part) return damageArm(i, source, amount);
        return false;
    }

    public boolean damageArm(int i, DamageSource source, float amount) {
        if (introTick() > 0) return false;
        if (world.isRemote
                || phase() != 1
                || transition() > 0
                || source.isFireDamage()
                || !Float.isFinite(amount)
                || amount <= 0) return false;
        if (i >= 0 && i < 4 && armHealth(i) > 0 && hurt[i] == 0) {
            dataManager.set(key(i), Math.max(0, armHealth(i) - amount));
            hurt[i] = 8;
            burst(localToWorld(hand(i, 0)), armHealth(i) == 0 ? 40 : 8);
            if (source.getTrueSource() instanceof EntityLivingBase)
                setAttackTarget((EntityLivingBase) source.getTrueSource());
            return true;
        }
        return false;
    }

    public Vec3d hand(int i, float partial) {
        if (bipedal() && !coordinatedHand(i)) return martialPose(partial).hand[i % 2];
        if (coordinatedHand(i) && clashTick() == 0) {
            net.minecraft.nbt.NBTTagCompound n = dataManager.get(COORD_HANDS);
            Vec3d now = new Vec3d(n.getDouble("x" + i), n.getDouble("y" + i), n.getDouble("z" + i));
            if (world.isRemote && n.hasKey("px" + i)) {
                Vec3d before =
                        new Vec3d(
                                n.getDouble("px" + i),
                                n.getDouble("py" + i),
                                n.getDouble("pz" + i));
                return before.add(now.subtract(before).scale(MathHelper.clamp(partial, 0, 1)));
            }
            return now;
        }
        if (clashTick() > 0 && i == clashArm()) {
            Vec3d from =
                    new Vec3d(
                            dataManager.get(FIST_X),
                            dataManager.get(FIST_Y),
                            dataManager.get(FIST_Z));
            double t = clashTick() + partial, progress = BrawlerClash.returned(t);
            Vec3d impact = from.normalize().scale(1.65);
            return from.add(impact.subtract(from).scale(progress))
                    .addVector(
                            Math.sin(t * 1.8) * .025 * (1 - progress),
                            Math.sin(t * 2.1) * .035 * (1 - progress),
                            0);
        }
        if (introTick() > 0) {
            double[] rest = BrawlerScore.hand(BrawlerScore.Pattern.PISTON, i, 0);
            if (introTick() < 26 && i < 2) {
                double f =
                        BrawlerScore.smooth(introTick() / 12D)
                                * (1 - BrawlerScore.smooth((introTick() - 18) / 8D));
                return new Vec3d(
                        rest[0] + ((i == 0 ? -.65 : .65) - rest[0]) * f,
                        rest[1] * (1 - f),
                        rest[2] + 2 * f);
            }
            if (i == 0 && introTick() >= 140 && introTick() <= 171) {
                double f = BrawlerScore.smooth((introTick() + partial - 140) / 18D);
                return new Vec3d(
                        rest[0] * (1 - f) + dataManager.get(FIST_X) * f,
                        rest[1] * (1 - f) + dataManager.get(FIST_Y) * f,
                        rest[2] * (1 - f) + dataManager.get(FIST_Z) * f);
            }
            return new Vec3d(rest[0], rest[1], rest[2]);
        }
        double[] h =
                aerialAttack() >= 0
                        ? BrawlerAerialScore.hand(
                                BrawlerAerialScore.decode(aerialAttack()), i, clock() + partial)
                        : BrawlerScore.hand(pattern(), i, transition() > 0 ? 0 : clock() + partial);
        if (phase() == 1 && transition() == 0) {
            double reach = MathHelper.clamp((h[2] - .5) / 4, 0, 1);
            h[1] += (dataManager.get(AIM_Y) - h[1]) * reach;
            if (aerialAttack() >= 0) {
                h[0] += (dataManager.get(AIM_X) - h[0]) * reach;
                h[2] += (dataManager.get(AIM_Z) - h[2]) * reach;
            }
        }
        if (pattern() == BrawlerScore.Pattern.COUNTER
                && i == 1
                && dataManager.get(COUNTER_CONTACT) >= 0) {
            double impact = BrawlerScore.pulse(clock() + partial, dataManager.get(COUNTER_CONTACT));
            h[0] += (.6 - h[0]) * impact;
            h[1] += (.5 - h[1]) * impact;
            h[2] += (4.5 - h[2]) * impact;
        }
        if (phase() == 3 && transition() > 0) {
            double f = BrawlerScore.smooth((100 - transition()) / 70D);
            h[0] *= f;
            h[1] *= f;
            h[2] *= f;
        }
        return new Vec3d(h[0], h[1], h[2]);
    }

    public BrawlerScore.Pattern pattern() {
        return BrawlerScore.decode(attack());
    }

    public void counterContact(int tick) {
        dataManager.set(COUNTER_CONTACT, tick);
    }

    public int counterContact() {
        return dataManager.get(COUNTER_CONTACT);
    }

    public float temporaryShield() {
        return dataManager.get(SHIELD);
    }

    public boolean canParry() {
        return parryContact;
    }

    public Vec3d parryOrigin() {
        return martialContact != null
                ? martialContact
                : contactArm >= 0
                        ? localToWorld(hand(contactArm, 0))
                        : getPositionVector().addVector(0, 1.4, 0);
    }

    public int parryCount() {
        return parries;
    }

    public void recalledCell() {
        combat.recalled();
    }

    public void reformShield() {
        dataManager.set(SHIELD, 25F);
    }

    public void stagger(int ticks) {
        stun = Math.max(stun, ticks);
    }

    public void combatMove(Vec3d v) {
        if (clashTick() > 0) return;
        move(MoverType.SELF, v.x, v.y, v.z);
        velocityChanged = true;
    }

    public void advancePattern() {
        int live = 0;
        for (int i = 0; i < 4; i++) if (armHealth(i) > 0) live |= 1 << i;
        dataManager.set(ATTACK, BrawlerScore.next(pattern(), phase(), live).ordinal());
        dataManager.set(CLOCK, 0);
        parries = 0;
    }

    public Vec3d localToWorld(Vec3d v) {
        if (bipedal()) v = martialPose(0).body(v);
        double angle = Math.toRadians(flightRoll(0));
        Vec3d tilted =
                new Vec3d(
                        v.x * Math.cos(angle) - v.y * Math.sin(angle),
                        v.x * Math.sin(angle) + v.y * Math.cos(angle),
                        v.z);
        return getPositionVector()
                .addVector(0, bodyHeight(0), 0)
                .add(tilted.rotateYaw((float) Math.toRadians(-rotationYaw)));
    }

    boolean martialStrike(int limb, float damage, double radius, boolean parry) {
        Vec3d local =
                limb < 2
                        ? hand(limb, 0)
                        : limb < 4 ? martialPose(0).foot(limb - 2) : new Vec3d(0, -2.5, 2.5);
        Vec3d previous =
                limb < 2 ? hand(limb, -1) : limb < 4 ? martialPose(-1).foot(limb - 2) : local;
        Vec3d to = localToWorld(local),
                from =
                        localToWorld(previous)
                                .addVector(prevPosX - posX, prevPosY - posY, prevPosZ - posZ);
        boolean hit = false;
        contactArm = limb < 2 ? limb : -1;
        parryContact = parry;
        martialContact = to;
        try {
            for (EntityPlayer player :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, new AxisAlignedBB(from, to).grow(radius))) {
                AxisAlignedBB box = player.getEntityBoundingBox().grow(radius);
                if (!player.isCreative()
                        && !player.isSpectator()
                        && (box.contains(to) || box.calculateIntercept(from, to) != null)
                        && world.rayTraceBlocks(to, player.getPositionEyes(1), false, true, false)
                                == null) {
                    hit |= player.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                    if (martial.paused()) break;
                }
            }
        } finally {
            contactArm = -1;
            parryContact = false;
            martialContact = null;
        }
        return hit;
    }

    private void burst(Vec3d p, int n) {
        ((WorldServer) world)
                .spawnParticle(EnumParticleTypes.CRIT_MAGIC, p.x, p.y, p.z, n, .6, .6, .6, .15);
    }

    void announce(String s) {
        for (EntityPlayer p : world.playerEntities)
            if (p.getDistanceSq(this) < 96 * 96)
                p.sendStatusMessage(new TextComponentString(s), false);
    }

    private void nextPhase(int p) {
        combat.reset();
        aerial.reset();
        limiter.reset();
        dataManager.set(LIMITER, -1);
        dataManager.set(AERIAL, -1);
        dataManager.set(PHASE, p);
        dataManager.set(TRANSITION, p == 2 ? 70 : 100);
        dataManager.set(CLOCK, 0);
        dataManager.set(
                ATTACK,
                (p == 2 ? BrawlerScore.Pattern.RAM : BrawlerScore.Pattern.BOXING).ordinal());
        motionX = motionY = motionZ = 0;
    }

    void steer(Vec3d destination, double speed) {
        Vec3d d = destination.subtract(getPositionVector());
        Vec3d desired = d.lengthVector() < speed ? d : d.normalize().scale(speed);
        motionX += (desired.x - motionX) * .18;
        motionY += (desired.y - motionY) * .18;
        motionZ += (desired.z - motionZ) * .18;
        move(MoverType.SELF, motionX, motionY, motionZ);
    }

    boolean strike(int arm, float damage, double radius, boolean parry) {
        if (clashTick() > 0 || phase() == 1 && armHealth(arm) <= 0) return false;
        Vec3d p = localToWorld(hand(arm, 0));
        burst(p, 10);
        boolean hit = false;
        contactArm = arm;
        parryContact = parry || phase() == 3 || phase() == 1 && arm == 0;
        try {
            for (EntityPlayer target :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, new AxisAlignedBB(p, p).grow(radius)))
                if (!target.isSpectator()
                        && !target.isCreative()
                        && world.rayTraceBlocks(p, target.getPositionEyes(1), false, true, false)
                                == null) {
                    hit |= target.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                    if (clashTick() > 0) break;
                }
        } finally {
            parryContact = false;
            contactArm = -1;
        }
        return hit;
    }

    void punchHeld(EntityPlayer player, int arm, float damage) {
        if (clashTick() > 0 || damage <= 0) return;
        contactArm = arm;
        parryContact = true;
        try {
            player.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
        } finally {
            contactArm = -1;
            parryContact = false;
        }
    }

    void ram(float damage, boolean parry) {
        if (damage <= 0) return;
        parryContact = parry;
        Vec3d from = new Vec3d(prevPosX, prevPosY + 1.4, prevPosZ),
                to = getPositionVector().addVector(0, 1.4, 0);
        try {
            for (EntityPlayer p :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class, new AxisAlignedBB(from, to).grow(2)))
                if (!p.isSpectator()
                        && !p.isCreative()
                        && (p.getEntityBoundingBox().grow(1.4).contains(to)
                                || p.getEntityBoundingBox().grow(1.4).calculateIntercept(from, to)
                                        != null)) {
                    p.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                    if (limiter.paused()) break;
                }
        } finally {
            parryContact = false;
        }
    }

    public void parried(EntityPlayer player) {
        if (!parryContact || clashTick() > 0) return;
        parries++;
        if (DraedonCollaboration.parried(this, player)) return;
        if (bipedal()) {
            martial.parried(player);
            return;
        }
        if (phase() == 2 && limiterAttack() >= 0) {
            limiter.parried(player);
            return;
        }
        if (introTick() == 0 && phase() == 1 && aerialAttack() >= 0) {
            aerial.parried(player);
            return;
        }
        if (contactArm >= 0 && (phase() == 3 || phase() == 1 && contactArm == 0)) {
            Vec3d fist =
                    player.getPositionEyes(1)
                            .add(player.getLookVec().scale(.7))
                            .subtract(getPositionVector().addVector(0, 1.4, 0))
                            .rotateYaw((float) Math.toRadians(rotationYaw));
            clashStand = player.getPositionVector();
            dataManager.set(FIST_X, (float) fist.x);
            dataManager.set(FIST_Y, (float) fist.y);
            dataManager.set(FIST_Z, (float) fist.z);
            dataManager.set(CLASH_ARM, contactArm);
            dataManager.set(CLASH_PLAYER, player.getEntityId());
            dataManager.set(CLASH, 1);
            combat.reset();
            motionX = motionY = motionZ = 0;
            return;
        }
        motionX *= -1;
        motionZ *= -1;
        burst(getPositionVector(), 25);
        if (pattern() == BrawlerScore.Pattern.PARRY_CHECK
                || pattern() == BrawlerScore.Pattern.HAYMAKER
                || pattern() == BrawlerScore.Pattern.ONE_TWO
                || pattern() == BrawlerScore.Pattern.PRIMITIVE) return;
        stagger(pattern() == BrawlerScore.Pattern.WAR_MACHINE ? 70 : 40);
    }

    private void tickClash() {
        Entity e = world.getEntityByID(clashPlayer());
        int t = clashTick();
        if (!(e instanceof EntityPlayer) || !e.isEntityAlive() || e.getDistanceSq(this) > 225) {
            endClash();
            return;
        }
        EntityPlayer player = (EntityPlayer) e;
        if (t <= BrawlerClash.STRUGGLE) {
            if (clashStand == null
                    || !world.getCollisionBoxes(
                                    player,
                                    player.getEntityBoundingBox()
                                            .offset(
                                                    clashStand.subtract(
                                                            player.getPositionVector())))
                            .isEmpty()) {
                endClash();
                return;
            }
            player.motionX = player.motionY = player.motionZ = 0;
            player.fallDistance = 0;
            player.velocityChanged = true;
            if (player instanceof EntityPlayerMP)
                ((EntityPlayerMP) player)
                        .connection.setPlayerLocation(
                                clashStand.x,
                                clashStand.y,
                                clashStand.z,
                                player.rotationYaw,
                                player.rotationPitch);
            if (t % 4 == 0) burst(localToWorld(hand(clashArm(), 0)), 4);
        } else if (t < BrawlerClash.IMPACT && t % 2 == 0)
            burst(localToWorld(hand(clashArm(), 0)), 9);
        if (t == BrawlerClash.IMPACT) {
            burst(localToWorld(hand(clashArm(), 0)), 55);
            world.playSound(
                    null,
                    getPosition(),
                    net.minecraft.init.SoundEvents.BLOCK_ANVIL_LAND,
                    SoundCategory.HOSTILE,
                    1.4F,
                    .65F);
            if (phase() == 3 && shielded()) {
                dataManager.set(SHIELD_HITS, BrawlerClash.hitsAfterReturn(shieldHits()));
                if (!shielded()) {
                    burst(getPositionVector().addVector(0, 1.4, 0), 100);
                    announce("TESLA SHIELD SHATTERED");
                }
            }
        }
        if (t >= BrawlerClash.END) {
            endClash();
            stagger(pattern() == BrawlerScore.Pattern.COUNTER ? 70 : 30);
        } else dataManager.set(CLASH, t + 1);
    }

    private void endClash() {
        clashStand = null;
        dataManager.set(CLASH, 0);
        dataManager.set(CLASH_PLAYER, -1);
        dataManager.set(CLASH_ARM, -1);
    }

    void prepareTrialEntrance() {
        dataManager.set(TRANSITION, 0);
    }

    @Override
    public void onLivingUpdate() {
        if (EntityDraedon.arriving(this)) return;
        super.onLivingUpdate();
        setNoGravity(true);
        if (bipedal() && height < 5) setSize(2.8F, 6F);
        if (!isEntityAlive()) {
            if (!world.isRemote && clashTick() > 0) endClash();
            return;
        }
        for (int i = 0; i < 4; i++) {
            Vec3d p = localToWorld(hand(i, 0));
            arms[i].onUpdate();
            arms[i].setPosition(p.x, p.y - .85, p.z);
        }
        if (world.isRemote) return;
        dataManager.set(
                EXPERT,
                com.scapeandrun.frostbite.world.FrostbiteWorldSettings.get(world).isExpert());
        for (int i = 0; i < 4; i++) {
            if (phase() == 1 && armHealth(i) > 0) {
                if (targets[i] == null || targets[i].isDead) {
                    targets[i] = new EntityBrawlerArm(this, i);
                    world.spawnEntity(targets[i]);
                }
                targets[i].follow(this);
            } else if (targets[i] != null) {
                targets[i].setDead();
                targets[i] = null;
            }
        }
        for (int i = 0; i < 4; i++) if (hurt[i] > 0) hurt[i]--;
        bar.setPercent(totalHealth() / 800F);
        if (clashTick() > 0) {
            tickClash();
            return;
        }
        if (phase() != 1 || getEntityData().getBoolean("BrawlerIntroFinished"))
            dataManager.set(INTRO, 0);
        if (introTick() > 0) {
            EntityLivingBase witness = getAttackTarget();
            if (witness == null) witness = world.getClosestPlayer(posX, posY, posZ, 96, false);
            if (witness != null) {
                Vec3d d = witness.getPositionVector().subtract(getPositionVector());
                rotationYaw = (float) Math.toDegrees(Math.atan2(-d.x, d.z));
                renderYawOffset = rotationYaw;
                opening.tick(witness);
            }
            return;
        }
        if (phase() == 1 && armHealth(0) + armHealth(1) + armHealth(2) + armHealth(3) <= 0)
            nextPhase(2);
        if (phase() == 2 && getHealth() <= getMaxHealth() * .5F && transition() == 0) nextPhase(3);
        EntityLivingBase target = getAttackTarget();
        if (target == null || !target.isEntityAlive() || getDistanceSq(target) > 16000) {
            target = world.getNearestAttackablePlayer(this, 112, 96);
            setAttackTarget(target);
        }
        if (transition() > 0) {
            int t = transition();
            dataManager.set(TRANSITION, t - 1);
            if (bipedal()) {
                martial.transition();
                return;
            }
            if (t % 8 == 0) burst(getPositionVector().addVector(0, 1.5, 0), 10);
            if (phase() == 2 && expert()) {
                engineMask(t > 30 ? 0 : 31);
                if (t < 20)
                    combatMove(
                            new Vec3d(0, .6, -1.4).rotateYaw((float) Math.toRadians(-rotationYaw)));
                if (t == 12)
                    world.playSound(
                            null,
                            getPosition(),
                            net.minecraft.init.SoundEvents.ENTITY_GENERIC_EXPLODE,
                            SoundCategory.HOSTILE,
                            2,
                            .7F);
            }
            return;
        }
        if (target == null) {
            combat.reset();
            aerial.reset();
            limiter.reset();
            martial.reset();
            dataManager.set(CLOCK, 0);
            return;
        }
        if (DraedonCollaboration.control(this)) {
            combat.reset();
            if (aerialAttack() >= 0) {
                aerial.reset();
                dataManager.set(AERIAL, -1);
            }
            return;
        }
        if (bipedal()) {
            if (martialAttack() < 0) {
                limiter.reset();
                dataManager.set(LIMITER, -1);
                advanceMartial();
            }
            if (martial.paused()) {
                martial.recover();
                return;
            }
            dataManager.set(CLOCK, clock() + 1);
            Vec3d direction = target.getPositionVector().subtract(getPositionVector());
            rotationYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            renderYawOffset = rotationYaw;
            martial.tick(target);
            velocityChanged = true;
            return;
        }
        if (phase() == 2 && expert()) {
            if (limiterAttack() < 0) {
                combat.reset();
                advanceLimiter();
            }
            if (limiter.paused()) {
                limiter.recover();
                return;
            }
            dataManager.set(CLOCK, clock() + 1);
            Vec3d direction = target.getPositionVector().subtract(getPositionVector());
            rotationYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            renderYawOffset = rotationYaw;
            limiter.tick(target);
            velocityChanged = true;
            return;
        } else if (limiterAttack() >= 0) {
            limiter.reset();
            dataManager.set(LIMITER, -1);
            engineMask(1);
            dataManager.set(CLOCK, 0);
        }
        if (phase() == 1 && expert()) {
            if (aerialAttack() < 0) {
                combat.reset();
                advanceAerial();
            }
            if ((BrawlerAerialScore.decode(aerialAttack()).arms & liveArms())
                    != BrawlerAerialScore.decode(aerialAttack()).arms) {
                aerial.reset();
                advanceAerial();
            }
            if (aerial.paused()) {
                aerial.tickRecovery();
                return;
            }
            dataManager.set(CLOCK, clock() + 1);
            Vec3d direction = target.getPositionVector().subtract(getPositionVector());
            rotationYaw = (float) Math.toDegrees(Math.atan2(-direction.x, direction.z));
            renderYawOffset = rotationYaw;
            aerial.tick(target);
            velocityChanged = true;
            return;
        } else if (aerialAttack() >= 0) {
            aerial.reset();
            dataManager.set(AERIAL, -1);
            dataManager.set(CLOCK, 0);
        }
        if (phase() == 1) aerialAim(target.getPositionEyes(1));
        if (stun > 0) {
            stun--;
            motionX *= .85;
            motionZ *= .85;
            move(MoverType.SELF, motionX, 0, motionZ);
            return;
        }
        if (phase() == 1) {
            int live = 0;
            for (int i = 0; i < 4; i++) if (armHealth(i) > 0) live |= 1 << i;
            if ((pattern().arms & live) != pattern().arms) {
                combat.reset();
                advancePattern();
            }
        }
        int t = clock() + 1;
        dataManager.set(CLOCK, t);
        Vec3d delta = target.getPositionVector().subtract(getPositionVector());
        rotationYaw = (float) Math.toDegrees(Math.atan2(-delta.x, delta.z));
        renderYawOffset = rotationYaw;
        combat.tick(target);
        velocityChanged = true;
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setInteger("BrawlerPhase", phase());
        n.setInteger("BrawlerTransition", transition());
        n.setInteger("ReturnedFists", shieldHits());
        for (int i = 0; i < 4; i++) n.setFloat("WeaponHealth" + i, armHealth(i));
    }

    @Override
    protected void onDeathUpdate() {
        combat.reset();
        aerial.reset();
        if (!world.isRemote) martial.reset();
        deathTime++;
        if (!world.isRemote) {
            if (deathTime % 8 == 0)
                burst(
                        getPositionVector()
                                .addVector(
                                        rand.nextDouble() * 2 - 1,
                                        rand.nextDouble() * 2,
                                        rand.nextDouble() * 2 - 1),
                        18);
            bar.setPercent(0);
            if (deathTime >= 80) {
                burst(getPositionVector().addVector(0, 1, 0), 100);
                setDead();
            }
        }
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dataManager.set(PHASE, MathHelper.clamp(n.getInteger("BrawlerPhase"), 1, 3));
        dataManager.set(
                SHIELD_HITS,
                MathHelper.clamp(n.getInteger("ReturnedFists"), 0, BrawlerClash.SHIELD_HITS));
        dataManager.set(
                ATTACK,
                (phase() == 2
                                ? BrawlerScore.Pattern.RAM
                                : phase() == 3
                                        ? BrawlerScore.Pattern.BOXING
                                        : BrawlerScore.Pattern.PISTON)
                        .ordinal());
        dataManager.set(TRANSITION, n.getInteger("BrawlerTransition"));
        for (int i = 0; i < 4; i++)
            dataManager.set(
                    key(i),
                    n.hasKey("WeaponHealth" + i)
                            ? MathHelper.clamp(n.getFloat("WeaponHealth" + i), 0, 100)
                            : 100F);
    }
}
