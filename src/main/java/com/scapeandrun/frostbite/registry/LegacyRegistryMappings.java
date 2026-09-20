package com.scapeandrun.frostbite.registry;

import com.scapeandrun.frostbite.Frostbite;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraft.util.datafix.IFixableData;
import net.minecraft.util.datafix.FixTypes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;

@Mod.EventBusSubscriber(modid = Frostbite.MODID)
public final class LegacyRegistryMappings {
    private LegacyRegistryMappings() {}

    public static void registerDataFixes() {
        ModFixs fixes = FMLCommonHandler.instance().getDataFixer().init(Frostbite.MODID, 1);
        IFixableData namespaceFix =
                new IFixableData() {
                    @Override
                    public int getFixVersion() {
                        return 1;
                    }

                    @Override
                    public NBTTagCompound fixTagCompound(NBTTagCompound tag) {
                        return migrateId(tag);
                    }
                };
        fixes.registerFix(FixTypes.BLOCK_ENTITY, namespaceFix);
        fixes.registerFix(FixTypes.ENTITY, namespaceFix);
        fixes.registerFix(FixTypes.ITEM_INSTANCE, namespaceFix);
    }

    public static NBTTagCompound migrateId(NBTTagCompound tag) {
        if (tag.hasKey("id", 8)) {
            String id = tag.getString("id");
            int separator = id.indexOf(':');
            if (separator > 0 && isLegacyNamespace(id.substring(0, separator))) {
                tag.setString("id", Frostbite.MODID + id.substring(separator));
            }
        }
        return tag;
    }

    public static boolean isLegacyNamespace(String namespace) {
        return Frostbite.PREVIOUS_MODID.equals(namespace)
                || Frostbite.LEGACY_MODID.equals(namespace);
    }

    public static ResourceLocation replacement(ResourceLocation oldName) {
        return isLegacyNamespace(oldName.getResourceDomain())
                ? new ResourceLocation(Frostbite.MODID, oldName.getResourcePath())
                : oldName;
    }

    private static <T extends IForgeRegistryEntry<T>> void remap(
            RegistryEvent.MissingMappings<T> event, IForgeRegistry<T> registry) {
        for (RegistryEvent.MissingMappings.Mapping<T> mapping : event.getAllMappings()) {
            if (!isLegacyNamespace(mapping.key.getResourceDomain())) continue;
            ResourceLocation name = replacement(mapping.key);
            if (registry.containsKey(name)) mapping.remap(registry.getValue(name));
        }
    }

    @SubscribeEvent
    public static void blocks(RegistryEvent.MissingMappings<Block> event) {
        remap(event, ForgeRegistries.BLOCKS);
    }

    @SubscribeEvent
    public static void items(RegistryEvent.MissingMappings<Item> event) {
        remap(event, ForgeRegistries.ITEMS);
    }

    @SubscribeEvent
    public static void biomes(RegistryEvent.MissingMappings<Biome> event) {
        remap(event, ForgeRegistries.BIOMES);
    }

    @SubscribeEvent
    public static void entities(RegistryEvent.MissingMappings<EntityEntry> event) {
        remap(event, ForgeRegistries.ENTITIES);
    }
}
