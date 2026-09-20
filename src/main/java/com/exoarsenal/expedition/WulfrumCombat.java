package com.exoarsenal.expedition;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.EnergyUtil;
import com.exoarsenal.network.*;
import net.minecraft.entity.*;
import net.minecraft.entity.player.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import java.util.*;

@Mod.EventBusSubscriber(modid = ExoArsenal.MODID)
public final class WulfrumCombat {
    private static final UUID ARMOR = UUID.fromString("88082dcc-49c2-476f-ad79-37e2d7859129");
    private static final Map<UUID, State> STATES = new HashMap<>();

    private static final class State {
        int move = -1, age, combo, target = -1;
        long ready, grappleReady, slamReady;
        boolean android;
        ItemStack weapon = ItemStack.EMPTY;
    }

    public static boolean android(EntityPlayer p) {
        AccessoryInventory inv = AccessoryInventory.get(p);
        if (inv == null) return false;
        for (int i = 0; i < inv.getSlots(); i++)
            if (inv.getStackInSlot(i).getItem() == WulfrumArsenal.HEART) return true;
        return false;
    }

    public static void request(EntityPlayer p, int action) {
        if (p.world.isRemote || !p.isEntityAlive() || p.isSpectator() || action < 0 || action > 1)
            return;
        ItemStack held = p.getHeldItemMainhand();
        boolean robot = android(p) && held.isEmpty();
        if (!robot && !(held.getItem() instanceof WulfrumArsenal.Weapon)) return;
        State s = STATES.computeIfAbsent(p.getUniqueID(), id -> new State());
        long now = p.world.getTotalWorldTime();
        if (s.move >= 0 || now < s.ready) return;
        int kind = robot ? 4 : ((WulfrumArsenal.Weapon) held.getItem()).kind;
        if (robot && action == 1 && now < (p.isSneaking() ? s.slamReady : s.grappleReady)) return;
        int cost = kind == 0 ? 80 : kind == 1 ? 30 : kind == 2 ? 60 : 50;
        if (!robot
                && !p.isCreative()
                && (!EnergyUtil.drain(held, cost, true) || !EnergyUtil.drain(held, cost, false)))
            return;
        s.android = android(p);
        s.weapon = held;
        s.age = 0;
        s.target = -1;
        s.combo = (s.combo + 1) % 3;
        s.move =
                robot
                        ? (action == 0 ? 4 : p.isSneaking() ? 6 : 5)
                        : kind == 1
                                ? 7
                                : kind == 3 && action == 1
                                        ? 3
                                        : kind == 0
                                                ? (action == 1 ? 8 : 0)
                                                : kind == 2 ? (action == 1 ? 9 : 1) : 2;
        if (robot && s.move == 5) s.grappleReady = now + 80;
        if (robot && s.move == 6) {
            s.slamReady = now + 120;
            p.motionY = .65;
            p.velocityChanged = true;
        }
        s.ready = now + (s.move == 7 ? 4 : s.move == 1 ? 12 : s.move == 2 ? 12 : 20);
        p.swingArm(EnumHand.MAIN_HAND);
        sync(p, s);
    }

    private static boolean enemy(EntityPlayer p, EntityLivingBase e) {
        return e != p
                && e.isEntityAlive()
                && !p.isOnSameTeam(e)
                && (!(e instanceof EntityPlayer) || p.canAttackPlayer((EntityPlayer) e))
                && p.canEntityBeSeen(e);
    }

    private static EntityLivingBase aimed(EntityPlayer p, double reach) {
        Vec3d from = p.getPositionEyes(1), to = from.add(p.getLookVec().scale(reach));
        RayTraceResult wall = p.world.rayTraceBlocks(from, to, false, true, false);
        if (wall != null) to = wall.hitVec;
        EntityLivingBase best = null;
        double distance = from.distanceTo(to);
        for (EntityLivingBase e :
                p.world.getEntitiesWithinAABB(
                        EntityLivingBase.class, new AxisAlignedBB(from, to).grow(1))) {
            if (!enemy(p, e)) continue;
            RayTraceResult hit = e.getEntityBoundingBox().grow(.35).calculateIntercept(from, to);
            if (hit != null && from.distanceTo(hit.hitVec) < distance) {
                best = e;
                distance = from.distanceTo(hit.hitVec);
            }
        }
        return best;
    }

    private static void strike(EntityPlayer p, double reach, float damage, boolean wide) {
        if (!wide) {
            EntityLivingBase target = aimed(p, reach);
            if (target != null) target.attackEntityFrom(DamageSource.causePlayerDamage(p), damage);
        } else
            for (EntityLivingBase e :
                    p.world.getEntitiesWithinAABB(
                            EntityLivingBase.class, p.getEntityBoundingBox().grow(reach)))
                if (enemy(p, e)
                        && p.getDistance(e) <= reach
                        && p.getLookVec()
                                        .dotProduct(
                                                e.getPositionVector()
                                                        .addVector(0, e.height * .5, 0)
                                                        .subtract(p.getPositionEyes(1))
                                                        .normalize())
                                > .25)
                    e.attackEntityFrom(DamageSource.causePlayerDamage(p), damage);
        p.world.playSound(
                null,
                p.posX,
                p.posY,
                p.posZ,
                SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP,
                SoundCategory.PLAYERS,
                .7F,
                1.25F);
    }

    @SubscribeEvent
    public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.player.world.isRemote) return;
        EntityPlayer p = event.player;
        boolean robot = android(p);
        boolean armor =
                p.getEntityAttribute(SharedMonsterAttributes.ARMOR).getModifier(ARMOR) != null;
        if (robot && !armor)
            p.getEntityAttribute(SharedMonsterAttributes.ARMOR)
                    .applyModifier(new AttributeModifier(ARMOR, "Wulfrum android", 8, 0));
        if (!robot && armor)
            p.getEntityAttribute(SharedMonsterAttributes.ARMOR).removeModifier(ARMOR);
        State s = STATES.get(p.getUniqueID());
        if (s == null && !robot) return;
        if (s == null) {
            s = new State();
            STATES.put(p.getUniqueID(), s);
        }
        if (s.android != robot) {
            s.android = robot;
            if (!robot && s.move >= 4 && s.move <= 6) s.move = -1;
            sync(p, s);
        }
        if (!p.isEntityAlive()) {
            s.move = -1;
            s.android = false;
            sync(p, s);
            STATES.remove(p.getUniqueID());
            return;
        }
        if (s.move >= 0) {
            if (s.move < 4 || s.move >= 7) {
                if (p.getHeldItemMainhand() != s.weapon) {
                    s.move = -1;
                    sync(p, s);
                    return;
                }
            }
            int t = ++s.age;
            if (s.move == 0 && t == 7) strike(p, 4, 8, true);
            if (s.move == 1 && t == 4) strike(p, 3, 6, true);
            if (s.move == 2 && t == 4) strike(p, 3.5, 7, false);
            if (s.move == 8 && t == 7) strike(p, 5, 10, false);
            if (s.move == 9 && t == 6) {
                strike(p, 3, 9, true);
                EntityLivingBase target = aimed(p, 3);
                if (target != null) {
                    target.motionY = .35;
                    target.velocityChanged = true;
                }
            }
            if (s.move == 3 && t == 5)
                ExpeditionItem.fire(p, EntityExpeditionShot.PG_DAGGER, 9, 1.8F, 0);
            if (s.move == 7 && t == 1) {
                ExpeditionItem.fire(p, EntityExpeditionShot.PG_LASER, 5, 3.2F, 0);
                p.world.playSound(
                        null,
                        p.posX,
                        p.posY,
                        p.posZ,
                        SoundEvents.BLOCK_NOTE_HAT,
                        SoundCategory.PLAYERS,
                        .65F,
                        1.7F);
            }
            if (s.move == 4 && t == 5) strike(p, 3, s.combo == 2 ? 10 : 6, s.combo == 2);
            if (s.move == 5) {
                if (t == 8) {
                    EntityLivingBase e = aimed(p, 14);
                    if (e != null && e.width <= 3) s.target = e.getEntityId();
                }
                Entity e = p.world.getEntityByID(s.target);
                if (e instanceof EntityLivingBase
                        && enemy(p, (EntityLivingBase) e)
                        && p.getDistance(e) < 18) {
                    if (t >= 9 && t <= 20) {
                        Vec3d delta =
                                p.getPositionVector()
                                        .add(p.getLookVec().scale(1.7))
                                        .subtract(e.getPositionVector());
                        if (p.world.rayTraceBlocks(
                                        e.getPositionVector().addVector(0, .5, 0),
                                        e.getPositionVector()
                                                .add(delta.normalize())
                                                .addVector(0, .5, 0),
                                        false,
                                        true,
                                        false)
                                == null) {
                            Vec3d velocity = delta.scale(.22);
                            if (velocity.lengthVector() > 1.1)
                                velocity = velocity.normalize().scale(1.1);
                            e.motionX = velocity.x;
                            e.motionY = Math.max(-.3, Math.min(.3, velocity.y));
                            e.motionZ = velocity.z;
                            e.velocityChanged = true;
                        }
                    }
                    if (t == 22 && p.getDistance(e) < 4) {
                        e.attackEntityFrom(DamageSource.causePlayerDamage(p), 12);
                        e.motionY = -.5;
                        e.velocityChanged = true;
                        impact(p);
                    }
                }
            }
            if (s.move == 6) {
                p.fallDistance = 0;
                if (t >= 10) {
                    p.motionY = Math.min(p.motionY, -.9);
                    p.velocityChanged = true;
                }
                if (t > 10 && p.onGround) {
                    for (EntityLivingBase e :
                            p.world.getEntitiesWithinAABB(
                                    EntityLivingBase.class,
                                    p.getEntityBoundingBox().grow(4, 1.5, 4)))
                        if (enemy(p, e) && p.getDistance(e) < 4.5) {
                            e.attackEntityFrom(DamageSource.causePlayerDamage(p), 12);
                            e.motionY = .35;
                            e.velocityChanged = true;
                        }
                    impact(p);
                    s.move = -1;
                }
            }
            int end =
                    s.move == 7
                            ? 3
                            : s.move == 5
                                    ? 30
                                    : s.move == 6
                                            ? 45
                                            : s.move == 0
                                                    ? 19
                                                    : s.move == 1 || s.move == 2 ? 11 : 15;
            if (t >= end) s.move = -1;
            if (s.move < 0 || t % 4 == 0) sync(p, s);
        } else if (p.ticksExisted % 20 == 0) sync(p, s);
    }

    private static void impact(EntityPlayer p) {
        p.world.playSound(
                null,
                p.posX,
                p.posY,
                p.posZ,
                SoundEvents.ENTITY_IRONGOLEM_ATTACK,
                SoundCategory.PLAYERS,
                1,
                .6F);
        ((WorldServer) p.world)
                .spawnParticle(
                        EnumParticleTypes.BLOCK_DUST,
                        p.posX,
                        p.posY + .1,
                        p.posZ,
                        24,
                        1.5,
                        .1,
                        1.5,
                        .05,
                        net.minecraft.block.Block.getStateId(
                                p.world.getBlockState(p.getPosition().down())));
    }

    private static void sync(EntityPlayer p, State s) {
        PacketWulfrumState.send(p, s.android, s.move, s.age, s.combo, s.target);
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent e) {
        STATES.remove(e.player.getUniqueID());
    }
}
