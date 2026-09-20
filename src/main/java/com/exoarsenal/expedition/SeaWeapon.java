package com.exoarsenal.expedition;

import com.google.common.collect.Multimap;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;

public final class SeaWeapon extends Item {
    public enum Kind {
        MACE,
        SPEAR,
        BLOWGUN,
        SPOUT,
        CNIDARIAN,
        BOOMERANG,
        SHIELD,
        SPARK,
        PENDANT,
        PEARL
    }

    public final Kind kind;

    public SeaWeapon(Kind kind) {
        this.kind = kind;
        setMaxStackSize(1);
        if (kind == Kind.SHIELD) setMaxDamage(420);
    }

    @Override
    public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot slot) {
        Multimap<String, AttributeModifier> values = super.getItemAttributeModifiers(slot);
        if (slot == EntityEquipmentSlot.MAINHAND && (kind == Kind.MACE || kind == Kind.SPEAR)) {
            values.put(
                    SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER,
                            "Sea weapon damage",
                            kind == Kind.MACE ? 7 : 5,
                            0));
            values.put(
                    SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(
                            ATTACK_SPEED_MODIFIER,
                            "Sea weapon speed",
                            kind == Kind.MACE ? -3 : -2.6,
                            0));
        }
        return values;
    }

    @Override
    public boolean isShield(ItemStack stack, net.minecraft.entity.EntityLivingBase entity) {
        return kind == Kind.SHIELD;
    }

    @Override
    public EnumAction getItemUseAction(ItemStack stack) {
        return kind == Kind.SHIELD ? EnumAction.BLOCK : EnumAction.NONE;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return kind == Kind.SHIELD || kind == Kind.SPOUT ? 72000 : 0;
    }

    @Override
    public void onPlayerStoppedUsing(
            ItemStack stack,
            World world,
            net.minecraft.entity.EntityLivingBase user,
            int remaining) {
        if (kind != Kind.SPOUT || world.isRemote || !(user instanceof EntityPlayer)) return;
        EntityPlayer p = (EntityPlayer) user;
        if (p.getCooldownTracker().hasCooldown(this)) return;
        float charge = Math.min(1, (getMaxItemUseDuration(stack) - remaining) / 25F),
                spread = 8 - 6 * charge;
        for (int i = -2; i <= 2; i++)
            ExpeditionItem.fire(p, EntityExpeditionShot.CORAL, 3, 1.1F, i * spread);
        p.addExhaustion(.2F);
        p.getCooldownTracker().setCooldown(this, 18);
        p.swingArm(p.getActiveHand());
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (kind == Kind.SHIELD) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (kind == Kind.PENDANT
                || kind == Kind.PEARL
                || player.getCooldownTracker().hasCooldown(this))
            return new ActionResult<>(EnumActionResult.PASS, stack);
        if (kind == Kind.SPOUT) {
            player.setActiveHand(hand);
            return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        }
        if (!world.isRemote) {
            int cooldown = 20;
            switch (kind) {
                case MACE:
                    ExpeditionItem.fire(player, EntityExpeditionShot.URCHIN, 7, .65F, 0);
                    cooldown = 32;
                    break;
                case SPEAR:
                    ExpeditionItem.fire(player, EntityExpeditionShot.WATER, 6, 1.7F, 0);
                    cooldown = 22;
                    break;
                case BLOWGUN:
                    for (int i = -1; i <= 1; i++)
                        ExpeditionItem.fire(player, EntityExpeditionShot.BUBBLE, 3, 1.8F, i * 2);
                    cooldown = 25;
                    break;
                case SPOUT:
                    for (int i = -2; i <= 2; i++)
                        ExpeditionItem.fire(player, EntityExpeditionShot.CORAL, 3, 1.1F, i * 8);
                    cooldown = 28;
                    player.addExhaustion(.2F);
                    break;
                case BOOMERANG:
                    ExpeditionItem.fire(player, EntityExpeditionShot.FISHBONE, 6, 1.2F, 0);
                    cooldown = 24;
                    break;
                case SPARK:
                    ExpeditionItem.fire(player, EntityExpeditionShot.WATER, 5, 1.5F, 0);
                    cooldown = 24;
                    break;
                case CNIDARIAN:
                    List<EntityExpeditionMinion> pets =
                            world.getEntitiesWithinAABB(
                                    EntityExpeditionMinion.class,
                                    player.getEntityBoundingBox().grow(64),
                                    m ->
                                            m.variant() != 3
                                                    && player.getUniqueID().equals(m.owner()));
                    int limit = PrebossProgress.minionLimit(player);
                    if (pets.size() >= limit)
                        return new ActionResult<>(EnumActionResult.FAIL, stack);
                    world.spawnEntity(new EntityExpeditionMinion(world, player, 2, pets.size()));
                    break;
                default:
                    break;
            }
            player.getCooldownTracker().setCooldown(this, cooldown);
            world.playSound(
                    null,
                    player.posX,
                    player.posY,
                    player.posZ,
                    net.minecraft.init.SoundEvents.ENTITY_PLAYER_SPLASH,
                    SoundCategory.PLAYERS,
                    .4F,
                    1.4F);
        }
        player.swingArm(hand);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        switch (kind) {
            case MACE:
                text.add("A heavy coral mace. Use to throw a venomous urchin.");
                break;
            case SPEAR:
                text.add("Use to drive a piercing jet of water forward.");
                break;
            case BLOWGUN:
                text.add("Fires a tight stream of high-pressure bubbles.");
                text.add("Requires no ammunition.");
                break;
            case SPOUT:
                text.add("Release to fire five short-range exploding coral spikes.");
                text.add("Hold to tighten the spread.");
                break;
            case CNIDARIAN:
                text.add("Summons a jellyfish that fires venomous darts.");
                break;
            case BOOMERANG:
                text.add("A returning fishbone blade.");
                break;
            case SHIELD:
                text.add("Hold in a hand and use to block.");
                text.add("Accessory + full Victide set: faster movement and regeneration.");
                break;
            case SPARK:
                text.add("Use to release a piercing water blast.");
                break;
            case PENDANT:
                text.add("Accessory: taking damage releases a burst of water.");
                break;
            case PEARL:
                text.add("Accessory: improved underwater regeneration.");
                break;
        }
    }
}
