package com.scapeandrun.frostbite.compat;

import com.scapeandrun.frostbite.registry.ModContent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import slimeknights.tconstruct.library.TinkerRegistry;
import slimeknights.tconstruct.library.smeltery.ICastingRecipe;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.events.TinkerCastingEvent;

import java.util.ArrayList;
import java.util.List;

public class TConstructCompat {
    private static boolean registered;

    public static void registerCastRecipes() {
        if (registered) return;
        registered = true;
        List<ICastingRecipe> originals =
                new ArrayList<>(TinkerRegistry.getAllTableCastingRecipes());
        for (ICastingRecipe recipe : originals) {
            TinkerRegistry.registerTableCasting(new MoldedCastingRecipe(recipe, false));
            TinkerRegistry.registerTableCasting(new MoldedCastingRecipe(recipe, true));
        }
    }

    @SubscribeEvent
    public void castWear(TinkerCastingEvent.OnCasted event) {
        ItemStack cast = event.tile.getStackInSlot(0);
        if (cast.getItem() != ModContent.FIRECLAY_CAST) return;
        event.consumeCast = false;
        Fluid fluid =
                event.tile.tank.getFluid() == null ? null : event.tile.tank.getFluid().getFluid();
        int temperature =
                event.recipe instanceof MoldedCastingRecipe
                        ? ((MoldedCastingRecipe) event.recipe).getLastTemperature()
                        : fluid == null ? 300 : fluid.getTemperature();
        int wear = temperature > 1373 ? 5 : 2;
        cast.setItemDamage(cast.getItemDamage() + wear);
        if (cast.getItemDamage() >= cast.getMaxDamage())
            event.tile.setInventorySlotContents(0, ItemStack.EMPTY);
        else event.tile.setInventorySlotContents(0, cast);
    }

    private static final class MoldedCastingRecipe implements ICastingRecipe {
        private final ICastingRecipe delegate;
        private final boolean fireclay;
        private int lastTemperature = 300;

        private MoldedCastingRecipe(ICastingRecipe delegate, boolean fireclay) {
            this.delegate = delegate;
            this.fireclay = fireclay;
        }

        private ItemStack gold(ItemStack molded) {
            Item part = Pattern.getPartFromTag(molded);
            return part == null
                    ? ItemStack.EMPTY
                    : Pattern.setTagForPart(new ItemStack(TinkerSmeltery.cast), part);
        }

        private void remember(Fluid fluid) {
            if (fluid != null) lastTemperature = fluid.getTemperature();
        }

        private int getLastTemperature() {
            return lastTemperature;
        }

        @Override
        public ItemStack getResult(ItemStack cast, Fluid fluid) {
            remember(fluid);
            return delegate.getResult(gold(cast), fluid);
        }

        @Override
        public FluidStack getFluid(ItemStack cast, Fluid fluid) {
            remember(fluid);
            return delegate.getFluid(gold(cast), fluid);
        }

        @Override
        public boolean matches(ItemStack cast, Fluid fluid) {
            if (cast.getItem() != (fireclay ? ModContent.FIRECLAY_CAST : ModContent.CLAY_CAST))
                return false;
            if (fireclay && cast.getItemDamage() >= cast.getMaxDamage()) return false;
            remember(fluid);
            ItemStack gold = gold(cast);
            return !gold.isEmpty() && delegate.matches(gold, fluid);
        }

        @Override
        public boolean switchOutputs() {
            return delegate.switchOutputs();
        }

        @Override
        public boolean consumesCast() {
            return !fireclay;
        }

        @Override
        public int getTime() {
            return delegate.getTime();
        }

        @Override
        public int getFluidAmount() {
            return delegate.getFluidAmount();
        }
    }
}
