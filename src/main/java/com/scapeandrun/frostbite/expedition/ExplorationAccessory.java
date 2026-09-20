package com.scapeandrun.frostbite.expedition;

import com.google.common.collect.Multimap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import java.util.List;
import java.util.UUID;

public final class ExplorationAccessory extends Item {
    public enum Kind {
        HERMES,
        AGLET,
        ANKLET,
        SHACKLE,
        SKULL,
        HORSESHOE,
        CLAWS,
        CLOUD,
        BLIZZARD,
        SANDSTORM
    }

    private static final UUID SPEED = UUID.fromString("23d9fcb5-23da-4c69-b2cb-4dd70c0f8e73");
    private static final UUID ARMOR = UUID.fromString("14c795ea-d12d-4611-a777-dc031d0083db");
    private static final UUID ATTACK = UUID.fromString("7a583aa5-156e-440b-8664-462205e27797");
    public final Kind kind;

    public ExplorationAccessory(Kind kind) {
        this.kind = kind;
        setMaxStackSize(1);
    }

    public int jumpTier() {
        return kind == Kind.CLOUD ? 1 : kind == Kind.BLIZZARD ? 2 : kind == Kind.SANDSTORM ? 3 : 0;
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> attributes = super.getItemAttributeModifiers(slot);
        if (slot != EntityEquipmentSlot.OFFHAND) return attributes;
        if (kind == Kind.AGLET || kind == Kind.ANKLET)
            attributes.put(
                    SharedMonsterAttributes.MOVEMENT_SPEED.getName(),
                    new AttributeModifier(
                            SPEED, "Exploration movement", kind == Kind.AGLET ? .05 : .10, 2));
        if (kind == Kind.SHACKLE || kind == Kind.SKULL)
            attributes.put(
                    SharedMonsterAttributes.ARMOR.getName(),
                    new AttributeModifier(ARMOR, "Exploration armor", 1, 0));
        if (kind == Kind.CLAWS)
            attributes.put(
                    SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK, "Feral claws", .12, 2));
        return attributes;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        text.add("Equip in an accessory slot or your offhand.");
        switch (kind) {
            case HERMES:
                text.add("Build up running speed while sprinting: up to +60%.");
                text.add("Stopping or hitting a wall breaks your stride.");
                break;
            case AGLET:
                text.add("+5% movement speed.");
                break;
            case ANKLET:
                text.add("+10% movement speed.");
                break;
            case SHACKLE:
                text.add("+1 armor.");
                break;
            case SKULL:
                text.add("+1 armor. Protects against hot blocks, not lava or fire.");
                break;
            case HORSESHOE:
                text.add("Negates fall damage.");
                break;
            case CLAWS:
                text.add("+12% melee attack speed.");
                break;
            case CLOUD:
                text.add("Press jump again in midair for a second jump.");
                break;
            case BLIZZARD:
                text.add("A higher second jump, carried by a burst of snow.");
                text.add("Press jump again in midair.");
                break;
            case SANDSTORM:
                text.add("A powerful second jump, carried by swirling sand.");
                text.add("Press jump again in midair.");
                break;
        }
    }
}
