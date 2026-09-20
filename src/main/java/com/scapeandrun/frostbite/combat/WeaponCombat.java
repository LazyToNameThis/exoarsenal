package com.scapeandrun.frostbite.combat;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.entity.EntityBrawler;
import com.scapeandrun.frostbite.entity.EntityExcavator;
import com.scapeandrun.frostbite.entity.EntityX20Scout;
import com.scapeandrun.frostbite.event.RmorEventHandler;
import com.scapeandrun.frostbite.item.EnergyUtil;
import com.scapeandrun.frostbite.item.ItemRBlade;
import com.scapeandrun.frostbite.network.PacketCombatState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class WeaponCombat {
    public static final int LIGHT = 1, HEAVY = 2, DODGE = 3, PARRY = 4, STAGGER = 5;
    private static final UUID STANCE_SPEED_MODIFIER =
            UUID.fromString("d420e274-8791-4808-a4ae-9b35bdad8049");
    private static final Map<UUID, State> STATES = new HashMap<>();
    private static final int BOSS_PARRY_SEARCH_RADIUS = 32;
    private static final int UNARMED_PARRY_SEARCH_RADIUS = 24;
    private static final int SAW_ATTACK_RF = 180;
    private static final int STATE_SYNC_INTERVAL = 5;
    private static final int DODGE_DRIVE_TICKS = 5;
    private static final double DODGE_START_SPEED = 0.72;
    private static final double DODGE_DRIVE_SPEED = 0.60;
    private static final double STANCE_SPEED_PENALTY = -0.65;
    private static final double MINIMUM_PARRY_ALIGNMENT = 0.25;

    private WeaponCombat() {}

    private static final class State {
        int action, actionTick, combo, regenDelayTicks, bufferedAction, dimension;
        long lastActionEndTick;
        float stamina = CombatRules.MAX_STAMINA, lastSyncedStamina = -1, attackDamage;
        WeaponDiscipline discipline = WeaponDiscipline.SWORD;
        ItemStack weapon = ItemStack.EMPTY;
        Vec3d direction = Vec3d.ZERO;
    }

    public static boolean busy(EntityPlayer player) {
        State state = STATES.get(player.getUniqueID());
        return state != null && state.action != 0;
    }

    public static boolean parryReady(EntityPlayer player) {
        State state = STATES.get(player.getUniqueID());
        return state != null && CombatRules.parryActive(state.action, state.actionTick, false);
    }

    public static boolean dodgeReady(EntityPlayer player) {
        State state = STATES.get(player.getUniqueID());
        return state != null && CombatRules.dodgeActive(state.action, state.actionTick);
    }

    public static void request(EntityPlayerMP player, int action, float forward, float strafe) {
        if (action == PARRY
                && !player.isDead
                && !player.isSpectator()
                && tryBossEntranceParry(player)) return;
        if (player.isDead || player.isSpectator() || player.isRiding() || player.isHandActive())
            return;
        if (RmorEventHandler.isDefenseForm(player)) return;
        WeaponDiscipline discipline = WeaponDiscipline.of(player.getHeldItemMainhand());
        if (discipline == null
                && action == PARRY
                && !player.world
                        .getEntitiesWithinAABB(
                                EntityBrawler.class,
                                player.getEntityBoundingBox().grow(UNARMED_PARRY_SEARCH_RADIUS))
                        .isEmpty()) discipline = WeaponDiscipline.SWORD;
        if (discipline == null) return;
        State state = STATES.computeIfAbsent(player.getUniqueID(), id -> new State());
        if (state.action != 0) {

            if ((action == LIGHT || action == HEAVY)
                    && (state.action == LIGHT || state.action == HEAVY)
                    && state.actionTick
                            >= state.discipline.duration(state.action == HEAVY)
                                    - CombatRules.INPUT_BUFFER_TICKS) state.bufferedAction = action;
            return;
        }
        if (action < DODGE
                && player.getHeldItemMainhand().getItem() instanceof ItemRBlade
                && (!ItemRBlade.isActive(player.getHeldItemMainhand())
                        || ItemRBlade.isSpinning(player.getHeldItemMainhand()))) return;
        if (action == DODGE && (!player.onGround || player.capabilities.isFlying)) return;
        int cost = CombatRules.staminaCost(action, discipline);
        if (state.stamina < cost) return;
        ItemStack weapon = player.getHeldItemMainhand();
        float damage =
                (float)
                        player.getEntityAttribute(SharedMonsterAttributes.ATTACK_DAMAGE)
                                .getAttributeValue();
        if (action == LIGHT || action == HEAVY) {
            int rf =
                    weapon.getItem() instanceof ItemRBlade
                            ? ItemRBlade.ATTACK_COST
                            : discipline == WeaponDiscipline.SAW ? SAW_ATTACK_RF : 0;
            if (rf > 0 && !EnergyUtil.drain(weapon, rf * (action == HEAVY ? 2 : 1), false)) return;
        }
        state.stamina -= cost;
        state.regenDelayTicks = CombatRules.REGEN_DELAY_TICKS;
        state.action = action;
        state.actionTick = 0;
        state.discipline = discipline;
        state.weapon = weapon;
        state.dimension = player.dimension;
        state.attackDamage = damage;
        state.combo =
                CombatRules.nextCombo(
                        player.world.getTotalWorldTime() - state.lastActionEndTick, state.combo);
        state.direction = player.getLookVec();
        player.setSprinting(false);
        if (action == DODGE) {
            double forwardInput = Math.max(-1, Math.min(1, forward));
            double strafeInput = Math.max(-1, Math.min(1, strafe));
            if (Math.abs(forwardInput) + Math.abs(strafeInput) < 0.1) forwardInput = -1;
            double yaw = Math.toRadians(player.rotationYaw);
            state.direction =
                    new Vec3d(
                                    -Math.sin(yaw) * forwardInput + Math.cos(yaw) * strafeInput,
                                    0,
                                    Math.cos(yaw) * forwardInput + Math.sin(yaw) * strafeInput)
                            .normalize();
            player.motionX = state.direction.x * DODGE_START_SPEED;
            player.motionZ = state.direction.z * DODGE_START_SPEED;
            player.velocityChanged = true;
        } else {
            player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .removeModifier(STANCE_SPEED_MODIFIER);
            player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                    .applyModifier(
                            new AttributeModifier(
                                    STANCE_SPEED_MODIFIER,
                                    "Committed weapon stance",
                                    STANCE_SPEED_PENALTY,
                                    2));
        }
        sync(player, state);
    }

    private static boolean tryBossEntranceParry(EntityPlayerMP player) {
        for (EntityBrawler boss :
                player.world.getEntitiesWithinAABB(
                        EntityBrawler.class,
                        player.getEntityBoundingBox().grow(BOSS_PARRY_SEARCH_RADIUS))) {
            if (boss.tryIntroParry(player)) return true;
        }
        for (EntityExcavator boss :
                player.world.getEntitiesWithinAABB(
                        EntityExcavator.class,
                        player.getEntityBoundingBox().grow(BOSS_PARRY_SEARCH_RADIUS))) {
            if (boss.tryTutorialParry(player)) return true;
        }
        return false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void attack(AttackEntityEvent event) {
        if (WeaponDiscipline.of(event.getEntityPlayer().getHeldItemMainhand()) == null) return;
        event.setCanceled(true);
        if (event.getEntityPlayer() instanceof EntityPlayerMP)
            request((EntityPlayerMP) event.getEntityPlayer(), LIGHT, 0, 0);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void use(PlayerInteractEvent.RightClickItem event) {
        if (!event.getWorld().isRemote && busy(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void block(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getWorld().isRemote && busy(event.getEntityPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.player;
        State state = STATES.get(player.getUniqueID());
        if (state == null) return;
        if (!player.isEntityAlive() || state.dimension != player.dimension) {
            clear(player);
            return;
        }
        if (state.regenDelayTicks > 0) state.regenDelayTicks--;
        else state.stamina = CombatRules.regenerate(state.stamina);
        if (state.action != 0) {
            state.actionTick++;
            if (state.action == DODGE && state.actionTick <= DODGE_DRIVE_TICKS) {
                player.motionX = state.direction.x * DODGE_DRIVE_SPEED;
                player.motionZ = state.direction.z * DODGE_DRIVE_SPEED;
                player.velocityChanged = true;
            }
            boolean heavy = state.action == HEAVY;
            if ((state.action == LIGHT || heavy)
                    && state.actionTick == state.discipline.contact(heavy)
                    && player.getHeldItemMainhand() == state.weapon
                    && WeaponDiscipline.of(state.weapon) == state.discipline)
                MeleeAttackResolver.resolve(
                        player,
                        state.discipline,
                        state.weapon,
                        state.direction,
                        state.attackDamage,
                        heavy,
                        WeaponCombat::stagger);
            int duration = CombatRules.duration(state.action, state.discipline);
            if (state.actionTick >= duration) {
                int next = state.bufferedAction;
                state.bufferedAction = 0;
                state.action = 0;
                state.lastActionEndTick = player.world.getTotalWorldTime();
                player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                        .removeModifier(STANCE_SPEED_MODIFIER);
                sync(player, state);
                if (next != 0) request(player, next, 0, 0);
            }
        }
        if (player.ticksExisted % STATE_SYNC_INTERVAL == 0
                && (state.action != 0 || state.stamina != state.lastSyncedStamina))
            sync(player, state);
    }

    private static void stagger(EntityPlayerMP player) {
        State state = STATES.computeIfAbsent(player.getUniqueID(), id -> new State());
        state.action = STAGGER;
        state.actionTick = 0;
        state.bufferedAction = 0;
        state.dimension = player.dimension;
        sync(player, state);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void defend(LivingAttackEvent event) {
        if (!(event.getEntityLiving() instanceof EntityPlayerMP)) return;
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityLiving();
        State state = STATES.get(player.getUniqueID());
        if (state == null) return;
        DamageSource source = event.getSource();
        Entity attacker = source.getTrueSource();
        if (source.isUnblockable()
                || source.isExplosion()
                || source.isFireDamage()
                || attacker == null) return;
        if (CombatRules.dodgeActive(state.action, state.actionTick)) {
            event.setCanceled(true);
            return;
        }
        boolean brawler = attacker instanceof EntityBrawler;
        if (!CombatRules.parryActive(state.action, state.actionTick, brawler) || attacker == player)
            return;
        if (attacker instanceof EntityBrawler && !((EntityBrawler) attacker).canParry()) return;
        Vec3d attackDirection =
                (brawler ? ((EntityBrawler) attacker).parryOrigin() : attacker.getPositionVector())
                        .subtract(player.getPositionEyes(1))
                        .normalize();
        boolean opening =
                attacker instanceof EntityExcavator
                        && ((EntityExcavator) attacker).introTick() == -1;
        if (!opening && player.getLookVec().dotProduct(attackDirection) < MINIMUM_PARRY_ALIGNMENT)
            return;
        event.setCanceled(true);
        state.stamina = CombatRules.rewardParry(state.stamina);
        state.regenDelayTicks = CombatRules.PARRY_REGEN_DELAY_TICKS;
        if (attacker instanceof EntityX20Scout) ((EntityX20Scout) attacker).onExpertParry();
        else if (attacker instanceof EntityExcavator)
            ((EntityExcavator) attacker).onExpertParry(player);
        else if (attacker instanceof EntityBrawler) ((EntityBrawler) attacker).parried(player);
        else if (attacker instanceof EntityPlayerMP) stagger((EntityPlayerMP) attacker);
        else if (attacker instanceof EntityLivingBase
                && ((EntityLivingBase) attacker).isNonBoss()) {
            ((EntityLivingBase) attacker)
                    .knockBack(player, 0.8F, -attackDirection.x, -attackDirection.z);
            ((EntityLivingBase) attacker)
                    .addPotionEffect(
                            new net.minecraft.potion.PotionEffect(
                                    net.minecraft.init.MobEffects.SLOWNESS, 20, 4));
        }
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.ITEM_SHIELD_BLOCK,
                SoundCategory.PLAYERS,
                1,
                1.4F);
        sync(player, state);
    }

    private static void sync(EntityPlayerMP player, State state) {
        PacketCombatState.send(
                player,
                state.action,
                state.discipline.ordinal(),
                state.actionTick,
                state.combo,
                state.stamina);
        state.lastSyncedStamina = state.stamina;
    }

    private static void clear(EntityPlayer player) {
        STATES.remove(player.getUniqueID());
        player.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED)
                .removeModifier(STANCE_SPEED_MODIFIER);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.player);
    }

    @SubscribeEvent
    public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        clear(event.player);
    }

    @SubscribeEvent
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        clear(event.player);
    }
}
