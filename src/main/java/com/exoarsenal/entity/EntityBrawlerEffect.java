package com.exoarsenal.entity;

import io.netty.buffer.ByteBuf;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public final class EntityBrawlerEffect extends Entity implements IEntityAdditionalSpawnData {
    public static final int SHARD = 0,
            CUT = 1,
            CHAIN = 2,
            HEX = 3,
            FAULT = 4,
            RING = 5,
            TERRAIN = 6,
            RECALL = 7,
            BLADE = 8,
            SHELL = 9,
            PLANE = 10,
            FIST = 11,
            HEAD_ECHO = 12,
            JETWASH = 13,
            GROUND_CELL = 14;
    private static final DataParameter<Integer> AGE =
            EntityDataManager.createKey(EntityBrawlerEffect.class, DataSerializers.VARINT);
    private static final DataParameter<Float>
            END_X = EntityDataManager.createKey(EntityBrawlerEffect.class, DataSerializers.FLOAT),
            END_Y = EntityDataManager.createKey(EntityBrawlerEffect.class, DataSerializers.FLOAT),
            END_Z = EntityDataManager.createKey(EntityBrawlerEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> TERRAIN_STATE =
            EntityDataManager.createKey(EntityBrawlerEffect.class, DataSerializers.VARINT);
    private int kind, owner, warning, life;
    private float damage, radius;
    private Vec3d end = Vec3d.ZERO, velocity = Vec3d.ZERO;
    private boolean tether;
    private Vec3d guidedPosition;
    private final Set<Integer> struck = new HashSet<>();

    public EntityBrawlerEffect(World w) {
        super(w);
        setSize(.9F, .9F);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityBrawlerEffect(
            EntityBrawler boss,
            int kind,
            Vec3d start,
            Vec3d end,
            int warning,
            int life,
            float radius,
            float damage) {
        this(boss.world);
        owner = boss.getEntityId();
        this.kind = kind;
        this.warning = warning;
        this.life = life;
        this.radius = radius;
        this.damage = damage;
        endpoints(start, end);
    }

    public EntityBrawlerEffect velocity(Vec3d v) {
        velocity = v;
        return this;
    }

    public EntityBrawlerEffect launch(Vec3d v, float contactDamage) {
        guidedPosition = null;
        velocity = v;
        damage = contactDamage;
        struck.clear();
        return this;
    }

    public EntityBrawlerEffect tether() {
        tether = true;
        return this;
    }

    public int kind() {
        return kind;
    }

    public int age() {
        return dataManager.get(AGE);
    }

    public int warning() {
        return warning;
    }

    public int life() {
        return life;
    }

    public float radius() {
        return radius;
    }

    public Vec3d end() {
        return world.isRemote
                ? getPositionVector()
                        .addVector(
                                dataManager.get(END_X),
                                dataManager.get(END_Y),
                                dataManager.get(END_Z))
                : end;
    }

    public boolean active() {
        return age() >= warning;
    }

    public EntityBrawler boss() {
        Entity e = world.getEntityByID(owner);
        return e instanceof EntityBrawler ? (EntityBrawler) e : null;
    }

    public void endpoints(Vec3d start, Vec3d finish) {
        setPosition(start.x, start.y, start.z);
        end = finish;
        Vec3d offset = finish.subtract(start);
        dataManager.set(END_X, (float) offset.x);
        dataManager.set(END_Y, (float) offset.y);
        dataManager.set(END_Z, (float) offset.z);
    }

    public void guide(Vec3d position, float contactDamage) {
        guidedPosition = position;
        damage = contactDamage;
    }

    public void rearm() {
        struck.clear();
    }

    public EntityBrawlerEffect terrainState(net.minecraft.block.state.IBlockState state) {
        dataManager.set(TERRAIN_STATE, net.minecraft.block.Block.getStateId(state));
        return this;
    }

    public net.minecraft.block.state.IBlockState terrainState() {
        return net.minecraft.block.Block.getStateById(dataManager.get(TERRAIN_STATE));
    }

    @Override
    protected void entityInit() {
        dataManager.register(AGE, 0);
        dataManager.register(END_X, 0F);
        dataManager.register(END_Y, 0F);
        dataManager.register(END_Z, 0F);
        dataManager.register(TERRAIN_STATE, 1);
    }

    @Override
    public boolean canBeCollidedWith() {
        return kind == RECALL;
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (kind != RECALL || world.isRemote || amount <= 0) return false;
        setDead();
        return true;
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        EntityBrawler b = boss();
        if (b == null || !b.isEntityAlive() || b.transition() > 0 || b.clashTick() > 0) {
            setDead();
            return;
        }
        int t = age() + 1;
        dataManager.set(AGE, t);
        if (t > life) {
            setDead();
            return;
        }
        if (tether) {
            Vec3d socket = b.localToWorld(b.hand(2, 0));
            endpoints(socket, end);
        }
        if (t < warning) return;
        Vec3d from = getPositionVector(), to = from;
        if (guidedPosition != null) {
            to = guidedPosition;
            endpoints(to, end);
            velocityChanged = true;
        } else if (kind == SHARD
                || kind == CUT
                || kind == TERRAIN
                || kind == BLADE
                || kind == FIST) {
            to = from.add(velocity);
            RayTraceResult wall = world.rayTraceBlocks(from, to, false, true, false);
            if (wall != null) {
                to = wall.hitVec;
                life = t;
            }
            setPosition(to.x, to.y, to.z);
            velocityChanged = true;
        }
        if (kind == RECALL) {
            to =
                    from.add(
                            b.getPositionVector()
                                    .addVector(0, 1.4, 0)
                                    .subtract(from)
                                    .normalize()
                                    .scale(.22));
            setPosition(to.x, to.y, to.z);
            if (getDistanceSq(b) < 3) {
                b.recalledCell();
                setDead();
                return;
            }
        }
        AxisAlignedBB area =
                (kind == FAULT || kind == CHAIN || kind == JETWASH)
                        ? new AxisAlignedBB(from, end).grow(radius)
                        : new AxisAlignedBB(from, to)
                                .grow(kind == RING || kind == PLANE ? radius : radius + .65);
        for (EntityPlayer p : world.getEntitiesWithinAABB(EntityPlayer.class, area)) {
            if (p.isCreative()
                    || p.isSpectator()
                    || struck.contains(p.getEntityId())
                    || damage <= 0) continue;
            boolean contact;
            if (kind == JETWASH) {
                Vec3d axis = end.subtract(from), offset = p.getPositionEyes(1).subtract(from);
                double f = offset.dotProduct(axis) / Math.max(.001, axis.lengthSquared());
                contact =
                        f >= 0
                                && f <= 1
                                && offset.subtract(axis.scale(f)).lengthVector() < .3 + radius * f;
                if (contact) {
                    Vec3d push = axis.normalize().scale(.35);
                    p.addVelocity(push.x, push.y + .06, push.z);
                    p.velocityChanged = true;
                }
            } else if (kind == FAULT || kind == CHAIN)
                contact =
                        p.getEntityBoundingBox().grow(radius).calculateIntercept(from, end) != null;
            else if (kind == RING) {
                double r = radius * Math.min(1, (t - warning + 1D) / Math.max(1, life - warning));
                double d = Math.sqrt(p.getDistanceSq(posX, p.posY, posZ));
                contact = Math.abs(d - r) < .85 && Math.abs(p.posY - posY) < 2;
            } else if (kind == PLANE) {
                Vec3d offset = p.getPositionEyes(1).subtract(from), normal = end.normalize();
                contact =
                        Math.abs(offset.dotProduct(normal)) < .8
                                && offset.lengthSquared() < radius * radius;
            } else if (kind == GROUND_CELL)
                contact =
                        Math.hypot(p.posX - posX, p.posZ - posZ) < radius
                                && Math.abs(p.posY - posY) < 2.5;
            else if (kind == HEX || kind == SHELL)
                contact = p.getDistanceSq(this) < radius * radius;
            else
                contact =
                        p.getEntityBoundingBox().grow(radius).contains(from)
                                || p.getEntityBoundingBox()
                                                .grow(radius)
                                                .calculateIntercept(from, to)
                                        != null;
            if (contact) {
                p.attackEntityFrom(DamageSource.causeIndirectDamage(this, b), damage);
                struck.add(p.getEntityId());
            }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf b) {
        b.writeInt(kind);
        b.writeInt(owner);
        b.writeInt(warning);
        b.writeInt(life);
        b.writeFloat(radius);
        b.writeDouble(end.x);
        b.writeDouble(end.y);
        b.writeDouble(end.z);
    }

    @Override
    public void readSpawnData(ByteBuf b) {
        kind = b.readInt();
        owner = b.readInt();
        warning = b.readInt();
        life = b.readInt();
        radius = b.readFloat();
        end = new Vec3d(b.readDouble(), b.readDouble(), b.readDouble());
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }
}
