package com.scapeandrun.frostbite.event;

import com.scapeandrun.frostbite.item.EnergyUtil;
import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.UUID;

public class KXSystems {
    public static final int MUTATION_COST = 250000;
    private static final UUID FRAIL_ARMOR = UUID.fromString("60f4715c-d7e4-4937-b4c4-24ed7a862087");

    public static boolean hasSet(EntityLivingBase wearer) {
        return wearer != null
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem()
                        == ModContent.KX20_HELMET
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem()
                        == ModContent.KX20_CHESTPLATE
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem()
                        == ModContent.KX20_LEGGINGS
                && wearer.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem()
                        == ModContent.KX20_BOOTS;
    }

    private static NBTTagCompound state(EntityLivingBase wearer) {
        ItemStack chest = wearer.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (!chest.hasTagCompound()) chest.setTagCompound(new NBTTagCompound());
        return chest.getTagCompound();
    }

    public static boolean overload(EntityLivingBase wearer) {
        return hasSet(wearer)
                && state(wearer).getLong("KXOverloadUntil") > wearer.world.getTotalWorldTime();
    }

    public static void toggleOverload(EntityPlayer player) {
        if (!hasSet(player)) return;
        NBTTagCompound tag = state(player);
        long now = player.world.getTotalWorldTime();
        if (tag.getLong("KXOverloadUntil") > now) {
            tag.setLong("KXOverloadUntil", 0L);
            return;
        }
        tag.setLong("KXOverloadUntil", now + 1200L);
        tag.setLong("KXOverloadStart", now);
    }

    public static void selfDestruct(EntityPlayer player, boolean held) {
        if (!hasSet(player)) return;
        NBTTagCompound tag = state(player);
        if (held) {
            if (!tag.hasKey("KXSelfDestructStart"))
                tag.setLong("KXSelfDestructStart", player.world.getTotalWorldTime());
        } else tag.removeTag("KXSelfDestructStart");
    }

    public static void chainsaw(EntityPlayer player) {
        if (hasSet(player)
                && player.getHeldItemOffhand().isEmpty()
                && EnergyUtil.drainArmor(player, 320, false))
            state(player).setLong("KXChainsawParry", player.world.getTotalWorldTime() + 4L);
    }

    public static boolean beginEditor(EntityPlayer player, int entityId) {
        if (!hasSet(player)) return false;
        Entity entity = player.world.getEntityByID(entityId);
        if (!(entity instanceof EntityLiving)
                || entity.getDistanceSq(player) > 144.0D
                || !player.canEntityBeSeen(entity)) return false;
        endEditor(player);
        EntityLiving mob = (EntityLiving) entity;
        NBTTagCompound tag = state(player);
        tag.setInteger("KXEditorTarget", entityId);
        mob.getEntityData().setBoolean("KXEditorWasNoAI", mob.isAIDisabled());
        mob.setNoAI(true);
        return true;
    }

    public static void endEditor(EntityPlayer player) {
        ItemStack chest = player.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
        if (chest.isEmpty()
                || !chest.hasTagCompound()
                || !chest.getTagCompound().hasKey("KXEditorTarget")) return;
        NBTTagCompound tag = chest.getTagCompound();
        Entity e = player.world.getEntityByID(tag.getInteger("KXEditorTarget"));
        if (e instanceof EntityLiving)
            ((EntityLiving) e).setNoAI(e.getEntityData().getBoolean("KXEditorWasNoAI"));
        tag.removeTag("KXEditorTarget");
    }

    public static void applyMutation(EntityPlayer player, int mutation) {
        if (mutation < 0 || mutation >= Mutation.values().length || !hasSet(player)) return;
        NBTTagCompound tag = state(player);
        Entity e = player.world.getEntityByID(tag.getInteger("KXEditorTarget"));
        if (!(e instanceof EntityLiving)
                || e.getDistanceSq(player) > 144.0D
                || !EnergyUtil.drainArmor(player, MUTATION_COST, false)) return;
        long mask = e.getEntityData().getLong("KXGeneticMutations");
        mask ^= 1L << mutationBit(mutation);
        e.getEntityData().setLong("KXGeneticMutations", mask);
    }

    public static int mutationBit(int index) {
        return index >= 16 ? index + 1 : index;
    }

    @SubscribeEvent
    public void playerTick(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        if (event.phase != TickEvent.Phase.END) return;
        if (!hasSet(player)) {
            if (player.getEntityData().getBoolean("KXGrantedFlight")
                    && !player.capabilities.isCreativeMode) {
                player.capabilities.allowFlying = false;
                player.capabilities.isFlying = false;
                player.getEntityData().setBoolean("KXGrantedFlight", false);
                if (player instanceof EntityPlayerMP)
                    ((EntityPlayerMP) player).sendPlayerAbilities();
            }
            return;
        }
        if (player.world.isRemote) return;
        if (!player.capabilities.allowFlying) {
            player.capabilities.allowFlying = true;
            player.getEntityData().setBoolean("KXGrantedFlight", true);
            if (player instanceof EntityPlayerMP) ((EntityPlayerMP) player).sendPlayerAbilities();
        }
        player.capabilities.setFlySpeed(overload(player) ? 0.20F : 0.085F);
        if (player.capabilities.isFlying) {
            if (!EnergyUtil.drainArmor(player, overload(player) ? 520 : 180, false))
                player.capabilities.isFlying = false;
            player.fallDistance = 0;
        }
        NBTTagCompound tag = state(player);
        if (overload(player)) {
            int capacity = 0;
            for (EntityEquipmentSlot slot :
                    new EntityEquipmentSlot[] {
                        EntityEquipmentSlot.HEAD,
                        EntityEquipmentSlot.CHEST,
                        EntityEquipmentSlot.LEGS,
                        EntityEquipmentSlot.FEET
                    }) capacity += EnergyUtil.capacity(player.getItemStackFromSlot(slot));
            if (!EnergyUtil.drainArmor(player, Math.max(1, capacity / 2400), false))
                tag.setLong("KXOverloadUntil", 0L);
            player.addPotionEffect(new PotionEffect(MobEffects.SPEED, 10, 3, true, false));
            player.addPotionEffect(new PotionEffect(MobEffects.HASTE, 10, 3, true, false));
            player.addPotionEffect(new PotionEffect(MobEffects.JUMP_BOOST, 10, 2, true, false));
        }
        if (tag.hasKey("KXSelfDestructStart")
                && player.world.getTotalWorldTime() - tag.getLong("KXSelfDestructStart") >= 200L)
            detonate(player);
        if (tag.hasKey("KXEditorTarget")) {
            Entity e = player.world.getEntityByID(tag.getInteger("KXEditorTarget"));
            if (!(e instanceof EntityLiving) || e.getDistanceSq(player) > 144.0D) {
                endEditor(player);
            } else {
                e.motionX = e.motionY = e.motionZ = 0;
                e.velocityChanged = true;
            }
        }
    }

    @SubscribeEvent
    public void livingTick(
            net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase mob = event.getEntityLiving();
        if (mob.world.isRemote) return;
        long mask = mob.getEntityData().getLong("KXGeneticMutations");
        if (mask == 0) return;
        Mutation[] values = Mutation.values();
        for (int i = 0; i < values.length; i++)
            if ((mask & (1L << mutationBit(i))) != 0 && values[i].potion != null)
                mob.addPotionEffect(
                        new PotionEffect(values[i].potion, 40, values[i].amplifier, true, false));
        if ((mask & (1L << 12)) != 0
                && mob.world.isDaytime()
                && mob.world.canSeeSky(mob.getPosition())) mob.setFire(2);
        if ((mask & (1L << 17)) != 0 && mob.ticksExisted % 40 < 4) {
            mob.motionX = mob.motionZ = 0;
            mob.velocityChanged = true;
        }
        net.minecraft.entity.ai.attributes.IAttributeInstance armor =
                mob.getEntityAttribute(SharedMonsterAttributes.ARMOR);
        if (armor != null
                && ((mask & (1L << 10)) != 0) != (armor.getModifier(FRAIL_ARMOR) != null)) {
            if (armor.getModifier(FRAIL_ARMOR) != null) armor.removeModifier(FRAIL_ARMOR);
            if ((mask & (1L << 10)) != 0)
                armor.applyModifier(
                        new AttributeModifier(FRAIL_ARMOR, "KX brittle carapace", -8.0D, 0));
        }
    }

    @SubscribeEvent
    public void hurt(LivingHurtEvent event) {
        Entity source = event.getSource().getTrueSource();
        if (source instanceof EntityPlayer && overload((EntityPlayer) source))
            event.setAmount(event.getAmount() * 4.0F);
        if (event.getEntityLiving() instanceof EntityPlayer && overload(event.getEntityLiving()))
            event.setAmount(event.getAmount() * 0.25F);
        long mask = event.getEntityLiving().getEntityData().getLong("KXGeneticMutations");
        if ((mask & (1L << 14)) != 0) event.setAmount(event.getAmount() * 1.35F);
        if ((mask & (1L << 19)) != 0 && event.getSource().isMagicDamage())
            event.setAmount(event.getAmount() * 1.75F);
    }

    @SubscribeEvent
    public void parry(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayer)) return;
        EntityPlayer player = (EntityPlayer) event.getEntityLiving();
        if (!hasSet(player)
                || state(player).getLong("KXChainsawParry") < player.world.getTotalWorldTime())
            return;
        Entity direct = event.getSource().getImmediateSource();
        Entity attacker = event.getSource().getTrueSource();
        if (direct != null && direct != attacker) {
            direct.motionX *= -2.2D;
            direct.motionY *= -1.2D;
            direct.motionZ *= -2.2D;
            direct.velocityChanged = true;
        } else if (attacker != null) {
            Vec3d push =
                    attacker.getPositionVector().subtract(player.getPositionVector()).normalize();
            attacker.addVelocity(push.x * 1.8D, 0.55D, push.z * 1.8D);
        }
        event.setCanceled(true);
    }

    private static void detonate(EntityPlayer player) {
        WorldServer world = (WorldServer) player.world;
        AxisAlignedBB area = player.getEntityBoundingBox().grow(37.5D);
        for (EntityLivingBase living : world.getEntitiesWithinAABB(EntityLivingBase.class, area))
            living.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
        for (EntityEquipmentSlot slot :
                new EntityEquipmentSlot[] {
                    EntityEquipmentSlot.HEAD,
                    EntityEquipmentSlot.CHEST,
                    EntityEquipmentSlot.LEGS,
                    EntityEquipmentSlot.FEET
                }) player.setItemStackToSlot(slot, ItemStack.EMPTY);
        world.newExplosion(player, player.posX, player.posY, player.posZ, 38.0F, true, true);
        player.attackEntityFrom(DamageSource.OUT_OF_WORLD, Float.MAX_VALUE);
    }

    public enum Mutation {
        SLOWED_SYNAPSES("Slowed Synapses", MobEffects.SLOWNESS, 2),
        MUSCLE_ATROPHY("Muscle Atrophy", MobEffects.WEAKNESS, 2),
        BRITTLE_ENZYMES("Brittle Enzymes", MobEffects.MINING_FATIGUE, 2),
        OCULAR_COLLAPSE("Ocular Collapse", MobEffects.BLINDNESS, 0),
        VESTIBULAR_FAULT("Vestibular Fault", MobEffects.NAUSEA, 0),
        STARVED_METABOLISM("Starved Metabolism", MobEffects.HUNGER, 2),
        NECROTIC_BLOOD("Necrotic Blood", MobEffects.WITHER, 0),
        TOXIN_RETENTION("Toxin Retention", MobEffects.POISON, 1),
        LUMINOUS_TISSUE("Luminous Tissue", MobEffects.GLOWING, 0),
        BUOYANT_SACS("Buoyant Sacs", MobEffects.LEVITATION, 0),
        FRAIL_CARAPACE("Frail Carapace", null, 0),
        STUNTED_GROWTH("Stunted Growth", MobEffects.WEAKNESS, 1),
        PHOTOSENSITIVE_CELLS("Photosensitive Cells", null, 0),
        CRYO_LABILITY("Cryo-Lability", MobEffects.SLOWNESS, 1),
        HEMOPHILIA("Hemophilia", null, 0),
        UNSTABLE_GENOME("Unstable Genome", MobEffects.UNLUCK, 2),
        NEURAL_SEIZURES("Neural Seizures", null, 0),
        HOLLOW_BONES("Hollow Bones", MobEffects.LEVITATION, 0),
        RF_HYPERSENSITIVITY("RF Hypersensitivity", null, 0);
        public final String display;
        final Potion potion;
        final int amplifier;

        Mutation(String display, Potion potion, int amplifier) {
            this.display = display;
            this.potion = potion;
            this.amplifier = amplifier;
        }
    }
}
