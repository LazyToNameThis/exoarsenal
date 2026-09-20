package com.scapeandrun.frostbite.entity;

import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import java.util.UUID;

public final class EntityScourgeSand extends Entity {
    private static final DataParameter<Integer> KIND =
            EntityDataManager.createKey(EntityScourgeSand.class, DataSerializers.VARINT);
    private UUID owner;
    private int age;

    public EntityScourgeSand(World w) {
        super(w);
        setSize(.4F, .4F);
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityScourgeSand(EntityDesertScourge boss, Vec3d p, Vec3d motion, int kind) {
        this(boss.world);
        owner = boss.getUniqueID();
        dataManager.set(KIND, kind);
        setPosition(p.x, p.y, p.z);
        motionX = motion.x;
        motionY = motion.y;
        motionZ = motion.z;
    }

    @Override
    protected void entityInit() {
        dataManager.register(KIND, 0);
    }

    public int kind() {
        return dataManager.get(KIND);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        age++;
        if (world.isRemote) return;
        Entity source = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
        if (!(source instanceof EntityDesertScourge)
                || !source.isEntityAlive()
                || age > (kind() == 2 ? 100 : 120)) {
            setDead();
            return;
        }
        Vec3d from = getPositionVector(), to = from.addVector(motionX, motionY, motionZ);
        if (kind() == 2) {
            to =
                    new Vec3d(
                            to.x,
                            world.getTopSolidOrLiquidBlock(new BlockPos(to.x, 0, to.z)).getY(),
                            to.z);
            setSize(2, 12);
        } else {
            RayTraceResult hit = world.rayTraceBlocks(from, to, false, true, false);
            if (hit != null) {
                setDead();
                return;
            }
            if (kind() == 0) motionY -= .012;
        }
        setPosition(to.x, to.y, to.z);
        AxisAlignedBB bounds =
                kind() == 2 ? getEntityBoundingBox() : new AxisAlignedBB(from, to).grow(.35);
        for (EntityPlayer p : world.getEntitiesWithinAABB(EntityPlayer.class, bounds)) {
            if (p.isSpectator() || p.capabilities.isCreativeMode) continue;
            p.attackEntityFrom(
                    DamageSource.causeIndirectDamage(this, (EntityLivingBase) source)
                            .setProjectile(),
                    kind() == 2 ? 7 : 5);
            if (kind() != 2) {
                setDead();
                break;
            }
        }
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {
        n.setInteger("Kind", kind());
        n.setInteger("Age", age);
        if (owner != null) n.setUniqueId("Owner", owner);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        dataManager.set(KIND, n.getInteger("Kind"));
        age = n.getInteger("Age");
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
    }
}
