package com.exoarsenal.entity;

import com.exoarsenal.expedition.ExpeditionContent;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.*;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.*;
import java.util.*;

@net.minecraftforge.fml.common.Mod.EventBusSubscriber(modid = com.exoarsenal.ExoArsenal.MODID)
public final class EntityDraedon extends EntityLiving {
    final DraedonCollaboration collaboration = new DraedonCollaboration();
    private BlockPos terminal = BlockPos.ORIGIN;
    private UUID owner;
    private int stage, selected, clock, absent, missing;
    private final List<UUID> actors = new ArrayList<>();
    private final Set<UUID> defeated = new HashSet<>();
    private NBTTagList reserve = new NBTTagList();
    private float openingMaximum;
    private int dialogue;
    private final java.util.ArrayDeque<String> speech = new java.util.ArrayDeque<>();
    private int speechDelay, pendingOptions = -1;
    private NBTTagCompound arrivals = new NBTTagCompound();
    private static final net.minecraft.network.datasync.DataParameter<NBTTagCompound> ARRIVALS =
            net.minecraft.network.datasync.EntityDataManager.createKey(
                    EntityDraedon.class,
                    net.minecraft.network.datasync.DataSerializers.COMPOUND_TAG);
    private static final net.minecraft.network.datasync.DataParameter<Integer> TITLE =
            net.minecraft.network.datasync.EntityDataManager.createKey(
                    EntityDraedon.class, net.minecraft.network.datasync.DataSerializers.VARINT);
    private static final net.minecraft.network.datasync.DataParameter<Integer> CONTACT =
            net.minecraft.network.datasync.EntityDataManager.createKey(
                    EntityDraedon.class, net.minecraft.network.datasync.DataSerializers.VARINT);
    private static final net.minecraft.network.datasync.DataParameter<BlockPos> TERMINAL =
            net.minecraft.network.datasync.EntityDataManager.createKey(
                    EntityDraedon.class, net.minecraft.network.datasync.DataSerializers.BLOCK_POS);

    @Override
    protected void entityInit() {
        super.entityInit();
        dataManager.register(CONTACT, 0);
        dataManager.register(TERMINAL, BlockPos.ORIGIN);
        dataManager.register(ARRIVALS, new NBTTagCompound());
        dataManager.register(TITLE, 0);
    }

    public NBTTagCompound arrivals() {
        return dataManager.get(ARRIVALS);
    }

    public int titleAge() {
        return dataManager.get(TITLE);
    }

    public static boolean trialActor(Entity e) {
        for (Entity x : e.world.loadedEntityList)
            if (x instanceof EntityDraedon
                    && ((EntityDraedon) x).arrivals().hasKey(e.getUniqueID().toString()))
                return true;
        return false;
    }

    public static boolean arriving(EntityLivingBase e) {
        if (e.world.isRemote || !e.getEntityData().hasUniqueId("DraedonTrial")) return false;
        Entity owner =
                ((WorldServer) e.world)
                        .getEntityFromUuid(e.getEntityData().getUniqueId("DraedonTrial"));
        if (!(owner instanceof EntityDraedon)) return false;
        NBTTagCompound a =
                ((EntityDraedon) owner).arrivals.getCompoundTag(e.getUniqueID().toString());
        if (a.hasKey("Age") && a.getInteger("Age") < WulfrumArrival.END) {
            e.motionX = e.motionY = e.motionZ = 0;
            e.fallDistance = 0;
            return true;
        }
        return false;
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public static void arrivalDamage(
            net.minecraftforge.event.entity.living.LivingAttackEvent event) {
        if (arriving(event.getEntityLiving())) event.setCanceled(true);
    }

    private void arrive(EntityLivingBase e, Vec3d at) {
        NBTTagCompound a = new NBTTagCompound();
        a.setInteger("Age", 0);
        a.setDouble("X", at.x);
        a.setDouble("Y", at.y);
        a.setDouble("Z", at.z);
        arrivals.setTag(e.getUniqueID().toString(), a);
        e.setPosition(at.x, at.y + 48, at.z);
        e.prevPosX = e.lastTickPosX = e.posX;
        e.prevPosY = e.lastTickPosY = e.posY;
        e.prevPosZ = e.lastTickPosZ = e.posZ;
        dataManager.set(ARRIVALS, arrivals.copy());
        getEntityData().setTag("TrialArrivals", arrivals.copy());
    }

    private void tickArrivals() {
        boolean changed = false;
        for (String key : arrivals.getKeySet()) {
            NBTTagCompound a = arrivals.getCompoundTag(key);
            int age = a.getInteger("Age");
            if (age >= WulfrumArrival.END) continue;
            a.setInteger("Age", ++age);
            changed = true;
            EntityLivingBase e = actor(UUID.fromString(key));
            if (e != null) {
                e.setPosition(
                        a.getDouble("X"),
                        a.getDouble("Y") + WulfrumArrival.height(age),
                        a.getDouble("Z"));
                e.motionX = e.motionY = e.motionZ = 0;
                e.velocityChanged = true;
            }
        }
        if (changed) {
            dataManager.set(ARRIVALS, arrivals.copy());
            getEntityData().setTag("TrialArrivals", arrivals.copy());
        }
        int t = titleAge();
        if (t > 0 && t < 180) dataManager.set(TITLE, t + 1);
        getEntityData().setInteger("TrialTitleAge", titleAge());
    }

    public int contactAge() {
        return dataManager.get(CONTACT);
    }

    public BlockPos terminal() {
        return dataManager.get(TERMINAL);
    }

    public EntityDraedon(World world) {
        super(world);
        setSize(1.4F, 2.4F);
        setNoGravity(true);
        setNoAI(true);
        isImmuneToFire = true;
        ignoreFrustumCheck = true;
        enablePersistence();
    }

    @Override
    public boolean attackEntityFrom(DamageSource source, float amount) {
        return false;
    }

    @Override
    protected boolean canDespawn() {
        return false;
    }

    public int encounterStage() {
        return stage;
    }

    @net.minecraftforge.fml.common.eventhandler.SubscribeEvent
    public static void death(net.minecraftforge.event.entity.living.LivingDeathEvent event) {
        EntityLivingBase victim = event.getEntityLiving();
        if (victim.world.isRemote || !victim.getEntityData().hasUniqueId("DraedonTrial")) return;
        Entity owner =
                ((WorldServer) victim.world)
                        .getEntityFromUuid(victim.getEntityData().getUniqueId("DraedonTrial"));
        if (owner instanceof EntityDraedon)
            ((EntityDraedon) owner).defeated.add(victim.getUniqueID());
    }

    private static EntityDraedon at(World w, BlockPos p) {
        for (EntityDraedon d :
                w.getEntitiesWithinAABB(EntityDraedon.class, new AxisAlignedBB(p).grow(160)))
            if (d.terminal.equals(p)) return d;
        return null;
    }

    public static void contact(World w, BlockPos p, EntityPlayer player) {
        if (w.isRemote) return;
        EntityDraedon existing = at(w, p);
        if (existing != null) {
            if (existing.stage == 0
                    && existing.clock >= 110
                    && player.getUniqueID().equals(existing.owner)) {
                if (existing.dialogue == 3) existing.choose(player);
                else existing.options(player, existing.dialogue == 2 || existing.dialogue == 4);
            }
            return;
        }
        EntityDraedon d = new EntityDraedon(w);
        d.terminal = p;
        d.dataManager.set(TERMINAL, p);
        d.owner = player.getUniqueID();
        d.setPosition(p.getX() + .5, p.getY() + 32, p.getZ() + .5);
        w.spawnEntity(d);
    }

    public static void select(World w, BlockPos p, EntityPlayer player, int choice) {
        if (w.isRemote
                || choice < 0
                || choice > 2
                || w.getBlockState(p).getBlock() != ExpeditionContent.CODEBREAKER_BASE) return;
        EntityDraedon d = at(w, p);
        if (d == null || d.stage != 0 || d.dialogue != 3 || !player.getUniqueID().equals(d.owner))
            return;
        for (EntityDraedon other :
                w.getEntitiesWithinAABB(EntityDraedon.class, new AxisAlignedBB(p).grow(192)))
            if (other != d && other.stage > 0) {
                player.sendMessage(
                        new TextComponentString("Another trial is already active nearby."));
                return;
            }
        d.speech.clear();
        d.pendingOptions = -1;
        d.getEntityData().setBoolean("FirstLossSpoken", false);
        d.arrivals = new NBTTagCompound();
        d.dataManager.set(TITLE, 1);
        d.owner = player.getUniqueID();
        d.selected = choice;
        d.stage = 1;
        d.clock = 0;
        d.spawnMachine(choice, player.getPositionVector().addVector(18, 0, 0));
    }

    private void spawnMachine(int kind, Vec3d at) {
        EntityLiving e =
                kind == 0
                        ? new EntityWulfrumEye.Observer(world)
                        : kind == 1 ? new EntityExcavator(world) : new EntityBrawler(world);
        e.setPosition(at.x, at.y + 3, at.z);
        e.enablePersistence();
        e.getEntityData().setUniqueId("DraedonTrial", getUniqueID());
        if (e instanceof EntityExcavator) ((EntityExcavator) e).prepareTrialEntrance();
        if (e instanceof EntityBrawler) ((EntityBrawler) e).prepareTrialEntrance();
        Vec3d landing = e.getPositionVector();
        arrive(e, landing);
        if (world.spawnEntity(e)) {
            actors.add(e.getUniqueID());
            if (e instanceof EntityWulfrumEye) {
                EntityWulfrumEye other = ((EntityWulfrumEye) e).prepareTrialPair(landing);
                other.getEntityData().setUniqueId("DraedonTrial", getUniqueID());
                arrive(other, landing.addVector(-8, 0, 0));
                actors.add(other.getUniqueID());
            }
        }
    }

    private EntityLivingBase actor(UUID id) {
        Entity e = ((WorldServer) world).getEntityFromUuid(id);
        return e instanceof EntityLivingBase ? (EntityLivingBase) e : null;
    }

    private void collectPartner() {
        List<UUID> add = new ArrayList<>();
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e instanceof EntityWulfrumEye) {
                EntityWulfrumEye p = ((EntityWulfrumEye) e).partner();
                if (p != null && !actors.contains(p.getUniqueID())) {
                    p.getEntityData().setUniqueId("DraedonTrial", getUniqueID());
                    add.add(p.getUniqueID());
                }
            }
        }
        actors.addAll(add);
    }

    private float integrity(boolean maximum) {
        float n = 0;
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e == null) continue;
            n +=
                    e instanceof EntityBrawler
                            ? (maximum ? 800 : ((EntityBrawler) e).totalHealth())
                            : maximum ? e.getMaxHealth() : e.getHealth();
        }
        return n;
    }

    private void withdraw() {
        reserve = new NBTTagList();
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e != null && e.isEntityAlive()) {
                NBTTagCompound n = new NBTTagCompound();
                if (e.writeToNBTOptional(n)) reserve.appendTag(n);
            }
        }
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e != null) e.setDead();
        }
        actors.clear();
        defeated.clear();
        missing = 0;
    }

    private void restore() {
        actors.clear();
        defeated.clear();
        for (int i = 0; i < reserve.tagCount(); i++) {
            Entity e = EntityList.createEntityFromNBT(reserve.getCompoundTagAt(i), world);
            if (e instanceof EntityLivingBase) {
                e.isDead = false;
                Vec3d at = e.getPositionVector();
                at = new Vec3d(at.x, world.getHeight(new BlockPos(at)).getY() + 3, at.z);
                arrive((EntityLivingBase) e, at);
                world.spawnEntity(e);
                actors.add(e.getUniqueID());
            }
        }
        reserve = new NBTTagList();
    }

    private void say(String s) {
        for (String line : s.split("(?<=[.!?])\\s+")) speech.add(line);
    }

    private void tickSpeech() {
        if (speechDelay > 0) {
            speechDelay--;
            return;
        }
        if (!speech.isEmpty()) {
            String line = speech.removeFirst();
            for (EntityPlayer p : world.playerEntities)
                if (p.getDistanceSq(this) < 192 * 192)
                    p.sendMessage(new TextComponentString("Draedon: " + line));
            speechDelay = Math.max(40, Math.min(100, line.length()));
        } else if (pendingOptions >= 0) {
            EntityPlayer p = world.getPlayerEntityByUUID(owner);
            boolean ready = pendingOptions == 1;
            pendingOptions = -1;
            if (p != null) options(p, ready);
        }
    }

    private void button(EntityPlayer player, String text, String action) {
        TextComponentString line = new TextComponentString("[" + text + "]");
        line.getStyle()
                .setColor(net.minecraft.util.text.TextFormatting.AQUA)
                .setBold(true)
                .setClickEvent(
                        new net.minecraft.util.text.event.ClickEvent(
                                net.minecraft.util.text.event.ClickEvent.Action.RUN_COMMAND,
                                "/draedonreply " + getUniqueID() + " " + action));
        player.sendMessage(line);
    }

    private void options(EntityPlayer player, boolean ready) {
        if (!speech.isEmpty() || speechDelay > 0) {
            pendingOptions = ready ? 1 : 0;
            return;
        }
        dialogue = ready ? 2 : 1;
        if (ready) {
            button(player, "Yes", "yes");
            button(player, "No", "no");
        } else {
            button(player, "What's your latest technology?", "technology");
            button(player, "Skip the chit chat, Let me fight your machines", "skip");
        }
    }

    private void choose(EntityPlayer player) {
        dialogue = 3;
        TextComponentString line = new TextComponentString("Draedon: Now choose.");
        line.getStyle().setColor(net.minecraft.util.text.TextFormatting.RED);
        player.sendMessage(line);
        player.openGui(
                com.exoarsenal.ExoArsenal.INSTANCE,
                1,
                world,
                terminal.getX(),
                terminal.getY(),
                terminal.getZ());
    }

    public void reply(EntityPlayer player, String action) {
        if (!speech.isEmpty() || speechDelay > 0) return;
        if (world.isRemote
                || stage != 0
                || clock < 110
                || !player.getUniqueID().equals(owner)
                || player.getDistanceSq(terminal) > 256
                || world.getBlockState(terminal).getBlock() != ExpeditionContent.CODEBREAKER_BASE)
            return;
        if (DraedonDialogue.next(dialogue, action) == dialogue) return;
        if (dialogue == 1 && action.equals("technology")) {
            say(
                    "My latest technology is something I call Wulfrum. It is essentially, one would say, a living metal of sorts. It reforms itself if damaged. Albeit slowly.");
            say("Now, you wish to face my machines, do you not?");
            options(player, true);
        } else if (dialogue == 1 && action.equals("skip")) {
            dialogue = 3;
            TextComponentString line = new TextComponentString("Draedon: Choose.");
            line.getStyle().setColor(net.minecraft.util.text.TextFormatting.RED);
            player.sendMessage(line);
            player.openGui(
                    com.exoarsenal.ExoArsenal.INSTANCE,
                    1,
                    world,
                    terminal.getX(),
                    terminal.getY(),
                    terminal.getZ());
        } else if (dialogue == 2 && action.equals("yes")) choose(player);
        else if (dialogue == 2 && action.equals("no")) {
            dialogue = 4;
            say("Then, I will observe until you are ready.");
        }
    }

    @Override
    public void onUpdate() {
        super.onUpdate();
        setNoGravity(true);
        motionX = motionY = motionZ = 0;
        if (world.isRemote) return;
        if (ticksExisted == 1 && getEntityData().hasKey("TrialArrivals")) {
            arrivals = getEntityData().getCompoundTag("TrialArrivals").copy();
            dataManager.set(ARRIVALS, arrivals.copy());
            dataManager.set(TITLE, getEntityData().getInteger("TrialTitleAge"));
        }
        tickArrivals();
        tickSpeech();
        EntityPlayer p = owner == null ? null : world.getPlayerEntityByUUID(owner);
        if (p != null) getLookHelper().setLookPositionWithEntity(p, 15, 15);
        if (stage == 0) {
            ++clock;
            dataManager.set(CONTACT, Math.min(160, clock));
            dataManager.set(TERMINAL, terminal);
            if (clock <= 100)
                setPosition(
                        terminal.getX() + .5,
                        terminal.getY() + DraedonDialogue.height(clock),
                        terminal.getZ() + .5);
            if (clock == 110 && p != null) {
                say(
                        "Hmm. I have watched you closely. You have found my planetoid lab. I think you are worthy, to face my latest technology.");
                options(p, false);
            }
            if (world.getBlockState(terminal).getBlock() != ExpeditionContent.CODEBREAKER_BASE)
                setDead();
            return;
        }
        dataManager.set(CONTACT, 160);
        dataManager.set(TERMINAL, terminal);
        if (p == null || !p.isEntityAlive() || p.getDistanceSq(terminal) > 192 * 192) {
            if (++absent > 200) abort();
            return;
        }
        absent = 0;
        clock++;
        collectPartner();
        boolean all = true, unresolved = false;
        if (stage == 2) {
            List<EntityLivingBase> live = new ArrayList<>();
            for (UUID id : actors) {
                EntityLivingBase e = actor(id);
                if (e != null && e.isEntityAlive()) live.add(e);
            }
            collaboration.tick(this, live, p, clock);
        }
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e != null
                    && e.getHealth() <= 0
                    && defeated.add(id)
                    && !getEntityData().getBoolean("FirstLossSpoken")) {
                boolean wholeMech =
                        !(e instanceof EntityWulfrumEye)
                                || ((EntityWulfrumEye) e).partner() == null;
                if (wholeMech) {
                    getEntityData().setBoolean("FirstLossSpoken", true);
                    say(
                            "I don't truly care if my mechs are destroyed due to this experiment. If anything, that is a good thing. I can always iterate upon their weaknesses to build a better mech.");
                }
            }
            if (!defeated.contains(id)) {
                all = false;
                if (e == null) unresolved = true;
            }
        }
        if (unresolved) {
            if (++missing > 200) abort();
            return;
        }
        missing = 0;
        if (stage == 1) {
            openingMaximum = Math.max(openingMaximum, integrity(true));
            if (clock > 220 && openingMaximum > 0 && integrity(false) <= openingMaximum * .75F) {
                say(
                        "Interesting, you are quite determined. Though I wonder how you will fare against the other two.");
                withdraw();
                stage = 2;
                clock = 0;
                int side = -1;
                for (int k = 0; k < 3; k++)
                    if (k != selected) {
                        spawnMachine(k, p.getPositionVector().addVector(side * 22, 0, 8));
                        side = 1;
                    }
            }
        } else if (stage == 2 && all && !actors.isEmpty()) {
            restore();
            stage = 3;
            clock = 0;
        } else if (stage == 3 && all) {
            stage = 0;
            clock = 160;
            dialogue = 4;
            actors.clear();
            defeated.clear();
            openingMaximum = 0;
            say(
                    "It appears my machines were too weak. I must return to the drawing board, to find something worthy of challenging you.");
        }
    }

    private void abort() {
        for (UUID id : actors) {
            EntityLivingBase e = actor(id);
            if (e != null) e.setDead();
        }
        actors.clear();
        reserve = new NBTTagList();
        setDead();
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound n) {
        super.writeEntityToNBT(n);
        n.setInteger("Dialogue", dialogue);
        n.setLong("Terminal", terminal.toLong());
        if (owner != null) n.setUniqueId("Operator", owner);
        n.setInteger("TrialStage", stage);
        n.setInteger("Selection", selected);
        n.setInteger("TrialClock", clock);
        n.setFloat("OpeningMaximum", openingMaximum);
        n.setTag("Reserve", reserve);
        NBTTagList list = new NBTTagList();
        for (UUID id : actors) {
            NBTTagCompound a = new NBTTagCompound();
            a.setUniqueId("Id", id);
            a.setBoolean("Defeated", defeated.contains(id));
            list.appendTag(a);
        }
        n.setTag("Actors", list);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound n) {
        super.readEntityFromNBT(n);
        dialogue = n.getInteger("Dialogue");
        terminal = BlockPos.fromLong(n.getLong("Terminal"));
        owner = n.hasUniqueId("Operator") ? n.getUniqueId("Operator") : null;
        stage = n.getInteger("TrialStage");
        selected = n.getInteger("Selection");
        clock = n.getInteger("TrialClock");
        dataManager.set(CONTACT, stage == 0 ? Math.min(160, clock) : 160);
        dataManager.set(TERMINAL, terminal);
        openingMaximum = n.getFloat("OpeningMaximum");
        reserve = n.getTagList("Reserve", 10);
        actors.clear();
        defeated.clear();
        NBTTagList list = n.getTagList("Actors", 10);
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound a = list.getCompoundTagAt(i);
            UUID id = a.getUniqueId("Id");
            actors.add(id);
            if (a.getBoolean("Defeated")) defeated.add(id);
        }
    }
}
