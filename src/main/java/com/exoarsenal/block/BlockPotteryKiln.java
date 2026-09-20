package com.exoarsenal.block;

import com.exoarsenal.tile.TileEntityPotteryKiln;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import java.util.Random;

public class BlockPotteryKiln extends BlockContainer {
    public static final PropertyBool LIT = PropertyBool.create("lit");

    public BlockPotteryKiln() {
        super(Material.ROCK);
        setHardness(3.2F);
        setResistance(12F);
        setSoundType(SoundType.STONE);
        setDefaultState(blockState.getBaseState().withProperty(LIT, false));
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityPotteryKiln();
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer(this, LIT);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        return getDefaultState().withProperty(LIT, (meta & 1) != 0);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(LIT) ? 1 : 0;
    }

    @Override
    public boolean onBlockActivated(
            World world,
            BlockPos pos,
            IBlockState state,
            EntityPlayer player,
            EnumHand hand,
            EnumFacing facing,
            float hitX,
            float hitY,
            float hitZ) {
        TileEntity tile = world.getTileEntity(pos);
        if (!(tile instanceof TileEntityPotteryKiln)) return false;
        if (world.isRemote) return true;
        TileEntityPotteryKiln kiln = (TileEntityPotteryKiln) tile;
        ItemStack held = player.getHeldItem(hand);
        if (held.isEmpty() || player.isSneaking()) {
            ItemStack extracted = kiln.extractOne();
            if (!extracted.isEmpty()) ItemHandlerHelper.giveItemToPlayer(player, extracted);
        } else if (kiln.insertOne(held) && !player.capabilities.isCreativeMode) held.shrink(1);
        return true;
    }

    @Override
    public void breakBlock(World world, BlockPos pos, IBlockState state) {
        TileEntity tile = world.getTileEntity(pos);
        if (tile instanceof TileEntityPotteryKiln) {
            TileEntityPotteryKiln kiln = (TileEntityPotteryKiln) tile;
            for (int slot = 0; slot < kiln.getSizeInventory(); slot++) {
                ItemStack stack = kiln.removeStackFromSlot(slot);
                if (!stack.isEmpty())
                    net.minecraft.inventory.InventoryHelper.spawnItemStack(
                            world, pos.getX(), pos.getY(), pos.getZ(), stack);
            }
        }
        super.breakBlock(world, pos, state);
    }

    @Override
    public boolean hasComparatorInputOverride(IBlockState state) {
        return true;
    }

    @Override
    public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {
        return net.minecraft.inventory.Container.calcRedstone(world.getTileEntity(pos));
    }

    @Override
    public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random random) {
        if (!state.getValue(LIT)) return;
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.45D;
        double z = pos.getZ() + 0.5D;
        world.spawnParticle(
                EnumParticleTypes.SMOKE_NORMAL,
                x + (random.nextDouble() - 0.5D) * 0.45D,
                y,
                z + 0.51D,
                0,
                0.02D,
                0);
        if (random.nextInt(3) == 0)
            world.spawnParticle(EnumParticleTypes.FLAME, x, y - 0.1D, z + 0.52D, 0, 0, 0);
    }
}
