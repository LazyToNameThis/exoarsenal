package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.*;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.player.*;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.util.*;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.loot.*;
import net.minecraft.world.storage.loot.conditions.*;
import net.minecraft.world.storage.loot.functions.LootFunction;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class ExplorationEvents {
    private static final UUID STRIDE = UUID.fromString("6623a1c5-292d-4c91-8890-b6121063c7ab");
    private static final String AIR_USED = "ExoArsenalBottleJumpMask",
            AIR_TICKS = "ExoArsenalBottleAirTicks";

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote) return;
        NBTTagCompound data = player.getEntityData();
        if (player.onGround || player.isInWater() || player.isOnLadder()) {
            data.setInteger(AIR_USED, 0);
            data.setInteger(AIR_TICKS, 0);
        } else data.setInteger(AIR_TICKS, Math.min(100, data.getInteger(AIR_TICKS) + 1));
        double movedX = player.posX - data.getDouble("ExoArsenalStrideX"),
                movedZ = player.posZ - data.getDouble("ExoArsenalStrideZ");
        boolean running =
                data.hasKey("ExoArsenalStrideX")
                        && AccessoryInventory.has(player, PrebossContent.HERMES_BOOTS)
                        && player.isSprinting()
                        && !player.collidedHorizontally
                        && !player.isInWater()
                        && !player.isInLava()
                        && !player.isRiding()
                        && !player.capabilities.isFlying
                        && movedX * movedX + movedZ * movedZ > .0004;
        data.setDouble("ExoArsenalStrideX", player.posX);
        data.setDouble("ExoArsenalStrideZ", player.posZ);
        int stride =
                PrebossRules.runningTicks(
                        data.getInteger("ExoArsenalHermesStride"), running, player.onGround);
        data.setInteger("ExoArsenalHermesStride", stride);
        IAttributeInstance speed =
                player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED);
        AttributeModifier old = speed.getModifier(STRIDE);
        double bonus = PrebossRules.runningBonus(stride);
        if (old != null && Math.abs(old.getAmount() - bonus) > .00001) {
            speed.removeModifier(STRIDE);
            old = null;
        }
        if (bonus > 0 && old == null)
            speed.applyModifier(
                    new AttributeModifier(STRIDE, "Hermes running stride", bonus, 2)
                            .setSaved(false));
        if (stride >= 20 && player.onGround && player.ticksExisted % 4 == 0)
            ((WorldServer) player.world)
                    .spawnParticle(
                            EnumParticleTypes.CLOUD,
                            player.posX,
                            player.posY + .08,
                            player.posZ,
                            2,
                            .15,
                            .02,
                            .15,
                            .015);
    }

    public static void jump(EntityPlayerMP player) {
        NBTTagCompound data = player.getEntityData();
        if (!player.isEntityAlive()
                || player.isSpectator()
                || player.capabilities.isFlying
                || player.isElytraFlying()
                || player.isRiding()
                || player.onGround
                || player.isInWater()
                || player.isInLava()
                || player.isOnLadder()
                || data.getInteger(AIR_TICKS) < 2) return;
        int tier =
                PrebossRules.nextBottleTier(
                        AccessoryInventory.bottleMask(player), data.getInteger(AIR_USED));
        if (tier == 0) return;
        data.setInteger(AIR_USED, data.getInteger(AIR_USED) | (1 << (tier - 1)));
        player.motionY = PrebossRules.bottleVelocity(tier);
        player.fallDistance = 0;
        player.velocityChanged = true;
        player.connection.sendPacket(new SPacketEntityVelocity(player));
        WorldServer world = player.getServerWorld();
        EnumParticleTypes effect =
                tier == 2
                        ? EnumParticleTypes.SNOW_SHOVEL
                        : tier == 3 ? EnumParticleTypes.BLOCK_DUST : EnumParticleTypes.CLOUD;
        if (tier == 3)
            world.spawnParticle(
                    effect,
                    player.posX,
                    player.posY + .1,
                    player.posZ,
                    18,
                    .35,
                    .1,
                    .35,
                    .06,
                    net.minecraft.block.Block.getStateId(
                            net.minecraft.init.Blocks.SAND.getDefaultState()));
        else
            world.spawnParticle(
                    effect, player.posX, player.posY + .1, player.posZ, 18, .35, .1, .35, .06);
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                net.minecraft.init.SoundEvents.ENTITY_BAT_TAKEOFF,
                SoundCategory.PLAYERS,
                .45F,
                1.2F);
    }

    @SubscribeEvent
    public static void fall(LivingFallEvent event) {
        if (event.getEntityLiving() instanceof EntityPlayer
                && AccessoryInventory.has(
                        (EntityPlayer) event.getEntityLiving(), PrebossContent.LUCKY_HORSESHOE))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void hotFloor(LivingAttackEvent event) {
        if (event.getSource() == DamageSource.HOT_FLOOR
                && event.getEntityLiving() instanceof EntityPlayer
                && AccessoryInventory.has(
                        (EntityPlayer) event.getEntityLiving(), PrebossContent.OBSIDIAN_SKULL))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void zombieLoot(LivingDropsEvent event) {
        if (event.getEntityLiving().world.isRemote
                || !(event.getEntityLiving() instanceof EntityZombie)
                || !event.isRecentlyHit()
                || event.getEntityLiving().getRNG().nextInt(50) != 0) return;
        event.getDrops()
                .add(
                        new EntityItem(
                                event.getEntityLiving().world,
                                event.getEntityLiving().posX,
                                event.getEntityLiving().posY,
                                event.getEntityLiving().posZ,
                                new ItemStack(PrebossContent.SHACKLE)));
    }

    @SubscribeEvent
    public static void loot(LootTableLoadEvent event) {
        if (!event.getName().getResourceDomain().equals("minecraft")) return;
        String path = event.getName().getResourcePath();
        Item[] items;
        switch (path) {
            case "chests/simple_dungeon":
            case "chests/abandoned_mineshaft":
                items =
                        new Item[] {
                            PrebossContent.HERMES_BOOTS,
                            PrebossContent.CLOUD_BOTTLE,
                            PrebossContent.LUCKY_HORSESHOE,
                            PrebossContent.MAGIC_MIRROR
                        };
                break;
            case "chests/desert_pyramid":
                items = new Item[] {PrebossContent.SANDSTORM_BOTTLE, PrebossContent.HERMES_BOOTS};
                break;
            case "chests/igloo_chest":
                items =
                        new Item[] {
                            PrebossContent.BLIZZARD_BOTTLE,
                            PrebossContent.HERMES_BOOTS,
                            PrebossContent.ICE_MIRROR
                        };
                break;
            case "chests/jungle_temple":
                items = new Item[] {PrebossContent.FERAL_CLAWS, PrebossContent.WIND_ANKLET};
                break;
            case "chests/village_blacksmith":
            case "chests/spawn_bonus_chest":
                items = new Item[] {PrebossContent.AGLET};
                break;
            default:
                return;
        }
        LootEntry[] entries = new LootEntry[items.length];
        for (int i = 0; i < items.length; i++)
            entries[i] =
                    new LootEntryItem(
                            items[i],
                            1,
                            0,
                            new LootFunction[0],
                            new LootCondition[0],
                            "exoarsenal_exploration_" + i);
        event.getTable()
                .addPool(
                        new LootPool(
                                entries,
                                new LootCondition[] {new RandomChance(.55F)},
                                new RandomValueRange(1),
                                new RandomValueRange(0),
                                "exoarsenal_exploration_accessory"));
    }
}
