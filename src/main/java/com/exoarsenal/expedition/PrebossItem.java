package com.exoarsenal.expedition;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraft.world.World;
import net.minecraft.client.util.ITooltipFlag;
import java.util.List;

public final class PrebossItem extends Item {
    public enum Kind {
        MATERIAL,
        STAR,
        MANA_CRYSTAL,
        LIFE_CRYSTAL,
        MANA_POTION,
        WOOD_BOOMERANG,
        ENCHANTED_BOOMERANG,
        KNIFE,
        CRYSTALLINE,
        SPARKING,
        FROSTING,
        FROST_BOLT,
        STORM_SPEAR,
        THUNDER_ZAPPER,
        STORMJAW,
        FLINX,
        DIAMOND,
        EMERALD,
        REGEN_BAND,
        STARPOWER_BAND,
        JAVELIN
    }

    public final Kind kind;

    public PrebossItem(Kind kind) {
        this.kind = kind;
        if (kind.ordinal() > Kind.MANA_POTION.ordinal()) setMaxStackSize(1);
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer player,
            World world,
            net.minecraft.util.math.BlockPos pos,
            EnumHand hand,
            EnumFacing face,
            float hitX,
            float hitY,
            float hitZ) {
        if (this != PrebossContent.SHIVERTHORN || face != EnumFacing.UP)
            return EnumActionResult.PASS;
        net.minecraft.util.math.BlockPos target =
                world.getBlockState(pos).getBlock().isReplaceable(world, pos) ? pos : pos.up();
        if (!player.canPlayerEdit(target, face, player.getHeldItem(hand))
                || !PrebossContent.SHIVERTHORN_PLANT.canPlaceBlockAt(world, target))
            return EnumActionResult.FAIL;
        if (!world.isRemote) {
            if (!world.setBlockState(target, PrebossContent.SHIVERTHORN_PLANT.getDefaultState(), 3))
                return EnumActionResult.FAIL;
            if (!player.isCreative()) player.getHeldItem(hand).shrink(1);
        }
        return EnumActionResult.SUCCESS;
    }

    @Override
    public com.google.common.collect.Multimap<
                    String, net.minecraft.entity.ai.attributes.AttributeModifier>
            getItemAttributeModifiers(net.minecraft.inventory.EntityEquipmentSlot slot) {
        com.google.common.collect.Multimap<
                        String, net.minecraft.entity.ai.attributes.AttributeModifier>
                values = super.getItemAttributeModifiers(slot);
        if (slot == net.minecraft.inventory.EntityEquipmentSlot.MAINHAND
                && (kind == Kind.STORM_SPEAR || kind == Kind.KNIFE)) {
            values.put(
                    net.minecraft.entity.SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new net.minecraft.entity.ai.attributes.AttributeModifier(
                            ATTACK_DAMAGE_MODIFIER,
                            "Starter weapon damage",
                            kind == Kind.STORM_SPEAR ? 4 : 2,
                            0));
            values.put(
                    net.minecraft.entity.SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new net.minecraft.entity.ai.attributes.AttributeModifier(
                            ATTACK_SPEED_MODIFIER,
                            "Starter weapon speed",
                            kind == Kind.STORM_SPEAR ? -2.7 : -2,
                            0));
        }
        return values;
    }

    public int manaCost() {
        switch (kind) {
            case SPARKING:
            case FROSTING:
                return 2;
            case FROST_BOLT:
                return 8;
            case THUNDER_ZAPPER:
                return 6;
            case DIAMOND:
                return 8;
            case EMERALD:
                return 6;
            case STORMJAW:
            case FLINX:
                return 10;
            default:
                return 0;
        }
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (kind == Kind.MATERIAL
                || kind == Kind.STAR
                || kind == Kind.REGEN_BAND
                || kind == Kind.STARPOWER_BAND) return result(stack, false);
        if (player.getCooldownTracker().hasCooldown(this)) return result(stack, false);
        if (world.isRemote) return result(stack, true);
        int cooldown = 20;
        if (kind == Kind.MANA_CRYSTAL || kind == Kind.LIFE_CRYSTAL) {
            boolean success =
                    kind == Kind.MANA_CRYSTAL
                            ? PrebossProgress.manaCrystal(player)
                            : PrebossProgress.lifeCrystal(player);
            if (!success) return result(stack, false);
            if (!player.isCreative()) stack.shrink(1);
        } else if (kind == Kind.MANA_POTION) {
            net.minecraft.nbt.NBTTagCompound data = PrebossProgress.data(player);
            if (data.getInteger("Mana") >= PrebossProgress.maximum(player))
                return result(stack, false);
            data.setInteger(
                    "Mana",
                    Math.min(PrebossProgress.maximum(player), data.getInteger("Mana") + 50));
            data.setInteger("ManaSickness", 100);
            if (!player.isCreative()) stack.shrink(1);
            PrebossProgress.sync(player);
            cooldown = 100;
        } else if (kind == Kind.STORMJAW || kind == Kind.FLINX) {
            java.util.List<EntityExpeditionMinion> pets =
                    world.getEntitiesWithinAABB(
                            EntityExpeditionMinion.class,
                            player.getEntityBoundingBox().grow(64),
                            m -> m.variant() != 3 && player.getUniqueID().equals(m.owner()));
            if (player.isSneaking()) {
                for (EntityExpeditionMinion pet : pets) pet.setDead();
                return result(stack, true);
            }
            int max = PrebossProgress.minionLimit(player);
            if (pets.size() >= max || !PrebossProgress.spend(player, manaCost()))
                return result(stack, false);
            world.spawnEntity(
                    new EntityExpeditionMinion(
                            world, player, kind == Kind.STORMJAW ? 4 : 5, pets.size()));
            cooldown = 30;
        } else {
            if (!PrebossProgress.spend(player, manaCost())) return result(stack, false);
            EntityPrebossShot shot = new EntityPrebossShot(world, player, kind);
            if (kind == Kind.CRYSTALLINE) {
                boolean ready = player.getEntityData().getInteger("ExoArsenalRogueFocus") >= 60;
                shot.setEmpowered(ready);
                player.getEntityData().setInteger("ExoArsenalRogueFocus", 0);
            }
            shot.shoot(
                    player,
                    player.rotationPitch,
                    player.rotationYaw,
                    0,
                    kind == Kind.THUNDER_ZAPPER ? 1.9F : kind == Kind.FROST_BOLT ? .7F : 1.2F,
                    .2F);
            world.spawnEntity(shot);
            cooldown =
                    kind == Kind.KNIFE
                            ? 12
                            : kind == Kind.CRYSTALLINE
                                    ? 18
                                    : kind == Kind.FROST_BOLT
                                            ? 30
                                            : kind == Kind.THUNDER_ZAPPER ? 17 : 20;
        }
        player.swingArm(hand);
        player.getCooldownTracker().setCooldown(this, cooldown);
        world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                net.minecraft.init.SoundEvents.ENTITY_SNOWBALL_THROW,
                SoundCategory.PLAYERS,
                .45F,
                1.1F);
        return result(stack, true);
    }

    private static ActionResult<ItemStack> result(ItemStack stack, boolean success) {
        return new ActionResult<>(
                success ? EnumActionResult.SUCCESS : EnumActionResult.FAIL, stack);
    }

    @Override
    public boolean onEntityItemUpdate(net.minecraft.entity.item.EntityItem entity) {
        if (kind == Kind.STAR
                && !entity.world.isRemote
                && entity.world.provider.getDimension() == 0
                && entity.world.isDaytime()) {
            entity.setDead();
            return true;
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, ITooltipFlag flag) {
        switch (kind) {
            case STAR:
                text.add("Gather at night. Five stars make a Mana Crystal.");
                break;
            case MANA_CRYSTAL:
                text.add("Permanently adds 20 mana. Maximum: 9 upgrades.");
                break;
            case LIFE_CRYSTAL:
                text.add("Permanently adds 1 heart. Maximum: 15 upgrades.");
                break;
            case MANA_POTION:
                text.add("Restores 50 mana. Briefly reduces spell damage by 25%.");
                break;
            case CRYSTALLINE:
                text.add("Splits into three smaller blades during flight.");
                text.add(
                        "Hold still for 3 seconds: the next throw splits again on flight and shatters on impact.");
                break;
            case FROST_BOLT:
                text.add("A heavy frost orb. Bounces twice and bursts on impact.");
                break;
            case WOOD_BOOMERANG:
            case ENCHANTED_BOOMERANG:
                text.add("Returns to your hand after a short flight.");
                break;
            case STORMJAW:
                text.add("Summons a baby stormlion that shocks nearby enemies.");
                text.add("Sneak-use dismisses your summoned companions.");
                break;
            case FLINX:
                text.add("Summons a hopping snow flinx.");
                text.add("Sneak-use dismisses your summoned companions.");
                break;
            case SPARKING:
                text.add("A short, curving spark. May ignite its target.");
                break;
            case FROSTING:
                text.add("A curving ice spark that chills its target.");
                break;
            case THUNDER_ZAPPER:
                text.add("A fast, zigzagging electrical bolt.");
                break;
            case DIAMOND:
            case EMERALD:
                text.add("Fires a piercing gemstone bolt.");
                break;
            case STORM_SPEAR:
                text.add("Launches an electric spearhead.");
                break;
            case KNIFE:
                text.add("A reusable throwing knife.");
                break;
            case REGEN_BAND:
                text.add("Accessory: slowly regenerate after avoiding damage.");
                break;
            case STARPOWER_BAND:
                text.add("Accessory: +20 maximum mana.");
                break;
            default:
                if (this == PrebossContent.SHIVERTHORN)
                    text.add("Plant on soil or snow blocks. Use a blooming plant to harvest it.");
                break;
        }
        if (manaCost() > 0) text.add("Mana cost: " + manaCost());
    }
}
