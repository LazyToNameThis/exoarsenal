package com.exoarsenal.entity;

import com.exoarsenal.entity.slime.KingSlimeAttackRules;
import com.exoarsenal.world.ExoArsenalWorldSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import java.util.UUID;

public class EntityKingSlime extends EntityMob {
    private static final DataParameter<Integer>
            STATE = EntityDataManager.createKey(EntityKingSlime.class, DataSerializers.VARINT),
            TIME = EntityDataManager.createKey(EntityKingSlime.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean>
            EXPERT = EntityDataManager.createKey(EntityKingSlime.class, DataSerializers.BOOLEAN),
            NINJA = EntityDataManager.createKey(EntityKingSlime.class, DataSerializers.BOOLEAN);
    private final BossInfoServer bar =
            new BossInfoServer(
                    new TextComponentString("King Slime"),
                    BossInfo.Color.BLUE,
                    BossInfo.Overlay.PROGRESS);
    private boolean balanced, wasGrounded, dying, finishedDeath;
    private int sequence, hops, groundTimer, stuck, missingTarget, jewelRespawn, teleportSide = 1;
    private float lastSummonHealth = 1;
    private UUID jewel, ninja;
    private double jumpX, jumpZ;
    public float squash, previousSquash;

    public EntityKingSlime(World w) {
        super(w);
        setSize(4.6F, 3.1F);
        setNoAI(true);
        experienceValue = 100;
        stepHeight = 1;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(STATE, 0);
        dataManager.register(TIME, 0);
        dataManager.register(EXPERT, false);
        dataManager.register(NINJA, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(300);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(96);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(7);
    }

    public int state() {
        return dataManager.get(STATE);
    }

    public int clock() {
        return dataManager.get(TIME);
    }

    public boolean expert() {
        return dataManager.get(EXPERT);
    }

    public boolean ninjaEscaped() {
        return dataManager.get(NINJA);
    }

    public float fraction() {
        return getHealth() / getMaxHealth();
    }

    public float bodySize() {
        return KingSlimeAttackRules.size(expert(), fraction());
    }

    public float visibleScale() {
        return state() == KingSlimeAttackRules.TELEPORT
                ? KingSlimeAttackRules.teleportScale(clock())
                : 1;
    }

    private EntityKingSlimeSupport support(UUID id) {
        if (id == null || world.isRemote) return null;
        Entity e = ((WorldServer) world).getEntityFromUuid(id);
        return e instanceof EntityKingSlimeSupport && !e.isDead ? (EntityKingSlimeSupport) e : null;
    }

    private void begin(int state) {
        dataManager.set(STATE, state);
        dataManager.set(TIME, 0);
        groundTimer = 0;
        hops = 0;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        previousSquash = squash;
        squash *= .7F;
        if (onGround && !wasGrounded) {
            squash = -.24F;
            if (!world.isRemote) {
                burst(28);
                playSound(SoundEvents.ENTITY_SLIME_SQUISH, 1.5F, .6F);
            }
        }
        wasGrounded = onGround;
        float size = bodySize() * visibleScale();
        double centerX = posX, feetY = posY, centerZ = posZ;
        setSize(Math.max(.45F, size), Math.max(.35F, size * .68F));
        setPosition(centerX, feetY, centerZ);
        if (world.isRemote) return;
        if (!balanced) {
            float health = fraction();
            ExoArsenalWorldSettings settings = ExoArsenalWorldSettings.get(world);
            dataManager.set(EXPERT, settings.isExpert());
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .setBaseValue((expert() ? 420 : 300) * (settings.isMaster() ? 1.5 : 1));
            setHealth(getMaxHealth() * health);
            balanced = true;
        }
        bar.setPercent(dying ? 0 : fraction());
        dataManager.set(TIME, clock() + 1);
        if (dying) {
            deathSequence();
            return;
        }
        EntityPlayer p = world.getNearestAttackablePlayer(this, 96, 48);
        if (p == null) {
            motionX *= .8;
            motionZ *= .8;
            if (++missingTarget > 200) setDead();
            return;
        }
        missingTarget = 0;
        setAttackTarget(p);
        rotationYaw = (float) Math.toDegrees(Math.atan2(posZ - p.posZ, posX - p.posX)) + 90;
        renderYawOffset = rotationYaw;
        if (fraction() <= .75F && state() != KingSlimeAttackRules.TELEPORT) {
            if (support(jewel) == null && (jewel == null || ++jewelRespawn >= 700)) {
                EntityKingSlimeSupport j = new EntityKingSlimeSupport(this, 0);
                j.setPosition(p.posX, p.posY + 10, p.posZ);
                if (world.spawnEntity(j)) {
                    jewel = j.getUniqueID();
                    jewelRespawn = 0;
                }
            }
        }
        if (expert() && fraction() <= .3F && !ninjaEscaped()) spawnNinja();
        if (!expert() && lastSummonHealth - fraction() >= .05F) {
            lastSummonHealth = fraction();
            int count =
                    world.getEntitiesWithinAABB(
                                    EntityKingSlimeSupport.class,
                                    getEntityBoundingBox().grow(64),
                                    e -> e.belongs(this) && e.kind() == 2)
                            .size();
            if (count < 12) {
                EntityKingSlimeSupport s = new EntityKingSlimeSupport(this, 2);
                s.setPosition(posX, posY + .4, posZ);
                s.motionX = (rand.nextDouble() - .5) * .7;
                s.motionZ = (rand.nextDouble() - .5) * .7;
                s.motionY = .55;
                world.spawnEntity(s);
            }
        }
        if (state() == KingSlimeAttackRules.TELEPORT) {
            teleport(p);
            return;
        }
        if (onGround && Math.abs(posX - prevPosX) + Math.abs(posZ - prevPosZ) < .03) stuck++;
        else stuck = Math.max(0, stuck - 2);
        int teleportDelay = (!getEntitySenses().canSee(p) || p.posY > posY + 20) ? 50 : 100;
        if (stuck >= 100
                || !expert()
                        && clock() >= teleportDelay
                        && (getDistanceSq(p) > 900 || !getEntitySenses().canSee(p))) {
            begin(KingSlimeAttackRules.TELEPORT);
            return;
        }
        if (onGround) {
            motionX *= .65;
            motionZ *= .65;
            groundTimer++;
            boolean big =
                    expert()
                            ? state() == KingSlimeAttackRules.LARGE
                            : fraction() > .2F && hops % 4 == 3;
            int wait =
                    expert()
                            ? KingSlimeAttackRules.ticks(big ? 35 : 25)
                            : Math.max(5, (int) (16 * fraction()));
            if (groundTimer >= wait) {
                if (expert() && hops >= (big ? 1 : 3)) {
                    sequence++;
                    begin(KingSlimeAttackRules.attack(sequence));
                } else {
                    Vec3d d = new Vec3d(p.posX - posX, 0, p.posZ - posZ).normalize();
                    double speed = big ? .92 : .67;
                    motionX = jumpX = d.x * speed;
                    motionZ = jumpZ = d.z * speed;
                    motionY = (big ? 1.05 : .68) + MathHelper.clamp((p.posY - posY) * .04, 0, .4);
                    if (collidedHorizontally) motionY *= 1.5;
                    isAirBorne = true;
                    velocityChanged = true;
                    groundTimer = 0;
                    hops++;
                    squash = .19F;
                    playSound(SoundEvents.ENTITY_SLIME_JUMP, 1.2F, .6F);
                }
            }
        } else {
            if (!collidedHorizontally) {
                motionX = jumpX;
                motionZ = jumpZ;
            }
            if (motionY < 0) motionY -= .015 + .04 * (1 - fraction());
        }
        if (state() != KingSlimeAttackRules.TELEPORT)
            for (EntityPlayer hit :
                    world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox()))
                if (!hit.capabilities.isCreativeMode && !hit.isSpectator())
                    hit.attackEntityFrom(DamageSource.causeMobDamage(this), expert() ? 8 : 7);
    }

    private void teleport(EntityPlayer p) {
        motionX = motionZ = 0;
        if (clock() == 20) {
            Vec3d facing = p.getLookVec();
            Vec3d side = new Vec3d(facing.z, 0, -facing.x).normalize().scale(teleportSide * 18);
            teleportSide = -teleportSide;
            Vec3d destination =
                    safeGround(p.getPositionVector().add(side), bodySize(), bodySize() * .68F);
            if (destination != null) {
                setPositionAndUpdate(destination.x, destination.y, destination.z);
                motionY = 0;
                burst(45);
            }
            stuck = 0;
        }
        if (clock() >= 39) {
            if (expert()) {
                sequence++;
                begin(KingSlimeAttackRules.attack(sequence));
            } else begin(KingSlimeAttackRules.SMALL);
        }
    }

    public Vec3d safeGround(Vec3d desired, double width, double height) {
        for (int ring = 0; ring < 5; ring++)
            for (int i = 0; i < 8; i++) {
                double angle = i * Math.PI / 4;
                int x = MathHelper.floor(desired.x + Math.cos(angle) * ring * 2),
                        z = MathHelper.floor(desired.z + Math.sin(angle) * ring * 2);
                for (int y =
                                Math.min(
                                        world.getActualHeight() - 8,
                                        MathHelper.floor(desired.y) + 12);
                        y >= Math.max(1, MathHelper.floor(desired.y) - 20);
                        y--) {
                    BlockPos floor = new BlockPos(x, y - 1, z);
                    if (!world.isBlockLoaded(floor) || !world.getBlockState(floor).isTopSolid())
                        continue;
                    AxisAlignedBB box =
                            new AxisAlignedBB(
                                    x + .5 - width / 2,
                                    y,
                                    z + .5 - width / 2,
                                    x + .5 + width / 2,
                                    y + height,
                                    z + .5 + width / 2);
                    if (world.getCollisionBoxes(this, box).isEmpty()
                            && !world.containsAnyLiquid(box)) return new Vec3d(x + .5, y, z + .5);
                }
            }
        return null;
    }

    private void spawnNinja() {
        EntityKingSlimeSupport n = new EntityKingSlimeSupport(this, 1);
        n.setPosition(posX, posY + .5, posZ);
        n.motionY = .7;
        if (world.spawnEntity(n)) {
            ninja = n.getUniqueID();
            dataManager.set(NINJA, true);
            burst(45);
        }
    }

    private void deathSequence() {
        motionX *= .85;
        motionZ *= .85;
        if (clock() == 1) {
            if (support(jewel) != null) support(jewel).setDead();
            if (expert() && !ninjaEscaped()) spawnNinja();
            motionY = expert() ? .9 : .25;
        }
        if (clock() > 24 && clock() % 4 == 0) burst(8);
        if (clock() >= (expert() ? 77 : 30)) {
            burst(100);
            finishedDeath = true;
            super.attackEntityFrom(DamageSource.OUT_OF_WORLD, 100000);
        }
    }

    public void burst(int count) {
        if (world instanceof WorldServer)
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.SLIME,
                            posX,
                            posY + height * .4,
                            posZ,
                            count,
                            width * .4,
                            height * .3,
                            width * .4,
                            .12);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL
                || source == DamageSource.FALL
                || source.getTrueSource() instanceof EntityKingSlimeSupport
                || dying && !finishedDeath
                || state() == KingSlimeAttackRules.TELEPORT) return false;
        boolean result = super.attackEntityFrom(source, amount);
        return result;
    }

    @Override
    protected void damageEntity(DamageSource source, float amount) {
        if (!finishedDeath && amount >= getHealth()) {
            dying = true;
            begin(KingSlimeAttackRules.DEATH);
            setHealth(1);
            return;
        }
        super.damageEntity(source, amount);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        dropItem(Items.SLIME_BALL, 72 + rand.nextInt(29));
        dropItem(Items.GOLD_INGOT, 3 + rand.nextInt(3));
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
    public void setDead() {
        bar.setVisible(false);
        super.setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setBoolean("KingBalanced", balanced);
        n.setBoolean("KingExpert", expert());
        n.setBoolean("KingNinja", ninjaEscaped());
        n.setInteger("KingSequence", sequence);
        n.setInteger("KingState", state());
        n.setInteger("KingTime", clock());
        n.setBoolean("KingDying", dying);
        n.setInteger("KingHops", hops);
        n.setInteger("KingRespawn", jewelRespawn);
        n.setFloat("KingLastSummon", lastSummonHealth);
        if (jewel != null) n.setUniqueId("KingJewel", jewel);
        if (ninja != null) n.setUniqueId("KingNinjaId", ninja);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        balanced = n.getBoolean("KingBalanced");
        dataManager.set(EXPERT, n.getBoolean("KingExpert"));
        dataManager.set(NINJA, n.getBoolean("KingNinja"));
        sequence = n.getInteger("KingSequence");
        dataManager.set(STATE, n.getInteger("KingState"));
        dataManager.set(TIME, n.getInteger("KingTime"));
        dying = n.getBoolean("KingDying");
        hops = n.getInteger("KingHops");
        jewelRespawn = n.getInteger("KingRespawn");
        lastSummonHealth = n.hasKey("KingLastSummon") ? n.getFloat("KingLastSummon") : fraction();
        jewel = n.hasUniqueId("KingJewel") ? n.getUniqueId("KingJewel") : null;
        ninja = n.hasUniqueId("KingNinjaId") ? n.getUniqueId("KingNinjaId") : null;
    }
}
