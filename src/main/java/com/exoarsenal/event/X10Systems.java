package com.exoarsenal.event;

import com.exoarsenal.item.EnergyUtil;
import com.exoarsenal.item.ItemX10Blade;
import com.exoarsenal.registry.ModContent;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityCreeper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.monster.EntityWitherSkeleton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class X10Systems {
    public static final int ANALYZER = 1;
    public static final int NIGHT = 2;
    public static final int WINGS = 4;
    public static final int JETPACK = 8;
    public static final int SWIM = 16;
    public static final int SPRINT = 32;
    public static final int ALL = ANALYZER | NIGHT | WINGS | JETPACK | SWIM | SPRINT;

    public static boolean hasSet(EntityLivingBase wearer) {
        boolean x10 =
                wearer != null
                        && wearer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                                == ModContent.X10_HELMET
                        && wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                                == ModContent.X10_CHESTPLATE
                        && wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                                == ModContent.X10_LEGGINGS
                        && wearer.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()
                                == ModContent.X10_BOOTS;
        return x10 || KXSystems.hasSet(wearer);
    }

    public static int mask(EntityLivingBase wearer) {
        ItemStack legs = wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (!systemLeggings(legs)) return 0;
        NBTTagCompound tag = tag(legs);
        if (!tag.hasKey("X10Systems")) tag.setInteger("X10Systems", ALL);
        return tag.getInteger("X10Systems");
    }

    public static boolean enabled(EntityLivingBase wearer, int system) {
        return (mask(wearer) & system) != 0;
    }

    public static void toggle(EntityPlayer player, int system) {
        ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (!systemLeggings(legs)) return;
        int current = mask(player);
        if (system == ALL) current = current == 0 ? ALL : 0;
        else current ^= system;
        tag(legs).setInteger("X10Systems", current);
    }

    public static void storeHeld(EntityPlayer player) {
        ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        ItemStack held = player.getHeldItemMainhand();
        if (!systemLeggings(legs) || held.isEmpty() || held == legs) return;
        NBTTagCompound tag = tag(legs);
        for (int i = 0; i < 5; i++)
            if (!tag.hasKey("Belt" + i, 10)) {
                NBTTagCompound stored = new NBTTagCompound();
                held.writeToNBT(stored);
                tag.setTag("Belt" + i, stored);
                player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, ItemStack.EMPTY);
                player.inventory.markDirty();
                return;
            }
    }

    public static void swapBelt(EntityPlayer player, int slot) {
        if (slot < 0 || slot >= 5) return;
        ItemStack legs = player.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (!systemLeggings(legs)) return;
        NBTTagCompound tag = tag(legs);
        ItemStack stored =
                tag.hasKey("Belt" + slot, 10)
                        ? new ItemStack(tag.getCompoundTag("Belt" + slot))
                        : ItemStack.EMPTY;
        ItemStack held = player.getHeldItemMainhand();
        if (held.isEmpty()) tag.removeTag("Belt" + slot);
        else {
            NBTTagCompound replacement = new NBTTagCompound();
            held.writeToNBT(replacement);
            tag.setTag("Belt" + slot, replacement);
        }
        player.setHeldItem(net.minecraft.util.EnumHand.MAIN_HAND, stored);
        player.inventory.markDirty();
    }

    public static ItemStack beltItem(EntityLivingBase wearer, int slot) {
        ItemStack legs = wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
        if (!systemLeggings(legs) || slot < 0 || slot >= 5) return ItemStack.EMPTY;
        NBTTagCompound tag = tag(legs);
        return tag.hasKey("Belt" + slot, 10)
                ? new ItemStack(tag.getCompoundTag("Belt" + slot))
                : ItemStack.EMPTY;
    }

    public static void jet(EntityPlayer player) {
        if (!hasSet(player)
                || !enabled(player, JETPACK)
                || player.onGround
                || !EnergyUtil.drainArmor(player, 70, false)) return;
        Vec3d look = player.getLookVec();
        player.motionY = Math.min(0.72D, Math.max(player.motionY + 0.17D, 0.18D));
        player.motionX += look.x * 0.035D;
        player.motionZ += look.z * 0.035D;
        player.fallDistance = 0.0F;
        player.velocityChanged = true;
    }

    @SubscribeEvent
    public void tick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END || player.world.isRemote || !hasSet(player)) return;
        if (enabled(player, NIGHT))
            player.addPotionEffect(new PotionEffect(MobEffects.NIGHT_VISION, 240, 0, true, false));
        if (enabled(player, SPRINT))
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 20, 1, true, false));
        if (enabled(player, WINGS)
                && !player.onGround
                && player.isSneaking()
                && player.motionY < -0.12D
                && EnergyUtil.drainArmor(player, 12, false)) {
            player.motionY = -0.12D;
            player.fallDistance = 0.0F;
            player.velocityChanged = true;
        }
        if (enabled(player, SWIM)
                && player.isInWater()
                && EnergyUtil.drainArmor(player, 5, false)) {
            player.motionX *= 1.075D;
            player.motionZ *= 1.075D;
            double horizontal = player.motionX * player.motionX + player.motionZ * player.motionZ;
            if (horizontal > 0.64D) {
                double scale = 0.8D / Math.sqrt(horizontal);
                player.motionX *= scale;
                player.motionZ *= scale;
            }
            player.velocityChanged = true;
        }
    }

    @SubscribeEvent
    public void harvestHead(LivingDeathEvent event) {
        if (!(event.getSource().getTrueSource() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getSource().getTrueSource();
        ItemStack blade = player.getHeldItemMainhand();
        if (!(blade.getItem() instanceof ItemX10Blade)
                || ItemX10Blade.getForm(blade) != ItemX10Blade.SCISSORS) return;
        int looting = EnchantmentHelper.getLootingModifier(player);
        if (player.getRNG().nextFloat() > Math.min(0.85F, 0.35F + looting * 0.15F)) return;
        ItemStack head = head(event.getEntityLiving());
        if (!head.isEmpty()) event.getEntityLiving().entityDropItem(head, 0.15F);
    }

    private static ItemStack head(EntityLivingBase entity) {
        if (entity instanceof EntityPlayer) {
            ItemStack stack = new ItemStack(Items.SKULL, 1, 3);
            NBTTagCompound tag = new NBTTagCompound();
            tag.setString("SkullOwner", ((EntityPlayer) entity).getName());
            stack.setTagCompound(tag);
            return stack;
        }
        if (entity instanceof EntityCreeper) return new ItemStack(Items.SKULL, 1, 4);
        if (entity instanceof EntityZombie) return new ItemStack(Items.SKULL, 1, 2);
        if (entity instanceof EntityWitherSkeleton) return new ItemStack(Items.SKULL, 1, 1);
        if (entity instanceof EntitySkeleton) return new ItemStack(Items.SKULL, 1, 0);
        return ItemStack.EMPTY;
    }

    private static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    private static boolean systemLeggings(ItemStack stack) {
        return stack.getItem() == ModContent.X10_LEGGINGS
                || stack.getItem() == ModContent.KX20_LEGGINGS;
    }
}
