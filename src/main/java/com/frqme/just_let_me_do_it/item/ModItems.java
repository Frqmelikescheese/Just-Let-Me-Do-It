package com.frqme.just_let_me_do_it.item;
import com.frqme.just_let_me_do_it.Just_let_me_do_it;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
public class ModItems {
    public static final CopperGolemCoreItem COPPER_GOLEM_CORE = Registry.register(
            Registries.ITEM,
            Identifier.of(Just_let_me_do_it.MOD_ID, "copper_golem_core"),
            new CopperGolemCoreItem(new Item.Settings()
                    .maxCount(1)
                    .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Just_let_me_do_it.MOD_ID, "copper_golem_core"))))
    );
    public static void register() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(COPPER_GOLEM_CORE);
        });
        Just_let_me_do_it.LOGGER.info("Registering mod items");
    }
}
