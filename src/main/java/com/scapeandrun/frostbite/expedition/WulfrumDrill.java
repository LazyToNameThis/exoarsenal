package com.scapeandrun.frostbite.expedition;

import net.minecraft.item.*;

public final class WulfrumDrill extends ItemPickaxe {
    public WulfrumDrill() {
        super(ToolMaterial.IRON);
        setMaxDamage(220);
        efficiency = 4.5F;
    }

    @Override
    public boolean getIsRepairable(ItemStack tool, ItemStack repair) {
        return repair.getItem() == ExpeditionContent.SCRAP;
    }
}
