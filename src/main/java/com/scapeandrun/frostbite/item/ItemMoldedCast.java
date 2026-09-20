package com.scapeandrun.frostbite.item;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.ICast;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.Pattern;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ItemMoldedCast extends Pattern implements ICast {
    private final boolean fireclay;

    public ItemMoldedCast(boolean fireclay) {
        this.fireclay = fireclay;
        setMaxStackSize(1);
        if (fireclay) setMaxDamage(10);
    }

    public boolean isFireclay() {
        return fireclay;
    }

    @Override
    protected Collection<Item> getSubItemToolparts() {
        Collection<Item> result = new ArrayList<>();
        for (IToolPart part : TinkerRegistry.getToolParts())
            if (part instanceof Item) result.add((Item) part);
        return result;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        super.addInformation(stack, world, tooltip, flag);
        if (fireclay) {
            tooltip.add(
                    net.minecraft.util.text.TextFormatting.GOLD
                            + "Thermal wear: "
                            + stack.getItemDamage()
                            + " / 10");
            tooltip.add(
                    net.minecraft.util.text.TextFormatting.GRAY + "Low-temperature pour: 2 wear");
            tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + "Above 1100 C: 5 wear");
        } else tooltip.add(net.minecraft.util.text.TextFormatting.GRAY + "Single use");
    }
}
