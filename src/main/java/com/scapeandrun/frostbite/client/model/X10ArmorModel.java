package com.scapeandrun.frostbite.client.model;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.event.RmorEventHandler;
import com.scapeandrun.frostbite.item.ItemRmorArmor;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;

public class X10ArmorModel extends RmorArmorModel {
    @Override
    public ResourceLocation getModelLocation(ItemRmorArmor object) {
        return new ResourceLocation(Frostbite.MODID, "geo/x10_armor_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemRmorArmor object) {
        return new ResourceLocation(
                Frostbite.MODID, "textures/models/armor/x10_refined_" + chargeName() + ".png");
    }

    @Override
    public void setLivingAnimations(ItemRmorArmor armor, Integer id, AnimationEvent event) {
        super.setLivingAnimations(armor, id, event);
        if (event.getExtraDataOfType(EntityLivingBase.class).isEmpty()) return;
        EntityLivingBase wearer =
                (EntityLivingBase) event.getExtraDataOfType(EntityLivingBase.class).get(0);
        boolean powered = RmorEventHandler.getPowerLevel(wearer) > 0;
        boolean defense = RmorEventHandler.isDefenseForm(wearer);
        int weapon = RmorEventHandler.getDefenseWeapon(wearer);

        setBone("x10Analyzer", powered);
        setBone("x10Chest", true);
        setBone("x10WingLeft", powered);
        setBone("x10WingRight", powered);
        setBone("x10BeltLeft", true);
        setBone("x10BeltRight", true);
        setBone("leftShotgun", false);
        setBone("rightShotgun", false);
        setBone("x10ShotgunLeft", defense && weapon == 0);
        setBone("x10ShotgunRight", defense && weapon == 0);
        setBone("x10BlasterLeft", defense && weapon == 1);
        setBone("x10BlasterRight", defense && weapon == 1);
        setBone("x10KatanaLeft", defense && weapon == 2);
        setBone("x10KatanaRight", defense && weapon == 2);
        setKxBones(false);
    }

    protected void setKxBones(boolean visible) {
        for (String name :
                new String[] {
                    "kxCrown",
                    "kxVisor",
                    "kxReactor",
                    "kxShoulderLeft",
                    "kxShoulderRight",
                    "kxWingletLeft",
                    "kxWingletRight",
                    "kxForearmLeft",
                    "kxForearmRight",
                    "kxLegLeft",
                    "kxLegRight"
                }) setBone(name, visible);
    }
}
