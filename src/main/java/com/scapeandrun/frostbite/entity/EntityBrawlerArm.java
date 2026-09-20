package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public final class EntityBrawlerArm extends EntityLiving {
    private static final DataParameter<Integer>
            OWNER = EntityDataManager.createKey(EntityBrawlerArm.class, DataSerializers.VARINT),
            INDEX = EntityDataManager.createKey(EntityBrawlerArm.class, DataSerializers.VARINT);

    public EntityBrawlerArm(World world) {
        super(world);
        setSize(1.7F, 1.7F);
        setNoAI(true);
        setNoGravity(true);
        noClip = true;
        isImmuneToFire = true;
    }

    public EntityBrawlerArm(EntityBrawler boss, int index) {
        this(boss.world);
        dataManager.set(OWNER, boss.getEntityId());
        dataManager.set(INDEX, index);
        follow(boss);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(OWNER, -1);
        dataManager.register(INDEX, 0);
    }

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(100);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
    }

    private EntityBrawler owner() {
        Entity e = world.getEntityByID(dataManager.get(OWNER));
        return e instanceof EntityBrawler ? (EntityBrawler) e : null;
    }

    public void follow(EntityBrawler boss) {
        Vec3d p = boss.localToWorld(boss.hand(dataManager.get(INDEX), 0));
        setPosition(p.x, p.y - height / 2, p.z);
        setHealth(boss.armHealth(dataManager.get(INDEX)));
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        EntityBrawler boss = owner();
        if (boss == null) {
            if (!world.isRemote && ticksExisted > 20) setDead();
            return;
        }
        if (!boss.isEntityAlive()
                || boss.phase() != 1
                || boss.armHealth(dataManager.get(INDEX)) <= 0) {
            setDead();
            return;
        }
        follow(boss);
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        EntityBrawler boss = owner();
        return boss != null && boss.damageArm(dataManager.get(INDEX), source, amount);
    }

    @Override
    public boolean canBeCollidedWith() {
        EntityBrawler boss = owner();
        return !isDead
                && boss != null
                && boss.phase() == 1
                && boss.armHealth(dataManager.get(INDEX)) > 0;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public void travel(float strafe, float vertical, float forward) {}

    @Override
    public boolean writeToNBTOptional(NBTTagCompound n) {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }
}
