package com.scapeandrun.frostbite.registry;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.*;
import com.scapeandrun.frostbite.expedition.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class MachineRewards {
    public static final Item EXCAVATOR_BAG = new Reward("excavator_treasure_bag", 0);
    public static final Item SEER_BAG = new Reward("seer_treasure_bag", 1);
    public static final Item KEY = new Reward("excavator_key", 2);
    public static final Item PROBE = new Reward("personal_probe", 3);
    public static final Item[] ITEMS = {EXCAVATOR_BAG, SEER_BAG, KEY, PROBE};

    @SubscribeEvent
    public static void register(RegistryEvent.Register<Item> event) {
        event.getRegistry().registerAll(ITEMS);
    }

    private static void give(EntityPlayer player, ItemStack stack) {
        if (!player.inventory.addItemStackToInventory(stack) && !stack.isEmpty())
            player.dropItem(stack, false);
    }

    private static final class Reward extends Item {
        private final int kind;

        Reward(String name, int kind) {
            this.kind = kind;
            setRegistryName(Frostbite.MODID, name);
            setUnlocalizedName(Frostbite.MODID + "." + name);
            setCreativeTab(ModContent.TAB);
            setMaxStackSize(kind == 2 ? 1 : 16);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(
                World world, EntityPlayer player, EnumHand hand) {
            ItemStack stack = player.getHeldItem(hand);
            if (world.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);
            if (player.getCooldownTracker().hasCooldown(this))
                return new ActionResult<>(EnumActionResult.FAIL, stack);
            if (kind == 0) {
                ExcavatorLoot loot = ExcavatorLoot.roll(world.rand);
                stack.shrink(1);
                give(player, new ItemStack(ExpeditionContent.SCRAP, loot.scrap));
                give(player, new ItemStack(ExpeditionContent.CORE, loot.cores));
                give(player, new ItemStack(PROBE, loot.probes));
                give(player, new ItemStack(KEY));
            } else if (kind == 1) {
                stack.shrink(1);
                WulfrumEncounterLoot loot = WulfrumEncounterLoot.roll(world.rand, true);
                give(player, new ItemStack(WulfrumArsenal.SCRAP, loot.scrap));
                give(player, new ItemStack(ExpeditionContent.CORE, loot.cores));
                give(player, WulfrumArsenal.charged(WulfrumArsenal.AWL));
                give(player, WulfrumArsenal.charged(WulfrumArsenal.LITTLE_BOY));
                give(player, new ItemStack(WulfrumArsenal.HEART));
            } else {
                EntityPersonalMachine existing = null;
                int count = 0;
                for (EntityPersonalMachine machine :
                        world.getEntitiesWithinAABB(
                                EntityPersonalMachine.class,
                                player.getEntityBoundingBox().grow(96)))
                    if (machine.ownedBy(player) && machine.mount() == (kind == 2)) {
                        existing = machine;
                        count++;
                    }
                if (kind == 2 && existing != null) {
                    if (player.isRiding() && player.getRidingEntity() == existing)
                        existing.trigger(player);
                    else if (!existing.isBeingRidden() && existing.getDistanceSq(player) < 36)
                        player.startRiding(existing);
                    else return new ActionResult<>(EnumActionResult.FAIL, stack);
                } else {
                    if (kind == 3 && count >= 4) {
                        player.sendStatusMessage(
                                new net.minecraft.util.text.TextComponentString(
                                        "You already have four Personal Probes deployed."),
                                true);
                        return new ActionResult<>(EnumActionResult.FAIL, stack);
                    }
                    EntityPersonalMachine machine =
                            new EntityPersonalMachine(world, player, kind == 2);
                    if (!world.getCollisionBoxes(machine, machine.getEntityBoundingBox()).isEmpty()
                            || !world.spawnEntity(machine))
                        return new ActionResult<>(EnumActionResult.FAIL, stack);
                    if (kind == 2) player.startRiding(machine);
                    else if (!player.isCreative()) stack.shrink(1);
                }
            }
            player.getCooldownTracker().setCooldown(this, 10);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }

        @Override
        public void addInformation(
                ItemStack stack,
                World world,
                java.util.List<String> lines,
                net.minecraft.client.util.ITooltipFlag flag) {
            if (kind < 2) lines.add("Right-click to open.");
            else if (kind == 2) {
                lines.add("Summons your personal Excavator.");
                lines.add("Ride with forward/back. Sneak to dismount.");
                lines.add("Use while riding: drill rush → turret shot → seismic pulse.");
                lines.add("Attacks do not destroy terrain.");
            } else {
                lines.add("Deploys a Personal Probe. Maximum: 4.");
                lines.add("Follows you and attacks enemies you hit.");
                lines.add("Defends you against attackers.");
            }
        }
    }
}
