package com.scapeandrun.frostbite.client.render;

import com.scapeandrun.frostbite.client.model.EnergyShieldModel;
import com.scapeandrun.frostbite.item.ItemEnergyShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoCube;
import software.bernie.geckolib3.renderers.geo.GeoItemRenderer;
import software.bernie.geckolib3.renderers.geo.IGeoRenderer;

public class EnergyShieldRenderer extends GeoItemRenderer<ItemEnergyShield> {
    public EnergyShieldRenderer() {
        super(new EnergyShieldModel());
    }

    @Override
    public void renderRecursively(
            BufferBuilder buffer, GeoBone bone, float red, float green, float blue, float alpha) {
        IGeoRenderer.MATRIX_STACK.push();
        IGeoRenderer.MATRIX_STACK.translate(bone);
        IGeoRenderer.MATRIX_STACK.moveToPivot(bone);
        IGeoRenderer.MATRIX_STACK.rotate(bone);
        IGeoRenderer.MATRIX_STACK.scale(bone);
        IGeoRenderer.MATRIX_STACK.moveBackFromPivot(bone);
        float r = red, g = green, b = blue;
        if (bone.getName().contains("Field")) {
            float pulse = (float) ((Math.sin(Minecraft.getSystemTime() * 0.012D) + 1.0D) * 0.5D);
            r *= 1.0F;
            g *= 0.58F + pulse * 0.42F;
            b *= 0.3F + pulse * 0.35F;
        }
        if (!bone.isHidden())
            for (GeoCube cube : bone.childCubes) {
                IGeoRenderer.MATRIX_STACK.push();
                GlStateManager.pushMatrix();
                renderCube(buffer, cube, r, g, b, alpha);
                GlStateManager.popMatrix();
                IGeoRenderer.MATRIX_STACK.pop();
            }
        if (!bone.childBonesAreHiddenToo())
            for (GeoBone child : bone.childBones)
                renderRecursively(buffer, child, red, green, blue, alpha);
        IGeoRenderer.MATRIX_STACK.pop();
    }
}
