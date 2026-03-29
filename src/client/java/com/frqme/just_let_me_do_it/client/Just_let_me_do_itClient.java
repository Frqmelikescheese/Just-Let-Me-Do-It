package com.frqme.just_let_me_do_it.client;
import com.frqme.just_let_me_do_it.entity.ModEntities;
import com.frqme.just_let_me_do_it.network.ModPackets;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
public class Just_let_me_do_itClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntities.COPPER_GOLEM, CopperGolemEntityRenderer::new);
        ClientPlayNetworking.registerGlobalReceiver(ModPackets.OpenDialogPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                context.client().setScreen(new GolemDialogScreen(payload.entityId()));
            });
        });
    }
}
