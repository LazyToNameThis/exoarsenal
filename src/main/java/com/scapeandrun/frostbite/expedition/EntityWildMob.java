package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import static com.scapeandrun.frostbite.expedition.WildSpecies.*;

public abstract class EntityWildMob extends EntityMob {
    private static final DataParameter<Integer> ATTACK =
            EntityDataManager.createKey(EntityWildMob.class, DataSerializers.VARINT);
    private static final DataParameter<BlockPos> ROOT =
            EntityDataManager.createKey(EntityWildMob.class, DataSerializers.BLOCK_POS);
    public final Vec3d[] segments = new Vec3d[10], previousSegments = new Vec3d[10];
    private int cooldown, hopDelay;
    private boolean rooted;

    public abstract WildSpecies species();

    public EntityWildMob(World world) {
        super(world);
        Shape s = species().shape;
        setSize(
                s == Shape.HUMAN
                        ? .65F
                        : s == Shape.SLIME && species() == MOTHER_SLIME ? 1.4F : .9F,
                s == Shape.HUMAN
                        ? 1.9F
                        : s == Shape.SLIME && species() == MOTHER_SLIME ? 1.2F : .7F);
        experienceValue = species() == TIM ? 12 : 5;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, 0);
        dataManager.register(ROOT, BlockPos.ORIGIN);
    }

    public int attackTicks() {
        return dataManager.get(ATTACK);
    }

    public BlockPos root() {
        return dataManager.get(ROOT);
    }

    private boolean flight() {
        Shape s = species().shape;
        return s == Shape.BAT
                || s == Shape.HORNET
                || s == Shape.EATER
                || s == Shape.WORM
                || s == Shape.PLANT
                || s == Shape.FISH;
    }

    @Override
    protected void initEntityAI() {
        if (!flight()) {
            tasks.addTask(0, new EntityAISwimming(this));
            tasks.addTask(2, new EntityAIAttackMelee(this, 1, true));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, .6));
        }
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2,
                new EntityAINearestAttackableTarget<>(
                        this, EntityPlayer.class, species().shape != Shape.WORM));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        WildSpecies s = species();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(s.health);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(s.damage);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(s.armor);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(s == ANTLION_CHARGER ? .3 : s == FACE_MONSTER ? .28 : .23);
        getEntityAttribute(SharedMonsterAttributes.FOLLOW_RANGE).setBaseValue(24);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (cooldown > 0 || attackTicks() > 0) return false;
        dataManager.set(ATTACK, 16);
        cooldown = species().shape == Shape.PLANT ? 42 : 28;
        return true;
    }

    @Override
    public void onLivingUpdate() {
        WildSpecies s = species();
        Shape shape = s.shape;
        noClip = shape == Shape.WORM || s == CURSED_SKULL;
        super.onLivingUpdate();
        if (shape == Shape.WORM) updateSegments();
        if (world.isRemote) return;
        if (cooldown > 0) cooldown--;
        if (hopDelay > 0) hopDelay--;
        EntityLivingBase target = getAttackTarget();
        if (target != null && !target.isEntityAlive()) {
            setAttackTarget(null);
            target = null;
        }
        if (shape == Shape.PLANT && !rooted) {
            dataManager.set(ROOT, getPosition().down());
            rooted = true;
        }
        setNoGravity(flight() && shape != Shape.FISH || shape == Shape.FISH && isInWater());
        noClip = shape == Shape.WORM || s == CURSED_SKULL;
        if (flight()) {
            Vec3d goal = getPositionVector();
            double speed = .22;
            if (shape == Shape.PLANT) {
                Vec3d anchor = new Vec3d(root()).addVector(.5, 1.2, .5);
                goal =
                        target == null
                                ? anchor
                                : anchor.add(
                                        target.getPositionEyes(1)
                                                .subtract(anchor)
                                                .normalize()
                                                .scale(
                                                        Math.min(
                                                                s == MAN_EATER ? 6 : 4,
                                                                target.getPositionEyes(1)
                                                                        .distanceTo(anchor))));
                speed = attackTicks() > 0 ? .4 : .16;
            } else if (target != null) {
                goal = target.getPositionEyes(1);
                if (shape == Shape.HORNET && s == HORNET && getDistanceSq(target) < 49)
                    goal = goal.add(getPositionVector().subtract(goal).normalize().scale(8));
                if (shape == Shape.WORM) {
                    goal = goal.addVector(0, ticksExisted % 90 < 55 ? -3 : .8, 0);
                    speed = .32;
                }
                if (shape == Shape.BAT) {
                    goal =
                            goal.addVector(
                                    Math.sin(ticksExisted * .14) * .7,
                                    Math.cos(ticksExisted * .09) * .4,
                                    Math.cos(ticksExisted * .14) * .7);
                    speed = .28;
                }
                if (shape == Shape.FISH && !target.isInWater()) goal = getPositionVector();
            } else
                goal =
                        goal.addVector(
                                Math.sin(ticksExisted * .04) * 2,
                                Math.sin(ticksExisted * .06) * .5,
                                Math.cos(ticksExisted * .04) * 2);
            Vec3d delta = goal.subtract(getPositionVector());
            Vec3d velocity =
                    delta.lengthSquared() < .03
                            ? Vec3d.ZERO
                            : delta.normalize().scale(Math.min(speed, delta.lengthVector() * .2));
            if (shape != Shape.FISH || isInWater()) {
                motionX = motionX * .7 + velocity.x * .3;
                motionY = motionY * .7 + velocity.y * .3;
                motionZ = motionZ * .7 + velocity.z * .3;
            }
            if (collidedHorizontally && !noClip) motionY += .12;
            rotationYaw = (float) -Math.toDegrees(Math.atan2(motionX, motionZ));
            rotationPitch =
                    (float)
                            -Math.toDegrees(
                                    Math.atan2(
                                            motionY,
                                            Math.sqrt(motionX * motionX + motionZ * motionZ)));
            if (target != null && getDistanceSq(target) < 3.4) attackEntityAsMob(target);
        }
        if (shape == Shape.SLIME && onGround && hopDelay == 0) {
            motionY = s == MOTHER_SLIME ? .48 : .4;
            hopDelay = target == null ? 35 : 20;
            if (target != null) {
                Vec3d d =
                        target.getPositionVector()
                                .subtract(getPositionVector())
                                .normalize()
                                .scale(.26);
                motionX = d.x;
                motionZ = d.z;
            }
            isAirBorne = true;
        }
        if (shape == Shape.SPIDER && collidedHorizontally) motionY = .2;
        if ((s == GIANT_SHELLY || s == CRAWDAD)
                && target != null
                && onGround
                && getDistanceSq(target) > 4
                && getDistanceSq(target) < 64
                && hopDelay == 0) {
            Vec3d d =
                    target.getPositionVector()
                            .subtract(getPositionVector())
                            .normalize()
                            .scale(s == GIANT_SHELLY ? .8 : .5);
            motionX = d.x;
            motionZ = d.z;
            motionY = .48;
            hopDelay = 70;
        }
        boolean ranged =
                s == HORNET
                        || s == SPIKED_JUNGLE_SLIME
                        || s == SPIKED_ICE_SLIME
                        || s == TIM
                        || s == ANTLION;
        if (ranged
                && target != null
                && cooldown == 0
                && attackTicks() == 0
                && getDistanceSq(target) > 6
                && getDistanceSq(target) < 256
                && canEntityBeSeen(target)) {
            dataManager.set(ATTACK, 26);
            cooldown = s == TIM ? 95 : 75;
        }
        int attack = attackTicks();
        if (attack > 0) {
            dataManager.set(ATTACK, attack - 1);
            if (target != null && attack == 8) {
                if (ranged && getDistanceSq(target) > 6 && canEntityBeSeen(target)) {
                    int type = s == TIM ? 2 : s == SPIKED_ICE_SLIME ? 1 : s == ANTLION ? 3 : 0;
                    int count = s == SPIKED_ICE_SLIME || s == SPIKED_JUNGLE_SLIME ? 3 : 1;
                    for (int i = 0; i < count; i++) {
                        EntityWildBolt bolt = new EntityWildBolt(world, this, type);
                        Vec3d d =
                                target.getPositionEyes(1)
                                        .subtract(bolt.getPositionVector())
                                        .rotateYaw((i - (count - 1) * .5F) * .13F);
                        bolt.shoot(d.x, d.y, d.z, .85F, 1);
                        world.spawnEntity(bolt);
                    }
                    if (s == TIM) teleportAway(target);
                } else if (getDistanceSq(target) < 5 && canEntityBeSeen(target)) {
                    super.attackEntityAsMob(target);
                    if (s == MAN_EATER || s == SPIKED_JUNGLE_SLIME)
                        target.addPotionEffect(new PotionEffect(MobEffects.POISON, 60));
                }
                swingArm(EnumHand.MAIN_HAND);
            }
        }
    }

    private void teleportAway(EntityLivingBase target) {
        for (int i = 0; i < 8; i++) {
            double angle = rand.nextDouble() * Math.PI * 2;
            BlockPos pos =
                    new BlockPos(
                            target.posX + Math.sin(angle) * 7,
                            target.posY,
                            target.posZ + Math.cos(angle) * 7);
            if (!world.isBlockLoaded(pos)) continue;
            if (world.getBlockState(pos.down()).isFullCube()
                    && world.isAirBlock(pos)
                    && world.isAirBlock(pos.up())) {
                setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
                world.playSound(
                        null,
                        pos,
                        SoundEvents.ENTITY_ENDERMEN_TELEPORT,
                        SoundCategory.HOSTILE,
                        .6F,
                        1.3F);
                break;
            }
        }
    }

    private void updateSegments() {
        Vec3d previous = getPositionVector();
        for (int i = 0; i < segments.length; i++) {
            if (segments[i] == null) segments[i] = previous.addVector(0, 0, .48);
            previousSegments[i] = segments[i];
            Vec3d d = segments[i].subtract(previous);
            segments[i] =
                    previous.add(
                            d.lengthSquared() < 1e-6
                                    ? new Vec3d(0, 0, .48)
                                    : d.normalize().scale(.48));
            previous = segments[i];
        }
    }

    @Override
    public void fall(float distance, float multiplier) {
        if (!flight()) super.fall(distance, multiplier);
    }

    @Override
    public boolean isOnLadder() {
        return species().shape == Shape.SPIDER && collidedHorizontally;
    }

    @Override
    public boolean getCanSpawnHere() {
        if (world.provider.getDimension() != 0
                || world.getDifficulty() == EnumDifficulty.PEACEFUL
                || !world.checkNoEntityCollision(getEntityBoundingBox())
                || !world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()) return false;
        WildSpecies s = species();
        Biome biome = world.getBiome(getPosition());
        boolean underground = posY < 55 && !world.canSeeSky(getPosition());
        boolean match;
        switch (s.habitat) {
            case JUNGLE:
                match = WildBiomes.jungle(biome);
                break;
            case JUNGLE_CAVE:
                match = WildBiomes.jungle(biome) && underground;
                break;
            case CORRUPTION:
                match = biome == WildBiomes.CORRUPTION;
                break;
            case CRIMSON:
                match = biome == WildBiomes.CRIMSON;
                break;
            case ICE:
                match = BiomeDictionary.hasType(biome, BiomeDictionary.Type.COLD) && underground;
                break;
            case MUSHROOM:
                match = GeologyWorld.caveAt(world, getPosition()) == GeologyWorld.MUSHROOM;
                break;
            case DESERT:
                match = BiomeDictionary.hasType(biome, BiomeDictionary.Type.SANDY) && underground;
                break;
            case DEEP:
                match = underground && posY < 28 && rand.nextInt(10) == 0;
                break;
            default:
                match =
                        underground
                                && !WildBiomes.jungle(biome)
                                && biome != WildBiomes.CRIMSON
                                && biome != WildBiomes.CORRUPTION
                                && !ExpeditionWorldGenerator.inSea(world, getPosition());
        }
        if (s == BABY_SLIME) return false;
        if (s.shape == Shape.FISH)
            return match
                    && world.getBlockState(getPosition()).getMaterial()
                            == net.minecraft.block.material.Material.WATER;
        return match
                && !world.containsAnyLiquid(getEntityBoundingBox())
                && (!underground || world.getLight(getPosition()) <= 7);
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        WildSpecies s = species();
        if (s == EATER_OF_SOULS || s == DEVOURER)
            dropItem(WildContent.ROTTEN_CHUNK, 1 + rand.nextInt(2));
        else if (s.habitat == Habitat.CRIMSON) dropItem(WildContent.VERTEBRA, 1 + rand.nextInt(2));
        else if (s == HORNET || s == SPIKED_JUNGLE_SLIME)
            dropItem(WildContent.STINGER, 1 + rand.nextInt(2));
        else if (s.shape == Shape.PLANT) dropItem(WildContent.VINE, 1 + rand.nextInt(2));
        else if (s.shape == Shape.SLIME) dropItem(WildContent.GEL, 1 + rand.nextInt(3));
        else if (s == COCHINEAL_BEETLE) dropItem(WildContent.COCHINEAL, 1);
        else if (s == CYAN_BEETLE) dropItem(WildContent.CYAN, 1);
        else if (s == LAC_BEETLE) dropItem(WildContent.LAC, 1);
        else if (s == UNDEAD_MINER) {
            dropItem(Items.BONE, 2);
            dropItem(Items.COAL, 1 + rand.nextInt(3));
            if (rand.nextInt(10) == 0) entityDropItem(new ItemStack(Items.IRON_PICKAXE, 1, 150), 0);
        } else if (s == TIM) {
            dropItem(PrebossContent.MANA_POTION, 2);
            dropItem(Items.GOLD_NUGGET, 4);
        } else if (s == SPORE_SKELETON) {
            dropItem(GeologyContent.GLOW_MUSHROOM, 2);
            dropItem(Items.BONE, 2);
        } else if (s.shape == Shape.FISH) dropItem(Items.FISH, 1);
        else if (s.habitat == Habitat.DESERT) dropItem(PrebossContent.MANDIBLE, 1);
        else if (s.shape == Shape.HUMAN) dropItem(Items.BONE, 1 + rand.nextInt(3));
        else dropItem(Items.LEATHER, 1);
    }

    @Override
    public void onDeath(DamageSource source) {
        if (!world.isRemote && species() == MOTHER_SLIME)
            for (int i = 0; i < 2; i++) {
                EntityWildMob child = new BabySlime(world);
                child.setPosition(posX + (i == 0 ? -.4 : .4), posY, posZ);
                child.motionY = .3;
                world.spawnEntity(child);
            }
        super.onDeath(source);
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setBoolean("Rooted", rooted);
        tag.setLong("PlantRoot", root().toLong());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        rooted = tag.getBoolean("Rooted");
        dataManager.set(ROOT, BlockPos.fromLong(tag.getLong("PlantRoot")));
    }

    public static final class CaveBat extends EntityWildMob {
        public CaveBat(World w) {
            super(w);
        }

        public WildSpecies species() {
            return CAVE_BAT;
        }
    }

    public static final class GiantWorm extends EntityWildMob {
        public GiantWorm(World w) {
            super(w);
        }

        public WildSpecies species() {
            return GIANT_WORM;
        }
    }

    public static final class MotherSlime extends EntityWildMob {
        public MotherSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return MOTHER_SLIME;
        }
    }

    public static final class BabySlime extends EntityWildMob {
        public BabySlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return BABY_SLIME;
        }
    }

    public static final class UndeadMiner extends EntityWildMob {
        public UndeadMiner(World w) {
            super(w);
        }

        public WildSpecies species() {
            return UNDEAD_MINER;
        }
    }

    public static final class Tim extends EntityWildMob {
        public Tim(World w) {
            super(w);
        }

        public WildSpecies species() {
            return TIM;
        }
    }

    public static final class Crawdad extends EntityWildMob {
        public Crawdad(World w) {
            super(w);
        }

        public WildSpecies species() {
            return CRAWDAD;
        }
    }

    public static final class GiantShelly extends EntityWildMob {
        public GiantShelly(World w) {
            super(w);
        }

        public WildSpecies species() {
            return GIANT_SHELLY;
        }
    }

    public static final class Salamander extends EntityWildMob {
        public Salamander(World w) {
            super(w);
        }

        public WildSpecies species() {
            return SALAMANDER;
        }
    }

    public static final class CochinealBeetle extends EntityWildMob {
        public CochinealBeetle(World w) {
            super(w);
        }

        public WildSpecies species() {
            return COCHINEAL_BEETLE;
        }
    }

    public static final class CyanBeetle extends EntityWildMob {
        public CyanBeetle(World w) {
            super(w);
        }

        public WildSpecies species() {
            return CYAN_BEETLE;
        }
    }

    public static final class LacBeetle extends EntityWildMob {
        public LacBeetle(World w) {
            super(w);
        }

        public WildSpecies species() {
            return LAC_BEETLE;
        }
    }

    public static final class IceBat extends EntityWildMob {
        public IceBat(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ICE_BAT;
        }
    }

    public static final class IceSlime extends EntityWildMob {
        public IceSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ICE_SLIME;
        }
    }

    public static final class SpikedIceSlime extends EntityWildMob {
        public SpikedIceSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return SPIKED_ICE_SLIME;
        }
    }

    public static final class SporeSkeleton extends EntityWildMob {
        public SporeSkeleton(World w) {
            super(w);
        }

        public WildSpecies species() {
            return SPORE_SKELETON;
        }
    }

    public static final class JungleBat extends EntityWildMob {
        public JungleBat(World w) {
            super(w);
        }

        public WildSpecies species() {
            return JUNGLE_BAT;
        }
    }

    public static final class JungleSlime extends EntityWildMob {
        public JungleSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return JUNGLE_SLIME;
        }
    }

    public static final class SpikedJungleSlime extends EntityWildMob {
        public SpikedJungleSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return SPIKED_JUNGLE_SLIME;
        }
    }

    public static final class Hornet extends EntityWildMob {
        public Hornet(World w) {
            super(w);
        }

        public WildSpecies species() {
            return HORNET;
        }
    }

    public static final class Snatcher extends EntityWildMob {
        public Snatcher(World w) {
            super(w);
        }

        public WildSpecies species() {
            return SNATCHER;
        }
    }

    public static final class ManEater extends EntityWildMob {
        public ManEater(World w) {
            super(w);
        }

        public WildSpecies species() {
            return MAN_EATER;
        }
    }

    public static final class Piranha extends EntityWildMob {
        public Piranha(World w) {
            super(w);
        }

        public WildSpecies species() {
            return PIRANHA;
        }
    }

    public static final class EaterOfSouls extends EntityWildMob {
        public EaterOfSouls(World w) {
            super(w);
        }

        public WildSpecies species() {
            return EATER_OF_SOULS;
        }
    }

    public static final class Devourer extends EntityWildMob {
        public Devourer(World w) {
            super(w);
        }

        public WildSpecies species() {
            return DEVOURER;
        }
    }

    public static final class Crimera extends EntityWildMob {
        public Crimera(World w) {
            super(w);
        }

        public WildSpecies species() {
            return CRIMERA;
        }
    }

    public static final class FaceMonster extends EntityWildMob {
        public FaceMonster(World w) {
            super(w);
        }

        public WildSpecies species() {
            return FACE_MONSTER;
        }
    }

    public static final class BloodCrawler extends EntityWildMob {
        public BloodCrawler(World w) {
            super(w);
        }

        public WildSpecies species() {
            return BLOOD_CRAWLER;
        }
    }

    public static final class Antlion extends EntityWildMob {
        public Antlion(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ANTLION;
        }
    }

    public static final class AntlionCharger extends EntityWildMob {
        public AntlionCharger(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ANTLION_CHARGER;
        }
    }

    public static final class AntlionSwarmer extends EntityWildMob {
        public AntlionSwarmer(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ANTLION_SWARMER;
        }
    }

    public static final class TombCrawler extends EntityWildMob {
        public TombCrawler(World w) {
            super(w);
        }

        public WildSpecies species() {
            return TOMB_CRAWLER;
        }
    }
}
