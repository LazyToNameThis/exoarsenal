package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.*;
import net.minecraft.entity.ai.*;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public abstract class EntityCaveMob extends EntityMob {
    private static final DataParameter<Integer> ATTACK =
            EntityDataManager.createKey(EntityCaveMob.class, DataSerializers.VARINT);
    private int cooldown;

    public abstract int kind();

    protected EntityCaveMob(World world) {
        super(world);
        setSize(kind() == 3 ? .85F : 1, kind() == 3 ? .6F : kind() == 2 ? 1.9F : 1.5F);
        experienceValue = 5;
    }

    public int attackTicks() {
        return dataManager.get(ATTACK);
    }

    private boolean flying() {
        return kind() == 1 || kind() == 3;
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(ATTACK, 0);
    }

    @Override
    protected void initEntityAI() {
        tasks.addTask(0, new EntityAISwimming(this));
        if (kind() != 1 && kind() != 3) {
            tasks.addTask(2, new EntityAIAttackMelee(this, 1, true));
            tasks.addTask(5, new EntityAIWanderAvoidWater(this, .6));
        }
        targetTasks.addTask(1, new EntityAIHurtByTarget(this, false));
        targetTasks.addTask(
                2, new EntityAINearestAttackableTarget<>(this, EntityPlayer.class, true));
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH)
                .setBaseValue(kind() == 0 ? 36 : kind() == 1 ? 24 : kind() == 2 ? 28 : 12);
        getEntityAttribute(SharedMonsterAttributes.ARMOR)
                .setBaseValue(kind() == 0 ? 10 : kind() == 2 ? 8 : 2);
        getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                .setBaseValue(kind() == 0 ? 7 : kind() == 2 ? 5 : 3);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .setBaseValue(kind() == 0 ? .18 : .24);
    }

    @Override
    public boolean attackEntityAsMob(Entity target) {
        if (cooldown > 0 || attackTicks() > 0) return false;
        dataManager.set(ATTACK, 18);
        cooldown = 36;
        return true;
    }

    @Override
    public void onLivingUpdate() {
        super.onLivingUpdate();
        if (world.isRemote) return;
        if (cooldown > 0) cooldown--;
        EntityLivingBase target = getAttackTarget();
        if (flying()) {
            setNoGravity(true);
            noClip = kind() == 1;
            Vec3d goal =
                    target == null
                            ? getPositionVector()
                                    .addVector(
                                            Math.sin(ticksExisted * .04),
                                            Math.cos(ticksExisted * .06) * .4,
                                            Math.cos(ticksExisted * .04))
                            : target.getPositionEyes(1);
            Vec3d direction =
                    goal.subtract(getPositionVector()).normalize().scale(kind() == 1 ? .13 : .2);
            motionX = motionX * .8 + direction.x * .2;
            motionY = motionY * .8 + direction.y * .2;
            motionZ = motionZ * .8 + direction.z * .2;
            rotationYaw = (float) -Math.toDegrees(Math.atan2(motionX, motionZ));
            if (target != null && getDistanceSq(target) < 2.8) attackEntityAsMob(target);
        }
        if (kind() == 2
                && target != null
                && getDistanceSq(target) > 9
                && getDistanceSq(target) < 225
                && canEntityBeSeen(target)
                && cooldown == 0
                && attackTicks() == 0) {
            dataManager.set(ATTACK, 24);
            cooldown = 70;
        }
        int attack = attackTicks();
        if (attack > 0) {
            dataManager.set(ATTACK, attack - 1);
            if (target != null && target.isEntityAlive()) {
                getLookHelper().setLookPositionWithEntity(target, 30, 30);
                if (attack == 8) {
                    if (kind() == 2 && getDistanceSq(target) > 9 && canEntityBeSeen(target)) {
                        EntityPrebossShot spear =
                                new EntityPrebossShot(world, this, PrebossItem.Kind.JAVELIN);
                        Vec3d delta = target.getPositionEyes(1).subtract(spear.getPositionVector());
                        spear.shoot(
                                delta.x, delta.y + delta.lengthVector() * .035, delta.z, 1.1F, 2);
                        world.spawnEntity(spear);
                    } else if (getDistanceSq(target) < 5) super.attackEntityAsMob(target);
                    swingArm(net.minecraft.util.EnumHand.MAIN_HAND);
                }
            }
        }
    }

    @Override
    public boolean getCanSpawnHere() {
        int cave = GeologyWorld.caveAt(world, getPosition());
        return (kind() == 0 || kind() == 1
                        ? cave == GeologyWorld.GRANITE
                        : kind() == 2 ? cave == GeologyWorld.MARBLE : cave == GeologyWorld.MUSHROOM)
                && super.getCanSpawnHere();
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {
        if (kind() == 0 || kind() == 1)
            entityDropItem(
                    new net.minecraft.item.ItemStack(Blocks.STONE, 3 + rand.nextInt(5), 1), 0);
        else if (kind() == 2) {
            dropItem(Items.BONE, 1 + rand.nextInt(3));
            dropItem(GeologyContent.JAVELIN, 2 + rand.nextInt(5));
        } else dropItem(GeologyContent.GLOW_MUSHROOM, 1 + rand.nextInt(2));
    }

    public static final class GraniteGolem extends EntityCaveMob {
        public GraniteGolem(World world) {
            super(world);
        }

        public int kind() {
            return 0;
        }
    }

    public static final class GraniteElemental extends EntityCaveMob {
        public GraniteElemental(World world) {
            super(world);
        }

        public int kind() {
            return 1;
        }
    }

    public static final class Hoplite extends EntityCaveMob {
        public Hoplite(World world) {
            super(world);
        }

        public int kind() {
            return 2;
        }
    }

    public static final class SporeBat extends EntityCaveMob {
        public SporeBat(World world) {
            super(world);
        }

        public int kind() {
            return 3;
        }
    }

    public static final class WallCreeper extends EntitySpider {
        public WallCreeper(World world) {
            super(world);
            setSize(1.25F, .8F);
        }

        @Override
        protected void applyEntityAttributes() {
            super.applyEntityAttributes();
            getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(24);
            getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).setBaseValue(4);
        }

        @Override
        public boolean getCanSpawnHere() {
            return GeologyWorld.caveAt(world, getPosition()) == GeologyWorld.SPIDER
                    && super.getCanSpawnHere();
        }

        @Override
        protected void dropFewItems(boolean hit, int looting) {
            dropItem(Items.STRING, 2 + rand.nextInt(3));
            if (rand.nextInt(3) == 0) dropItem(Items.SPIDER_EYE, 1);
        }
    }
}
