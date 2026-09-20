package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import javax.annotation.Nullable;
import java.util.UUID;

public final class EntityScoutHardpoint extends EntityLiving {
    public static final int MINIGUN_LEFT = 0;
    public static final int MINIGUN_RIGHT = 1;
    public static final int LASER_LEFT = 2;
    public static final int LASER_RIGHT = 3;
    public static final int CLAW = 4;
    public static final int VICE = 5;
    public static final int COUNT = 6;

    private static final DataParameter<Integer> PARENT =
            EntityDataManager.createKey(EntityScoutHardpoint.class, DataSerializers.VARINT);
    private static final DataParameter<Byte> KIND =
            EntityDataManager.createKey(EntityScoutHardpoint.class, DataSerializers.BYTE);
    private UUID parentUuid;
    private boolean notified;

    public EntityScoutHardpoint(World world) {
        super(world);
        setNoAI(true);
        setNoGravity(true);
        noClip = false;
        experienceValue = 0;
        setSize(1.25F, 1.25F);
    }

    public EntityScoutHardpoint(World world, EntityX20Scout parent, int kind) {
        this(world);
        dataManager.set(PARENT, parent.getEntityId());
        dataManager.set(KIND, (byte) kind);
        parentUuid = parent.getUniqueID();
        setCustomNameTag(displayName(kind));
        setAlwaysRenderNameTag(false);
        resize(kind);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth(kind));
        setHealth(getMaxHealth());
        follow(parent);
    }

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(PARENT, -1);
        dataManager.register(KIND, (byte) 0);
    }

    @Override
    protected void initEntityAI() {}

    @Override
    protected void applyEntityAttributes() {
        super.applyEntityAttributes();
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(75.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR).setBaseValue(12.0D);
        getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).setBaseValue(5.0D);
        getEntityAttribute(SharedMonsterAttributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);
        getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).setBaseValue(0.0D);
    }

    @Override
    public void onLivingUpdate() {
        EntityX20Scout parent = parent();
        if (getKind() != MINIGUN_RIGHT) {
            if (!world.isRemote) setDead();
            return;
        }
        if (width != 2.5F || height != 4.2F) resize(getKind());
        if (parent == null
                || !parent.isEntityAlive()
                || parent.getPhase() == EntityX20Scout.PHASE_BRAWL
                || !parent.isHardpointAlive(getKind())) {
            if (!world.isRemote) setDead();
            return;
        }
        follow(parent);
        motionX = motionY = motionZ = 0.0D;
        fallDistance = 0.0F;
        hurtResistantTime = Math.min(hurtResistantTime, 3);
        super.onLivingUpdate();
    }

    private void follow(EntityX20Scout parent) {
        Vec3d pos = parent.getHardpointPosition(getKind());
        setPositionAndRotation(pos.x, pos.y - height * 0.5D, pos.z, parent.rotationYaw, 0.0F);
    }

    private void resize(int kind) {
        if (kind == MINIGUN_RIGHT) setSize(2.5F, 4.2F);
        else if (kind == CLAW || kind == VICE) setSize(1.55F, 1.45F);
        else if (kind == LASER_LEFT || kind == LASER_RIGHT) setSize(1.15F, 1.15F);
        else setSize(1.25F, 1.15F);
    }

    private static double maxHealth(int kind) {
        if (kind == CLAW || kind == VICE) return 105.0D;
        if (kind == LASER_LEFT || kind == LASER_RIGHT) return 82.0D;
        return 72.0D;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (source == DamageSource.IN_WALL
                || source == DamageSource.FALL
                || source == DamageSource.DROWN) return false;
        EntityX20Scout owner = parent();
        if (getKind() == MINIGUN_RIGHT && owner != null) return owner.hitShield(source, amount);
        boolean hit = super.attackEntityFrom(source, amount);
        if (hit) {
            EntityX20Scout parent = parent();
            if (parent != null) parent.onHardpointHit(this, amount);
        }
        return hit;
    }

    @Override
    public void onDeath(DamageSource cause) {
        if (!notified) {
            notified = true;
            EntityX20Scout parent = parent();
            if (parent != null) parent.onHardpointDestroyed(getKind());
        }
        super.onDeath(cause);
    }

    @Nullable
    public EntityX20Scout parent() {
        Entity entity = world.getEntityByID(dataManager.get(PARENT));
        if (entity instanceof EntityX20Scout) return (EntityX20Scout) entity;
        if (!world.isRemote && parentUuid != null && world instanceof WorldServer) {
            entity = ((WorldServer) world).getEntityFromUuid(parentUuid);
            if (entity instanceof EntityX20Scout) {
                dataManager.set(PARENT, entity.getEntityId());
                return (EntityX20Scout) entity;
            }
        }
        return null;
    }

    public boolean belongsTo(EntityX20Scout scout) {
        return dataManager.get(PARENT) == scout.getEntityId()
                || parentUuid != null && parentUuid.equals(scout.getUniqueID());
    }

    public int getKind() {
        return dataManager.get(KIND) & 255;
    }

    @Override
    public boolean canBeCollidedWith() {
        EntityX20Scout owner = parent();
        return super.canBeCollidedWith() && (owner == null || !owner.isGuardBroken());
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    protected void collideWithEntity(Entity entityIn) {}

    @Override
    protected void collideWithNearbyEntities() {}

    @Override
    public void writeEntityToNBT(NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        tag.setByte("HardpointKind", (byte) getKind());
        if (parentUuid != null) tag.setUniqueId("ScoutParent", parentUuid);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        float savedHealth = tag.hasKey("Health", 99) ? tag.getFloat("Health") : getHealth();
        int kind = tag.getByte("HardpointKind") & 255;
        dataManager.set(KIND, (byte) kind);
        if (tag.hasUniqueId("ScoutParent")) parentUuid = tag.getUniqueId("ScoutParent");
        resize(kind);
        getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(maxHealth(kind));
        setHealth(Math.min(savedHealth, getMaxHealth()));
    }

    public static String displayName(int kind) {
        switch (kind) {
            case MINIGUN_LEFT:
                return "Scout Left Ice Blade";
            case MINIGUN_RIGHT:
                return "Scout Shield Arm";
            case LASER_LEFT:
                return "Scout Left Frost Conduit";
            case LASER_RIGHT:
                return "Scout Right Frost Conduit";
            case CLAW:
                return "Scout Claw Arm";
            default:
                return "Scout Vice Arm";
        }
    }
}
