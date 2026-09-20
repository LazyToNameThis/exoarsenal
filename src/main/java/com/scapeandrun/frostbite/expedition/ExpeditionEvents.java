package com.scapeandrun.frostbite.expedition;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class ExpeditionEvents {
    private static final UUID BASTION = UUID.fromString("31db1a5a-0dfe-401b-8ae1-c149f01f5f2d");

    public static boolean active(EntityPlayer p) {
        ItemStack hat = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        return WulfrumArmor.full(p)
                && hat.hasTagCompound()
                && hat.getTagCompound().getLong("BastionUntil") > p.world.getTotalWorldTime();
    }

    public static void toggle(EntityPlayer p) {
        if (!WulfrumArmor.full(p)) return;
        ItemStack hat = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
        if (!hat.hasTagCompound()) hat.setTagCompound(new NBTTagCompound());
        long now = p.world.getTotalWorldTime();
        if (active(p)) {
            hat.getTagCompound().setLong("BastionUntil", 0);
            p.getEntityData().setLong("BastionReady", now + 400);
            return;
        }
        if (now < p.getEntityData().getLong("BastionReady")
                || !ExpeditionItem.consume(p, ExpeditionContent.SCRAP, 1)) return;
        hat.getTagCompound().setLong("BastionUntil", now + 600);
        p.getEntityData().setLong("BastionReady", now + 1000);
    }

    public static void fire(EntityPlayer p) {
        long now = p.world.getTotalWorldTime();
        if (!active(p) || now < p.getEntityData().getLong("BastionShot")) return;
        p.getEntityData().setLong("BastionShot", now + 12);
        p.getEntityData().setInteger("BastionBurst", 3);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent e) {
        if (e.phase != TickEvent.Phase.END || e.player.world.isRemote) return;
        EntityPlayer p = e.player;
        long now = p.world.getTotalWorldTime();
        boolean active = active(p);
        boolean modified =
                p.getEntityAttribute(SharedMonsterAttributes.ARMOR).getModifier(BASTION) != null;
        if (active && !modified)
            p.getEntityAttribute(SharedMonsterAttributes.ARMOR)
                    .applyModifier(new AttributeModifier(BASTION, "Wulfrum Bastion", 12, 0));
        else if (!active && modified)
            p.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(BASTION);
        int burst = p.getEntityData().getInteger("BastionBurst");
        if (active && burst > 0 && now % 3 == 0) {
            p.getEntityData().setInteger("BastionBurst", burst - 1);
            ExpeditionItem.fire(p, EntityExpeditionShot.PELLET, 5, 1.35F, 0);
        }
        if (!active) p.getEntityData().setInteger("BastionBurst", 0);
        if (AccessoryInventory.has(p, ExpeditionContent.OCEAN_CREST)) {
            p.setAir(300);
            if (p.isInWater()) {
                p.addPotionEffect(new PotionEffect(MobEffects.WATER_BREATHING, 40, 0, true, false));
                if (p.moveForward > 0) {
                    Vec3d look = p.getLookVec();
                    p.addVelocity(look.x * .012, look.y * .012, look.z * .012);
                }
            }
        }
        ItemStack drive = AccessoryInventory.equipped(p, ExpeditionContent.ROVER_DRIVE);
        if (!drive.isEmpty() && now - p.getEntityData().getLong("RoverHurt") > 200) {
            if (!drive.hasTagCompound()) drive.setTagCompound(new NBTTagCompound());
            drive.getTagCompound().setFloat("Shield", 6);
        }
        if (AccessoryInventory.has(p, ExpeditionContent.ACROBATICS)
                && p.isSneaking()
                && p.motionY < -.12) {
            p.motionY = -.12;
            p.fallDistance = 0;
            p.velocityChanged = true;
        }
        if (now % 20 == 0) {
            boolean guard = false;
            for (EntityExpeditionMinion m :
                    p.world.getEntitiesWithinAABB(
                            EntityExpeditionMinion.class, p.getEntityBoundingBox().grow(4)))
                if (m.variant() == 0 && m.guard() && p.getUniqueID().equals(m.owner()))
                    guard = true;
            if (guard)
                p.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, 25, 0, true, false));
        }
    }

    @SubscribeEvent
    public static void hurt(LivingHurtEvent e) {
        if (!(e.getEntityLiving() instanceof EntityPlayer) || e.getEntityLiving().world.isRemote)
            return;
        EntityPlayer p = (EntityPlayer) e.getEntityLiving();
        long now = p.world.getTotalWorldTime();
        if (active(p)) {
            e.setAmount(e.getAmount() * .9F);
            ItemStack hat = p.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
            hat.getTagCompound()
                    .setLong(
                            "BastionUntil",
                            Math.max(now, hat.getTagCompound().getLong("BastionUntil") - 40));
        }
        ItemStack drive = AccessoryInventory.equipped(p, ExpeditionContent.ROVER_DRIVE);
        if (!drive.isEmpty()) {
            p.getEntityData().setLong("RoverHurt", now);
            float shield = drive.hasTagCompound() ? drive.getTagCompound().getFloat("Shield") : 0;
            float n = Math.min(shield, e.getAmount());
            if (n > 0) {
                drive.getTagCompound().setFloat("Shield", shield - n);
                e.setAmount(e.getAmount() - n);
            }
        }
        if (AccessoryInventory.has(p, ExpeditionContent.SAND_CLOAK)
                && now >= p.getEntityData().getLong("SandCloakReady")) {
            p.getEntityData().setLong("SandCloakReady", now + 100);
            for (int i = 0; i < 8; i++)
                ExpeditionItem.fire(p, EntityExpeditionShot.SAND, 4, .6F, i * 45);
        }
    }

    @SubscribeEvent
    public static void use(PlayerInteractEvent.RightClickItem e) {
        if (active(e.getEntityPlayer())) {
            if (!e.getWorld().isRemote) fire(e.getEntityPlayer());
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock e) {
        if (active(e.getEntityPlayer())) {
            if (!e.getWorld().isRemote) fire(e.getEntityPlayer());
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void attack(AttackEntityEvent e) {
        if (active(e.getEntityPlayer())) {
            if (!e.getEntityPlayer().world.isRemote) fire(e.getEntityPlayer());
            e.setCanceled(true);
        }
    }
}
