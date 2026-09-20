package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.*;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.*;
import net.minecraft.world.storage.loot.functions.*;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class PrebossEvents {
    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {
        NBTTagCompound source = PrebossProgress.data(event.getOriginal()).copy();
        PrebossProgress.data(event.getEntityPlayer());
        event.getEntityPlayer()
                .getEntityData()
                .getCompoundTag(EntityPlayer.PERSISTED_NBT_TAG)
                .setTag(PrebossProgress.KEY, source);
        if (event.isWasDeath())
            source.setInteger("Mana", PrebossProgress.maximum(event.getEntityPlayer()));
        PrebossProgress.applyHealth(event.getEntityPlayer());
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer
                && !event.getEntityLiving().world.isRemote) {
            event.getEntityLiving().getEntityData().setInteger("ExoArsenalRegenWait", 100);
            event.getEntityLiving().getEntityData().setInteger("ExoArsenalRogueFocus", 0);
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) return;
        NBTTagCompound data = PrebossProgress.data(player);
        int maximum = PrebossProgress.maximum(player),
                mana = MathHelper.clamp(data.getInteger("Mana"), 0, maximum),
                delay = data.getInteger("RegenDelay");
        if (delay > 0) data.setInteger("RegenDelay", delay - 1);
        else if (player.ticksExisted % 5 == 0) mana = Math.min(maximum, mana + 2);
        data.setInteger("Mana", mana);
        if (data.getInteger("ManaSickness") > 0)
            data.setInteger("ManaSickness", data.getInteger("ManaSickness") - 1);
        if (player.ticksExisted % 5 == 0
                && (player.getEntityData().getInteger("LastManaSent") != mana
                        || player.getEntityData().getInteger("LastMaxManaSent") != maximum
                        || player.getEntityData().getInteger("LastFocusSent")
                                != player.getEntityData().getInteger("ExoArsenalRogueFocus")
                        || player.ticksExisted < 30)) {
            PrebossProgress.sync(player);
            player.getEntityData().setInteger("LastManaSent", mana);
            player.getEntityData().setInteger("LastMaxManaSent", maximum);
            player.getEntityData()
                    .setInteger(
                            "LastFocusSent",
                            player.getEntityData().getInteger("ExoArsenalRogueFocus"));
        }
        if (player.ticksExisted % 20 == 0) PrebossProgress.applyHealth(player);
        if (player.ticksExisted % 40 == 0) {
            java.util.List<EntityExpeditionMinion> pets =
                    player.world.getEntitiesWithinAABB(
                            EntityExpeditionMinion.class,
                            player.getEntityBoundingBox().grow(64),
                            m -> m.variant() != 3 && player.getUniqueID().equals(m.owner()));
            pets.sort(java.util.Comparator.comparingInt(net.minecraft.entity.Entity::getEntityId));
            for (int i = PrebossProgress.minionLimit(player); i < pets.size(); i++)
                pets.get(i).setDead();
        }
        int wait = player.getEntityData().getInteger("ExoArsenalRegenWait");
        if (wait > 0) player.getEntityData().setInteger("ExoArsenalRegenWait", wait - 1);
        else if (AccessoryInventory.has(player, PrebossContent.REGEN_BAND)
                && player.ticksExisted % 40 == 0) player.heal(.5F);
        boolean holding = player.getHeldItemMainhand().getItem() == PrebossContent.CRYSTALLINE;
        int focus = player.getEntityData().getInteger("ExoArsenalRogueFocus");
        player.getEntityData()
                .setInteger(
                        "ExoArsenalRogueFocus",
                        holding
                                        && player.onGround
                                        && player.motionX * player.motionX
                                                        + player.motionZ * player.motionZ
                                                < .0025
                                ? Math.min(60, focus + 1)
                                : 0);
        if (player.world.provider.getDimension() == 0
                && !player.world.isDaytime()
                && player.ticksExisted % 100 == 0
                && player.getRNG().nextInt(4) == 0) {
            BlockPos at =
                    player.getPosition()
                            .add(
                                    player.getRNG().nextInt(49) - 24,
                                    0,
                                    player.getRNG().nextInt(49) - 24);
            if (player.world.isBlockLoaded(at) && player.world.canSeeSky(at.up(2))) {
                int height =
                        Math.min(
                                245,
                                Math.max(player.world.getHeight(at).getY() + 25, at.getY() + 25));
                EntityItem star =
                        new EntityItem(
                                player.world,
                                at.getX() + .5,
                                height,
                                at.getZ() + .5,
                                new ItemStack(PrebossContent.FALLEN_STAR));
                star.motionY = -.25;
                star.setDefaultPickupDelay();
                player.world.spawnEntity(star);
            }
        }
    }

    @SubscribeEvent(priority = net.minecraftforge.fml.common.eventhandler.EventPriority.LOWEST)
    public static void herbs(BlockEvent.HarvestDropsEvent event) {
        if (event.getWorld().isRemote
                || event.getHarvester() == null
                || event.getHarvester().isCreative()) return;
        if (event.getState().getBlock() == net.minecraft.init.Blocks.TALLGRASS
                && com.exoarsenal.world.FrigidSpawnBiomes.isCold(
                        event.getWorld().getBiome(event.getPos()))
                && event.getWorld().rand.nextInt(5) == 0)
            event.getDrops().add(new ItemStack(PrebossContent.SHIVERTHORN));
    }

    @SubscribeEvent
    public static void loot(LootTableLoadEvent event) {
        if (!event.getName().getResourceDomain().equals("minecraft")) return;
        String path = event.getName().getResourcePath();
        boolean underground =
                path.equals("chests/abandoned_mineshaft") || path.equals("chests/simple_dungeon");
        boolean ruins =
                path.equals("chests/desert_pyramid")
                        || path.equals("chests/jungle_temple")
                        || path.equals("chests/igloo_chest")
                        || path.equals("chests/village_blacksmith");
        if (!underground && !ruins) return;
        LootEntry[] entries = {
            entry(PrebossContent.SPARKING, 5, "sparking"),
            entry(PrebossContent.WOOD_BOOMERANG, 5, "boomerang"),
            entry(PrebossContent.REGEN_BAND, 3, "regeneration"),
            entry(PrebossContent.STARPOWER_BAND, 3, "starpower"),
            entry(PrebossContent.LIFE_CRYSTAL, underground ? 7 : 2, "life"),
            entry(PrebossContent.MANA_POTION, 4, "mana")
        };
        event.getTable()
                .addPool(
                        new LootPool(
                                entries,
                                new LootCondition[] {new RandomChance(.65F)},
                                new RandomValueRange(1),
                                new RandomValueRange(0),
                                "exoarsenal_preboss_supplies"));
    }

    private static LootEntry entry(net.minecraft.item.Item item, int weight, String name) {
        return new LootEntryItem(
                item, weight, 0, new LootFunction[0], new LootCondition[0], "exoarsenal_" + name);
    }
}
