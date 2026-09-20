package com.exoarsenal.expedition;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;
import java.util.List;

public final class ExpeditionItem extends Item {
    public enum Kind {
        MATERIAL,
        CLOAK,
        CREST,
        SLICERS,
        BOW,
        SAND,
        STAR,
        SPEAR,
        HEAL,
        LORE,
        THANKS,
        CELL,
        SCHEMATIC,
        LOG,
        SEEKER,
        SCREWDRIVER,
        BLUNDERBUSS,
        PROSTHESIS,
        CONTROLLER,
        BATTERY,
        DRIVE,
        PACK,
        SCAFFOLD,
        LURE
    }

    public final Kind kind;

    public ExpeditionItem(Kind kind) {
        this.kind = kind;
        if (kind != Kind.MATERIAL && kind != Kind.CELL && kind != Kind.HEAL) setMaxStackSize(1);
    }

    public static boolean consume(EntityPlayer p, Item item, int count) {
        if (p.isCreative()) return true;
        int total = 0;
        for (ItemStack s : p.inventory.mainInventory)
            if (s.getItem() == item) total += s.getCount();
        if (total < count) return false;
        for (ItemStack s : p.inventory.mainInventory)
            if (s.getItem() == item) {
                int n = Math.min(count, s.getCount());
                s.shrink(n);
                count -= n;
                if (count == 0) break;
            }
        return true;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(World w, EntityPlayer p, EnumHand hand) {
        ItemStack stack = p.getHeldItem(hand);
        if (p.getCooldownTracker().hasCooldown(this))
            return new ActionResult<>(EnumActionResult.FAIL, stack);
        if (w.isRemote) return new ActionResult<>(EnumActionResult.SUCCESS, stack);
        int cooldown = 20;
        switch (kind) {
            case SLICERS:
                fire(p, EntityExpeditionShot.SLICER, 8, 1.1F, -8);
                fire(p, EntityExpeditionShot.SLICER, 8, 1.1F, 8);
                cooldown = 25;
                break;
            case BOW:
                if (!consume(p, Items.ARROW, 1)) return fail(stack);
                fire(p, EntityExpeditionShot.BARINADE, 7, 1.6F, 0);
                cooldown = 18;
                break;
            case SAND:
                fire(p, EntityExpeditionShot.SAND, 7, 1.7F, 0);
                p.addExhaustion(.15F);
                cooldown = 18;
                break;
            case SPEAR:
                fire(p, EntityExpeditionShot.SPEAR, 8, 1.35F, 0);
                cooldown = 20;
                break;
            case SCREWDRIVER:
                fire(p, EntityExpeditionShot.SCREW, 4, 1.2F, 0);
                cooldown = 12;
                break;
            case BLUNDERBUSS:
                if (!consume(p, ExpeditionContent.SCRAP, 1)) return fail(stack);
                for (int i = -2; i <= 2; i++) fire(p, EntityExpeditionShot.PELLET, 3, .9F, i * 4);
                cooldown = 28;
                break;
            case PROSTHESIS:
                fire(p, EntityExpeditionShot.PELLET, 4, 1.2F, 0);
                p.addExhaustion(.08F);
                cooldown = 10;
                break;
            case STAR:
            case CONTROLLER:
                {
                    List<EntityExpeditionMinion> pets =
                            w.getEntitiesWithinAABB(
                                    EntityExpeditionMinion.class,
                                    p.getEntityBoundingBox().grow(64),
                                    m -> p.getUniqueID().equals(m.owner()));
                    int variant = kind == Kind.STAR ? 0 : 1;
                    if (p.isSneaking()) {
                        for (EntityExpeditionMinion m : pets)
                            if (m.variant() == variant) m.toggleGuard();
                        break;
                    }
                    int max = PrebossProgress.minionLimit(p);
                    long n = pets.stream().filter(m -> m.variant() != 3).count();
                    if (n >= max) {
                        p.sendStatusMessage(
                                new TextComponentString(
                                        "Minion limit reached. Sneak-use to switch guard mode."),
                                true);
                        return fail(stack);
                    }
                    w.spawnEntity(new EntityExpeditionMinion(w, p, variant, (int) n));
                    break;
                }
            case HEAL:
                if (p.getHealth() >= p.getMaxHealth()) return fail(stack);
                p.heal(6);
                if (!p.isCreative()) stack.shrink(1);
                cooldown = 1200;
                break;
            case CELL:
                {
                    ItemStack other =
                            p.getHeldItem(
                                    hand == EnumHand.MAIN_HAND
                                            ? EnumHand.OFF_HAND
                                            : EnumHand.MAIN_HAND);
                    IEnergyStorage rf = other.getCapability(CapabilityEnergy.ENERGY, null);
                    if (rf == null || rf.receiveEnergy(5000, true) == 0) return fail(stack);
                    rf.receiveEnergy(5000, false);
                    if (!p.isCreative()) stack.shrink(1);
                    break;
                }
            case SEEKER:
                {
                    BlockPos at =
                            ExpeditionWorldGenerator.nearestSite(
                                    w, p.getPosition(), p.isSneaking());
                    if (at == null) {
                        p.sendStatusMessage(
                                new TextComponentString("No desert signal within 2,048 blocks."),
                                true);
                    } else
                        p.sendMessage(
                                new TextComponentString(
                                        (p.isSneaking() ? "Sunken Sea" : "Planetoid")
                                                + " signal: "
                                                + at.getX()
                                                + ", "
                                                + at.getY()
                                                + ", "
                                                + at.getZ()));
                    break;
                }
            case LURE:
                if (!consume(p, ExpeditionContent.CORE, 1)) return fail(stack);
                for (int i = 0; i < 4; i++) {
                    EntityWulfrum e =
                            i == 0
                                    ? new EntityWulfrum.Amplifier(w)
                                    : i == 1
                                            ? new EntityWulfrum.Rover(w)
                                            : new EntityWulfrum.Drone(w);
                    BlockPos at =
                            w.getTopSolidOrLiquidBlock(
                                    p.getPosition().add(i % 2 == 0 ? 6 : -6, 0, i < 2 ? 6 : -6));
                    e.setPosition(at.getX() + .5, at.getY(), at.getZ() + .5);
                    w.spawnEntity(e);
                }
                cooldown = 200;
                break;
            case LORE:
            case LOG:
            case SCHEMATIC:
            case THANKS:
                p.sendMessage(new TextComponentString(reading(stack)));
                break;
            default:
                return fail(stack);
        }
        p.getCooldownTracker().setCooldown(this, cooldown);
        p.swingArm(hand);
        w.playSound(
                null,
                p.posX,
                p.posY,
                p.posZ,
                SoundEvents.BLOCK_DISPENSER_DISPENSE,
                SoundCategory.PLAYERS,
                .45F,
                1.2F);
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    private ActionResult<ItemStack> fail(ItemStack s) {
        return new ActionResult<>(EnumActionResult.FAIL, s);
    }

    public static void fire(EntityPlayer p, int type, float damage, float speed, float yaw) {
        EntityExpeditionShot shot = new EntityExpeditionShot(p.world, p, type, damage);
        if (p.getHeldItemMainhand().getItem() instanceof SeaWeapon) {
            SeaWeapon.Kind kind = ((SeaWeapon) p.getHeldItemMainhand().getItem()).kind;
            if (kind == SeaWeapon.Kind.SPOUT || kind == SeaWeapon.Kind.SPARK)
                shot.setDamageClass(1);
        }
        shot.shoot(p, p.rotationPitch, p.rotationYaw + yaw, 0, speed, .35F);
        p.world.spawnEntity(shot);
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer p,
            World w,
            BlockPos pos,
            EnumHand h,
            EnumFacing face,
            float x,
            float y,
            float z) {
        if (kind != Kind.SCAFFOLD) return EnumActionResult.PASS;
        BlockPos at = pos.offset(face);
        if (!p.canPlayerEdit(at, face, p.getHeldItem(h))
                || !w.mayPlace(Blocks.OAK_FENCE, at, false, face, p)) return EnumActionResult.FAIL;
        if (!w.isRemote && consume(p, ExpeditionContent.SCRAP, 1))
            w.setBlockState(at, Blocks.OAK_FENCE.getDefaultState(), 3);
        return EnumActionResult.SUCCESS;
    }

    private String reading(ItemStack stack) {
        String id = getRegistryName().getResourcePath();
        if (kind == Kind.LORE)
            return "Field record: Desert Scourge. A marine predator adapted to a vanished sea. Crystals beneath the desert suggest that not all of its old habitat has dried out.";
        if (kind == Kind.THANKS)
            return "Thank you for exploring. Display this memento in an item frame.";
        if (kind == Kind.SCHEMATIC)
            return id.startsWith("sunken")
                    ? "Recovered schematic: a sealed underwater research module, pressure locks and prism-powered lighting. Combine circuitry and dubious plating to manufacture lab panels."
                    : "Recovered schematic: an orbital greenhouse, reinforced observation dome and a compact power-cell bank.";
        return id.contains("sunken")
                ? "Draedon — marine station. The prism formations sustain a stable submerged ecosystem. This facility isolates water samples behind independent pressure bulkheads. A second station remains in the planetoids above."
                : "Draedon — orbital station. The central greenhouse remains operational. Ore-rich satellites supply the workshop. The deep-desert station can be found using the seeking mechanism's secondary scan.";
    }

    @Override
    public void addInformation(ItemStack s, World w, List<String> out, ITooltipFlag f) {
        switch (kind) {
            case CLOAK:
                out.add("Accessory: taking a hit releases a defensive sand burst.");
                out.add("5 second cooldown.");
                break;
            case CREST:
                out.add("Accessory: breathe underwater and swim faster.");
                break;
            case BATTERY:
                out.add("Accessory: summoned minions deal 10% more damage.");
                break;
            case DRIVE:
                out.add("Accessory: absorbs up to 6 damage.");
                out.add("Recharges after 10 seconds without taking a hit.");
                break;
            case PACK:
                out.add("Accessory: hold sneak while falling to slow your descent.");
                break;
            case SLICERS:
                out.add("Throw paired returning sand blades.");
                break;
            case BOW:
                out.add("Fires a piercing electric arrow. Uses arrows.");
                break;
            case SAND:
                out.add("Launch sand that falls after impact, then bursts.");
                break;
            case SPEAR:
                out.add("Piercing spear. Each hit strengthens its next strike.");
                break;
            case STAR:
            case CONTROLLER:
                out.add("Summon a companion. Sneak-use switches guard mode.");
                break;
            case BLUNDERBUSS:
                out.add("Fires five pellets. Uses 1 Wulfrum Metal Scrap.");
                break;
            case PROSTHESIS:
                out.add("Fires compact energy pellets.");
                break;
            case SCREWDRIVER:
                out.add("Throw a short-range returning screwdriver.");
                break;
            case HEAL:
                out.add("Restores 3 hearts. 60 second cooldown.");
                break;
            case CELL:
                out.add("Use to transfer up to 5,000 RF to the other hand.");
                break;
            case SEEKER:
                out.add("Use: locate a planetoid lab.");
                out.add("Sneak-use: locate a Sunken Sea lab.");
                break;
            case LURE:
                out.add("Uses 1 Energy Core to attract four Wulfrum machines.");
                break;
            case SCAFFOLD:
                out.add("Places a wooden support using 1 scrap.");
                break;
            case LOG:
            case LORE:
            case SCHEMATIC:
            case THANKS:
                out.add("Right-click to read.");
                break;
            default:
                break;
        }
    }
}
