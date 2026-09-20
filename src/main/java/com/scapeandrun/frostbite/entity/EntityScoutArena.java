package com.scapeandrun.frostbite.entity;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.nbt.*;
import net.minecraft.network.datasync.*;
import net.minecraft.util.math.*;
import net.minecraft.world.*;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class EntityScoutArena extends Entity {
    private static final DataParameter<Integer> MODE =
            EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> START =
            EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT);
    private static final DataParameter<Integer>
            MOTION = EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT),
            MOTION_START =
                    EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT),
            ISLAND = EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> SPLIT =
            EntityDataManager.createKey(EntityScoutArena.class, DataSerializers.VARINT);
    private UUID owner;
    private int missing;
    private final Map<UUID, Vec3d> returns = new HashMap<>();
    private static final Map<World, ArenaIndex> INDICES = new WeakHashMap<>();

    private static final class ArenaIndex {
        long tick = Long.MIN_VALUE;
        final List<java.lang.ref.WeakReference<EntityScoutArena>> entries = new ArrayList<>();
    }

    private static synchronized List<EntityScoutArena> nearby(World world, AxisAlignedBB box) {
        ArenaIndex index = INDICES.computeIfAbsent(world, w -> new ArenaIndex());
        if (index.tick != world.getTotalWorldTime()) {
            index.tick = world.getTotalWorldTime();
            index.entries.clear();
            for (Entity entity : world.loadedEntityList)
                if (entity instanceof EntityScoutArena)
                    index.entries.add(new java.lang.ref.WeakReference<>((EntityScoutArena) entity));
        }
        List<EntityScoutArena> result = new ArrayList<>();
        for (java.lang.ref.WeakReference<EntityScoutArena> reference : index.entries) {
            EntityScoutArena arena = reference.get();
            if (arena != null
                    && !arena.isDead
                    && arena.getEntityBoundingBox().grow(1, 4, 1).intersects(box))
                result.add(arena);
        }
        return result;
    }

    @SubscribeEvent
    public static synchronized void joined(
            net.minecraftforge.event.entity.EntityJoinWorldEvent event) {
        if (event.getEntity() instanceof EntityScoutArena) INDICES.remove(event.getWorld());
    }

    @SubscribeEvent
    public static synchronized void unloaded(
            net.minecraftforge.event.world.WorldEvent.Unload event) {
        INDICES.remove(event.getWorld());
    }

    public EntityScoutArena(World w) {
        super(w);
        setSize(72, 48);
        noClip = true;
        setNoGravity(true);
        ignoreFrustumCheck = true;
    }

    public EntityScoutArena(EntityX20Scout boss, Vec3d center) {
        this(boss.world);
        owner = boss.getUniqueID();
        setPosition(center.x, center.y, center.z);
    }

    @Override
    protected void entityInit() {
        dataManager.register(MODE, 0);
        dataManager.register(START, 0);
        dataManager.register(MOTION, 0);
        dataManager.register(MOTION_START, 0);
        dataManager.register(ISLAND, 4);
        dataManager.register(SPLIT, 0);
    }

    public boolean split(int island) {
        return (dataManager.get(SPLIT) & (1 << island)) != 0;
    }

    public void splitIsland(int island) {
        if (!world.isRemote && island >= 0 && island < 9)
            dataManager.set(SPLIT, dataManager.get(SPLIT) | (1 << island));
    }

    public boolean raised() {
        return mode() == 1 || mode() == 3;
    }

    private double expansion(float partial) {
        return mode() == 1 ? ScoutArenaLayout.expansion(age(partial)) : 0;
    }

    public double half(float partial) {
        return ScoutArenaLayout.HALF + (36 - ScoutArenaLayout.HALF) * expansion(partial);
    }

    public void disturb(int style, int island) {
        if (world.isRemote || !raised()) return;
        dataManager.set(MOTION, style);
        dataManager.set(ISLAND, Math.max(0, Math.min(8, island)));
        dataManager.set(MOTION_START, (int) world.getTotalWorldTime());
    }

    public Vec3d islandPosition(int island, float partial) {
        double t = (int) world.getTotalWorldTime() - dataManager.get(MOTION_START) + partial;
        double[] d =
                ScoutIslandMotion.offset(
                        dataManager.get(MOTION), island, dataManager.get(ISLAND), t);
        double height = ScoutArenaLayout.height(island, age(partial));
        if (mode() == 3) height *= .4;
        double spacing = 1 + 1.5 * expansion(partial),
                limit = Math.max(0, half(partial) - ScoutArenaLayout.DECK_MARGIN);
        return new Vec3d(
                posX
                        + Math.max(
                                -limit,
                                Math.min(limit, ScoutArenaLayout.x(island) * spacing + d[0])),
                posY + height + d[1],
                posZ
                        + Math.max(
                                -limit,
                                Math.min(limit, ScoutArenaLayout.z(island) * spacing + d[2])));
    }

    public double islandYaw(int island, float partial) {
        return ScoutIslandMotion.yaw(
                dataManager.get(MOTION),
                island,
                dataManager.get(ISLAND),
                (int) world.getTotalWorldTime() - dataManager.get(MOTION_START) + partial);
    }

    public Vec3d islandPoint(int island, double x, double y, double z, float partial) {
        double a = islandYaw(island, partial), c = Math.cos(a), s = Math.sin(a);
        return islandPosition(island, partial).addVector(x * c - z * s, y, x * s + z * c);
    }

    public int nearestIsland(double x, double z) {
        int best = 4;
        double distance = Double.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            Vec3d p = islandPosition(i, 0);
            double d = (p.x - x) * (p.x - x) + (p.z - z) * (p.z - z);
            if (d < distance) {
                distance = d;
                best = i;
            }
        }
        return best;
    }

    private int supportingIsland(EntityLivingBase entity) {
        int best = nearestIsland(entity.posX, entity.posZ);
        double error = Double.MAX_VALUE;
        for (int i = 0; i < 9; i++) {
            Vec3d p = islandPosition(i, -1);
            double heightError = Math.abs(entity.posY - p.y);
            if (heightError > .65 || heightError >= error) continue;
            double a = islandYaw(i, -1), dx = entity.posX - p.x, dz = entity.posZ - p.z;
            double x = dx * Math.cos(a) + dz * Math.sin(a),
                    z = -dx * Math.sin(a) + dz * Math.cos(a);
            if (split(i) && Math.abs(x) < .9) continue;
            if (split(i)) x -= Math.signum(x) * .4;
            if (ScoutArenaLayout.tile((int) Math.floor(x + .5), (int) Math.floor(z + .5))) {
                best = i;
                error = heightError;
            }
        }
        return best;
    }

    public int mode() {
        return dataManager.get(MODE);
    }

    public double age(float partial) {
        return Math.max(0, (int) world.getTotalWorldTime() - dataManager.get(START) + partial);
    }

    public boolean belongs(EntityX20Scout boss) {
        return owner != null && owner.equals(boss.getUniqueID());
    }

    public void dormant() {
        dataManager.set(MODE, 2);
    }

    public void raiseKatana() {
        if (raised()) return;
        raise();
        dataManager.set(MODE, 3);
        if (world.isRemote) return;

        for (EntityPlayer p : world.playerEntities) {
            Vec3d saved = returns.get(p.getUniqueID());
            if (saved == null) continue;
            Vec3d deck = islandPosition(nearestIsland(saved.x, saved.z), 0);
            move(p, deck.x, deck.y + .2, deck.z);
            p.fallDistance = 0;
        }
        Entity boss = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
        if (boss != null) {
            Vec3d deck = islandPosition(nearestIsland(boss.posX, boss.posZ), 0);
            move(boss, deck.x, deck.y + .2, deck.z);
            boss.fallDistance = 0;
        }
    }

    public void raise() {
        if (mode() == 1) return;
        if (mode() == 3) {
            int equivalent =
                    (int)
                            Math.round(
                                    ScoutArenaLayout.riseAgeForHeight(
                                            ScoutArenaLayout.rise(age(0)) * .4));
            dataManager.set(MODE, 1);
            dataManager.set(START, (int) world.getTotalWorldTime() - equivalent);
            return;
        }
        dataManager.set(MODE, 1);
        dataManager.set(START, (int) world.getTotalWorldTime());
        for (EntityPlayer p :
                world.getEntitiesWithinAABB(
                        EntityPlayer.class,
                        new AxisAlignedBB(
                                posX - 12.5,
                                posY - 4,
                                posZ - 12.5,
                                posX + 12.5,
                                posY + 48,
                                posZ + 12.5))) {
            if (p.isSpectator()) continue;
            returns.put(p.getUniqueID(), p.getPositionVector());
            NBTTagCompound journal = new NBTTagCompound();
            journal.setUniqueId("Arena", getUniqueID());
            journal.setInteger("Dimension", dimension);
            journal.setDouble("X", p.posX);
            journal.setDouble("Y", p.posY);
            journal.setDouble("Z", p.posZ);
            p.getEntityData().setTag("FrostbiteArenaReturn", journal);
            move(p, posX, posY + .2, posZ);
            p.fallDistance = 0;
        }
    }

    private static void move(Entity e, double x, double y, double z) {
        if (e instanceof EntityPlayerMP)
            ((EntityPlayerMP) e)
                    .connection.setPlayerLocation(x, y, z, e.rotationYaw, e.rotationPitch);
        else e.setPosition(x, y, z);
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        motionX = motionY = motionZ = 0;
        if (!world.isRemote) {
            Entity e = owner == null ? null : ((WorldServer) world).getEntityFromUuid(owner);
            if (!(e instanceof EntityX20Scout) || e.isDead) {
                if (++missing > 100) {
                    finish();
                    return;
                }
            } else missing = 0;
        }
        if (!raised()) return;
        double now = age(0), before = age(0) - 1;
        for (EntityLivingBase e :
                world.getEntitiesWithinAABB(
                        EntityLivingBase.class, getEntityBoundingBox().grow(0, 4, 0))) {
            if (e instanceof EntityPlayer && ((EntityPlayer) e).isSpectator()) continue;
            if (!world.isRemote
                    && e instanceof EntityPlayer
                    && !returns.containsKey(e.getUniqueID())) continue;
            int island = supportingIsland(e);
            Vec3d current = islandPosition(island, 0), previous = islandPosition(island, -1);
            double top = current.y, old = previous.y;
            double angle = islandYaw(island, -1), cos = Math.cos(angle), sin = Math.sin(angle);
            double dx = e.posX - previous.x, dz = e.posZ - previous.z;
            double localX = dx * cos + dz * sin, localZ = -dx * sin + dz * cos;
            boolean overGap = split(island) && Math.abs(localX) < .9;
            if (split(island)) localX -= Math.signum(localX) * .4;
            boolean overDeck =
                    !overGap
                            && ScoutArenaLayout.tile(
                                    (int) Math.floor(localX + .5), (int) Math.floor(localZ + .5));

            if (e instanceof EntityX20Scout
                    && !e.hasNoGravity()
                    && overDeck
                    && e.posY < top
                    && e.posY > top - 2.5) {
                move(e, e.posX, top + .02, e.posZ);
                e.motionY = 0;
                e.onGround = true;
                e.fallDistance = 0;
                continue;
            }
            if (overDeck && e.posY >= old - .35 && e.posY <= old + .6 && e.motionY <= .1) {
                double turn = islandYaw(island, 0) - angle;
                Vec3d delta =
                        current.subtract(previous)
                                .addVector(
                                        dx * Math.cos(turn) - dz * Math.sin(turn) - dx,
                                        0,
                                        dx * Math.sin(turn) + dz * Math.cos(turn) - dz);
                e.move(net.minecraft.entity.MoverType.SELF, delta.x, delta.y, delta.z);
                e.onGround = true;
                e.fallDistance = 0;
                e.motionY = 0;
                if (!world.isRemote && e instanceof EntityPlayerMP && ticksExisted % 4 == 0)
                    move(e, e.posX, e.posY, e.posZ);
                if (!world.isRemote
                        && dataManager.get(MOTION) == 3
                        && island == dataManager.get(ISLAND)
                        && (int) world.getTotalWorldTime() - dataManager.get(MOTION_START) == 85) {
                    e.addVelocity(0, .8, 0);
                    e.velocityChanged = true;
                }
            }

            if (!world.isRemote
                    && now > 120
                    && e.posY < posY + (mode() == 3 ? -2 : 3)
                    && (e instanceof EntityPlayer || e instanceof EntityX20Scout)) {
                move(e, current.x, top + .3, current.z);
                e.fallDistance = 0;
                e.attackEntityFrom(net.minecraft.util.DamageSource.FALL, 6);
            }
        }
    }

    public double topAt(double x, double z) {
        return islandPosition(nearestIsland(x, z), 0).y;
    }

    public void finish() {
        if (!world.isRemote && raised())
            for (EntityPlayer p : world.playerEntities) {
                Vec3d saved = returns.get(p.getUniqueID());
                if (saved == null || p.getDistanceSq(this) > 10000) continue;
                BlockPos ground = world.getTopSolidOrLiquidBlock(new BlockPos(saved));
                move(p, ground.getX() + .5, ground.getY() + 1, ground.getZ() + .5);
                p.fallDistance = 0;
                p.getEntityData().removeTag("FrostbiteArenaReturn");
            }
        setDead();
    }

    @Override
    public AxisAlignedBB getCollisionBoundingBox() {
        return null;
    }

    @SubscribeEvent
    public static void rejoin(
            net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
        EntityPlayer p = event.player;
        if (p.world.isRemote || !p.getEntityData().hasKey("FrostbiteArenaReturn")) return;
        NBTTagCompound n = p.getEntityData().getCompoundTag("FrostbiteArenaReturn");
        if (p.dimension == n.getInteger("Dimension")) {
            Entity arena = ((WorldServer) p.world).getEntityFromUuid(n.getUniqueId("Arena"));
            if (arena instanceof EntityScoutArena && !arena.isDead) return;
            BlockPos ground =
                    p.world.getTopSolidOrLiquidBlock(
                            new BlockPos(n.getDouble("X"), n.getDouble("Y"), n.getDouble("Z")));
            move(p, ground.getX() + .5, ground.getY() + 1, ground.getZ() + .5);
            p.fallDistance = 0;
        }
        p.getEntityData().removeTag("FrostbiteArenaReturn");
    }

    @SubscribeEvent
    public static void teleport(net.minecraftforge.event.entity.living.EnderTeleportEvent event) {
        EntityLivingBase e = event.getEntityLiving();
        if (e instanceof EntityX20Scout
                || e instanceof EntityPlayer && ((EntityPlayer) e).isSpectator()) return;
        for (EntityScoutArena a : nearby(e.world, e.getEntityBoundingBox())) {
            if (a.mode() == 2) continue;
            if (Math.abs(e.posX - a.posX) > a.half(0) || Math.abs(e.posZ - a.posZ) > a.half(0))
                continue;
            if (Math.abs(event.getTargetX() - a.posX) > a.half(0) - e.width / 2
                    || Math.abs(event.getTargetZ() - a.posZ) > a.half(0) - e.width / 2
                    || event.getTargetY() < a.posY - 4
                    || event.getTargetY() + e.height > a.posY + ScoutArenaLayout.HEIGHT) {
                event.setCanceled(true);
                return;
            }
        }
    }

    @SubscribeEvent
    public static void collision(GetCollisionBoxesEvent event) {
        Entity e = event.getEntity();
        if (!(e instanceof EntityLivingBase)
                || e.noClip
                || e instanceof EntityPlayer && ((EntityPlayer) e).isSpectator()) return;
        AxisAlignedBB query = event.getAabb();
        for (EntityScoutArena arena : nearby(event.getWorld(), query.grow(1))) {
            if (arena.mode() == 2) continue;
            if (e instanceof EntityX20Scout
                    && (((EntityX20Scout) e).getScene() == 8
                            || ((EntityX20Scout) e).getScene() == 9
                            || e.hasNoGravity())) continue;
            double x = arena.posX,
                    z = arena.posZ,
                    y = arena.posY - 4,
                    h = arena.posY + ScoutArenaLayout.HEIGHT,
                    b = arena.half(0);
            add(event, new AxisAlignedBB(x - b - .5, y, z - b - .5, x - b, h, z + b + .5));
            add(event, new AxisAlignedBB(x + b, y, z - b - .5, x + b + .5, h, z + b + .5));
            add(event, new AxisAlignedBB(x - b, y, z - b - .5, x + b, h, z - b));
            add(event, new AxisAlignedBB(x - b, y, z + b, x + b, h, z + b + .5));
            add(event, new AxisAlignedBB(x - b, h, z - b, x + b, h + .5, z + b));
            add(event, new AxisAlignedBB(x - b, y - .5, z - b, x + b, y, z + b));
            if (arena.raised())
                for (int i = 0; i < 9; i++) {
                    Vec3d center = arena.islandPosition(i, 0);
                    if (query.maxY < center.y - 2
                            || query.minY > center.y
                            || query.maxX < center.x - 11
                            || query.minX > center.x + 11
                            || query.maxZ < center.z - 11
                            || query.minZ > center.z + 11) continue;
                    double yaw = arena.islandYaw(i, 0), c = Math.cos(yaw), s = Math.sin(yaw);
                    double dx = (query.minX + query.maxX) * .5 - center.x,
                            dz = (query.minZ + query.maxZ) * .5 - center.z;
                    double localX = dx * c + dz * s, localZ = -dx * s + dz * c;
                    double extentX =
                            ((query.maxX - query.minX) * Math.abs(c)
                                                    + (query.maxZ - query.minZ) * Math.abs(s))
                                            * .5
                                    + 1.5;
                    double extentZ =
                            ((query.maxX - query.minX) * Math.abs(s)
                                                    + (query.maxZ - query.minZ) * Math.abs(c))
                                            * .5
                                    + 1.5;
                    int
                            minCol =
                                    Math.max(
                                            -ScoutArenaLayout.RADIUS,
                                            (int) Math.floor(localX - extentX)),
                            maxCol =
                                    Math.min(
                                            ScoutArenaLayout.RADIUS,
                                            (int) Math.ceil(localX + extentX));
                    int
                            minRow =
                                    Math.max(
                                            -ScoutArenaLayout.RADIUS,
                                            (int) Math.floor(localZ - extentZ)),
                            maxRow =
                                    Math.min(
                                            ScoutArenaLayout.RADIUS,
                                            (int) Math.ceil(localZ + extentZ));
                    for (int row = minRow; row <= maxRow; row++)
                        for (int col = minCol; col <= maxCol; col++) {
                            if (!ScoutArenaLayout.tile(col, row) || arena.split(i) && col == 0)
                                continue;
                            Vec3d p =
                                    arena.islandPoint(
                                            i,
                                            col + (arena.split(i) ? Math.signum(col) * .4 : 0),
                                            0,
                                            row,
                                            0);

                            if (e instanceof EntityX20Scout
                                    && !ScoutArenaLayout.bossLanding(
                                            e.getEntityBoundingBox().minY,
                                            p.y,
                                            e.motionY,
                                            e.hasNoGravity())) continue;
                            double a = arena.islandYaw(i, 0),
                                    half = (Math.abs(Math.cos(a)) + Math.abs(Math.sin(a))) * .5;

                            add(
                                    event,
                                    new AxisAlignedBB(
                                            p.x - half,
                                            p.y - 2,
                                            p.z - half,
                                            p.x + half,
                                            p.y,
                                            p.z + half));
                        }
                }
        }
    }

    private static void add(GetCollisionBoxesEvent e, AxisAlignedBB b) {
        if (b.intersects(e.getAabb())) e.getCollisionBoxesList().add(b);
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound n) {
        if (owner != null) n.setUniqueId("Owner", owner);
        n.setInteger("Mode", mode());
        n.setLong("Start", dataManager.get(START));
        n.setInteger("Motion", dataManager.get(MOTION));
        n.setInteger("MotionStart", dataManager.get(MOTION_START));
        n.setInteger("Island", dataManager.get(ISLAND));
        n.setInteger("Split", dataManager.get(SPLIT));
        NBTTagList list = new NBTTagList();
        for (Map.Entry<UUID, Vec3d> e : returns.entrySet()) {
            NBTTagCompound t = new NBTTagCompound();
            t.setUniqueId("Player", e.getKey());
            t.setDouble("X", e.getValue().x);
            t.setDouble("Y", e.getValue().y);
            t.setDouble("Z", e.getValue().z);
            list.appendTag(t);
        }
        n.setTag("Returns", list);
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound n) {
        owner = n.hasUniqueId("Owner") ? n.getUniqueId("Owner") : null;
        dataManager.set(MODE, n.getInteger("Mode"));
        dataManager.set(START, (int) n.getLong("Start"));
        dataManager.set(MOTION, Math.max(0, Math.min(5, n.getInteger("Motion"))));
        dataManager.set(MOTION_START, n.getInteger("MotionStart"));
        dataManager.set(ISLAND, Math.max(0, Math.min(8, n.getInteger("Island"))));
        dataManager.set(SPLIT, n.getInteger("Split") & 511);
        returns.clear();
        NBTTagList list = n.getTagList("Returns", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound t = list.getCompoundTagAt(i);
            returns.put(
                    t.getUniqueId("Player"),
                    new Vec3d(t.getDouble("X"), t.getDouble("Y"), t.getDouble("Z")));
        }
    }
}
