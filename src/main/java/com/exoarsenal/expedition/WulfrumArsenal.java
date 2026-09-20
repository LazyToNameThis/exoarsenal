package com.exoarsenal.expedition;

import com.exoarsenal.item.EnergyUtil;
import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import java.util.List;

public final class WulfrumArsenal {
    public static final Item SCRAP =
            ExpeditionContent.register(new Item(), "energized_wulfrum_scrap");
    public static final Weapon AWL = (Weapon) ExpeditionContent.register(new Weapon(0), "pg_c_awl");
    public static final Weapon LITTLE_BOY =
            (Weapon) ExpeditionContent.register(new Weapon(1), "pg_c_little_boy");
    public static final Weapon TALONS =
            (Weapon) ExpeditionContent.register(new Weapon(2), "pg_c_talons");
    public static final Weapon STRIKER =
            (Weapon) ExpeditionContent.register(new Weapon(3), "pg_c_striker");
    public static final Item HEART =
            ExpeditionContent.register(
                    new Item() {
                        {
                            setMaxStackSize(1);
                        }

                        @Override
                        public void addInformation(
                                ItemStack s, World w, List<String> lines, ITooltipFlag flag) {
                            lines.add("\u00a7aAccessory: Wulfrum Android");
                            lines.add("Replaces your body with an articulated Wulfrum chassis.");
                            lines.add("Empty main hand: attack to punch; use to grapple.");
                            lines.add("Sneak + use: leap and slam. +8 armor while equipped.");
                        }
                    },
                    "wulfrum_heart");

    public static void init() {}

    public static ItemStack charged(Item item) {
        ItemStack s = new ItemStack(item);
        if (EnergyUtil.get(s) != null) EnergyUtil.get(s).receiveEnergy(10000, false);
        return s;
    }

    public static final class Weapon extends Item {
        public final int kind;

        Weapon(int kind) {
            this.kind = kind;
            setMaxStackSize(1);
        }

        @Override
        public ICapabilityProvider initCapabilities(ItemStack s, NBTTagCompound n) {
            return EnergyUtil.provider(100000, 10000);
        }

        @Override
        public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
            if (!w.isRemote) WulfrumCombat.request(p, 1);
            return new ActionResult<>(EnumActionResult.SUCCESS, p.getHeldItem(hand));
        }

        @Override
        public boolean onLeftClickEntity(
                ItemStack s, EntityPlayer p, net.minecraft.entity.Entity target) {
            if (!p.world.isRemote) WulfrumCombat.request(p, 0);
            return true;
        }

        @Override
        public boolean showDurabilityBar(ItemStack s) {
            return EnergyUtil.stored(s) < 100000;
        }

        @Override
        public double getDurabilityForDisplay(ItemStack s) {
            return 1 - EnergyUtil.stored(s) / 100000D;
        }

        @Override
        public int getRGBDurabilityForDisplay(ItemStack s) {
            return 0x73EC99;
        }

        @Override
        public void addInformation(ItemStack s, World w, List<String> lines, ITooltipFlag flag) {
            lines.add(
                    "\u00a7a"
                            + String.format(
                                    java.util.Locale.ROOT,
                                    "%,d / 100,000 RF",
                                    EnergyUtil.stored(s)));
            if (kind == 0) {
                lines.add("Attack: sweeping energy blade. Use: driving thrust.");
                lines.add("8 damage \u2022 80 RF per swing");
            }
            if (kind == 1) {
                lines.add("Hold use to fire a steady stream of green lasers.");
                lines.add("5 damage \u2022 36-block range \u2022 30 RF per shot");
            }
            if (kind == 2) {
                lines.add("Attack: alternating claws. Use: rising double cut.");
                lines.add("6 damage \u2022 60 RF per cut");
            }
            if (kind == 3) {
                lines.add("Attack: quick stab. Use: throw a returning energy dagger.");
                lines.add("7 melee / 9 thrown damage \u2022 50 RF");
            }
        }
    }
}
