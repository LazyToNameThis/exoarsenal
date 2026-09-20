package com.scapeandrun.frostbite.expedition;

import net.minecraft.world.*;
import net.minecraft.entity.*;
import net.minecraft.init.*;
import net.minecraft.util.math.*;
import net.minecraft.util.DamageSource;
import static com.scapeandrun.frostbite.expedition.WildSpecies.*;

public abstract class EntityDeepMob extends EntityWildMob {
    private static final net.minecraft.network.datasync.DataParameter<Integer> CAST =
            net.minecraft.network.datasync.EntityDataManager.createKey(
                    EntityDeepMob.class, net.minecraft.network.datasync.DataSerializers.VARINT);
    private int castCooldown;

    public EntityDeepMob(World world) {
        super(world);
        isImmuneToFire = species().habitat == Habitat.UNDERWORLD;
        if (species() == DEMON) setSize(1.1F, 1.7F);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CAST, 0);
    }

    public int castTicks() {
        return dataManager.get(CAST);
    }

    @Override
    public boolean getCanSpawnHere() {
        return world.getDifficulty() != EnumDifficulty.PEACEFUL
                && (species().habitat == Habitat.UNDERWORLD
                        ? world.provider.getDimension() == -1
                        : world.provider.getDimension() == 0
                                && DeepWorld.dungeonAt(world, getPosition()))
                && world.checkNoEntityCollision(getEntityBoundingBox())
                && world.getCollisionBoxes(this, getEntityBoundingBox()).isEmpty()
                && !world.containsAnyLiquid(getEntityBoundingBox())
                && world.getLight(getPosition()) < 8;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        WildSpecies s = species();
        if (castCooldown > 0) castCooldown--;
        EntityLivingBase target = getAttackTarget();
        if ((s == DARK_CASTER || s == FIRE_IMP || s == DEMON)
                && target != null
                && canEntityBeSeen(target)
                && getDistanceSq(target) > 9
                && getDistanceSq(target) < 324
                && castCooldown == 0
                && castTicks() == 0) {
            castCooldown = 85;
            dataManager.set(CAST, 20);
        }
        int cast = castTicks();
        if (cast > 0) {
            dataManager.set(CAST, cast - 1);
            if (s != DEMON) {
                getNavigator().clearPath();
                motionX *= .2;
                motionZ *= .2;
            }
        }
        if (cast == 8 && target != null && target.isEntityAlive() && canEntityBeSeen(target)) {
            if (s == DEMON) {
                EntityRelicShot shot = new EntityRelicShot(world, this, DeepWeapon.Kind.SCYTHE);
                Vec3d d = target.getPositionEyes(1).subtract(shot.getPositionVector());
                shot.shoot(d.x, d.y, d.z, .24F, 1);
                world.spawnEntity(shot);
            } else {
                EntityWildBolt shot = new EntityWildBolt(world, this, s == FIRE_IMP ? 4 : 2);
                Vec3d d = target.getPositionEyes(1).subtract(shot.getPositionVector());
                shot.shoot(d.x, d.y, d.z, .8F, 1);
                world.spawnEntity(shot);
            }
            swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
        }
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        WildSpecies s = species();
        if (s.habitat == Habitat.DUNGEON) {
            dropItem(Items.BONE, 2 + rand.nextInt(4));
            if (s == DUNGEON_SLIME || rand.nextInt(12) == 0) dropItem(DeepContent.GOLD_KEY, 1);
            if (s == DARK_CASTER && rand.nextInt(25) == 0) dropItem(DeepContent.WATER_BOLT, 1);
        } else {
            dropItem(
                    s == BONE_SERPENT
                            ? Items.BONE
                            : s == LAVA_SLIME ? WildContent.GEL : DeepContent.HELLSTONE,
                    1 + rand.nextInt(3));
            if (s == DEMON && rand.nextInt(25) == 0) dropItem(DeepContent.DEMON_SCYTHE, 1);
        }
    }

    public static final class AngryBones extends EntityDeepMob {
        public AngryBones(World w) {
            super(w);
        }

        public WildSpecies species() {
            return ANGRY_BONES;
        }
    }

    public static final class DarkCaster extends EntityDeepMob {
        public DarkCaster(World w) {
            super(w);
        }

        public WildSpecies species() {
            return DARK_CASTER;
        }
    }

    public static final class CursedSkull extends EntityDeepMob {
        public CursedSkull(World w) {
            super(w);
        }

        public WildSpecies species() {
            return CURSED_SKULL;
        }
    }

    public static final class DungeonSlime extends EntityDeepMob {
        public DungeonSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return DUNGEON_SLIME;
        }
    }

    public static final class Hellbat extends EntityDeepMob {
        public Hellbat(World w) {
            super(w);
        }

        public WildSpecies species() {
            return HELLBAT;
        }
    }

    public static final class LavaSlime extends EntityDeepMob {
        public LavaSlime(World w) {
            super(w);
        }

        public WildSpecies species() {
            return LAVA_SLIME;
        }
    }

    public static final class FireImp extends EntityDeepMob {
        public FireImp(World w) {
            super(w);
        }

        public WildSpecies species() {
            return FIRE_IMP;
        }
    }

    public static final class Demon extends EntityDeepMob {
        public Demon(World w) {
            super(w);
        }

        public WildSpecies species() {
            return DEMON;
        }
    }

    public static final class BoneSerpent extends EntityDeepMob {
        public BoneSerpent(World w) {
            super(w);
        }

        public WildSpecies species() {
            return BONE_SERPENT;
        }
    }
}
