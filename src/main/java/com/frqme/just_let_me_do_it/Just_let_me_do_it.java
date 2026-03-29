package com.frqme.just_let_me_do_it;
import com.frqme.just_let_me_do_it.entity.ModEntities;
import com.frqme.just_let_me_do_it.item.ModItems;
import com.frqme.just_let_me_do_it.network.ModPackets;
import com.frqme.just_let_me_do_it.recipe.ModRecipes;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class Just_let_me_do_it implements ModInitializer {
    public static final String MOD_ID = "just_let_me_do_it";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    @Override
    public void onInitialize() {
        ModEntities.register();
        ModItems.register();
        ModRecipes.register();
        ModPackets.registerServerReceivers();
        FabricDefaultAttributeRegistry.register(
            ModEntities.COPPER_GOLEM,
            CopperGolemEntity.createAttributes()
        );
        LOGGER.info("Just Let Me Do It initialized!");
    }
}
