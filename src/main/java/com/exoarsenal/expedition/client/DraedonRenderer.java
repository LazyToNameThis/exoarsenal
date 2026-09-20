package com.exoarsenal.expedition.client;

import com.exoarsenal.client.ModelTexture;
import com.exoarsenal.entity.EntityDraedon;
import net.minecraft.client.model.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class DraedonRenderer extends Render<EntityDraedon> {
    private final ModelBase model = new ModelBase() {};
    private final ModelRenderer head, body, leftArm, rightArm, leftLeg, rightLeg, chair;

    public DraedonRenderer(RenderManager manager) {
        super(manager);
        shadowSize = .7F;
        model.textureWidth = 32;
        model.textureHeight = 64;
        body = part(8, 20, 0, -10.5F, 0, -1.5F, -5, -1, 3, 6, 2);
        body.rotateAngleX = .28F;
        ModelRenderer breast = part(16, 10, 0, -3, -1, -2, -2, -1, 4, 3, 2);
        breast.rotateAngleX = -.3F;
        body.addChild(breast);
        body.addChild(part(8, 20, 0, -.5F, -1, -1, -1, -1, 2, 2, 2));
        head = part(0, 0, 0, -15.3F, -2, -1.5F, -3, -1, 3, 3, 2);

        ModelRenderer brow = part(0, 0, 0, -2.8F, -1, -2, -1, -1.5F, 4, 1, 3);
        brow.rotateAngleX = .58F;
        head.addChild(brow);
        ModelRenderer jaw = part(20, 0, 0, -.2F, -1.8F, -1, -1, -1.5F, 2, 1, 3);
        jaw.rotateAngleX = -.45F;
        head.addChild(jaw);
        ModelRenderer cheek = part(20, 0, 1.2F, -1.5F, -.8F, 0, -1, -1, 1, 2, 2);
        cheek.rotateAngleZ = .55F;
        head.addChild(cheek);
        cheek = part(20, 0, -1.2F, -1.5F, -.8F, -1, -1, -1, 1, 2, 2);
        cheek.rotateAngleZ = -.55F;
        head.addChild(cheek);
        head.addChild(part(20, 6, 0, -1.7F, -2.2F, -1, -.3F, -.2F, 2, 1, 1));
        ModelRenderer crown = part(0, 0, 0, -2.6F, .2F, -1.5F, -1, -.5F, 3, 1, 2);
        crown.rotateAngleX = -.45F;
        head.addChild(crown);
        ModelRenderer chin = part(20, 0, 0, -.1F, -2, -.5F, -.5F, -1, 1, 1, 2);
        chin.rotateAngleX = -.35F;
        head.addChild(chin);
        leftArm = part(8, 20, 2.5F, -14, -.5F, -.5F, 0, -.5F, 1, 5, 1);
        rightArm = part(8, 20, -2.5F, -14, -.5F, -.5F, 0, -.5F, 1, 5, 1);
        ModelRenderer shoulder = part(16, 10, 0, 0, 0, -1, -.5F, -1, 2, 2, 2);
        shoulder.rotateAngleZ = -.6F;
        leftArm.addChild(shoulder);
        shoulder = part(16, 10, 0, 0, 0, -1, -.5F, -1, 2, 2, 2);
        shoulder.rotateAngleZ = .6F;
        rightArm.addChild(shoulder);
        leftLeg = part(0, 20, 1.4F, -8.5F, -.3F, -1, 0, -1, 2, 6, 2);
        rightLeg = part(0, 20, -1.4F, -8.5F, -.3F, -1, 0, -1, 2, 6, 2);
        ModelRenderer forearm = part(24, 10, .3F, 4.8F, 0, -1, 0, -1, 2, 3, 2);
        forearm.rotateAngleX = -.65F;
        leftArm.addChild(forearm);
        hand(forearm);
        forearm = part(24, 10, -.3F, 4.8F, 0, -1, 0, -1, 2, 3, 2);
        forearm.rotateAngleX = -.65F;
        rightArm.addChild(forearm);
        hand(forearm);
        for (ModelRenderer leg : new ModelRenderer[] {leftLeg, rightLeg}) {
            ModelRenderer calf = part(8, 20, 0, 5.8F, 0, -.5F, 0, -.5F, 1, 5, 1);
            calf.rotateAngleX = 1.65F;
            leg.addChild(calf);
            ModelRenderer shin = part(16, 10, 0, 1, -.7F, -1, 0, -.5F, 2, 3, 1);
            shin.rotateAngleX = -.2F;
            calf.addChild(shin);
            ModelRenderer boot = part(16, 20, 0, 4.6F, 0, -1, 0, -2.5F, 2, 1, 4);
            boot.rotateAngleX = -.3F;
            calf.addChild(boot);
        }
        chair = part(0, 32, 0, 0, 0, -4, -8, -3, 8, 2, 6);
        ModelRenderer spine = part(20, 40, 0, -7, 2, -1, -10, 0, 2, 10, 2);
        spine.rotateAngleX = -.28F;
        chair.addChild(spine);
        for (int side : new int[] {-1, 1}) {
            ModelRenderer rail = part(0, 52, side * 4, -7, 2, -.5F, -7, 0, 1, 7, 2);
            rail.rotateAngleZ = side * .24F;
            rail.rotateAngleX = -.3F;
            chair.addChild(rail);
            ModelRenderer console = part(6, 52, side * 3.5F, -10, -1, -.5F, -1, -5, 1, 1, 7);
            console.rotateAngleX = .08F;
            console.rotateAngleZ = -side * .12F;
            chair.addChild(console);
            ModelRenderer strut = part(0, 52, side * 3.5F, -8, 0, -.5F, -4, -.5F, 1, 4, 1);
            strut.rotateAngleX = .65F;
            chair.addChild(strut);
            ModelRenderer control = part(6, 52, side * 3.5F, -10.4F, -5, -1, -.5F, -1, 2, 1, 2);
            control.rotateAngleX = -.4F;
            chair.addChild(control);
            chair.addChild(part(24, 52, side * 4, -11.2F, -3, -1, -.4F, -.5F, 2, 1, 1));
            ModelRenderer engine = part(0, 40, side * 3, -5, 1, -1.5F, 0, -1.5F, 3, 4, 3);
            engine.rotateAngleZ = -side * .25F;
            chair.addChild(engine);
            ModelRenderer fin = part(6, 52, side * 4, -7, 3, -2, 0, 0, 4, 1, 5);
            fin.rotateAngleZ = side * .45F;
            fin.rotateAngleX = -.4F;
            chair.addChild(fin);
        }
    }

    private void hand(ModelRenderer forearm) {
        forearm.addChild(part(16, 10, 0, 3, 0, -1, 0, -.8F, 2, 1, 1));

        for (int i = 0; i < 3; i++) {
            ModelRenderer finger = part(8, 20, -.75F + i * .65F, 3.7F, -.3F, 0, 0, -.5F, 1, 1, 1);
            finger.rotateAngleX = -.45F;
            forearm.addChild(finger);
        }
        ModelRenderer thumb = part(8, 20, -1, 3.2F, .2F, -.5F, 0, -.5F, 1, 1, 1);
        thumb.rotateAngleZ = -.6F;
        forearm.addChild(thumb);
    }

    private ModelRenderer part(
            int u,
            int v,
            float px,
            float py,
            float pz,
            float x,
            float y,
            float z,
            int w,
            int h,
            int d) {
        ModelRenderer p = new ModelRenderer(model, u, v);
        p.setRotationPoint(px, py, pz);
        p.addBox(x, y, z, w, h, d);
        return p;
    }

    public ModelRenderer[] reviewParts() {
        leftArm.rotateAngleX = rightArm.rotateAngleX = -.85F;
        leftLeg.rotateAngleX = rightLeg.rotateAngleX = -1.25F;
        head.rotateAngleX = .12F;
        return new ModelRenderer[] {chair, body, head, leftArm, rightArm, leftLeg, rightLeg};
    }

    @Override
    protected ResourceLocation getEntityTexture(EntityDraedon e) {
        return ModelTexture.get("draedon_wulfrum");
    }

    @Override
    public void doRender(EntityDraedon e, double x, double y, double z, float yaw, float partial) {
        contactBeam(e, x, y, z, partial);
        if (e.contactAge() < 30) return;
        GlStateManager.pushMatrix();
        GlStateManager.disableCull();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translate(x, y + .15 + Math.sin((e.ticksExisted + partial) * .06) * .055, z);
        GlStateManager.rotate(180 - yaw, 0, 1, 0);
        GlStateManager.scale(.115, -.115, .115);
        GlStateManager.color(1, 1, 1, 1);
        bindEntityTexture(e);
        head.rotateAngleY =
                Math.max(
                        -.7F,
                        Math.min(
                                .7F,
                                (e.rotationYawHead - e.renderYawOffset) * (float) Math.PI / 180));
        head.rotateAngleX = .12F + e.rotationPitch * (float) Math.PI / 360;
        float settle =
                (float)
                        com.exoarsenal.entity.BrawlerScore.smooth(
                                (e.contactAge() + partial - 82) / 20D);
        leftArm.rotateAngleX =
                -.25F - settle * .60F + (float) Math.sin((e.ticksExisted + partial) * .045) * .025F;
        rightArm.rotateAngleX =
                -.25F - settle * .61F + (float) Math.cos((e.ticksExisted + partial) * .05) * .025F;
        leftLeg.rotateAngleX = rightLeg.rotateAngleX = -1.25F;
        leftLeg.rotateAngleZ = .06F;
        rightLeg.rotateAngleZ = -.06F;
        chair.render(1);
        body.render(1);
        head.render(1);
        leftArm.render(1);
        rightArm.render(1);
        leftLeg.render(1);
        rightLeg.render(1);
        GlStateManager.disableRescaleNormal();
        GlStateManager.enableCull();
        GlStateManager.popMatrix();
        super.doRender(e, x, y, z, yaw, partial);
    }

    private void contactBeam(EntityDraedon entity, double x, double y, double z, float partial) {
        float age = entity.contactAge() + partial;
        if (age <= 0 || age >= 150) return;
        double strength = Math.min(1, age / 22D) * Math.min(1, (150 - age) / 35D);
        net.minecraft.util.math.BlockPos terminal = entity.terminal();
        double px = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial,
                py = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial,
                pz = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial;
        GlStateManager.pushMatrix();
        GlStateManager.translate(
                x + terminal.getX() + .5 - px,
                y + terminal.getY() + .72 - py,
                z + terminal.getZ() + .5 - pz);
        com.exoarsenal.client.render.WulfrumRayRenderer.begin();
        GlStateManager.depthMask(false);
        GlStateManager.blendFunc(org.lwjgl.opengl.GL11.GL_SRC_ALPHA, org.lwjgl.opengl.GL11.GL_ONE);
        net.minecraft.client.renderer.BufferBuilder buffer =
                net.minecraft.client.renderer.Tessellator.getInstance().getBuffer();
        buffer.begin(7, net.minecraft.client.renderer.vertex.DefaultVertexFormats.POSITION_COLOR);
        net.minecraft.util.math.Vec3d bottom = net.minecraft.util.math.Vec3d.ZERO,
                top = new net.minecraft.util.math.Vec3d(0, 64, 0);
        com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                buffer, bottom, top, .14 * strength, 0xD8FFE7, (float) strength * .85F);
        com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                buffer, bottom, top, .46 * strength, 0x50EF9B, (float) strength * .23F);
        com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                buffer, bottom, top, .9 * strength, 0x22AD68, (float) strength * .07F);
        for (int strand = 0; strand < 3; strand++) {
            net.minecraft.util.math.Vec3d old = null;
            for (int step = 0; step <= 64; step++) {
                double angle = step * .35 + age * .10 + strand * Math.PI * 2 / 3,
                        r = .58 * strength;
                net.minecraft.util.math.Vec3d point =
                        new net.minecraft.util.math.Vec3d(
                                Math.cos(angle) * r, step, Math.sin(angle) * r);
                if (old != null)
                    com.exoarsenal.client.render.WulfrumRayRenderer.tube(
                            buffer, old, point, .025, 0x8DFFC1, (float) strength * .55F);
                old = point;
            }
        }
        net.minecraft.client.renderer.Tessellator.getInstance().draw();
        GlStateManager.depthMask(true);
        com.exoarsenal.client.render.WulfrumRayRenderer.finish();
        GlStateManager.popMatrix();
    }
}
