package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import io.netty.buffer.ByteBuf;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class EntityExcavatorPayload extends Entity implements IEntityAdditionalSpawnData {
    public static final int BEAM = 0,
            RING = 1,
            CHUNK = 2,
            FAULT = 3,
            DEBRIS = 4,
            TETHER = 5,
            LASER_WALL = 6,
            SURVEY_LINE = 7;
    private static final DataParameter<Integer> AGE =
            EntityDataManager.createKey(EntityExcavatorPayload.class, DataSerializers.VARINT);
    private static final DataParameter<Float> TILT =
            EntityDataManager.createKey(EntityExcavatorPayload.class, DataSerializers.FLOAT);
    private static final DataParameter<Integer> SECTION =
            EntityDataManager.createKey(EntityExcavatorPayload.class, DataSerializers.VARINT);
    private static final DataParameter<Float> TENSION =
            EntityDataManager.createKey(EntityExcavatorPayload.class, DataSerializers.FLOAT);
    private int kind, owner = -1, warning = 20, life = 60;
    private float damage = 6, size = 1;
    private Vec3d end = Vec3d.ZERO;
    private boolean launched;
    private int[] blocks = new int[0];
    private double originY, encounterClock, bulgeHeight;

    public void bulge(double height) {
        bulgeHeight = height;
    }

    public EntityExcavatorPayload(World w) {
        super(w);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityExcavatorPayload(
            EntityExcavator boss,
            int kind,
            Vec3d start,
            Vec3d end,
            int warning,
            int life,
            float size,
            float damage) {
        this(boss.world);
        this.owner = boss.getEntityId();
        this.kind = kind;
        this.end = end;
        this.warning = warning;
        this.life = life;
        this.size = size;
        this.damage = damage;
        originY = start.y;
        setSize(kind == CHUNK ? size * 2 : .2F, kind == CHUNK ? 1.2F : .2F);
        setPosition(start.x, start.y, start.z);
        if (kind == CHUNK || kind == DEBRIS) {
            int radius = kind == CHUNK ? (int) size : 0, span = Math.max(1, radius * 2);
            blocks = new int[span * span];
            for (int x = 0; x < span; x++)
                for (int z = 0; z < span; z++) {
                    BlockPos p = new BlockPos(start).add(x - radius, 1, z - radius);
                    if (kind == DEBRIS) p = world.getTopSolidOrLiquidBlock(p).down();
                    net.minecraft.block.state.IBlockState state = world.getBlockState(p);
                    if (!state.isFullCube())
                        state = net.minecraft.init.Blocks.STONE.getDefaultState();
                    blocks[x * span + z] = net.minecraft.block.Block.getStateId(state);
                }
        }
    }

    public net.minecraft.block.state.IBlockState block(int x, int z) {
        int radius = kind == CHUNK ? (int) size : 0,
                span = Math.max(1, radius * 2),
                index = (x + radius) * span + z + radius;
        return index >= 0 && index < blocks.length
                ? net.minecraft.block.Block.getStateById(blocks[index])
                : net.minecraft.init.Blocks.STONE.getDefaultState();
    }

    @Override
    protected void entityInit() {
        dataManager.register(AGE, 0);
        dataManager.register(TILT, 0F);
        dataManager.register(SECTION, -1);
        dataManager.register(TENSION, 0F);
    }

    public void tension(float value) {
        dataManager.set(TENSION, MathHelper.clamp(value, -4, 4));
    }

    public Vec3d tetherPoint(double f) {
        return ExcavatorGeometry.tether(getPositionVector(), end, dataManager.get(TENSION), f);
    }

    public void section(int index) {
        dataManager.set(SECTION, index);
    }

    public static int sectionFor(int x, int z) {
        double xx = x + .5, zz = z + .5;
        if (Math.abs(xx) < 3 && Math.abs(zz) < 3) return 0;
        return Math.abs(xx) > Math.abs(zz) ? xx < 0 ? 1 : 2 : zz < 0 ? 3 : 4;
    }

    public boolean hasTile(int x, int z) {
        int section = dataManager.get(SECTION);
        return tile(x, z, size)
                && (section < 0
                        || section == 5 && x < 0
                        || section == 6 && x >= 0
                        || section < 5 && sectionFor(x, z) == section);
    }

    public void splitInHalf() {
        if (world.isRemote || kind != CHUNK) return;
        Entity e = world.getEntityByID(owner);
        if (!(e instanceof EntityExcavator)) return;
        for (int side : new int[] {-1, 1}) {
            EntityExcavatorPayload half =
                    new EntityExcavatorPayload(
                            (EntityExcavator) e, CHUNK, getPositionVector(), end, 0, 70, size, 0);
            half.blocks = blocks.clone();
            half.section(side < 0 ? 5 : 6);
            half.launch(new Vec3d(side * .32, .06, 0));
            world.spawnEntity(half);
        }
        setDead();
    }

    public float tilt() {
        return dataManager.get(TILT);
    }

    public void tilt(float degrees) {
        dataManager.set(TILT, MathHelper.clamp(degrees, 0, 90));
    }

    public AxisAlignedBB tileBounds(int x, int z) {
        double a = Math.toRadians(tilt()), s = Math.sin(a), c = Math.cos(a);
        double minY = Double.MAX_VALUE,
                minZ = Double.MAX_VALUE,
                maxY = -Double.MAX_VALUE,
                maxZ = -Double.MAX_VALUE;
        for (int iy = 0; iy < 2; iy++)
            for (int iz = 0; iz < 2; iz++) {
                double y = iy * 1.2, zz = z + iz, ry = y * c - zz * s, rz = y * s + zz * c;
                minY = Math.min(minY, ry);
                maxY = Math.max(maxY, ry);
                minZ = Math.min(minZ, rz);
                maxZ = Math.max(maxZ, rz);
            }
        return new AxisAlignedBB(
                posX + x, posY + minY, posZ + minZ, posX + x + 1, posY + maxY, posZ + maxZ);
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

    public float size() {
        return size;
    }

    public Vec3d end() {
        return end;
    }

    public boolean active() {
        return age() >= warning;
    }

    public void launch(Vec3d velocity) {
        launched = true;
        motionX = velocity.x;
        motionY = velocity.y;
        motionZ = velocity.z;
        velocityChanged = true;
    }

    public void place(Vec3d p) {
        Vec3d delta = p.subtract(getPositionVector());
        carry(delta);
        setPosition(p.x, p.y, p.z);
        velocityChanged = true;
    }

    private void carry(Vec3d delta) {
        if (kind != CHUNK || tilt() > 1) return;
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class, getEntityBoundingBox().grow(.2, .6, .2))) {
            if (Math.abs(p.posY - (posY + 1.2)) < .7
                    && hasTile(MathHelper.floor(p.posX - posX), MathHelper.floor(p.posZ - posZ))) {
                p.setPosition(p.posX + delta.x, p.posY + delta.y, p.posZ + delta.z);
                p.fallDistance = 0;
                p.onGround = true;
            }
        }
    }

    public void shatter() {
        if (world.isRemote) return;
        Entity e = world.getEntityByID(owner);
        if (kind == CHUNK && e instanceof EntityExcavator)
            ((EntityExcavator) e).debris(getPositionVector(), 8);
        setDead();
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        if (world.isRemote) return;
        Entity boss = world.getEntityByID(owner);
        if (!(boss instanceof EntityExcavator) || !boss.isEntityAlive()) {
            setDead();
            return;
        }
        encounterClock += ((EntityExcavator) boss).encounterRate();
        if (encounterClock < 1) return;
        encounterClock -= 1;
        dataManager.set(AGE, age() + 1);
        if (age() > life) {
            setDead();
            return;
        }
        if (kind == CHUNK && !launched && (life <= 16 || bulgeHeight > 0))
            place(
                    new Vec3d(
                            posX,
                            originY
                                    + Math.sin(age() * Math.PI / life)
                                            * (bulgeHeight > 0 ? bulgeHeight : .35),
                            posZ));
        if (launched) {
            Vec3d old = getPositionVector(), next = old.addVector(motionX, motionY, motionZ);
            carry(next.subtract(old));
            setPosition(next.x, next.y, next.z);
            motionY -= kind == DEBRIS ? .035 : .025;
            if (world.rayTraceBlocks(old, next, false, true, false) != null) {
                if (kind == CHUNK) ((EntityExcavator) boss).shock(next, 9);
                shatter();
                return;
            }
        }
        if (!active() || damage <= 0) return;
        double radius = kind == RING ? (age() - warning) * size : .6;
        AxisAlignedBB bounds =
                kind == BEAM || kind == FAULT || kind == TETHER || kind == LASER_WALL
                        ? new AxisAlignedBB(getPositionVector(), end)
                                .expand(0, kind == LASER_WALL ? size : 0, 0)
                                .grow(kind == TETHER ? 5 : 1.5)
                        : getEntityBoundingBox().grow(kind == RING ? radius + 1 : 1);
        for (EntityPlayer p : world.getEntitiesWithinAABB(EntityPlayer.class, bounds)) {
            if (p.isCreative() || p.isSpectator()) continue;
            boolean hit = false;
            if (kind == BEAM) {
                RayTraceResult wall =
                        world.rayTraceBlocks(getPositionVector(), end, false, true, false);
                Vec3d stop = wall == null ? end : wall.hitVec;
                hit =
                        p.getEntityBoundingBox()
                                        .grow(size)
                                        .calculateIntercept(getPositionVector(), stop)
                                != null;
            } else if (kind == TETHER) {
                for (int i = 0; i < 12 && !hit; i++)
                    hit =
                            p.getEntityBoundingBox()
                                            .grow(.15)
                                            .calculateIntercept(
                                                    tetherPoint(i / 12D),
                                                    tetherPoint((i + 1) / 12D))
                                    != null;
            } else if (kind == LASER_WALL) {
                Vec3d foot = new Vec3d(p.posX, posY, p.posZ);
                hit =
                        p.posY + p.height >= posY
                                && p.posY <= posY + size
                                && ExcavatorGeometry.distanceToSegment(
                                                foot, getPositionVector(), end)
                                        < .8;
            } else if (kind == FAULT) {
                Vec3d d = end.subtract(getPositionVector()),
                        v = p.getPositionVector().subtract(getPositionVector());
                double f =
                        MathHelper.clamp(v.dotProduct(d) / Math.max(.01, d.lengthSquared()), 0, 1);
                Vec3d at = getPositionVector().add(d.scale(f));
                hit =
                        Math.abs(p.posX - at.x) < 1.4
                                && Math.abs(p.posZ - at.z) < 1.4
                                && p.posY >= at.y - 1
                                && p.posY < at.y + 6;
            } else if (kind == RING) {
                Vec3d point = p.getPositionVector().subtract(getPositionVector());
                double nearest = Double.MAX_VALUE;
                for (int i = 0; i < 6; i++) {
                    Vec3d a = ringPoint(radius, i),
                            d = ringPoint(radius, i + 1).subtract(a),
                            v = new Vec3d(point.x, 0, point.z).subtract(a);
                    double f =
                            MathHelper.clamp(
                                    v.dotProduct(d) / Math.max(.0001, d.lengthSquared()), 0, 1);
                    nearest = Math.min(nearest, v.subtract(d.scale(f)).lengthVector());
                }
                hit = nearest < 1.2 && Math.abs(p.posY - posY) < 2.5;
            } else
                hit =
                        launched
                                && p.getEntityBoundingBox()
                                        .intersects(getEntityBoundingBox().grow(.3))
                                && !(kind == CHUNK
                                        && Math.abs(p.posY - posY - 1.2) < .7
                                        && hasTile(
                                                MathHelper.floor(p.posX - posX),
                                                MathHelper.floor(p.posZ - posZ)));
            if (hit)
                p.attackEntityFrom(
                        net.minecraft.util.DamageSource.causeMobDamage((EntityLivingBase) boss),
                        damage);
        }
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @Override
    public boolean canBeCollidedWith() {
        return kind == CHUNK;
    }

    public static boolean tile(int x, int z, float radius) {
        return (x + .5) * (x + .5) + (z + .5) * (z + .5) <= radius * radius;
    }

    public static Vec3d ringPoint(double radius, int corner) {
        double angle = corner * Math.PI / 3;
        return new Vec3d(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
    }

    @SubscribeEvent
    public static void collision(GetCollisionBoxesEvent e) {
        if (e.getEntity() != null && e.getEntity().noClip) return;
        AxisAlignedBB query = e.getAabb();
        for (EntityExcavatorPayload p :
                e.getWorld()
                        .getEntitiesWithinAABB(
                                EntityExcavatorPayload.class,
                                query.grow(16),
                                p -> p.kind == CHUNK && !p.isDead)) {
            int radius = (int) p.size;
            int minX = Math.max(-radius, MathHelper.floor(query.minX - p.posX));
            int maxX = Math.min(radius - 1, MathHelper.floor(query.maxX - p.posX));
            int minZ = -radius, maxZ = radius - 1;
            if (p.tilt() == 0) {
                if (query.maxY <= p.posY || query.minY >= p.posY + 1.2) continue;
                minZ = Math.max(minZ, MathHelper.floor(query.minZ - p.posZ));
                maxZ = Math.min(maxZ, MathHelper.floor(query.maxZ - p.posZ));
            }
            for (int x = minX; x <= maxX; x++)
                for (int z = minZ; z <= maxZ; z++)
                    if (p.hasTile(x, z)) {
                        AxisAlignedBB box =
                                p.tilt() == 0
                                        ? new AxisAlignedBB(
                                                p.posX + x,
                                                p.posY,
                                                p.posZ + z,
                                                p.posX + x + 1,
                                                p.posY + 1.2,
                                                p.posZ + z + 1)
                                        : p.tileBounds(x, z);
                        if (box.intersects(query)) e.getCollisionBoxesList().add(box);
                    }
        }
    }

    @Override
    public void writeSpawnData(ByteBuf b) {
        b.writeInt(kind);
        b.writeInt(owner);
        b.writeInt(warning);
        b.writeInt(life);
        b.writeFloat(size);
        b.writeDouble(end.x);
        b.writeDouble(end.y);
        b.writeDouble(end.z);
        b.writeInt(blocks.length);
        for (int state : blocks) b.writeInt(state);
    }

    @Override
    public void readSpawnData(ByteBuf b) {
        kind = b.readInt();
        owner = b.readInt();
        warning = b.readInt();
        life = b.readInt();
        size = b.readFloat();
        end = new Vec3d(b.readDouble(), b.readDouble(), b.readDouble());
        int count = b.readInt();
        if (!Float.isFinite(size) || size < 0 || size > 16 || count < 0 || count > 1024)
            throw new IllegalArgumentException("Invalid Excavator block payload");
        blocks = new int[count];
        for (int i = 0; i < count; i++) blocks[i] = b.readInt();
        setSize(kind == CHUNK ? size * 2 : .2F, kind == CHUNK ? 1.2F : .2F);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        setDead();
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {}
}
