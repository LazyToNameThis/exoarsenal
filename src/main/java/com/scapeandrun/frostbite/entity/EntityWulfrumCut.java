package com.scapeandrun.frostbite.entity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;

public final class EntityWulfrumCut extends Entity implements IEntityAdditionalSpawnData {
    private int owner = -1, warning = 12, life = 40;
    private float radius = 3, damage = 4;
    private boolean saw;
    private int plane;
    private boolean follow, sphere;

    public EntityWulfrumCut sphere() {
        sphere = true;
        return this;
    }

    public boolean spherical() {
        return sphere;
    }

    public EntityWulfrumCut plane(int p) {
        plane = Math.floorMod(p, 3);
        return this;
    }

    public int plane() {
        return plane;
    }

    public EntityWulfrumCut follow() {
        follow = true;
        return this;
    }

    public EntityWulfrumCut(World w) {
        super(w);
        setSize(.2F, .2F);
        noClip = true;
        ignoreFrustumCheck = true;
    }

    public EntityWulfrumCut(
            EntityWulfrumEye eye,
            Vec3d p,
            float radius,
            int warning,
            int life,
            float damage,
            boolean saw) {
        this(eye.world);
        owner = eye.getEntityId();
        setPosition(p.x, p.y, p.z);
        this.radius = radius;
        this.warning = WulfrumCombatClock.ticks(warning);
        this.life = WulfrumCombatClock.ticks(life);
        this.damage = damage;
        this.saw = saw;
    }

    public float radius() {
        return radius;
    }

    public boolean saw() {
        return saw;
    }

    public boolean active() {
        return ticksExisted >= warning;
    }

    public float fade() {
        return Math.min(1, (warning + life - ticksExisted) / 8F);
    }

    @Override
    protected void entityInit() {}

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        Entity e = world.getEntityByID(owner);
        if (ticksExisted > warning + life
                || !(e instanceof EntityWulfrumEye)
                || !e.isEntityAlive()
                || ((EntityWulfrumEye) e).changing()) {
            setDead();
            return;
        }
        if (follow) {
            Vec3d at = ((EntityWulfrumEye) e).pupil();
            setPosition(at.x, at.y, at.z);
        }
        if (!active()) return;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, getEntityBoundingBox().grow(radius))) {
            if (p.isCreative() || p.isSpectator()) continue;
            double dx = p.posX - posX,
                    dy = p.posY + p.height * .5 - posY,
                    dz = p.posZ - posZ,
                    d =
                            plane == 0
                                    ? Math.sqrt(dx * dx + dz * dz)
                                    : plane == 1
                                            ? Math.sqrt(dx * dx + dy * dy)
                                            : Math.sqrt(dy * dy + dz * dz),
                    off = plane == 0 ? Math.abs(dy) : plane == 1 ? Math.abs(dz) : Math.abs(dx);
            boolean hit =
                    sphere
                            ? Math.abs(Math.sqrt(dx * dx + dy * dy + dz * dz) - radius) < .9
                            : off < (plane == 0 ? p.height * .5 + .2 : .6)
                                    && (saw ? d < radius + .3 : Math.abs(d - radius) < .6);
            if (hit)
                p.attackEntityFrom(
                        ((EntityWulfrumEye) e).projectileDamage(this),
                        ((EntityWulfrumEye) e).balancedDamage(damage));
        }
    }

    @Override
    public void writeSpawnData(ByteBuf b) {
        b.writeInt(owner);
        b.writeInt(warning);
        b.writeInt(life);
        b.writeFloat(radius);
        b.writeFloat(damage);
        b.writeBoolean(saw);
        b.writeInt(plane);
        b.writeBoolean(sphere);
    }

    @Override
    public void readSpawnData(ByteBuf b) {
        owner = b.readInt();
        warning = b.readInt();
        life = b.readInt();
        radius = b.readFloat();
        damage = b.readFloat();
        saw = b.readBoolean();
        plane = b.readInt();
        sphere = b.readBoolean();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }
}
