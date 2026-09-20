package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.item.ItemEnergyGun;
import com.scapeandrun.frostbite.item.ItemRBlade;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class EnergyCombatPoseHandler {
    private static final Map<UUID, PoseState> ACTIVE_POSES = new HashMap<>();

    private EnergyCombatPoseHandler() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void applyPose(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (player.getEntityData().getInteger("FrostCombatAction") != 0) return;
        ModelPlayer model = event.getRenderer().getMainModel();
        UUID id = player.getUniqueID();

        PoseState stale = ACTIVE_POSES.remove(id);
        if (stale != null) stale.restore(player, model);

        BladeAction blade = findBladeAction(player);
        GunAction gun = blade == null ? findGunAction(player) : null;
        if (blade == null && gun == null) return;

        PoseState original = new PoseState(player, model);
        ACTIVE_POSES.put(id, original);
        if (blade != null) applyBladePose(event, player, model, original, blade);
        else applyGunPose(event, player, model, original, gun);
    }

    private static void applyBladePose(
            RenderPlayerEvent.Pre event,
            EntityPlayer player,
            ModelPlayer model,
            PoseState original,
            BladeAction blade) {
        boolean attacking = isAttack(blade.action);
        setHeldArm(model, player, blade.hand, ModelBiped.ArmPose.ITEM);
        if (!attacking) return;

        float elapsed =
                player.world.getTotalWorldTime()
                        - ItemRBlade.getAnimationTick(blade.stack)
                        + event.getPartialRenderTick();
        float duration = Math.max(1.0F, ItemRBlade.animationDuration(blade.action));
        float timeline = MathHelper.clamp(elapsed / duration, 0.0F, 1.0F);
        float swing = timeline;
        float direction = direction(blade.action);

        if ("x10_rapid".equals(blade.action)) {
            float cycle = Math.max(0.0F, elapsed) / 6.0F;
            int stroke = MathHelper.floor(cycle);
            swing = cycle - stroke;
            direction = (stroke & 1) == 0 ? 1.0F : -1.0F;
        } else if ("x10_cleave_3".equals(blade.action)) {
            if (timeline < 0.5F) {
                swing = timeline * 2.0F;
                direction = 1.0F;
            } else {
                swing = (timeline - 0.5F) * 2.0F;
                direction = -1.0F;
            }
        }

        swing = CombatMotion.strike(swing);
        player.swingingHand = blade.hand;
        player.prevSwingProgress = swing;
        player.swingProgress = swing;

        float bodyDrive = MathHelper.sin(swing * (float) Math.PI);
        float twist = bodyDrive * direction * twistStrength(blade.action);
        player.prevRenderYawOffset = original.prevRenderYawOffset + twist;
        player.renderYawOffset = original.renderYawOffset + twist;

        if (blade.action.endsWith("stylish_2") || "x10_cleave_3".equals(blade.action)) {
            float stride = MathHelper.sin(timeline * (float) Math.PI);
            player.limbSwing = timeline * (blade.action.endsWith("stylish_2") ? 3.2F : 2.25F);
            player.prevLimbSwingAmount = Math.max(original.prevLimbSwingAmount, stride * 0.68F);
            player.limbSwingAmount = Math.max(original.limbSwingAmount, stride * 0.68F);
        }
    }

    private static void applyGunPose(
            RenderPlayerEvent.Pre event,
            EntityPlayer player,
            ModelPlayer model,
            PoseState original,
            GunAction gun) {
        ItemEnergyGun.Type type = gun.item.getType();
        boolean twoHanded =
                !type.dual
                        || type.cannon()
                        || type.automatic()
                        || type == ItemEnergyGun.Type.X10_NET_LAUNCHER;
        if (twoHanded) {

            model.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
            model.leftArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
        } else {
            setHeldArm(model, player, gun.hand, ModelBiped.ArmPose.ITEM);
            ItemStack other =
                    player.getHeldItem(
                            gun.hand == EnumHand.MAIN_HAND
                                    ? EnumHand.OFF_HAND
                                    : EnumHand.MAIN_HAND);
            if (other.getItem() instanceof ItemEnergyGun)
                setHeldArm(
                        model,
                        player,
                        gun.hand == EnumHand.MAIN_HAND ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND,
                        ModelBiped.ArmPose.ITEM);
        }

        float age = gunAnimationAge(player, gun.stack, event.getPartialRenderTick());
        if (age > 11.0F) return;
        float duration = "automatic".equals(gun.action) ? 5.0F : 10.0F;
        float phase = MathHelper.clamp(age / duration, 0.0F, 1.0F);
        float kick = CombatMotion.recoil(phase);
        float sign = gun.hand == EnumHand.MAIN_HAND ? 1.0F : -1.0F;
        player.swingingHand = gun.hand;
        player.prevSwingProgress = kick * (type.cannon() ? 0.30F : type.shotgun() ? 0.18F : 0.10F);
        player.swingProgress = player.prevSwingProgress;
        float shoulder = kick * (type.cannon() ? 8.0F : type.shotgun() ? 4.5F : 2.0F) * sign;
        player.prevRenderYawOffset = original.prevRenderYawOffset + shoulder;
        player.renderYawOffset = original.renderYawOffset + shoulder;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void firstPersonHold(RenderSpecificHandEvent event) {
        net.minecraft.client.entity.EntityPlayerSP player =
                net.minecraft.client.Minecraft.getMinecraft().player;
        if (player != null && player.getEntityData().getInteger("FrostCombatAction") != 0) return;
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof ItemEnergyGun) {
            firstPersonGun(event, stack, (ItemEnergyGun) stack.getItem());
            return;
        }
        if (stack.getItem() instanceof ItemRBlade) firstPersonBlade(event, stack);
    }

    private static void firstPersonGun(
            RenderSpecificHandEvent event, ItemStack stack, ItemEnergyGun gun) {
        float side = event.getHand() == EnumHand.MAIN_HAND ? 1.0F : -1.0F;
        ItemEnergyGun.Type type = gun.getType();
        float age =
                gunAnimationAge(
                        net.minecraft.client.Minecraft.getMinecraft().player,
                        stack,
                        event.getPartialTicks());
        float window = "automatic".equals(gunAction(stack)) ? 5.0F : 10.0F;
        float kick = age <= window ? CombatMotion.recoil(age / window) : 0.0F;

        float shoulder =
                type.cannon() ? 0.16F : type.shotgun() ? 0.10F : type.automatic() ? 0.07F : 0.035F;
        GlStateManager.translate(
                side * shoulder,
                type.cannon() ? -0.10F : -0.045F,
                (type.cannon() ? -0.20F : -0.08F)
                        + kick * (type.cannon() ? 0.25F : type.shotgun() ? 0.16F : 0.08F));
        GlStateManager.rotate(side * (type.cannon() ? -5.0F : -2.0F), 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(
                kick * (type.cannon() ? 11.0F : type.shotgun() ? 6.0F : 3.0F), 1.0F, 0.0F, 0.0F);
    }

    private static void firstPersonBlade(RenderSpecificHandEvent event, ItemStack stack) {
        EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
        if (player == null) return;
        String action = ItemRBlade.getCurrentAnimation(stack, player.world.getTotalWorldTime());
        if (!isAttack(action)) return;
        float elapsed =
                player.world.getTotalWorldTime()
                        - ItemRBlade.getAnimationTick(stack)
                        + event.getPartialTicks();
        float duration = Math.max(1.0F, ItemRBlade.animationDuration(action));
        float t = MathHelper.clamp(elapsed / duration, 0.0F, 1.0F);
        float arc = MathHelper.sin(CombatMotion.strike(t) * (float) Math.PI);
        float side = event.getHand() == EnumHand.MAIN_HAND ? 1.0F : -1.0F;
        float direction = direction(action) * side;

        if ("x10_rapid".equals(action)) {
            float cycle = Math.max(0.0F, elapsed) / 6.0F;
            int stroke = MathHelper.floor(cycle);
            float local = cycle - stroke;
            arc = MathHelper.sin(CombatMotion.strike(local) * (float) Math.PI);
            direction = ((stroke & 1) == 0 ? 1.0F : -1.0F) * side;
            GlStateManager.translate(direction * arc * 0.28F, -arc * 0.07F, -arc * 0.18F);
            GlStateManager.rotate(direction * arc * 54.0F, 0.0F, 1.0F, 0.18F);
        } else if (action.startsWith("x10_cleave_") || action.startsWith("attack_titanic")) {
            GlStateManager.translate(direction * arc * 0.34F, arc * 0.05F, -arc * 0.38F);
            GlStateManager.rotate(direction * arc * 72.0F, 0.15F, 1.0F, 0.25F);
            GlStateManager.rotate(-arc * 24.0F, 1.0F, 0.0F, 0.0F);
        } else {
            GlStateManager.translate(direction * arc * 0.22F, 0.0F, -arc * 0.22F);
            GlStateManager.rotate(direction * arc * 52.0F, 0.0F, 1.0F, 0.25F);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void restorePose(RenderPlayerEvent.Post event) {
        PoseState state = ACTIVE_POSES.remove(event.getEntityPlayer().getUniqueID());
        if (state != null)
            state.restore(event.getEntityPlayer(), event.getRenderer().getMainModel());
    }

    private static BladeAction findBladeAction(EntityPlayer player) {
        ItemStack main = player.getHeldItemMainhand();
        String action = ItemRBlade.getCurrentAnimation(main, player.world.getTotalWorldTime());
        if (!action.isEmpty()) return new BladeAction(main, EnumHand.MAIN_HAND, action);
        ItemStack off = player.getHeldItemOffhand();
        action = ItemRBlade.getCurrentAnimation(off, player.world.getTotalWorldTime());
        return action.isEmpty() ? null : new BladeAction(off, EnumHand.OFF_HAND, action);
    }

    private static GunAction findGunAction(EntityPlayer player) {
        ItemStack main = player.getHeldItemMainhand();
        if (main.getItem() instanceof ItemEnergyGun)
            return new GunAction(
                    main, EnumHand.MAIN_HAND, (ItemEnergyGun) main.getItem(), gunAction(main));
        ItemStack off = player.getHeldItemOffhand();
        return off.getItem() instanceof ItemEnergyGun
                ? new GunAction(
                        off, EnumHand.OFF_HAND, (ItemEnergyGun) off.getItem(), gunAction(off))
                : null;
    }

    private static String gunAction(ItemStack stack) {
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        return nbt == null ? "" : nbt.getString("GunAnimation");
    }

    private static float gunAnimationAge(EntityPlayer player, ItemStack stack, float partialTicks) {
        NBTTagCompound nbt = stack.hasTagCompound() ? stack.getTagCompound() : null;
        return player == null || nbt == null
                ? Float.MAX_VALUE
                : player.world.getTotalWorldTime() - nbt.getLong("GunAnimationTick") + partialTicks;
    }

    private static void setHeldArm(
            ModelPlayer model, EntityPlayer player, EnumHand hand, ModelBiped.ArmPose pose) {
        boolean right =
                hand == EnumHand.MAIN_HAND
                        ? player.getPrimaryHand() == net.minecraft.util.EnumHandSide.RIGHT
                        : player.getPrimaryHand() != net.minecraft.util.EnumHandSide.RIGHT;
        if (right) model.rightArmPose = pose;
        else model.leftArmPose = pose;
    }

    private static boolean isAttack(String action) {
        return action.startsWith("attack_")
                || action.startsWith("x10_cleave_")
                || "x10_rapid".equals(action);
    }

    private static float direction(String action) {
        return action.endsWith("_2") ? -1.0F : 1.0F;
    }

    private static float twistStrength(String action) {
        if ("x10_rapid".equals(action)) return 10.0F;
        if (action.startsWith("x10_cleave_")) return 24.0F;
        if (action.startsWith("attack_titanic")) return 22.0F;
        if (action.startsWith("attack_berserk")) return 17.0F;
        return 11.0F;
    }

    private static final class BladeAction {
        final ItemStack stack;
        final EnumHand hand;
        final String action;

        BladeAction(ItemStack stack, EnumHand hand, String action) {
            this.stack = stack;
            this.hand = hand;
            this.action = action;
        }
    }

    private static final class GunAction {
        final ItemStack stack;
        final EnumHand hand;
        final ItemEnergyGun item;
        final String action;

        GunAction(ItemStack stack, EnumHand hand, ItemEnergyGun item, String action) {
            this.stack = stack;
            this.hand = hand;
            this.item = item;
            this.action = action;
        }
    }

    private static final class PoseState {
        final float swingProgress;
        final float prevSwingProgress;
        final EnumHand swingingHand;
        final float renderYawOffset;
        final float prevRenderYawOffset;
        final float limbSwing;
        final float limbSwingAmount;
        final float prevLimbSwingAmount;
        final ModelBiped.ArmPose rightArmPose;
        final ModelBiped.ArmPose leftArmPose;

        PoseState(EntityPlayer player, ModelPlayer model) {
            swingProgress = player.swingProgress;
            prevSwingProgress = player.prevSwingProgress;
            swingingHand = player.swingingHand;
            renderYawOffset = player.renderYawOffset;
            prevRenderYawOffset = player.prevRenderYawOffset;
            limbSwing = player.limbSwing;
            limbSwingAmount = player.limbSwingAmount;
            prevLimbSwingAmount = player.prevLimbSwingAmount;
            rightArmPose = model.rightArmPose;
            leftArmPose = model.leftArmPose;
        }

        void restore(EntityPlayer player, ModelPlayer model) {
            player.swingProgress = swingProgress;
            player.prevSwingProgress = prevSwingProgress;
            player.swingingHand = swingingHand;
            player.renderYawOffset = renderYawOffset;
            player.prevRenderYawOffset = prevRenderYawOffset;
            player.limbSwing = limbSwing;
            player.limbSwingAmount = limbSwingAmount;
            player.prevLimbSwingAmount = prevLimbSwingAmount;
            model.rightArmPose = rightArmPose;
            model.leftArmPose = leftArmPose;
        }
    }
}
