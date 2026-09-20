package com.scapeandrun.frostbite.client;

import com.scapeandrun.frostbite.Frostbite;
import com.scapeandrun.frostbite.client.gui.GuiX10Radial;
import com.scapeandrun.frostbite.event.RmorEventHandler;
import com.scapeandrun.frostbite.event.X10Systems;
import com.scapeandrun.frostbite.item.ItemX10Blade;
import com.scapeandrun.frostbite.network.ModNetwork;
import com.scapeandrun.frostbite.network.PacketX10Action;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Frostbite.MODID, value = Side.CLIENT)
public final class X10ClientHandler {
    private static final KeyBinding WEAPON_BELT =
            new KeyBinding(
                    "key.exoarsenal.x10_weapon_belt", Keyboard.KEY_G, "key.categories.exoarsenal");
    private static final KeyBinding ARMOR_SYSTEMS =
            new KeyBinding(
                    "key.exoarsenal.x10_systems", Keyboard.KEY_H, "key.categories.exoarsenal");
    private static final Map<UUID, HeldItems> HIDDEN_ITEMS = new HashMap<>();
    private static boolean registered;
    private static int jetDelay;

    private X10ClientHandler() {}

    @SubscribeEvent
    public static void registerKeys(ModelRegistryEvent event) {
        if (registered) return;
        ClientRegistry.registerKeyBinding(WEAPON_BELT);
        ClientRegistry.registerKeyBinding(ARMOR_SYSTEMS);
        registered = true;
    }

    @SubscribeEvent
    public static void key(InputEvent.KeyInputEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null || mc.currentScreen != null || !Keyboard.getEventKeyState()) return;
        int key = Keyboard.getEventKey();

        if (key == Keyboard.KEY_RIGHT
                && X10Systems.hasSet(mc.player)
                && RmorEventHandler.isDefenseForm(mc.player)) {
            ModNetwork.CHANNEL.sendToServer(new PacketX10Action(PacketX10Action.DEFENSE_WEAPON, 0));
            return;
        }

        ItemStack blade = heldBlade(mc.player);
        if (!blade.isEmpty()) {
            if (key == Keyboard.KEY_LEFT) {
                ItemX10Blade.cycleForm(blade);
                ModNetwork.CHANNEL.sendToServer(new PacketX10Action(PacketX10Action.BLADE_FORM, 0));
                return;
            }
            if (key == Keyboard.KEY_RIGHT) {
                ItemX10Blade.cycleAbility(blade);
                ModNetwork.CHANNEL.sendToServer(
                        new PacketX10Action(PacketX10Action.BLADE_ABILITY, 0));
                return;
            }
        }

        if (!X10Systems.hasSet(mc.player)) return;
        if (key == WEAPON_BELT.getKeyCode()) {
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)
                    || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT)) {
                ModNetwork.CHANNEL.sendToServer(new PacketX10Action(PacketX10Action.BELT_STORE, 0));
            } else {
                mc.displayGuiScreen(new GuiX10Radial(GuiX10Radial.Page.WEAPONS));
            }
        } else if (key == ARMOR_SYSTEMS.getKeyCode()) {
            mc.displayGuiScreen(new GuiX10Radial(GuiX10Radial.Page.SYSTEMS));
        }
    }

    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Minecraft mc = Minecraft.getMinecraft();
        if (jetDelay > 0) jetDelay--;
        if (mc.player == null
                || mc.currentScreen != null
                || !X10Systems.hasSet(mc.player)
                || mc.player.onGround
                || !mc.gameSettings.keyBindJump.isKeyDown()
                || jetDelay > 0) return;
        ModNetwork.CHANNEL.sendToServer(new PacketX10Action(PacketX10Action.JET, 0));
        jetDelay = 2;
    }

    @SubscribeEvent
    public static void analyzer(RenderGameOverlayEvent.Text event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null
                || !X10Systems.hasSet(mc.player)
                || !X10Systems.enabled(mc.player, X10Systems.ANALYZER)) return;
        Entity entity = mc.pointedEntity;
        if (entity instanceof EntityLivingBase) {
            EntityLivingBase living = (EntityLivingBase) entity;
            net.minecraft.util.ResourceLocation entityId =
                    net.minecraft.entity.EntityList.getKey(living);
            if (entityId != null && "srparasites".equals(entityId.getResourceDomain())) {
                NBTTagCompound data = living.getEntityData();
                int points =
                        data.hasKey("EvolutionPoints")
                                ? data.getInteger("EvolutionPoints")
                                : data.hasKey("SRPEvolutionPoints")
                                        ? data.getInteger("SRPEvolutionPoints")
                                        : data.getInteger("evolutionPoints");
                event.getLeft().add("\u00a7dEvolution points: \u00a7f" + points);
                long mutations = data.getLong("KXGeneticMutations");
                if (mutations != 0)
                    event.getLeft()
                            .add(
                                    "\u00a7bKX mutations: \u00a7f"
                                            + Long.bitCount(mutations)
                                            + " / 20");
            }
            event.getLeft().add("§eAnalyzer: §f" + living.getDisplayName().getFormattedText());
            event.getLeft()
                    .add(
                            "§7Health: §f"
                                    + oneDecimal(living.getHealth())
                                    + " / "
                                    + oneDecimal(living.getMaxHealth()));
            if (living instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) living;
                ItemStack held = player.getHeldItemMainhand();
                int used = 0;
                for (ItemStack stack : player.inventory.mainInventory) if (!stack.isEmpty()) used++;
                event.getLeft()
                        .add(
                                "§7Held Item: §f"
                                        + (held.isEmpty() ? "Empty" : held.getDisplayName()));
                event.getLeft().add("§7Inventory: §f" + used + " / 36 slots used");
            }
            return;
        }
        RayTraceResult hit = mc.objectMouseOver;
        if (hit == null || hit.typeOfHit != RayTraceResult.Type.BLOCK) return;
        BlockPos pos = hit.getBlockPos();
        IBlockState state = mc.world.getBlockState(pos);
        event.getLeft().add("§eAnalyzer: §f" + state.getBlock().getLocalizedName());
        event.getLeft().add("§7Position: §f" + pos.getX() + ", " + pos.getY() + ", " + pos.getZ());
        float hardness = state.getBlockHardness(mc.world, pos);
        event.getLeft()
                .add("§7Hardness: §f" + (hardness < 0 ? "Unbreakable" : oneDecimal(hardness)));
    }

    @SubscribeEvent
    public static void hideFirstPersonItem(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player != null && shotgunArms(mc.player)) event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void hideThirdPersonItems(RenderPlayerEvent.Pre event) {
        EntityPlayer player = event.getEntityPlayer();
        if (!shotgunArms(player) || HIDDEN_ITEMS.containsKey(player.getUniqueID())) return;
        HIDDEN_ITEMS.put(
                player.getUniqueID(),
                new HeldItems(player.getHeldItemMainhand(), player.getHeldItemOffhand()));
        player.setItemStackToSlot(
                net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, ItemStack.EMPTY);
        player.setItemStackToSlot(
                net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void restoreThirdPersonItems(RenderPlayerEvent.Post event) {
        EntityPlayer player = event.getEntityPlayer();
        HeldItems held = HIDDEN_ITEMS.remove(player.getUniqueID());
        if (held == null) return;
        player.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND, held.main);
        player.setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.OFFHAND, held.off);
    }

    private static boolean shotgunArms(EntityLivingBase wearer) {
        return X10Systems.hasSet(wearer)
                && RmorEventHandler.isDefenseForm(wearer)
                && RmorEventHandler.getDefenseWeapon(wearer) == 0;
    }

    private static ItemStack heldBlade(EntityLivingBase player) {
        ItemStack main = player.getHeldItemMainhand();
        if (main.getItem() instanceof ItemX10Blade) return main;
        ItemStack off = player.getHeldItemOffhand();
        return off.getItem() instanceof ItemX10Blade ? off : ItemStack.EMPTY;
    }

    private static String oneDecimal(float value) {
        return String.format(java.util.Locale.ROOT, "%.1f", value);
    }

    private static final class HeldItems {
        final ItemStack main;
        final ItemStack off;

        HeldItems(ItemStack main, ItemStack off) {
            this.main = main;
            this.off = off;
        }
    }
}
