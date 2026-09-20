package com.scapeandrun.frostbite.expedition;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.init.*;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.village.*;
import net.minecraft.world.World;

public final class EntitySeaKing extends EntityVillager {
    private MerchantRecipeList seaTrades;

    public EntitySeaKing(World world) {
        super(world);
        enablePersistence();
        setCustomNameTag("Amidias, the Sea King");
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public void onDeath(net.minecraft.util.DamageSource source) {
        if (!world.isRemote) ScourgeLoot.Progress.get(world).loseKing();
        super.onDeath(source);
    }

    @Override
    public MerchantRecipeList getRecipes(EntityPlayer player) {
        if (seaTrades == null) {
            seaTrades = new MerchantRecipeList();
            seaTrades.add(
                    new MerchantRecipe(
                            new ItemStack(Items.EMERALD, 2),
                            new ItemStack(ExpeditionContent.CORAL, 8)));
            seaTrades.add(
                    new MerchantRecipe(
                            new ItemStack(Items.EMERALD, 3),
                            new ItemStack(ExpeditionContent.PRISM_SHARD, 5)));
            seaTrades.add(
                    new MerchantRecipe(
                            new ItemStack(SeaContent.WHITE_PEARL),
                            new ItemStack(Items.EMERALD, 3)));
            seaTrades.add(
                    new MerchantRecipe(
                            new ItemStack(Items.EMERALD, 8),
                            new ItemStack(ExpeditionContent.OCEAN_CREST)));
        }
        return seaTrades;
    }

    @Override
    public boolean processInteract(EntityPlayer player, EnumHand hand) {
        if (player.isSneaking()) {
            if (!world.isRemote) {
                player.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 12000, 0));
                player.sendMessage(
                        new TextComponentString(
                                "Amidias: The sea will lend you its breath. Travel safely."));
            }
            return true;
        }
        return super.processInteract(player, hand);
    }

    @Override
    public void writeEntityToNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.writeEntityToNBT(tag);
        if (seaTrades != null) tag.setTag("SeaTrades", seaTrades.getRecipiesAsTags());
    }

    @Override
    public void readEntityFromNBT(net.minecraft.nbt.NBTTagCompound tag) {
        super.readEntityFromNBT(tag);
        if (tag.hasKey("SeaTrades", 10))
            seaTrades = new MerchantRecipeList(tag.getCompoundTag("SeaTrades"));
    }
}
