package com.exoarsenal.expedition;

import net.minecraft.item.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import net.minecraft.util.*;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;

public final class DeepWeapon extends Item {
    public enum Kind {
        SWORD,
        WATER,
        SCYTHE,
        FLAME,
        FIRE_SWORD,
        GRASS_SWORD
    }

    public final Kind kind;

    public DeepWeapon(Kind kind) {
        this.kind = kind;
        setMaxStackSize(1);
    }

    private boolean sword() {
        return kind == Kind.SWORD || kind == Kind.FIRE_SWORD || kind == Kind.GRASS_SWORD;
    }

    @Override
    public com.google.common.collect.Multimap<String, AttributeModifier> getItemAttributeModifiers(
            EntityEquipmentSlot slot) {
        com.google.common.collect.Multimap<String, AttributeModifier> result =
                super.getItemAttributeModifiers(slot);
        if (sword() && slot == EntityEquipmentSlot.MAINHAND) {
            result.put(
                    SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER,
                            "Relic blade damage",
                            kind == Kind.FIRE_SWORD ? 8 : 6,
                            0));
            result.put(
                    SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(
                            ATTACK_SPEED_MODIFIER,
                            "Relic blade speed",
                            kind == Kind.SWORD ? -1.8 : -2.6,
                            0));
        }
        return result;
    }

    @Override
    public boolean hitEntity(
            ItemStack stack,
            net.minecraft.entity.EntityLivingBase target,
            net.minecraft.entity.EntityLivingBase attacker) {
        if (!target.world.isRemote) {
            if (kind == Kind.FIRE_SWORD) target.setFire(4);
            if (kind == Kind.GRASS_SWORD)
                target.addPotionEffect(
                        new net.minecraft.potion.PotionEffect(
                                net.minecraft.init.MobEffects.POISON, 80));
        }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack held = player.getHeldItem(hand);
        if (sword() || player.getCooldownTracker().hasCooldown(this))
            return new ActionResult<>(EnumActionResult.PASS, held);
        if (!world.isRemote) {
            if (!PrebossProgress.spend(player, kind == Kind.SCYTHE ? 14 : 10))
                return new ActionResult<>(EnumActionResult.FAIL, held);
            EntityRelicShot shot = new EntityRelicShot(world, player, kind);
            shot.shoot(
                    player,
                    player.rotationPitch,
                    player.rotationYaw,
                    0,
                    kind == Kind.SCYTHE ? .25F : .9F,
                    0);
            world.spawnEntity(shot);
            player.getCooldownTracker().setCooldown(this, kind == Kind.SCYTHE ? 28 : 20);
        }
        if (kind == Kind.FLAME) player.setActiveHand(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, held);
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return kind == Kind.FLAME ? 100 : 0;
    }

    @Override
    public void addInformation(
            ItemStack stack,
            World world,
            java.util.List<String> lines,
            net.minecraft.client.util.ITooltipFlag flag) {
        switch (kind) {
            case SWORD:
                lines.add("A swift blue blade.");
                break;
            case WATER:
                lines.add("Piercing water bolt. Ricochets up to three times.");
                lines.add("10 mana");
                break;
            case SCYTHE:
                lines.add("A slow-starting scythe that accelerates through enemies.");
                lines.add("14 mana");
                break;
            case FLAME:
                lines.add("Hold use to guide the flame with your aim.");
                lines.add("Release to send it forward. 10 mana");
                break;
        }
    }
}
