package com.exoarsenal.combat;

import com.exoarsenal.expedition.DeepWeapon;
import com.exoarsenal.item.ItemRBlade;
import com.exoarsenal.item.ItemRTool;
import com.exoarsenal.item.ItemX10Blade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;

public enum WeaponDiscipline {
    SWORD(6, 17, 18, 3.1F, 70, 1.0F),
    AXE(11, 27, 28, 3.0F, 58, 1.25F),
    ENERGY(7, 19, 21, 3.5F, 82, 1.0F),
    KATANA(5, 15, 16, 3.6F, 78, 0.9F),
    SCISSORS(9, 23, 24, 3.3F, 42, 1.15F),
    HAMMER(15, 35, 36, 3.8F, 95, 1.55F),
    RAPIER(4, 13, 13, 4.0F, 22, 0.8F),
    SAW(8, 23, 25, 3.2F, 45, 1.05F);
    public final int contact, duration, cost;
    public final float reach, arc, multiplier;
    private static final int HEAVY_CONTACT_DELAY = 6;
    private static final int HEAVY_DURATION_EXTENSION = 10;
    private static final int HEAVY_STAMINA_SURCHARGE = 16;

    WeaponDiscipline(
            int contactTick,
            int durationTicks,
            int staminaCost,
            float reachBlocks,
            float arcDegrees,
            float damageMultiplier) {
        this.contact = contactTick;
        this.duration = durationTicks;
        this.cost = staminaCost;
        this.reach = reachBlocks;
        this.arc = arcDegrees;
        this.multiplier = damageMultiplier;
    }

    public int contact(boolean heavy) {
        return contact + (heavy ? HEAVY_CONTACT_DELAY : 0);
    }

    public int duration(boolean heavy) {
        return duration + (heavy ? HEAVY_DURATION_EXTENSION : 0);
    }

    public int cost(boolean heavy) {
        return cost + (heavy ? HEAVY_STAMINA_SURCHARGE : 0);
    }

    public static WeaponDiscipline of(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof ItemX10Blade) {
            switch (ItemX10Blade.getForm(stack)) {
                case ItemX10Blade.SCISSORS:
                    return SCISSORS;
                case ItemX10Blade.GREATHAMMER:
                    return HAMMER;
                case ItemX10Blade.RAPIER:
                    return RAPIER;
                default:
                    return KATANA;
            }
        }
        if (item instanceof ItemRBlade) return ENERGY;
        if (item instanceof ItemRTool)
            return ItemRTool.getForm(stack) == ItemRTool.SAW ? SAW : null;
        if (item instanceof DeepWeapon) {
            DeepWeapon.Kind kind = ((DeepWeapon) item).kind;
            if (kind == DeepWeapon.Kind.SWORD
                    || kind == DeepWeapon.Kind.FIRE_SWORD
                    || kind == DeepWeapon.Kind.GRASS_SWORD) return SWORD;
        }
        if (item instanceof ItemSword) return SWORD;
        if (item instanceof ItemAxe) return AXE;
        return null;
    }
}
