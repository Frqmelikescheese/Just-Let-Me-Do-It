package com.frqme.just_let_me_do_it.entity;
import com.frqme.just_let_me_do_it.CopperGolemEntity;
import com.frqme.just_let_me_do_it.Just_let_me_do_it;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
public class ModEntities {
    public static final EntityType<CopperGolemEntity> COPPER_GOLEM = Registry.register(
            Registries.ENTITY_TYPE,
            Identifier.of(Just_let_me_do_it.MOD_ID, "copper_golem"),
            EntityType.Builder.create(CopperGolemEntity::new, SpawnGroup.CREATURE)
                    .dimensions(0.6f, 1.4f)
                    .build(RegistryKey.of(RegistryKeys.ENTITY_TYPE, Identifier.of(Just_let_me_do_it.MOD_ID, "copper_golem")))
    );
    public static void register() {
        Just_let_me_do_it.LOGGER.info("Registering mod entities");
    }
}
