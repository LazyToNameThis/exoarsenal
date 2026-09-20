package com.scapeandrun.frostbite.item;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.scapeandrun.frostbite.client.ClientEquipmentHandler;
import com.scapeandrun.frostbite.network.PacketRToolEffect;
import com.scapeandrun.frostbite.schematic.SchematicLibrary;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Enchantments;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

public class ItemRTool extends Item implements IAnimatable {
    public static final int DRILL = 0;
    public static final int SAW = 1;
    public static final int BUILD = 2;
    public static final int SCYTHE = 3;
    public static final int SINGLE = 0;
    public static final int AREA = 1;
    public static final int VEIN = 2;
    public static final int CAPACITY = 500000;
    public static final int MAX_SCHEMATIC_BLOCKS = 3000;
    private static final UUID ATTACK_DAMAGE =
            UUID.fromString("930d70a7-9be8-4f9d-b4f6-dfb1c6422701");
    private static final UUID ATTACK_SPEED =
            UUID.fromString("42e3981b-4fb6-4c5b-a1d5-7c77d8f3aa0f");
    private final AnimationFactory factory = new AnimationFactory(this);

    public ItemRTool() {
        setMaxStackSize(1);
    }

    public static NBTTagCompound tag(ItemStack stack) {
        if (!stack.hasTagCompound()) stack.setTagCompound(new NBTTagCompound());
        return stack.getTagCompound();
    }

    public static int getForm(ItemStack stack) {
        int forms = !stack.isEmpty() && stack.getItem() instanceof ItemKXMultitool ? 4 : 3;
        return stack.isEmpty() ? DRILL : Math.floorMod(tag(stack).getInteger("RToolForm"), forms);
    }

    public static int getMode(ItemStack stack) {
        return stack.isEmpty() ? SINGLE : Math.floorMod(tag(stack).getInteger("RToolMode"), 3);
    }

    public static void cycleForm(ItemStack stack) {
        NBTTagCompound nbt = tag(stack);
        int forms = stack.getItem() instanceof ItemKXMultitool ? 4 : 3;
        nbt.setInteger("RToolForm", (getForm(stack) + 1) % forms);
        resetUse(stack);
    }

    public static void cycleMode(ItemStack stack) {
        if (getForm(stack) != DRILL) return;
        tag(stack).setInteger("RToolMode", (getMode(stack) + 1) % 3);
        resetUse(stack);
    }

    public static String formName(ItemStack stack) {
        switch (getForm(stack)) {
            case SAW:
                return "Saw";
            case BUILD:
                return "Build";
            case SCYTHE:
                return "Scythe";
            default:
                return "Drill";
        }
    }

    public static String modeName(ItemStack stack) {
        switch (getMode(stack)) {
            case AREA:
                return "3×3";
            case VEIN:
                return "Vein";
            default:
                return "Single Block";
        }
    }

    public static void stopUse(ItemStack stack) {
        if (stack.getItem() instanceof ItemRTool) resetUse(stack);
    }

    private static void resetUse(ItemStack stack) {
        NBTTagCompound nbt = tag(stack);
        nbt.setInteger("UseTicks", 0);
        nbt.setFloat("BreakProgress", 0.0F);
        nbt.removeTag("TargetX");
        nbt.removeTag("TargetY");
        nbt.removeTag("TargetZ");
    }

    public static void usePulse(EntityPlayer player) {
        if (com.scapeandrun.frostbite.combat.WeaponCombat.busy(player)) return;
        ItemStack stack = player.getHeldItemMainhand();
        if (!(stack.getItem() instanceof ItemRTool)) stack = player.getHeldItemOffhand();
        if (!(stack.getItem() instanceof ItemRTool) || player.world.isRemote) return;
        NBTTagCompound pulseState = tag(stack);
        long now = player.world.getTotalWorldTime();
        if (pulseState.hasKey("LastServerUsePulse")
                && pulseState.getLong("LastServerUsePulse") == now) return;
        pulseState.setLong("LastServerUsePulse", now);
        if (EnergyUtil.stored(stack) <= 0) {
            resetUse(stack);
            return;
        }
        if (getForm(stack) == DRILL) drillPulse(player, stack);
        else if (getForm(stack) == SAW) sawPulse(player, stack);
        else if (getForm(stack) == SCYTHE && stack.getItem() instanceof ItemKXMultitool)
            scythePulse(player, stack);
    }

    private static void scythePulse(EntityPlayer player, ItemStack tool) {
        long now = player.world.getTotalWorldTime();
        NBTTagCompound nbt = tag(tool);
        if (now - nbt.getLong("ScythePulse") < 5L || !EnergyUtil.drain(tool, 900, false)) return;
        nbt.setLong("ScythePulse", now);
        RayTraceResult hit = player.rayTrace(10.0D, 1.0F);
        BlockPos center =
                hit != null && hit.typeOfHit == RayTraceResult.Type.BLOCK
                        ? hit.getBlockPos()
                        : player.getPosition();
        int harvested = 0;
        for (BlockPos pos :
                BlockPos.getAllInBoxMutable(center.add(-3, -1, -3), center.add(3, 2, 3))) {
            IBlockState state = player.world.getBlockState(pos);
            if (!(state.getBlock() instanceof net.minecraft.block.BlockCrops)) continue;
            net.minecraft.block.BlockCrops crop = (net.minecraft.block.BlockCrops) state.getBlock();
            if (!crop.isMaxAge(state)) continue;
            TileEntity tile = player.world.getTileEntity(pos);
            BlockEvent.BreakEvent event =
                    new BlockEvent.BreakEvent(player.world, pos, state, player);
            if (MinecraftForge.EVENT_BUS.post(event)) continue;
            ItemStack fortuneTool = tool.copy();
            fortuneTool.addEnchantment(Enchantments.FORTUNE, 3);
            crop.harvestBlock(player.world, player, pos, state, tile, fortuneTool);
            player.world.setBlockState(pos, crop.withAge(0), 3);
            if (++harvested >= 49) break;
        }
        net.minecraft.util.math.AxisAlignedBB sheepArea =
                new net.minecraft.util.math.AxisAlignedBB(center).grow(5.0D);
        for (net.minecraft.entity.passive.EntitySheep sheep :
                player.world.getEntitiesWithinAABB(
                        net.minecraft.entity.passive.EntitySheep.class, sheepArea)) {
            if (!sheep.isEntityAlive() || sheep.getSheared()) continue;
            java.util.List<ItemStack> drops =
                    sheep.onSheared(tool, player.world, sheep.getPosition(), 3);
            for (ItemStack drop : drops) sheep.entityDropItem(drop, 1.0F);
            if (++harvested >= 56) break;
        }
        if (harvested > 0) {
            player.world.playSound(
                    null,
                    center,
                    SoundEvents.ENTITY_SHEEP_SHEAR,
                    SoundCategory.PLAYERS,
                    0.8F,
                    1.35F);
            PacketRToolEffect.send(
                    player,
                    PacketRToolEffect.KX_SCYTHE,
                    Collections.singletonList(new Vec3d(center).addVector(0.5D, 0.5D, 0.5D)));
        }
    }

    private static void drillPulse(EntityPlayer player, ItemStack tool) {
        RayTraceResult hit = player.rayTrace(24.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            resetUse(tool);
            return;
        }
        BlockPos target = hit.getBlockPos();
        IBlockState state = player.world.getBlockState(target);
        float hardness = state.getBlockHardness(player.world, target);
        if (hardness < 0.0F
                || state.getMaterial() == Material.AIR
                || player.world.getTileEntity(target) != null) {
            resetUse(tool);
            return;
        }
        NBTTagCompound nbt = tag(tool);
        if (!sameTarget(nbt, target)) {
            setTarget(nbt, target);
            nbt.setFloat("BreakProgress", 0.0F);
            nbt.setInteger("UseTicks", 0);
        }
        int mode = getMode(tool);
        float speed = tool.getItem() instanceof ItemX10Multitool ? 32.0F : 18.0F;
        float slowdown = mode == AREA ? 1.85F : mode == VEIN ? 2.25F : 1.0F;
        float required = Math.max(1.0F, hardness * 30.0F * slowdown / speed);
        if (!EnergyUtil.drain(tool, 14 + mode * 4, false)) return;
        nbt.setInteger("UseTicks", nbt.getInteger("UseTicks") + 1);
        float progress = nbt.getFloat("BreakProgress") + 1.0F;
        nbt.setFloat("BreakProgress", progress);
        PacketRToolEffect.send(
                player, PacketRToolEffect.HELIX, Collections.singletonList(center(target)));
        if (progress < required) return;

        List<BlockPos> targets;
        if (mode == AREA) targets = areaTargets(target, hit.sideHit);
        else if (mode == VEIN) targets = veinTargets(player.world, target, state);
        else targets = Collections.singletonList(target);
        int cost = mode == VEIN ? 220 : mode == AREA ? 190 : 160;
        int fortune =
                mode == VEIN
                        ? Math.max(
                                1,
                                EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool))
                        : 0;
        for (BlockPos pos : targets) {
            if (!EnergyUtil.drain(tool, cost, true)) break;
            if (breakBlock(player, tool, pos, fortune)) EnergyUtil.drain(tool, cost, false);
        }
        nbt.setFloat("BreakProgress", 0.0F);
        player.world.playSound(
                null,
                player.posX,
                player.posY,
                player.posZ,
                SoundEvents.BLOCK_REDSTONE_TORCH_BURNOUT,
                SoundCategory.PLAYERS,
                0.45F,
                1.65F);
    }

    private static void sawPulse(EntityPlayer player, ItemStack tool) {
        NBTTagCompound nbt = tag(tool);
        int useTicks = nbt.getInteger("UseTicks") + 1;
        nbt.setInteger("UseTicks", useTicks);
        EntityLivingBase contact = sawContact(player, 7.0D);
        boolean x10 = tool.getItem() instanceof ItemX10Multitool;
        if (contact != null) {
            int damageInterval = x10 ? 2 : 3;
            if (useTicks % damageInterval == 0 && EnergyUtil.drain(tool, x10 ? 110 : 90, false)) {
                float damage =
                        (x10 ? 4.5F : 3.0F)
                                + Math.min(x10 ? 8.0F : 6.0F, useTicks / (x10 ? 12.0F : 18.0F));
                player.getEntityData().setBoolean("WastelandEnergySweep", true);
                try {
                    if (contact.attackEntityFrom(
                            net.minecraft.util.DamageSource.causePlayerDamage(player)
                                    .setFireDamage(),
                            damage)) {
                        contact.setFire(2);
                    }
                } finally {
                    player.getEntityData().setBoolean("WastelandEnergySweep", false);
                }
                PacketRToolEffect.send(
                        player,
                        PacketRToolEffect.SAW,
                        Collections.singletonList(
                                contact.getPositionVector()
                                        .addVector(0.0D, contact.height * 0.55D, 0.0D)));
                if (player.world instanceof WorldServer) {
                    ((WorldServer) player.world)
                            .spawnParticle(
                                    net.minecraft.util.EnumParticleTypes.FIREWORKS_SPARK,
                                    contact.posX,
                                    contact.posY + contact.height * 0.55D,
                                    contact.posZ,
                                    9,
                                    0.28D,
                                    0.32D,
                                    0.28D,
                                    0.07D);
                }
            }
            return;
        }
        RayTraceResult hit = player.rayTrace(7.0D, 1.0F);
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) {
            resetUse(tool);
            return;
        }
        BlockPos target = hit.getBlockPos();
        IBlockState state = player.world.getBlockState(target);
        if (!isWood(player.world, target, state) || player.world.getTileEntity(target) != null) {
            resetUse(tool);
            return;
        }
        if (!sameTarget(nbt, target)) {
            setTarget(nbt, target);
            nbt.setFloat("BreakProgress", 0.0F);
            nbt.setInteger("UseTicks", 0);
            useTicks = 1;
        }
        float acceleration = 0.55F + Math.min(2.7F, useTicks / 22.0F);
        if (x10) acceleration *= 1.65F;
        float required =
                Math.max(
                        1.0F,
                        state.getBlockHardness(player.world, target)
                                * 30.0F
                                / (8.0F * acceleration));
        if (!EnergyUtil.drain(tool, 18, false)) return;
        float progress = nbt.getFloat("BreakProgress") + 1.0F;
        nbt.setFloat("BreakProgress", progress);
        PacketRToolEffect.send(
                player, PacketRToolEffect.SAW, Collections.singletonList(center(target)));
        if (progress < required) return;
        List<BlockPos> tree = treeTargets(player.world, target);
        for (BlockPos pos : tree) {
            if (!EnergyUtil.drain(tool, 95, true)) break;
            if (breakBlock(player, tool, pos, 0)) EnergyUtil.drain(tool, 95, false);
        }
        nbt.setFloat("BreakProgress", 0.0F);
        player.world.playSound(
                null,
                target,
                SoundEvents.BLOCK_WOOD_BREAK,
                SoundCategory.BLOCKS,
                0.9F,
                1.1F + Math.min(0.65F, useTicks / 100.0F));
    }

    @Nullable
    private static EntityLivingBase sawContact(EntityPlayer player, double range) {
        Vec3d eye = player.getPositionEyes(1.0F);
        Vec3d look = player.getLookVec().normalize();
        EntityLivingBase best = null;
        double bestDistance = range * range;
        for (EntityLivingBase target :
                player.world.getEntitiesWithinAABB(
                        EntityLivingBase.class, player.getEntityBoundingBox().grow(range))) {
            if (target == player || !target.isEntityAlive() || !player.canEntityBeSeen(target))
                continue;
            Vec3d center = target.getPositionVector().addVector(0.0D, target.height * 0.55D, 0.0D);
            Vec3d offset = center.subtract(eye);
            double distance = offset.lengthSquared();
            if (distance > bestDistance
                    || distance < 0.04D
                    || offset.normalize().dotProduct(look) < 0.91D) continue;
            best = target;
            bestDistance = distance;
        }
        return best;
    }

    private static boolean sameTarget(NBTTagCompound nbt, BlockPos pos) {
        return nbt.hasKey("TargetX")
                && nbt.getInteger("TargetX") == pos.getX()
                && nbt.getInteger("TargetY") == pos.getY()
                && nbt.getInteger("TargetZ") == pos.getZ();
    }

    private static void setTarget(NBTTagCompound nbt, BlockPos pos) {
        nbt.setInteger("TargetX", pos.getX());
        nbt.setInteger("TargetY", pos.getY());
        nbt.setInteger("TargetZ", pos.getZ());
    }

    private static List<BlockPos> areaTargets(BlockPos center, EnumFacing side) {
        List<BlockPos> result = new ArrayList<>(9);
        for (int a = -1; a <= 1; a++)
            for (int b = -1; b <= 1; b++) {
                if (side.getAxis() == EnumFacing.Axis.Y) result.add(center.add(a, 0, b));
                else if (side.getAxis() == EnumFacing.Axis.X) result.add(center.add(0, a, b));
                else result.add(center.add(a, b, 0));
            }
        return result;
    }

    private static List<BlockPos> veinTargets(World world, BlockPos start, IBlockState match) {
        if (!isOre(match)) return Collections.singletonList(start);
        List<BlockPos> result = new ArrayList<>();
        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        open.add(start);
        seen.add(start);
        while (!open.isEmpty() && result.size() < 256) {
            BlockPos pos = open.remove();
            IBlockState state = world.getBlockState(pos);
            if (state.getBlock() != match.getBlock()
                    || state.getBlock().getMetaFromState(state)
                            != match.getBlock().getMetaFromState(match)) continue;
            result.add(pos);
            for (EnumFacing face : EnumFacing.values()) {
                BlockPos next = pos.offset(face);
                if (seen.add(next)) open.add(next);
            }
        }
        return result;
    }

    private static List<BlockPos> treeTargets(World world, BlockPos start) {
        List<BlockPos> result = new ArrayList<>();
        Queue<BlockPos> open = new ArrayDeque<>();
        Set<BlockPos> seen = new HashSet<>();
        open.add(start);
        seen.add(start);
        while (!open.isEmpty() && result.size() < 256) {
            BlockPos pos = open.remove();
            IBlockState state = world.getBlockState(pos);
            if (!isTreePart(world, pos, state)) continue;
            result.add(pos);
            for (int x = -1; x <= 1; x++)
                for (int y = -1; y <= 1; y++)
                    for (int z = -1; z <= 1; z++) {
                        if (x == 0 && y == 0 && z == 0) continue;
                        BlockPos next = pos.add(x, y, z);
                        if (seen.add(next)) open.add(next);
                    }
        }
        return result;
    }

    private static boolean isOre(IBlockState state) {
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item != null) {
            ItemStack stack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
            for (int id : OreDictionary.getOreIDs(stack))
                if (OreDictionary.getOreName(id).startsWith("ore")) return true;
        }
        ResourceLocation id = state.getBlock().getRegistryName();
        return id != null && id.getResourcePath().toLowerCase().contains("ore");
    }

    private static boolean isWood(World world, BlockPos pos, IBlockState state) {
        if (state.getBlock().isWood(world, pos)) return true;
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == null) return false;
        ItemStack stack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
        for (int id : OreDictionary.getOreIDs(stack))
            if ("logWood".equals(OreDictionary.getOreName(id))) return true;
        return false;
    }

    private static boolean isTreePart(World world, BlockPos pos, IBlockState state) {
        if (isWood(world, pos, state) || state.getBlock().isLeaves(state, world, pos)) return true;
        Item item = Item.getItemFromBlock(state.getBlock());
        if (item == null) return false;
        ItemStack stack = new ItemStack(item, 1, state.getBlock().getMetaFromState(state));
        for (int id : OreDictionary.getOreIDs(stack)) {
            String name = OreDictionary.getOreName(id);
            if ("treeLeaves".equals(name) || "listAllleaf".equals(name)) return true;
        }
        return false;
    }

    private static boolean breakBlock(
            EntityPlayer player, ItemStack tool, BlockPos pos, int fortune) {
        World world = player.world;
        IBlockState state = world.getBlockState(pos);
        Block block = state.getBlock();
        if (state.getMaterial() == Material.AIR
                || state.getBlockHardness(world, pos) < 0.0F
                || world.getTileEntity(pos) != null) return false;
        BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(world, pos, state, player);
        if (MinecraftForge.EVENT_BUS.post(event)) return false;
        TileEntity tile = world.getTileEntity(pos);
        ItemStack harvestTool = tool;
        if (fortune > EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, tool)) {
            harvestTool = tool.copy();
            harvestTool.addEnchantment(Enchantments.FORTUNE, fortune);
        }
        if (!block.removedByPlayer(state, world, pos, player, true)) return false;
        block.onBlockDestroyedByPlayer(world, pos, state);
        if (!player.capabilities.isCreativeMode) {
            block.harvestBlock(world, player, pos, state, tile, harvestTool);
            if (event.getExpToDrop() > 0)
                block.dropXpOnBlockBreak(world, pos, event.getExpToDrop());
        }
        world.playEvent(2001, pos, Block.getStateId(state));
        return true;
    }

    @Override
    public EnumActionResult onItemUse(
            EntityPlayer player,
            World world,
            BlockPos pos,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (getForm(stack) != BUILD) return EnumActionResult.PASS;
        if (player.isSneaking()) {
            if (!world.isRemote) startPrinting(player, stack, pos.offset(facing));
            return EnumActionResult.SUCCESS;
        }
        if (world.isRemote) ClientEquipmentHandler.openSchematicScreen();
        return EnumActionResult.SUCCESS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(
            World world, EntityPlayer player, EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);
        if (getForm(stack) != BUILD) return new ActionResult<>(EnumActionResult.PASS, stack);
        if (world.isRemote) ClientEquipmentHandler.openSchematicScreen();
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    public static void setSchematic(ItemStack stack, SchematicLibrary.Data schematic) {
        NBTTagCompound nbt = tag(stack);
        nbt.setTag("Schematic", schematic.blocks.copy());
        nbt.setString("SchematicName", schematic.summary.displayName);
        nbt.setString("SchematicAuthor", schematic.summary.author);
        nbt.setString("SchematicFile", schematic.summary.fileName);
        nbt.setInteger("SchematicSizeX", schematic.summary.sizeX);
        nbt.setInteger("SchematicSizeY", schematic.summary.sizeY);
        nbt.setInteger("SchematicSizeZ", schematic.summary.sizeZ);
        nbt.setBoolean("Printing", false);
        nbt.setInteger("PrintIndex", 0);
        nbt.removeTag("PrintOriginX");
        nbt.removeTag("PrintOriginY");
        nbt.removeTag("PrintOriginZ");
    }

    private static void startPrinting(EntityPlayer player, ItemStack stack, BlockPos origin) {
        NBTTagCompound nbt = tag(stack);
        if (!nbt.hasKey("Schematic", 9) || nbt.getTagList("Schematic", 10).tagCount() == 0) {
            player.sendStatusMessage(
                    new TextComponentTranslation("status.exoarsenal.rtool_no_schematic"), true);
            return;
        }
        nbt.setInteger("PrintOriginX", origin.getX());
        nbt.setInteger("PrintOriginY", origin.getY());
        nbt.setInteger("PrintOriginZ", origin.getZ());
        nbt.setInteger("PrintIndex", 0);
        nbt.setBoolean("Printing", true);
        player.sendStatusMessage(
                new TextComponentTranslation("status.exoarsenal.rtool_print_started"), true);
    }

    public static void printerTick(EntityPlayer player) {
        if (player.world.isRemote) return;
        ItemStack tool = player.getHeldItemMainhand();
        if (!(tool.getItem() instanceof ItemRTool)) tool = player.getHeldItemOffhand();
        if (!(tool.getItem() instanceof ItemRTool)
                || getForm(tool) != BUILD
                || !tag(tool).getBoolean("Printing")) return;
        EnumHand toolHand =
                player.getHeldItemMainhand() == tool ? EnumHand.MAIN_HAND : EnumHand.OFF_HAND;
        NBTTagCompound nbt = tag(tool);
        NBTTagList schematic = nbt.getTagList("Schematic", 10);
        int index = nbt.getInteger("PrintIndex");
        if (index >= schematic.tagCount()) {
            nbt.setBoolean("Printing", false);
            player.sendStatusMessage(
                    new TextComponentTranslation("status.exoarsenal.rtool_print_complete"), true);
            return;
        }
        BlockPos origin =
                new BlockPos(
                        nbt.getInteger("PrintOriginX"),
                        nbt.getInteger("PrintOriginY"),
                        nbt.getInteger("PrintOriginZ"));
        List<Vec3d> lasers = new ArrayList<>(4);
        int processed = 0;
        int blocksPerTick = tool.getItem() instanceof ItemX10Multitool ? 8 : 4;
        while (processed < blocksPerTick && index < schematic.tagCount()) {
            NBTTagCompound entry = schematic.getCompoundTagAt(index);
            Block block =
                    ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.getString("Block")));
            if (block == null) {
                index++;
                continue;
            }
            IBlockState state;
            try {
                state = block.getStateFromMeta(entry.getByte("Meta") & 255);
            } catch (RuntimeException error) {
                state = block.getDefaultState();
            }
            BlockPos target =
                    origin.add(entry.getShort("X"), entry.getShort("Y"), entry.getShort("Z"));
            if (player.world.getBlockState(target).equals(state)) {
                index++;
                continue;
            }
            if (!player.world.getBlockState(target).getBlock().isReplaceable(player.world, target)
                    || !player.world.mayPlace(block, target, false, EnumFacing.UP, player)) {
                nbt.setBoolean("Printing", false);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.rtool_print_blocked",
                                target.getX(),
                                target.getY(),
                                target.getZ()),
                        true);
                break;
            }
            int slot = findBlock(player, block, entry.getByte("Meta") & 255);
            if (!player.capabilities.isCreativeMode && slot < 0) {
                nbt.setBoolean("Printing", false);
                player.sendStatusMessage(
                        new TextComponentTranslation(
                                "status.exoarsenal.rtool_missing_block", block.getLocalizedName()),
                        true);
                break;
            }
            if (!EnergyUtil.drain(tool, 60, false)) {
                nbt.setBoolean("Printing", false);
                player.sendStatusMessage(
                        new TextComponentTranslation("status.exoarsenal.rtool_empty"), true);
                break;
            }
            BlockEvent.PlaceEvent place =
                    new BlockEvent.PlaceEvent(
                            new net.minecraftforge.common.util.BlockSnapshot(
                                    player.world, target, state),
                            player.world.getBlockState(target),
                            player,
                            toolHand);
            if (MinecraftForge.EVENT_BUS.post(place)) {
                nbt.setBoolean("Printing", false);
                break;
            }
            player.world.setBlockState(target, state, 3);
            if (!player.capabilities.isCreativeMode) {
                ItemStack inventory = player.inventory.mainInventory.get(slot);
                inventory.shrink(1);
                if (inventory.isEmpty()) player.inventory.mainInventory.set(slot, ItemStack.EMPTY);
            }
            lasers.add(center(target));
            player.world.playSound(
                    null,
                    target,
                    block.getSoundType(state, player.world, target, player).getPlaceSound(),
                    SoundCategory.BLOCKS,
                    0.45F,
                    1.6F);
            index++;
            processed++;
        }
        nbt.setInteger("PrintIndex", index);
        if (!lasers.isEmpty()) PacketRToolEffect.send(player, PacketRToolEffect.BUILD, lasers);
    }

    private static int findBlock(EntityPlayer player, Block block, int meta) {
        Item item = Item.getItemFromBlock(block);
        for (int i = 0; i < player.inventory.mainInventory.size(); i++) {
            ItemStack stack = player.inventory.mainInventory.get(i);
            if (stack.getItem() != item) continue;
            if (!stack.getHasSubtypes() || stack.getMetadata() == meta) return i;
        }
        return -1;
    }

    private static Vec3d center(BlockPos pos) {
        return new Vec3d(pos).addVector(0.5D, 0.5D, 0.5D);
    }

    @Override
    public boolean hitEntity(ItemStack stack, EntityLivingBase target, EntityLivingBase attacker) {
        if (getForm(stack) != SAW || !EnergyUtil.drain(stack, 180, false)) return false;
        int spin = Math.min(60, tag(stack).getInteger("UseTicks"));
        target.attackEntityFrom(
                attacker instanceof EntityPlayer
                        ? net.minecraft.util.DamageSource.causePlayerDamage((EntityPlayer) attacker)
                        : net.minecraft.util.DamageSource.GENERIC,
                2.0F + spin / 12.0F);
        return true;
    }

    @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(
            EntityEquipmentSlot slot, ItemStack stack) {
        Multimap<String, AttributeModifier> map = HashMultimap.create();
        if (slot == EntityEquipmentSlot.MAINHAND && getForm(stack) == SAW) {
            map.put(
                    SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
                    new AttributeModifier(ATTACK_DAMAGE, "Prototype R-Tool saw damage", 7.0D, 0));
            map.put(
                    SharedMonsterAttributes.ATTACK_SPEED.getName(),
                    new AttributeModifier(ATTACK_SPEED, "Prototype R-Tool saw speed", -2.2D, 0));
        }
        return map;
    }

    @Override
    public float getDestroySpeed(ItemStack stack, IBlockState state) {
        if (getForm(stack) == DRILL) return 18.0F;
        if (getForm(stack) == SAW && state.getMaterial() == Material.WOOD) return 12.0F;
        return 1.0F;
    }

    @Override
    public boolean canHarvestBlock(IBlockState state, ItemStack stack) {
        return getForm(stack) == DRILL
                || (getForm(stack) == SAW && state.getMaterial() == Material.WOOD);
    }

    @Override
    public Set<String> getToolClasses(ItemStack stack) {
        if (getForm(stack) == DRILL) return Collections.singleton("pickaxe");
        if (getForm(stack) == SAW) return Collections.singleton("axe");
        return Collections.emptySet();
    }

    private <P extends IAnimatable> PlayState predicate(AnimationEvent<P> event) {
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : event.getExtraDataOfType(ItemStack.class).get(0);
        String animation;
        if (getForm(stack) == SAW)
            animation = ClientEquipmentHandler.isRToolUsing(stack) ? "saw_cut" : "saw_idle";
        else if (getForm(stack) == BUILD)
            animation = tag(stack).getBoolean("Printing") ? "build_print" : "build_idle";
        else if (getForm(stack) == SCYTHE)
            animation = ClientEquipmentHandler.isRToolUsing(stack) ? "saw_cut" : "saw_idle";
        else animation = ClientEquipmentHandler.isRToolUsing(stack) ? "drill_spin" : "drill_idle";
        event.getController()
                .setAnimation(
                        new AnimationBuilder().addAnimation("animation.rtool." + animation, true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(
                new AnimationController<ItemRTool>(this, "rtool", 2, this::predicate));
    }

    @Override
    public AnimationFactory getFactory() {
        return factory;
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable NBTTagCompound nbt) {
        return EnergyUtil.provider(CAPACITY, 16000);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return EnergyUtil.stored(stack) < CAPACITY;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        return 1.0D - EnergyUtil.stored(stack) / (double) CAPACITY;
    }

    @Override
    public void addInformation(
            ItemStack stack, @Nullable World world, List<String> tooltip, ITooltipFlag flag) {
        EquipmentTooltip.appendLegacy(stack, tooltip);
    }
}
