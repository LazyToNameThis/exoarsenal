package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.*;
import java.util.UUID;

public final class EntityScourgeSegment extends EntityLiving {
    private static final DataParameter<Integer>
            INDEX = EntityDataManager.createKey(EntityScourgeSegment.class, DataSerializers.VARINT),
            PARENT =
                    EntityDataManager.createKey(EntityScourgeSegment.class, DataSerializers.VARINT);
    private UUID owner;
    private int missing;

    public EntityScourgeSegment(World w) {
        super(w);
        setSize(1.8F, 1.8F);
        setNoAI(true);
        setNoGravity(true);
        noClip = true;
        isImmuneToFire = true;
    }

    public EntityScourgeSegment(EntityDesertScourge boss, int index) {
        this(boss.world);
        owner = boss.getUniqueID();
        dataManager.set(PARENT, boss.getEntityId());
        dataManager.set(INDEX, index);
        place(boss);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(INDEX, 1);
        dataManager.register(PARENT, -1);
    }

    public int index() {
        return dataManager.get(INDEX);
    }

    public boolean belongs(EntityDesertScourge boss) {
        return dataManager.get(PARENT) == boss.getEntityId()
                || owner != null && owner.equals(boss.getUniqueID());
    }

    private EntityDesertScourge parent() {
        Entity e = world.getEntityByID(dataManager.get(PARENT));
        if (!world.isRemote && !(e instanceof EntityDesertScourge) && owner != null)
            e = ((WorldServer) world).getEntityFromUuid(owner);
        return e instanceof EntityDesertScourge ? (EntityDesertScourge) e : null;
    }

    private void place(EntityDesertScourge boss) {
        Vec3d p = boss.segment(index(), 1);
        float scale = (float) boss.sizeScale();
        setSize(1.8F * scale, 1.8F * scale);
        setPosition(p.x, p.y - .8 * scale, p.z);
        motionX = motionY = motionZ = 0;
    }

    @Override
    public void onLivingUpdate() {
        if (world.isRemote) {
            super.onLivingUpdate();
            return;
        }
        EntityDesertScourge boss = parent();
        if (boss == null || boss.isDead) {
            if (++missing > 80) setDead();
            return;
        }
        missing = 0;
        dataManager.set(PARENT, boss.getEntityId());
        place(boss);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(boss.getMaxHealth());
        setHealth(Math.max(1, boss.getHealth()));
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float damage) {
        EntityDesertScourge boss = parent();
        return boss != null && boss.hurtSegment(source, damage);
    }

    @Override
    public boolean canBeCollidedWith() {
        EntityDesertScourge boss = parent();
        return boss != null && boss.isEntityAlive() && !boss.hidden();
    }

    @Override
    public void fall(float d, float m) {}

    @Override
    protected boolean canDespawn() {
        return false;
    }

    @Override
    protected void dropFewItems(boolean hit, int looting) {}

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        if (owner != null) n.setUniqueId("Owner", owner);
        n.setInteger("Index", index());
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
        dataManager.set(INDEX, Math.max(1, Math.min(24, n.getInteger("Index"))));
    }
}
