package com.scapeandrun.frostbite.item;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class EquipmentTooltip {
    public static final int WHITE = 0xF3F7F6;
    public static final int MUTED = 0xA6B0B3;
    public static final int DIM = 0x707C81;
    public static final int RED = 0xFF4654;
    public static final int RED_DARK = 0xC53B46;
    public static final int CYAN = 0x69E2E3;
    public static final int GOLD = 0xF2BE63;
    public static final int CANVAS = 0xD8B67C;
    public static final int GREEN = 0x79D991;
    public static final int YELLOW = 0xFFE56B;

    private EquipmentTooltip() {}

    public static final class Line {
        public final String text;
        public final int rgb;
        public final boolean gap;

        private Line(String text, int rgb, boolean gap) {
            this.text = text;
            this.rgb = rgb;
            this.gap = gap;
        }

        public static Line text(int rgb, String text) {
            return new Line(text, rgb, false);
        }

        public static Line gap() {
            return new Line("", WHITE, true);
        }
    }

    public static boolean supports(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        return item instanceof ItemRBlade
                || item instanceof ItemRTool
                || item instanceof ItemRmorArmor
                || item instanceof ItemCanvasArmor
                || item instanceof ItemEnergyGun
                || item instanceof ItemEnergyShield
                || item instanceof ItemScoutWeapon;
    }

    public static List<Line> build(ItemStack stack) {
        if (!supports(stack)) return Collections.emptyList();
        List<Line> lines = new ArrayList<>();
        lines.add(Line.text(WHITE, "§l" + stack.getDisplayName()));
        if (stack.getItem() instanceof ItemScoutWeapon) scoutWeapon(stack, lines);
        else if (stack.getItem() instanceof ItemEnergyGun) gun(stack, lines);
        else if (stack.getItem() instanceof ItemEnergyShield) shield(stack, lines);
        else if (stack.getItem() instanceof ItemX10Blade) x10Blade(stack, lines);
        else if (stack.getItem() instanceof ItemRBlade) blade(stack, lines);
        else if (stack.getItem() instanceof ItemRTool) tool(stack, lines);
        else if (stack.getItem() instanceof ItemX10Armor) x10Armor(stack, lines);
        else if (stack.getItem() instanceof ItemRmorArmor) rmor(stack, lines);
        else if (stack.getItem() instanceof ItemCanvasArmor) canvas(stack, lines);
        return lines;
    }

    private static void scoutWeapon(ItemStack stack, List<Line> out) {
        ItemScoutWeapon.Type type = ((ItemScoutWeapon) stack.getItem()).getType();
        out.add(Line.text(CYAN, "◆ X-20 Salvaged Weapon"));
        if (type == ItemScoutWeapon.Type.RAILGUN) energy(stack, out);
        out.add(Line.gap());
        switch (type) {
            case PINCER:
                feature(out, CYAN, "Hydraulic Snap");
                out.add(
                        Line.text(
                                MUTED,
                                "Fast snapping strikes. Every third hit releases a frost shockwave."));
                feature(out, WHITE, "Shatter");
                out.add(Line.text(MUTED, "Striking a frozen enemy sprays damaging ice fragments."));
                ability(out, CYAN, "Hold Right Click", "Hydraulic Crush");
                out.add(Line.text(GREEN, "A full charge locks smaller enemies in place."));
                break;
            case RAILGUN:
                feature(out, CYAN, "Frigid Rail");
                out.add(
                        Line.text(
                                MUTED,
                                "Pierces up to 10 enemies. Unused pierces add 10% damage each."));
                out.add(
                        Line.text(
                                GREEN,
                                "Repeated hits build accuracy and damage against one target."));
                ability(out, CYAN, "Right Click", "Fire Beam");
                ability(out, GOLD, "Sneak + Right Click", "Target Lock");
                out.add(Line.text(CYAN, "⚡ Shot Cost: 250 RF"));
                break;
            case SMASHER:
                feature(out, CYAN, "Reinforced Crusher");
                out.add(Line.text(MUTED, "Slow, crushing attacks release short frost shockwaves."));
                out.add(
                        Line.text(
                                GREEN, "Deals increased damage to armor and mechanical enemies."));
                ability(out, CYAN, "Hold Right Click", "Glacier Slam");
                out.add(
                        Line.text(
                                MUTED,
                                "Crushes an area and freezes ordinary enemies at full charge."));
                break;
            default:
                feature(out, CYAN, "Aurora Field");
                out.add(Line.text(MUTED, "Creates a damaging aurora for 60 seconds."));
                ability(out, CYAN, "Right Click", "Cast Aurora");
                ability(out, GOLD, "Cast Again", "Icefall");
                out.add(
                        Line.text(
                                MUTED, "Detonates the active aurora into seeking ice fragments."));
                out.add(
                        Line.text(
                                GREEN,
                                "The staff can be swapped out while the field remains active."));
                break;
        }
    }

    private static void gun(ItemStack stack, List<Line> out) {
        ItemEnergyGun.Type type = ((ItemEnergyGun) stack.getItem()).getType();
        int accent = type.x10 ? YELLOW : RED;
        out.add(Line.text(accent, type.x10 ? "◆ X-10 Energy Weapon" : "◆ Prototype Energy Weapon"));
        energy(stack, out);
        out.add(Line.text(GOLD, "⚡ Shot Cost: " + grouped(type.energyCost) + " RF"));
        out.add(Line.text(CYAN, "◎ Effective Range: " + Math.round(type.range) + " blocks"));
        out.add(Line.gap());

        if (type == ItemEnergyGun.Type.PROTOTYPE_SHOTGUN) {
            feature(out, accent, "Close-Range Scatter");
            out.add(Line.text(MUTED, "Fires seven incendiary energy pellets."));
        } else if (type == ItemEnergyGun.Type.X10_SHOTGUN) {
            feature(out, accent, "Coreburst");
            out.add(Line.text(MUTED, "Fires one heavy bolt surrounded by eight smaller bolts."));
            out.add(Line.text(GREEN, "A direct core hit releases a compact shockwave."));
        } else if (type.cannon()) {
            feature(out, accent, type.x10 ? "Empowered Impact" : "Charged Impact");
            out.add(
                    Line.text(
                            MUTED,
                            type.x10
                                    ? "Strikes a wide area with a high-energy impact."
                                    : "Detonates a focused energy charge on contact."));
            out.add(Line.text(DIM, "Impact damage is capped at 16 targets."));
        } else if (type == ItemEnergyGun.Type.X10_NET_LAUNCHER) {
            feature(out, accent, "Containment Net");
            out.add(Line.text(MUTED, "Pins the target for 8 seconds and marks it through walls."));
            out.add(Line.text(GREEN, "Applies Slowness, Weakness, and Mining Fatigue."));
        } else if (type.automatic()) {
            feature(out, accent, "Automatic Fire");
            out.add(
                    Line.text(
                            MUTED,
                            type.x10
                                    ? "Maintains a stable high-output energy stream."
                                    : "Rapid fire with controlled beam drift."));
        } else {
            feature(out, accent, "Sidearm");
            out.add(Line.text(MUTED, "A fast, accurate energy bolt with a short recovery."));
        }

        out.add(Line.gap());
        ability(out, accent, type.automatic() ? "Hold Right Click" : "Right Click", "Fire");
        if (type.dual)
            out.add(Line.text(GREEN, "Dual wield two matching weapons to fire both at once."));
        out.add(Line.text(RED, "Deals increased damage to parasites."));
    }

    private static void shield(ItemStack stack, List<Line> out) {
        ItemEnergyShield item = (ItemEnergyShield) stack.getItem();
        int accent = item.isX10() ? YELLOW : RED;
        int percent = Math.round(item.reduction() * 100.0F);
        out.add(
                Line.text(
                        accent,
                        item.isX10() ? "◆ X-10 Field Projector" : "◆ Prototype Field Projector"));
        energy(stack, out);
        out.add(Line.text(GOLD, "⛉ Damage Blocked: " + percent + "%"));
        out.add(Line.gap());
        ability(out, accent, "Hold Right Click", "Raise Shield");
        out.add(Line.text(MUTED, "Blocks frontal attacks while the field remains powered."));
        out.add(Line.text(CYAN, "RF cost scales with the strength of each hit."));
        out.add(Line.text(DIM, "Attacks from behind bypass the shield."));
    }

    public static void appendLegacy(ItemStack stack, List<String> tooltip) {
        List<Line> lines = build(stack);
        for (int i = 1; i < lines.size(); i++) {
            Line line = lines.get(i);
            tooltip.add(line.gap ? "" : nearestFormatting(line.rgb) + line.text);
        }
    }

    private static void blade(ItemStack stack, List<Line> out) {
        out.add(
                Line.text(
                        ItemRBlade.isActive(stack) ? RED : DIM,
                        ItemRBlade.isActive(stack) ? "◆ Blade Active" : "◇ Blade Inactive"));
        energy(stack, out);
        out.add(Line.text(GOLD, "⚔ Damage: +7"));
        out.add(Line.text(RED, "♨ Parasite Damage: +8"));
        out.add(Line.gap());
        ability(out, RED, "Right Click", "Activate / Retract");
        out.add(Line.text(MUTED, "Turns the energy blade on or off."));
        ability(out, GOLD, "Left Click", "Attack");
        out.add(Line.text(MUTED, "Each swing advances the current attack pattern."));
        ability(out, RED, "Ctrl + Right Click", "Spin & Throw");
        out.add(Line.text(MUTED, "Hold to spin the blade. Release to throw it."));
        out.add(
                Line.text(
                        DIM,
                        "Strike the returning blade up to three times to increase its damage."));
        out.add(Line.text(CYAN, "⚡ Attack Cost: 600 RF"));
    }

    private static void x10Blade(ItemStack stack, List<Line> out) {
        out.add(
                Line.text(
                        ItemRBlade.isActive(stack) ? YELLOW : DIM,
                        ItemRBlade.isActive(stack) ? "◆ Blade Active" : "◇ Blade Inactive"));
        energy(stack, out);
        out.add(Line.text(YELLOW, "⚔ Form: " + ItemX10Blade.formName(stack)));
        out.add(Line.text(GOLD, "✦ Ability: " + ItemX10Blade.abilityName(stack)));
        out.add(Line.gap());

        switch (ItemX10Blade.getForm(stack)) {
            case ItemX10Blade.SCISSORS:
                out.add(Line.text(WHITE, "Scissors"));
                out.add(Line.text(MUTED, "Snaps shut around enemies and can harvest their heads."));
                break;
            case ItemX10Blade.GREATHAMMER:
                out.add(Line.text(WHITE, "Greathammer"));
                out.add(Line.text(MUTED, "Slow, heavy strikes with high damage and reach."));
                break;
            case ItemX10Blade.RAPIER:
                out.add(Line.text(WHITE, "Rapier"));
                out.add(Line.text(MUTED, "Fast thrusts built for precise attacks."));
                break;
            default:
                out.add(Line.text(WHITE, "Katana"));
                out.add(Line.text(MUTED, "Fast, flowing slashes with flourishing movements."));
                break;
        }

        out.add(Line.gap());
        switch (ItemX10Blade.getAbility(stack)) {
            case ItemX10Blade.RAPID_SLASHES:
                ability(out, YELLOW, "Right Click", "Rapid Slashes");
                out.add(Line.text(MUTED, "Hold to unleash repeated slashes in front of you."));
                break;
            case ItemX10Blade.CLEAVE:
                ability(out, YELLOW, "Right Click", "Cleave");
                out.add(Line.text(MUTED, "Performs a three-hit combo: /, \\, then X."));
                out.add(Line.text(RED, "The third hit causes an explosion."));
                break;
            default:
                ability(out, YELLOW, "Right Click", "Spin & Throw");
                out.add(Line.text(MUTED, "Hold to spin the blade. Release to throw it."));
                out.add(
                        Line.text(
                                GREEN,
                                "The thrown blade locks onto its first target for five seconds."));
                break;
        }
        out.add(Line.gap());
        out.add(Line.text(DIM, "← Change form     → Change ability"));
    }

    private static void tool(ItemStack stack, List<Line> out) {
        int form = ItemRTool.getForm(stack);
        String formName =
                form == ItemRTool.SAW ? "Saw" : form == ItemRTool.BUILD ? "Build" : "Drill";
        boolean x10 = stack.getItem() instanceof ItemX10Multitool;
        out.add(Line.text(x10 ? YELLOW : RED, "◆ Current Form: " + formName));
        energy(stack, out);
        if (x10) out.add(Line.text(YELLOW, "X-10 upgrade: faster cutting, mining, and printing."));
        out.add(Line.gap());

        if (form == ItemRTool.DRILL) {
            String mode =
                    ItemRTool.getMode(stack) == ItemRTool.AREA
                            ? "3×3"
                            : ItemRTool.getMode(stack) == ItemRTool.VEIN ? "Vein" : "Single Block";
            ability(out, x10 ? YELLOW : RED, "Hold Left Click", "Drill Beam");
            out.add(Line.text(MUTED, "Projects a rotating mining beam up to 24 blocks."));
            out.add(Line.text(GOLD, "◇ Drill Mode: " + mode));
            if (ItemRTool.getMode(stack) == ItemRTool.VEIN) {
                out.add(Line.text(GREEN, "Fortune I; mines up to 256 connected ore blocks."));
            } else if (ItemRTool.getMode(stack) == ItemRTool.AREA) {
                out.add(Line.text(MUTED, "Mines a 3×3 area at reduced speed."));
            }
        } else if (form == ItemRTool.SAW) {
            ability(out, x10 ? YELLOW : RED, "Hold Left Click", "Energy Saw");
            out.add(Line.text(MUTED, "Cuts faster the longer the blade stays in contact."));
            out.add(Line.text(GREEN, "Cuts connected logs and their leaves."));
            out.add(Line.text(GOLD, "⚔ Also works as a powered melee weapon."));
        } else {
            NBTTagCompound tag = stack.getTagCompound();
            String name =
                    tag != null && tag.hasKey("SchematicName", 8)
                            ? tag.getString("SchematicName")
                            : "None";
            int blocks = tag == null ? 0 : tag.getTagList("Schematic", 10).tagCount();
            ability(out, x10 ? YELLOW : RED, "Right Click", "Select Schematic");
            out.add(Line.text(MUTED, "Select a file from config/exoarsenal/schematics."));
            out.add(
                    Line.text(
                            CYAN,
                            "Selected: "
                                    + name
                                    + "  •  "
                                    + blocks
                                    + "/"
                                    + ItemRTool.MAX_SCHEMATIC_BLOCKS
                                    + " blocks"));
            ability(out, GOLD, "Sneak + Right Click", "Start Printing");
            out.add(Line.text(MUTED, "Places blocks from your inventory with four build lasers."));
        }

        out.add(Line.gap());
        out.add(
                Line.text(
                        DIM,
                        form == ItemRTool.DRILL
                                ? "← Change form     → Change drill mode"
                                : "← Change form"));
    }

    private static void rmor(ItemStack stack, List<Line> out) {
        ItemArmor armor = (ItemArmor) stack.getItem();
        out.add(Line.text(RED, "◆ Powered Armor"));
        energy(stack, out);
        out.add(Line.text(GOLD, "⛨ Armor: +" + armor.damageReduceAmount + "     ❖ Toughness: +1"));
        out.add(Line.gap());
        EntityEquipmentSlot slot = armor.armorType;
        if (slot == EntityEquipmentSlot.CHEST) {
            feature(out, RED, "Energy Shield");
            out.add(Line.text(MUTED, "Absorbs five hits at a cost of 100 RF per hit."));
        } else if (slot == EntityEquipmentSlot.LEGS) {
            ability(out, RED, "Shift + F", "Change Combat Mode");
            out.add(Line.text(MUTED, "Cycles between Stylish, Berserk, and Titanic."));
        } else if (slot == EntityEquipmentSlot.FEET) {
            ability(out, CYAN, "Sneak in Water", "Diving Boots");
            out.add(Line.text(MUTED, "Lets you walk along the seabed without drifting."));
        }
        defenseForm(out, false);
    }

    private static void x10Armor(ItemStack stack, List<Line> out) {
        ItemArmor armor = (ItemArmor) stack.getItem();
        out.add(Line.text(YELLOW, "◆ X-10 Powered Armor"));
        energy(stack, out);
        out.add(Line.text(GOLD, "⛨ Armor: +" + armor.damageReduceAmount + "     ❖ Toughness: +2"));
        out.add(Line.gap());
        EntityEquipmentSlot slot = armor.armorType;
        if (slot == EntityEquipmentSlot.HEAD) {
            feature(out, YELLOW, "Entity and Block Analyzer");
            out.add(
                    Line.text(
                            MUTED,
                            "Displays information about the entity or block you are looking at."));
            feature(out, CYAN, "Player Equipment Analyzer");
            out.add(Line.text(MUTED, "Displays a player's held weapon and inventory information."));
            feature(out, GREEN, "Night Vision");
        } else if (slot == EntityEquipmentSlot.CHEST) {
            feature(out, YELLOW, "Energy Wings");
            out.add(Line.text(MUTED, "Hold Sneak while airborne to slow your fall."));
            feature(out, CYAN, "Jetpack");
            out.add(Line.text(MUTED, "Hold Jump while airborne to gain altitude."));
        } else if (slot == EntityEquipmentSlot.LEGS) {
            feature(out, YELLOW, "Five-Slot Weapon Belt");
            out.add(Line.text(MUTED, "Stores up to five weapons for quick swapping."));
            ability(out, YELLOW, "G", "Open Weapon Belt");
            ability(out, GOLD, "Shift + G", "Store Held Weapon");
            feature(out, CYAN, "System Controls");
            ability(out, CYAN, "H", "Open Armor Systems");
            feature(out, GREEN, "Swim Boost");
        } else {
            feature(out, GREEN, "Sprint Boost");
            out.add(Line.text(MUTED, "Increases your running speed while the armor is powered."));
        }
        defenseForm(out, true);
    }

    private static void defenseForm(List<Line> out, boolean x10) {
        out.add(Line.gap());
        ability(out, x10 ? YELLOW : RED, "Double-Tap ↓", "Defense Form");
        out.add(Line.text(GOLD, "+5 Armor     +4 Toughness"));
        if (x10) {
            out.add(
                    Line.text(
                            MUTED,
                            "Locks the hotbar and activates integrated shotguns, blasters, or katanas."));
            ability(out, YELLOW, "→", "Change Integrated Weapon");
        } else {
            out.add(
                    Line.text(
                            MUTED, "Locks the hotbar and activates the integrated twin shotguns."));
        }
        out.add(Line.text(CYAN, "Press Jump while airborne to use the jetpack double-jump."));
    }

    private static void canvas(ItemStack stack, List<Line> out) {
        ItemArmor armor = (ItemArmor) stack.getItem();
        out.add(Line.text(CANVAS, "◆ Insulated Canvas Armor"));
        out.add(Line.text(GOLD, "⛨ Armor: +" + armor.damageReduceAmount));
        out.add(Line.gap());
        feature(out, CANVAS, "Full Set: Cold Protection");
        out.add(Line.text(MUTED, "Keeps you warm during ordinary snowfall."));
        out.add(Line.text(RED_DARK, "Does not protect you from snowstorms or blizzards."));
    }

    private static void energy(ItemStack stack, List<Line> out) {
        int capacity = EnergyUtil.capacity(stack);
        int stored = EnergyUtil.stored(stack);
        int percent = capacity <= 0 ? 0 : Math.round(stored * 100.0F / capacity);
        out.add(
                Line.text(
                        CYAN,
                        "⚡ Energy: "
                                + grouped(stored)
                                + " / "
                                + grouped(capacity)
                                + " RF  •  "
                                + percent
                                + "%"));
    }

    private static void ability(List<Line> out, int color, String trigger, String name) {
        out.add(Line.text(color, "§l" + trigger + "  •  " + name));
    }

    private static void feature(List<Line> out, int color, String name) {
        out.add(Line.text(color, "§l" + name));
    }

    private static String grouped(int value) {
        return String.format("%,d", value);
    }

    private static String nearestFormatting(int rgb) {
        if (rgb == RED || rgb == RED_DARK) return TextFormatting.RED.toString();
        if (rgb == CYAN) return TextFormatting.AQUA.toString();
        if (rgb == GOLD || rgb == CANVAS || rgb == YELLOW) return TextFormatting.GOLD.toString();
        if (rgb == GREEN) return TextFormatting.GREEN.toString();
        if (rgb == WHITE) return TextFormatting.WHITE.toString();
        return TextFormatting.GRAY.toString();
    }
}
