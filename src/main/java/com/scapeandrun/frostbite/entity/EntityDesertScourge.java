package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.world.FrostbiteWorldSettings;
import net.minecraft.entity.*;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.*;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import net.minecraftforge.common.BiomeDictionary;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.*;
import java.util.*;

public class EntityDesertScourge extends EntityMob implements IAnimatable {
    private static final DataParameter<Integer>
            ATTACK = EntityDataManager.createKey(EntityDesertScourge.class, DataSerializers.VARINT),
            TICK = EntityDataManager.createKey(EntityDesertScourge.class, DataSerializers.VARINT);
    private static final DataParameter<Boolean>
            EXPERT =
                    EntityDataManager.createKey(EntityDesertScourge.class, DataSerializers.BOOLEAN),
            HIDDEN =
                    EntityDataManager.createKey(EntityDesertScourge.class, DataSerializers.BOOLEAN),
            YOUNG = EntityDataManager.createKey(EntityDesertScourge.class, DataSerializers.BOOLEAN);
    private final EntityScourgeSegment[] parts = new EntityScourgeSegment[25];
    private final Vec3d[] trail = new Vec3d[25], oldTrail = new Vec3d[25];
    private final BossInfoServer bar =
            new BossInfoServer(
                    new TextComponentString("Desert Scourge"),
                    BossInfo.Color.YELLOW,
                    BossInfo.Overlay.PROGRESS);
    private final AnimationFactory factory = new AnimationFactory(this);
    private boolean initialized, balanced, nuisancesSummoned, slamLanded;
    private int noTarget, burrowTimer, previousAttack;
    private Vec3d velocity = new Vec3d(0, 0, .3), locked = Vec3d.ZERO, origin = Vec3d.ZERO;
    private UUID owner;
    private final List<UUID> nuisances = new ArrayList<>();

    public EntityDesertScourge(World world) {
        super(world);
        setSize(2.3F, 2.3F);
        setNoAI(true);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
        experienceValue = 150;
        isImmuneToFire = true;
    }

    public boolean nuisance() {
        return false;
    }

    public boolean expert() {
        return dataManager.get(EXPERT);
    }

    public boolean hidden() {
        return dataManager.get(HIDDEN);
    }

    public int attack() {
        return dataManager.get(ATTACK);
    }

    public int attackTick() {
        return dataManager.get(TICK);
    }

    public int count() {
        return nuisance() ? (dataManager.get(YOUNG) ? 11 : 13) : 25;
    }

    public double sizeScale() {
        return nuisance() ? (dataManager.get(YOUNG) ? .48 : .6) : 1;
    }

    public Vec3d segment(int i, float partial) {
        if (!initialized) return getPositionVector();
        return oldTrail[i].add(trail[i].subtract(oldTrail[i]).scale(partial));
    }

    public Vec3d visibleSegment(int i, float partial) {
        EntityScourgeSegment p = parts[i];
        return i == 0 || p == null || p.isDead
                ? segment(i, partial)
                : new Vec3d(
                        p.lastTickPosX + (p.posX - p.lastTickPosX) * partial,
                        p.lastTickPosY + (p.posY - p.lastTickPosY) * partial + .8 * sizeScale(),
                        p.lastTickPosZ + (p.posZ - p.lastTickPosZ) * partial);
    }

    public static boolean desert(World w, BlockPos p) {
        net.minecraft.world.biome.Biome b = w.getBiome(p);
        return BiomeDictionary.hasType(b, BiomeDictionary.Type.SANDY)
                && !com.scapeandrun.frostbite.world.FrigidSpawnBiomes.isCold(b);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, DesertScourgePattern.INTRO);
        dataManager.register(TICK, 0);
        dataManager.register(EXPERT, false);
        dataManager.register(HIDDEN, false);
        dataManager.register(YOUNG, false);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(360);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(8);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(128);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    private void begin(int attack) {
        previousAttack = attack();
        dataManager.set(ATTACK, attack);
        dataManager.set(TICK, 0);
        slamLanded = false;
        EntityLivingBase target = getAttackTarget();
        if (target != null) {
            locked = target.getPositionVector();
            origin = locked;
        }
    }

    private double ground(Vec3d p) {
        return world.getTopSolidOrLiquidBlock(new BlockPos(p.x, 0, p.z)).getY();
    }

    private void steer(Vec3d to, double speed, double acceleration) {
        Vec3d wanted = to.subtract(getPositionVector()).normalize().scale(speed),
                delta = wanted.subtract(velocity);
        if (delta.lengthVector() > acceleration) delta = delta.normalize().scale(acceleration);
        velocity = velocity.add(delta);
    }

    private void roar() {
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_ENDERDRAGON_GROWL,
                SoundCategory.HOSTILE,
                2,
                .65F);
    }

    private void sand(Vec3d p, int count) {
        if (world instanceof WorldServer)
            ((WorldServer) world)
                    .spawnParticle(
                            EnumParticleTypes.BLOCK_DUST,
                            p.x,
                            p.y,
                            p.z,
                            count,
                            2,
                            .2,
                            2,
                            .12,
                            net.minecraft.block.Block.getStateId(Blocks.SAND.getDefaultState()));
    }

    private EntityPlayer target() {
        EntityLivingBase old = getAttackTarget();
        if (old instanceof EntityPlayer
                && old.isEntityAlive()
                && !((EntityPlayer) old).isSpectator()
                && !((EntityPlayer) old).capabilities.isCreativeMode) return (EntityPlayer) old;
        EntityPlayer p = world.getNearestAttackablePlayer(this, 112, 80);
        setAttackTarget(p);
        return p;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        motionX = motionY = motionZ = 0;
        noClip = true;
        setNoGravity(true);
        setSize((float) (2.3 * sizeScale()), (float) (2.3 * sizeScale()));
        if (world.isRemote) {
            updateTrail();
            if (ticksExisted % 10 == 0) discoverParts();
            return;
        }
        if (!balanced) {
            float fraction = getHealth() / getMaxHealth();
            if (!nuisance()) dataManager.set(EXPERT, FrostbiteWorldSettings.get(world).isExpert());
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                    .setBaseValue(
                            nuisance() ? (dataManager.get(YOUNG) ? 65 : 85) : expert() ? 540 : 360);
            setHealth(getMaxHealth() * fraction);
            balanced = true;
        }
        setSize((float) (2.3 * sizeScale()), (float) (2.3 * sizeScale()));
        if (!isEntityAlive()) return;
        if (nuisance() && owner != null) {
            Entity parent = ((WorldServer) world).getEntityFromUuid(owner);
            if (!(parent instanceof EntityDesertScourge) || !parent.isEntityAlive()) {
                setDead();
                return;
            }
        }
        EntityPlayer player = target();
        if (player == null) {
            velocity = velocity.addVector(0, -.025, 0);
            if (++noTarget > 100) {
                setDead();
                return;
            }
        } else {
            noTarget = 0;
            int tick = attackTick() + 1;
            dataManager.set(TICK, tick);
            tickPattern(player, tick);
        }
        Vec3d before = getPositionVector();
        setPosition(posX + velocity.x, posY + velocity.y, posZ + velocity.z);
        velocityChanged = true;
        rotationYaw = (float) Math.toDegrees(Math.atan2(-velocity.x, velocity.z));
        rotationPitch =
                (float)
                        -Math.toDegrees(
                                Math.atan2(
                                        velocity.y,
                                        Math.sqrt(
                                                velocity.x * velocity.x
                                                        + velocity.z * velocity.z)));
        renderYawOffset = rotationYaw;
        if ((before.y - ground(before)) * (posY - ground(getPositionVector())) < 0)
            sand(new Vec3d(posX, ground(getPositionVector()), posZ), 28);
        updateTrail();
        ensureParts();
        contact();
        bar.setPercent(getHealth() / getMaxHealth());
        bar.setVisible(!nuisance());
    }

    private void tickPattern(EntityPlayer p, int tick) {
        Vec3d aim = p.getPositionVector().addVector(0, .8, 0);
        float fraction = getHealth() / getMaxHealth();
        double rage = desert(world, p.getPosition()) ? 1 : 1.5;
        int a = attack();
        dataManager.set(
                HIDDEN,
                a == DesertScourgePattern.NUISANCES
                        || a == DesertScourgePattern.INTRO && tick < 60);
        if (nuisance()) {
            dataManager.set(HIDDEN, false);
            steer(aim, .48 * rage, .045);
            if (dataManager.get(YOUNG) && tick % 55 == 0)
                shot(
                        getPositionVector(),
                        aim.subtract(getPositionVector()).normalize().scale(.55),
                        0);
            return;
        }
        if (a == DesertScourgePattern.INTRO) {
            if (tick < 60) {
                velocity = Vec3d.ZERO;
                Vec3d behind = p.getLookVec().scale(-8);
                setPosition(p.posX + behind.x, ground(aim) - 9, p.posZ + behind.z);
                if (tick % 5 == 0) sand(new Vec3d(posX, ground(aim), posZ), 12);
            } else if (tick == 60) {
                roar();
                velocity =
                        aim.add(p.getLookVec().scale(-4))
                                .addVector(0, 8, 0)
                                .subtract(getPositionVector())
                                .normalize()
                                .scale(1.05);
            } else if (tick > 76) steer(aim.addVector(10, 5, 0), .4, .05);
            if (tick >= 100)
                begin(expert() ? DesertScourgePattern.SAND_SPIT : DesertScourgePattern.HUNT);
            return;
        }
        if (!expert()) {
            if (!nuisancesSummoned && fraction <= .5F) {
                nuisancesSummoned = true;
                spawnNuisances(p);
                begin(DesertScourgePattern.NUISANCES);
                return;
            }
            if (a == DesertScourgePattern.NUISANCES) {
                steer(new Vec3d(p.posX, ground(aim) - 14, p.posZ), .45, .05);
                if (nuisancesDead()) {
                    dataManager.set(HIDDEN, false);
                    begin(DesertScourgePattern.BURROW);
                    roar();
                }
                return;
            }
            if (a == DesertScourgePattern.HUNT) {
                Vec3d chase = aim;
                if (posY > p.posY + 8) chase = aim.addVector(0, -5, 0);
                steer(
                        chase,
                        DesertScourgePattern.deathSpeed(fraction) * rage,
                        DesertScourgePattern.deathTurn(fraction) * rage);
                if (posY < p.posY && getDistanceSq(p) > 64) burrowTimer++;
                if (burrowTimer >= 140) {
                    burrowTimer = 0;
                    begin(DesertScourgePattern.BURROW);
                    roar();
                }
            } else if (a == DesertScourgePattern.BURROW) {
                steer(new Vec3d(locked.x, ground(locked) - 8, locked.z), .85, .09);
                if (getDistanceSq(locked.x, ground(locked) - 8, locked.z) < 9 || tick >= 45) {
                    locked = aim.addVector(p.motionX * 8, 20, p.motionZ * 8);
                    begin(DesertScourgePattern.LUNGE);
                    locked = aim.addVector(p.motionX * 8, 20, p.motionZ * 8);
                    velocity = locked.subtract(getPositionVector()).normalize().scale(1.25);
                }
            } else if (a == DesertScourgePattern.LUNGE) {
                if (posY >= locked.y || tick >= 55) {
                    ring(24, .48, .2);
                    begin(DesertScourgePattern.RECOVER);
                    velocity = new Vec3d(velocity.x * .7, -.65, velocity.z * .7);
                }
            } else if (a == DesertScourgePattern.RECOVER) {
                steer(aim.addVector(0, -7, 0), .7, .06);
                if (tick >= 28) begin(DesertScourgePattern.HUNT);
            }
            if (tick > 200) begin(DesertScourgePattern.HUNT);
            return;
        }
        switch (a) {
            case DesertScourgePattern.SAND_SPIT:
                if (getDistanceSq(p) > 100) steer(aim, .82 * rage, .075);
                else velocity = velocity.normalize().scale(.95 * rage);
                if (tick % 27 == 0 && getDistanceSq(p) > 135) ring(7, .43, 0);
                break;
            case DesertScourgePattern.SAND_RUSH:
                if (!slamLanded) {
                    steer(new Vec3d(origin.x - 18, ground(origin) - 2, origin.z), .55, .06);
                    if (tick >= 20 && posY <= ground(getPositionVector()) - 1) {
                        slamLanded = true;
                        burrowTimer = tick;
                        roar();
                        Vec3d direction = new Vec3d(p.posX - posX, 0, p.posZ - posZ).normalize();
                        if (direction.lengthSquared() < .1) direction = new Vec3d(1, 0, 0);
                        velocity = direction.scale(1.15 * rage);
                    } else if (tick >= 100) {
                        begin(
                                DesertScourgePattern.choose(
                                        DesertScourgePattern.phase(true, fraction),
                                        a,
                                        rand.nextInt(1000)));
                        return;
                    }
                } else if (tick - burrowTimer < 60) {
                    if (tick % 5 == 0) {
                        Vec3d at = new Vec3d(posX, ground(getPositionVector()) + .1, posZ);
                        shot(
                                at,
                                new Vec3d(
                                        (rand.nextDouble() - .5) * .1,
                                        .7,
                                        (rand.nextDouble() - .5) * .1),
                                0);
                        sand(at, 5);
                    }
                } else steer(aim, .5, .04);
                break;
            case DesertScourgePattern.SANDSTORM:
                steer(aim, .32 * rage, .022);
                if (tick % 10 == 0) {
                    Vec3d side = p.getLookVec().crossProduct(new Vec3d(0, 1, 0)).normalize();
                    if (side.lengthSquared() < .1) side = new Vec3d(1, 0, 0);
                    int sign = tick % 20 == 0 ? 1 : -1;
                    for (int i = 0; i < 3; i++) {
                        Vec3d start = aim.add(side.scale(sign * 20)).addVector(0, 3 + i * 2, 0);
                        shot(start, side.scale(-sign * .42).addVector(0, -.035, 0), 1);
                    }
                }
                break;
            case DesertScourgePattern.GROUND_SLAM:
                if (tick <= 40)
                    steer(new Vec3d(origin.x + 10, ground(origin) + 26, origin.z), 1.05, .08);
                else if (tick == 41) {
                    roar();
                    locked = new Vec3d(p.posX + p.motionX * 8, ground(aim), p.posZ + p.motionZ * 8);
                    velocity = locked.subtract(getPositionVector()).normalize().scale(1.35);
                } else if (!slamLanded && (posY <= ground(getPositionVector()) + .5 || tick > 85)) {
                    slamLanded = true;
                    sand(getPositionVector(), 70);
                    ring(32, .4, .24);
                    for (int sign : new int[] {-1, 1})
                        shot(
                                new Vec3d(posX, ground(getPositionVector()), posZ),
                                new Vec3d(sign * .045, 0, 0),
                                2);
                    world.playSound(
                            null,
                            posX,
                            posY,
                            posZ,
                            SoundEvents.ENTITY_GENERIC_EXPLODE,
                            SoundCategory.HOSTILE,
                            1.5F,
                            .65F);
                }
                if (slamLanded) steer(aim.addVector(0, -3, 0), .25, .02);
                break;
            case DesertScourgePattern.VULTURES:
                steer(aim.addVector(12, 5, 0), .4, .04);
                if (tick == 18) {
                    roar();
                    spawnVultures(p);
                }
                break;
            default:
                steer(aim, .4, .04);
        }
        if (tick >= DesertScourgePattern.duration(a))
            begin(
                    DesertScourgePattern.choose(
                            DesertScourgePattern.phase(true, fraction), a, rand.nextInt(1000)));
    }

    private void shot(Vec3d start, Vec3d motion, int kind) {
        world.spawnEntity(new EntityScourgeSand(this, start, motion, kind));
    }

    private void ring(int count, double speed, double lift) {
        for (int i = 0; i < count; i++) {
            double a = i * Math.PI * 2 / count;
            shot(getPositionVector(), new Vec3d(Math.cos(a) * speed, lift, Math.sin(a) * speed), 0);
        }
    }

    private void spawnNuisances(EntityPlayer p) {
        for (int i = 0; i < 2; i++) {
            EntityDesertScourge n = new Nuisance(world);
            n.owner = getUniqueID();
            n.dataManager.set(YOUNG, i == 1);
            n.setPosition(p.posX + (i == 0 ? -14 : 14), ground(p.getPositionVector()) - 5, p.posZ);
            n.setAttackTarget(p);
            if (world.spawnEntity(n)) nuisances.add(n.getUniqueID());
        }
    }

    private boolean nuisancesDead() {
        for (UUID id : nuisances) {
            Entity e = ((WorldServer) world).getEntityFromUuid(id);
            if (e != null && !e.isDead && e.isEntityAlive()) return false;
        }
        return true;
    }

    private void spawnVultures(EntityPlayer p) {
        int count =
                world.getEntitiesWithinAABB(
                                EntityDesertVulture.class, p.getEntityBoundingBox().grow(96))
                        .size();
        if (count >= 8) return;
        for (int i = 0; i < Math.min(4, 8 - count); i++) {
            EntityDesertVulture v = new EntityDesertVulture(world);
            v.setOwner(getUniqueID());
            v.setPosition(p.posX + (i - 1.5) * 5, p.posY + 12, p.posZ + 6);
            v.setAttackTarget(p);
            world.spawnEntity(v);
        }
    }

    private void updateTrail() {
        if (!initialized) {
            Vec3d back = new Vec3d(0, 0, -1.35 * sizeScale());
            for (int i = 0; i < 25; i++)
                trail[i] =
                        oldTrail[i] =
                                getPositionVector()
                                        .addVector(0, 1.15 * sizeScale(), 0)
                                        .add(back.scale(i));
            initialized = true;
        }
        for (int i = 0; i < 25; i++) oldTrail[i] = trail[i];
        trail[0] = getPositionVector().addVector(0, 1.15 * sizeScale(), 0);
        for (int i = 1; i < count(); i++) {
            double[] p =
                    DesertScourgePattern.follow(
                            trail[i - 1].x,
                            trail[i - 1].y,
                            trail[i - 1].z,
                            trail[i].x,
                            trail[i].y,
                            trail[i].z,
                            1.35 * sizeScale());
            trail[i] = new Vec3d(p[0], p[1], p[2]);
        }
    }

    private void discoverParts() {
        for (EntityScourgeSegment part :
                world.getEntitiesWithinAABB(
                        EntityScourgeSegment.class, getEntityBoundingBox().grow(64)))
            if (part.belongs(this) && part.index() > 0 && part.index() < count())
                parts[part.index()] = part;
    }

    private void ensureParts() {
        if (ticksExisted % 20 == 1) discoverParts();
        for (int i = 1; i < count(); i++)
            if (parts[i] == null || parts[i].isDead) {
                EntityScourgeSegment part = new EntityScourgeSegment(this, i);
                if (world.spawnEntity(part)) parts[i] = part;
            }
    }

    private void contact() {
        if (hidden() || attack() == DesertScourgePattern.INTRO && attackTick() < 68) return;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(EntityPlayer.class, getEntityBoundingBox().grow(36))) {
            if (p.isSpectator() || p.capabilities.isCreativeMode) continue;
            for (int i = 0; i < count(); i++)
                if (p.getEntityBoundingBox()
                        .grow(.2)
                        .intersects(
                                new AxisAlignedBB(trail[i], trail[i])
                                        .grow(i == 0 ? 1.05 * sizeScale() : .8 * sizeScale()))) {
                    float speed = (float) Math.min(1, velocity.lengthVector() / .65);
                    float damage = (nuisance() ? 4 : i == 0 ? 9 : 5) * speed;
                    p.attackEntityFrom(DamageSource.causeMobDamage(this), damage);
                    break;
                }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL
                || source == DamageSource.FALL
                || hidden()
                || source.getTrueSource() instanceof EntityDesertScourge) return false;
        return super.attackEntityFrom(source, amount);
    }

    public boolean hurtSegment(DamageSource source, float damage) {
        return attackEntityFrom(source, damage * .65F);
    }

    @Override
    public boolean isNonBoss() {
        return nuisance();
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    public void addTrackingPlayer(EntityPlayerMP p) {
        super.addTrackingPlayer(p);
        if (!nuisance()) bar.addPlayer(p);
    }

    @Override
    public void removeTrackingPlayer(EntityPlayerMP p) {
        super.removeTrackingPlayer(p);
        bar.removePlayer(p);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (nuisance()) return;
        if (expert()) dropItem(com.scapeandrun.frostbite.registry.ModContent.DESERT_SCOURGE_BAG, 1);
        else
            for (ItemStack drop :
                    com.scapeandrun.frostbite.expedition.ScourgeLoot.roll(rand, false))
                entityDropItem(drop, 0);
        if (rand.nextInt(10) == 0)
            entityDropItem(
                    new ItemStack(com.scapeandrun.frostbite.expedition.ExpeditionContent.TROPHY),
                    0);

        entityDropItem(
                new ItemStack(com.scapeandrun.frostbite.expedition.ExpeditionContent.RELIC), 0);
        if (rand.nextInt(100) == 0)
            dropItem(com.scapeandrun.frostbite.expedition.ExpeditionContent.THANK_YOU, 1);
        if (com.scapeandrun.frostbite.expedition.ScourgeLoot.Progress.get(world).firstKill())
            dropItem(com.scapeandrun.frostbite.expedition.ExpeditionContent.LORE, 1);
    }

    @Override
    public void setDead() {
        bar.setVisible(false);
        if (!world.isRemote)
            for (EntityScourgeSegment part : parts) if (part != null) part.setDead();
        super.setDead();
    }

    @Override
    protected void onDeathUpdate() {
        deathTime++;
        if (!world.isRemote) {
            setPosition(posX, posY - .035, posZ);
            updateTrail();
            if (deathTime % 8 == 0) sand(segment(Math.min(count() - 1, deathTime / 3), 1), 18);
            if (deathTime >= 70) {
                if (world.getGameRules().getBoolean("doMobLoot")
                        && !nuisance()
                        && recentlyHit > 0) {
                    int xp = experienceValue;
                    while (xp > 0) {
                        int value = EntityXPOrb.getXPSplit(xp);
                        xp -= value;
                        world.spawnEntity(new EntityXPOrb(world, posX, posY, posZ, value));
                    }
                }
                setDead();
            }
        }
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Override
    public void registerControllers(AnimationData data) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setBoolean("Expert", expert());
        n.setBoolean("Balanced", balanced);
        n.setBoolean("Young", dataManager.get(YOUNG));
        n.setBoolean("NuisancesSummoned", nuisancesSummoned);
        n.setInteger("Attack", attack());
        n.setInteger("Tick", attackTick());
        n.setBoolean("SlamLanded", slamLanded);
        n.setInteger("BurrowTimer", burrowTimer);
        writeVec(n, "Velocity", velocity);
        writeVec(n, "Locked", locked);
        writeVec(n, "Origin", origin);
        if (owner != null) n.setUniqueId("Owner", owner);
        for (int i = 0; i < nuisances.size(); i++) n.setUniqueId("Nuisance" + i, nuisances.get(i));
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dataManager.set(EXPERT, n.getBoolean("Expert"));
        dataManager.set(YOUNG, n.getBoolean("Young"));
        balanced = n.getBoolean("Balanced");
        nuisancesSummoned = n.getBoolean("NuisancesSummoned");
        dataManager.set(ATTACK, MathHelper.clamp(n.getInteger("Attack"), 0, 10));
        dataManager.set(TICK, Math.max(0, n.getInteger("Tick")));
        slamLanded = n.getBoolean("SlamLanded");
        burrowTimer = n.getInteger("BurrowTimer");
        velocity = readVec(n, "Velocity");
        locked = readVec(n, "Locked");
        origin = readVec(n, "Origin");
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
        nuisances.clear();
        for (int i = 0; i < 2; i++)
            if (n.hasUniqueId("Nuisance" + i)) nuisances.add(n.getUniqueId("Nuisance" + i));
    }

    private static void writeVec(NBTTagCompound n, String key, Vec3d p) {
        n.setDouble(key + "X", p.x);
        n.setDouble(key + "Y", p.y);
        n.setDouble(key + "Z", p.z);
    }

    private static Vec3d readVec(NBTTagCompound n, String key) {
        return new Vec3d(n.getDouble(key + "X"), n.getDouble(key + "Y"), n.getDouble(key + "Z"));
    }

    public static final class Nuisance extends EntityDesertScourge {
        public Nuisance(World w) {
            super(w);
            experienceValue = 0;
        }

        @Override
        public boolean nuisance() {
            return true;
        }
    }
}
