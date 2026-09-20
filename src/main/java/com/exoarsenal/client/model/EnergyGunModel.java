package com.exoarsenal.client.model;

import com.exoarsenal.ExoArsenal;
import com.exoarsenal.item.ItemEnergyGun;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.processor.IBone;
import software.bernie.geckolib3.model.AnimatedGeoModel;

public class EnergyGunModel extends AnimatedGeoModel<ItemEnergyGun> {
    private static final String[] GROUPS = {
        "prototypeShotgun", "prototypeCannon", "prototypePistol", "prototypeMachinegun",
        "x10Shotgun", "x10Cannon", "x10Machinegun", "x10NetLauncher"
    };
    private static final String[][] PARTS = {
        {
            "prototypeShotgunPump",
            "prototypeShotgunEnergy",
            "prototypeShotgunEmitter",
            "prototypeShotgunMuzzle"
        },
        {
            "prototypeCannonCoil",
            "prototypeCannonEnergy",
            "prototypeCannonEmitter",
            "prototypeCannonMuzzle"
        },
        {
            "prototypePistolSlide",
            "prototypePistolEnergy",
            "prototypePistolEmitter",
            "prototypePistolMuzzle"
        },
        {
            "prototypeMachinegunRotor",
            "prototypeMachinegunEnergy",
            "prototypeMachinegunEmitter",
            "prototypeMachinegunMuzzle"
        },
        {"x10ShotgunAction", "x10ShotgunEnergy", "x10ShotgunEmitter", "x10ShotgunMuzzle"},
        {"x10CannonRails", "x10CannonEnergy", "x10CannonEmitter", "x10CannonMuzzle"},
        {
            "x10MachinegunRotor",
            "x10MachinegunEnergy",
            "x10MachinegunEmitter",
            "x10MachinegunMuzzle"
        },
        {"x10NetProngs", "x10NetEnergy", "x10NetEmitter", "x10NetMuzzle"}
    };
    private static final String[] KX_UPGRADES = {
        "kxShotgunUpgrade", "kxTribowUpgrade", "kxMinigunUpgrade", "kxRailgunUpgrade"
    };

    @Override
    public ResourceLocation getModelLocation(ItemEnergyGun object) {
        return new ResourceLocation(ExoArsenal.MODID, "geo/energy_guns_refined.geo.json");
    }

    @Override
    public ResourceLocation getTextureLocation(ItemEnergyGun object) {
        return new ResourceLocation(
                ExoArsenal.MODID,
                "textures/models/armor/"
                        + (object.getType().ordinal() < 4 ? "rmor" : "x10")
                        + "_refined_100.png");
    }

    @Override
    public ResourceLocation getAnimationFileLocation(ItemEnergyGun object) {
        return new ResourceLocation(ExoArsenal.MODID, "animations/energy_gun.animation.json");
    }

    @Override
    public void setLivingAnimations(ItemEnergyGun gun, Integer id, AnimationEvent event) {
        super.setLivingAnimations(gun, id, event);
        int selected = gun.getType().ordinal();

        switch (gun.getType()) {
            case KX_SHOTGUN:
                selected = 4;
                break;
            case KX_TRIBOW:
                selected = 7;
                break;
            case KX_MINIGUN:
                selected = 6;
                break;
            case KX_RAILGUN:
                selected = 5;
                break;
            default:
                break;
        }
        for (int i = 0; i < GROUPS.length; i++) {
            boolean hidden = i != selected;
            optionalBone(GROUPS[i]).setHidden(hidden);
            optionalBone(GROUPS[i] + "Grip").setHidden(hidden);
            for (String part : PARTS[i]) {
                IBone bone = optionalBone(part);
                bone.setHidden(hidden);

                if (!part.contains("Energy")) {
                    bone.setScaleX(1F);
                    bone.setScaleY(1F);
                    bone.setScaleZ(1F);
                }
            }
        }
        int kxUpgrade =
                gun.getType() == ItemEnergyGun.Type.KX_SHOTGUN
                        ? 0
                        : gun.getType() == ItemEnergyGun.Type.KX_TRIBOW
                                ? 1
                                : gun.getType() == ItemEnergyGun.Type.KX_MINIGUN
                                        ? 2
                                        : gun.getType() == ItemEnergyGun.Type.KX_RAILGUN ? 3 : -1;
        for (int i = 0; i < KX_UPGRADES.length; i++)
            optionalBone(KX_UPGRADES[i]).setHidden(i != kxUpgrade);
    }

    private IBone optionalBone(String name) {
        try {
            IBone bone = getBone(name);
            if (bone != null) return bone;
        } catch (RuntimeException ignored) {
        }
        throw new IllegalStateException("Missing energy gun bone: " + name);
    }
}
