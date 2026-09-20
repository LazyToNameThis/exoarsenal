package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.item.ItemRBlade;
import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSourceIndirect;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class EntityRBladeDisc extends Entity {
    private static final DataParameter<Boolean> X10 =
            EntityDataManager.createKey(EntityRBladeDisc.class, DataSerializers.BOOLEAN);
    private UUID ownerId;
    private boolean returning;
    private int returnAt = 14;
    private int boosts;
    private int life;
    private int lockTargetId = -1;
    private int lockTicks;
    private final Map<Integer, Integer> hitCooldowns = new HashMap<>();

    public EntityRBladeDisc(World world) {
        super(world);
        setSize(1.15F, 1.15F);
        noClip = true;
    }

    public EntityRBladeDisc(World world, EntityPlayer owner) {
        this(world, owner, false);
    }

    public EntityRBladeDisc(World world, EntityPlayer owner, boolean x10) {
        this(world);
        dataManager.set(X10, x10);
        ownerId = owner.getUniqueID();
        Vec3d look = owner.getLookVec().normalize();
        Vec3d start =
                owner.getPositionEyes(1.0F).add(look.scale(0.85D)).addVector(0.0D, -0.18D, 0.0D);
        setPosition(start.x, start.y, start.z);
        motionX = look.x * 1.35D;
        motionY = look.y * 1.35D + 0.08D;
        motionZ = look.z * 1.35D;
    }

    @Override
    protected void entityInit() {
        dataManager.register(X10, false);
    }

    public boolean isX10() {
        return dataManager.get(X10);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        prevPosX = posX;
        prevPosY = posY;
        prevPosZ = posZ;
        if (world.isRemote) {
            trailParticles();
            return;
        }

        EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
        if (owner == null) {
            motionX = motionY = motionZ = 0.0D;
            return;
        }

        life++;
        if (isX10() && lockTicks > 0) {
            Entity locked = world.getEntityByID(lockTargetId);
            if (locked instanceof EntityLivingBase && locked.isEntityAlive()) {
                double angle = life * 0.48D;
                setPosition(
                        locked.posX + Math.cos(angle) * 1.15D,
                        locked.posY + locked.height * 0.58D + Math.sin(angle * 0.7D) * 0.45D,
                        locked.posZ + Math.sin(angle) * 1.15D);
                motionX = motionY = motionZ = 0.0D;
                rotationYaw = (rotationYaw + 96.0F) % 360.0F;
                rotationPitch = (rotationPitch + 57.0F) % 360.0F;
                lockTicks--;
                if ((life & 3) == 0) damageNearby(owner);
                if (lockTicks == 0) returning = true;
                return;
            }
            lockTicks = 0;
            returning = true;
        }
        Vec3d start = getPositionVector();
        Vec3d end = start.addVector(motionX, motionY, motionZ);
        RayTraceResult blockHit = world.rayTraceBlocks(start, end, false, true, false);
        if (blockHit != null) returning = true;
        if (!returning && life >= returnAt) returning = true;

        if (returning) {
            Vec3d target =
                    owner.getPositionEyes(1.0F)
                            .add(owner.getLookVec().scale(0.35D))
                            .addVector(0.0D, -0.25D, 0.0D);
            Vec3d home = target.subtract(getPositionVector());
            if (home.lengthSquared() < 1.45D) {
                recover(owner);
                return;
            }
            double speed = 1.05D + boosts * 0.13D;
            Vec3d desired = home.normalize().scale(speed);
            motionX = motionX * 0.42D + desired.x * 0.58D;
            motionY = motionY * 0.42D + desired.y * 0.58D;
            motionZ = motionZ * 0.42D + desired.z * 0.58D;
        }

        setPosition(posX + motionX, posY + motionY, posZ + motionZ);
        rotationYaw = (rotationYaw + 72.0F + boosts * 12.0F) % 360.0F;
        rotationPitch = (rotationPitch + 43.0F) % 360.0F;
        damageNearby(owner);

        if (life > 320) recover(owner);
    }

    private void damageNearby(EntityPlayer owner) {
        for (EntityLivingBase target :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(0.75D))) {
            if (target == owner || !target.isEntityAlive()) continue;
            int last =
                    hitCooldowns.containsKey(target.getEntityId())
                            ? hitCooldowns.get(target.getEntityId())
                            : -20;
            if (life - last < 6) continue;
            float damage = (isX10() ? 9.0F : 6.0F) + boosts * 3.0F;
            net.minecraft.util.ResourceLocation id = EntityList.getKey(target);
            if (id != null && "srparasites".equals(id.getResourceDomain())) damage += 4.0F;
            DamageSource source =
                    new EntityDamageSourceIndirect("rblade_disc", this, owner)
                            .setProjectile()
                            .setFireDamage();
            owner.getEntityData().setBoolean("WastelandEnergySweep", true);
            try {
                if (target.attackEntityFrom(source, damage)) {
                    target.setFire(4 + boosts);
                    hitCooldowns.put(target.getEntityId(), life);
                    if (isX10() && lockTicks <= 0 && !returning) {
                        lockTargetId = target.getEntityId();
                        lockTicks = 100;
                    }
                    if (world instanceof net.minecraft.world.WorldServer) {
                        net.minecraft.world.WorldServer server =
                                (net.minecraft.world.WorldServer) world;
                        server.spawnParticle(
                                EnumParticleTypes.FIREWORKS_SPARK,
                                target.posX,
                                target.posY + target.height * 0.55D,
                                target.posZ,
                                isX10() ? 8 : 5,
                                0.38D,
                                0.42D,
                                0.38D,
                                0.09D);
                        server.spawnParticle(
                                EnumParticleTypes.FLAME,
                                target.posX,
                                target.posY + target.height * 0.5D,
                                target.posZ,
                                2,
                                0.24D,
                                0.3D,
                                0.24D,
                                0.04D);
                    }
                }
            } finally {
                owner.getEntityData().setBoolean("WastelandEnergySweep", false);
            }
        }
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        if (world.isRemote) return true;
        EntityPlayer owner = ownerId == null ? null : world.getPlayerEntityByUUID(ownerId);
        if (!returning || owner == null || source.getTrueSource() != owner || boosts >= 3)
            return false;
        boosts++;
        returning = false;
        returnAt = life + 9 + boosts * 2;
        Vec3d look = owner.getLookVec().normalize();
        double speed = 1.45D + boosts * 0.16D;
        motionX = look.x * speed;
        motionY = look.y * speed + 0.06D;
        motionZ = look.z * speed;
        velocityChanged = true;
        world.playSound(
                null,
                posX,
                posY,
                posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                0.9F,
                1.25F + boosts * 0.12F);
        return true;
    }

    private void recover(EntityPlayer owner) {
        ItemStack found = ItemStack.EMPTY;
        for (ItemStack stack : owner.inventory.mainInventory) {
            if (isThrownBlade(stack)) {
                found = stack;
                break;
            }
        }
        if (found.isEmpty()) {
            for (ItemStack stack : owner.inventory.offHandInventory) {
                if (isThrownBlade(stack)) {
                    found = stack;
                    break;
                }
            }
        }
        if (!found.isEmpty()) {
            found.getTagCompound().setBoolean("Thrown", false);
            found.getTagCompound().setBoolean("Spinning", false);
            found.getTagCompound().setBoolean("Active", true);
            ItemRBlade.animate(found, "catch", world.getTotalWorldTime());
            world.playSound(
                    null,
                    owner.posX,
                    owner.posY + 1.0D,
                    owner.posZ,
                    SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
                    SoundCategory.PLAYERS,
                    0.65F,
                    1.7F);
        }
        setDead();
    }

    private static boolean isThrownBlade(ItemStack stack) {
        return stack.getItem() instanceof ItemRBlade
                && stack.hasTagCompound()
                && stack.getTagCompound().getBoolean("Thrown");
    }

    private void trailParticles() {
        if ((ticksExisted & 1) != 0) return;
        double phase = ticksExisted * 0.9D;
        for (int i = 0; i < 2; i++) {
            double angle = phase + i * Math.PI;
            double px = posX + Math.cos(angle) * 0.42D;
            double py = posY + Math.sin(angle) * 0.42D;
            double pz = posZ + Math.sin(angle * 0.7D) * 0.22D;
            world.spawnParticle(EnumParticleTypes.REDSTONE, px, py, pz, 1.0D, 0.04D, 0.01D);
        }
        world.spawnParticle(
                EnumParticleTypes.FLAME,
                posX - motionX * 0.2D,
                posY - motionY * 0.2D,
                posZ - motionZ * 0.2D,
                -motionX * 0.035D,
                -motionY * 0.035D,
                -motionZ * 0.035D);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean canBePushed() {
        return false;
    }

    @Override
    public float getCollisionBorderSize() {
        return 0.65F;
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound tag) {
        if (tag.hasKey("Owner")) ownerId = UUID.fromString(tag.getString("Owner"));
        returning = tag.getBoolean("Returning");
        returnAt = tag.getInteger("ReturnAt");
        boosts = tag.getInteger("Boosts");
        life = tag.getInteger("Life");
        lockTargetId = tag.getInteger("LockTarget");
        lockTicks = tag.getInteger("LockTicks");
        dataManager.set(X10, tag.getBoolean("X10"));
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound tag) {
        if (ownerId != null) tag.setString("Owner", ownerId.toString());
        tag.setBoolean("Returning", returning);
        tag.setInteger("ReturnAt", returnAt);
        tag.setInteger("Boosts", boosts);
        tag.setInteger("Life", life);
        tag.setInteger("LockTarget", lockTargetId);
        tag.setInteger("LockTicks", lockTicks);
        tag.setBoolean("X10", isX10());
    }
}
