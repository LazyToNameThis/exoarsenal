package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemX10Blade;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class X10BladeModel extends RBladeModel {
    @Override
    public ResourceLocation getModelLocation(com.scapeandrun.frostbite.item.ItemRBlade object) {
        return new ResourceLocation(Frostbite.MODID, "geo/x10_blade_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(com.scapeandrun.frostbite.item.ItemRBlade object) {
        return new ResourceLocation(Frostbite.MODID, "textures/models/armor/x10_refined_100.png");
    }

    @Override
    public void setLivingAnimations(
            com.scapeandrun.frostbite.item.ItemRBlade blade, Integer id, AnimationEvent event) {
        super.setLivingAnimations(blade, id, event);
        ItemStack stack =
                event.getExtraDataOfType(ItemStack.class).isEmpty()
                        ? ItemStack.EMPTY
                        : (ItemStack) event.getExtraDataOfType(ItemStack.class).get(0);
        boolean thrown =
                !stack.isEmpty()
                        && stack.hasTagCompound()
                        && stack.getTagCompound().getBoolean("Thrown");
        int form = ItemX10Blade.getForm(stack);
        setVisible("katanaForm", !thrown && form == ItemX10Blade.KATANA);
        setVisible("scissorsForm", !thrown && form == ItemX10Blade.SCISSORS);
        setVisible("greathammerForm", !thrown && form == ItemX10Blade.GREATHAMMER);
        setVisible("rapierForm", !thrown && form == ItemX10Blade.RAPIER);
        setKxUpgrades(false);

        float open = 17, compression = 0;
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.world != null)
            for (net.minecraft.entity.player.EntityPlayer owner : mc.world.playerEntities) {
                if (owner.getHeldItemMainhand() != stack) continue;
                net.minecraft.nbt.NBTTagCompound state = owner.getEntityData();
                int action = state.getInteger("FrostCombatAction");
                if (action != 1 && action != 2) break;
                com.scapeandrun.frostbite.combat.WeaponDiscipline d =
                        com.scapeandrun.frostbite.combat.WeaponDiscipline.of(stack);
                float age =
                        owner.world.getTotalWorldTime()
                                - state.getLong("FrostCombatStart")
                                + mc.getRenderPartialTicks();
                float contact = d.contact(action == 2), end = d.duration(action == 2);
                if (age > end) break;
                if (age < contact - 3)
                    open =
                            17
                                    + 18
                                            * com.scapeandrun.frostbite.client.CombatMotion.smooth(
                                                    age / Math.max(1, contact - 3));
                else if (age < contact)
                    open =
                            35
                                    - 33
                                            * com.scapeandrun.frostbite.client.CombatMotion.smooth(
                                                    (age - contact + 3) / 3);
                else
                    open =
                            2
                                    + 15
                                            * com.scapeandrun.frostbite.client.CombatMotion.smooth(
                                                    (age - contact) / Math.max(1, end - contact));
                if (age >= contact && age < contact + 5)
                    compression =
                            -0.9F
                                    * com.scapeandrun.frostbite.client.CombatMotion.recoil(
                                            (age - contact) / 5);
                break;
            }
        getBone("bladeScissorLeft").setRotationZ((float) Math.toRadians(open));
        getBone("bladeScissorRight").setRotationZ((float) Math.toRadians(-open));
        getBone("bladeHammerHead").setPositionY(compression);
    }

    protected void setKxUpgrades(boolean visible) {
        for (String name :
                new String[] {
                    "kxKatanaSpine",
                    "kxScissorPhase",
                    "kxHammerReactor",
                    "kxRapierHalo",
                    "kxDiscHalo"
                }) setVisible(name, visible);
    }
}
