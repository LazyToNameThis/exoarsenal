package com.scapeandrun.frostbite.item;

import net.minecraft.item.Item;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.Pattern;

import java.util.ArrayList;
import java.util.Collection;

public class ItemUnfiredCast extends Pattern {
    @Override
    protected Collection<Item> getSubItemToolparts() {
        Collection<Item> result = new ArrayList<>();
        for (IToolPart part : TinkerRegistry.getToolParts())
            if (part instanceof Item) result.add((Item) part);
        return result;
    }
}
