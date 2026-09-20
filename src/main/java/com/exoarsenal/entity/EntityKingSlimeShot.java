package com.exoarsenal.entity;

import com.exoarsenal.entity.slime.KingSlimeAttackRules;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import java.util.UUID;

public class EntityKingSlimeShot extends Entity {
    private static final DataParameter<Integer> KIND =
            EntityDataManager.createKey(EntityKingSlimeShot.class, DataSerializers.VARINT);
    private boolean collide;
    private EntityLivingBase shooter;
    private UUID bossOwner;

    public EntityKingSlimeShot(World w) {
        super(w);
        setSize(.3F, .3F);
    }

    public EntityKingSlimeShot(
            EntityLivingBase shooter, Vec3d at, Vec3d v, int kind, boolean collide) {
        this(shooter.world);
        this.shooter = shooter;
        this.collide = collide;
        if (shooter instanceof EntityKingSlimeSupport) {
            EntityKingSlime owner = ((EntityKingSlimeSupport) shooter).boss();
            if (owner != null) bossOwner = owner.getUniqueID();
        }
        dataManager.set(KIND, kind);
        setPosition(at.x, at.y, at.z);
        motionX = v.x;
        motionY = v.y;
        motionZ = v.z;
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
        Vec3d from = getPositionVector(), to = from.addVector(motionX, motionY, motionZ);
        if (!world.isRemote) {
            Entity boss =
                    bossOwner == null ? null : ((WorldServer) world).getEntityFromUuid(bossOwner);
            if (bossOwner != null
                    && (!(boss instanceof EntityKingSlime)
                            || boss.isDead
                            || ((EntityKingSlime) boss).state() == KingSlimeAttackRules.DEATH)) {
                setDead();
                return;
            }
            if (ticksExisted > 120) {
                setDead();
                return;
            }
            RayTraceResult block =
                    (collide || kind() == 1 && ticksExisted > 90)
                            ? world.rayTraceBlocks(from, to)
                            : null;
            if (block != null) {
                to = block.hitVec;
            }
            for (EntityPlayer p :
                    world.getEntitiesWithinAABB(
                            EntityPlayer.class,
                            getEntityBoundingBox().expand(motionX, motionY, motionZ).grow(.5))) {
                if (p.capabilities.isCreativeMode || p.isSpectator()) continue;
                AxisAlignedBB hit = p.getEntityBoundingBox().grow(.15);
                if (hit.contains(from) || hit.calculateIntercept(from, to) != null) {
                    p.attackEntityFrom(
                            shooter == null
                                    ? DamageSource.MAGIC
                                    : DamageSource.causeIndirectMagicDamage(this, shooter),
                            kind() == 0 ? 6 : 4);
                    setDead();
                    return;
                }
            }
            if (block != null) {
                setDead();
                return;
            }
        }
        setPosition(to.x, to.y, to.z);
        if (kind() == 1 && motionX * motionX + motionY * motionY + motionZ * motionZ < 1.42) {
            motionX *= 1.044;
            motionY *= 1.044;
            motionZ *= 1.044;
        }
        if (kind() == 2) motionY -= .025;
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {
        n.setInteger("Kind", kind());
        n.setBoolean("Collide", collide);
        n.setInteger("Age", ticksExisted);
        if (bossOwner != null) n.setUniqueId("KingOwner", bossOwner);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        dataManager.set(KIND, n.getInteger("Kind"));
        collide = n.getBoolean("Collide");
        ticksExisted = n.getInteger("Age");
        bossOwner = n.hasUniqueId("KingOwner") ? n.getUniqueId("KingOwner") : null;
    }
}
