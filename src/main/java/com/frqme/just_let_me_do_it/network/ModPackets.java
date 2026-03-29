package com.frqme.just_let_me_do_it.network;
import com.frqme.just_let_me_do_it.CopperGolemEntity;
import com.frqme.just_let_me_do_it.Just_let_me_do_it;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
public class ModPackets {
    public record OpenDialogPayload(int entityId) implements CustomPayload {
        public static final CustomPayload.Id<OpenDialogPayload> ID =
                new CustomPayload.Id<>(Identifier.of(Just_let_me_do_it.MOD_ID, "open_dialog"));
        public static final PacketCodec<PacketByteBuf, OpenDialogPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, OpenDialogPayload::entityId, OpenDialogPayload::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record SendPromptPayload(int entityId, String prompt) implements CustomPayload {
        public static final CustomPayload.Id<SendPromptPayload> ID =
                new CustomPayload.Id<>(Identifier.of(Just_let_me_do_it.MOD_ID, "send_prompt"));
        public static final PacketCodec<PacketByteBuf, SendPromptPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, SendPromptPayload::entityId,
                        PacketCodecs.STRING, SendPromptPayload::prompt,
                        SendPromptPayload::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record SetApiKeyPayload(int entityId, String apiKey) implements CustomPayload {
        public static final CustomPayload.Id<SetApiKeyPayload> ID =
                new CustomPayload.Id<>(Identifier.of(Just_let_me_do_it.MOD_ID, "set_api_key"));
        public static final PacketCodec<PacketByteBuf, SetApiKeyPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, SetApiKeyPayload::entityId,
                        PacketCodecs.STRING, SetApiKeyPayload::apiKey,
                        SetApiKeyPayload::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public record SetModelPayload(int entityId, String modelId) implements CustomPayload {
        public static final CustomPayload.Id<SetModelPayload> ID =
                new CustomPayload.Id<>(Identifier.of(Just_let_me_do_it.MOD_ID, "set_model"));
        public static final PacketCodec<PacketByteBuf, SetModelPayload> CODEC =
                PacketCodec.tuple(PacketCodecs.INTEGER, SetModelPayload::entityId,
                        PacketCodecs.STRING, SetModelPayload::modelId,
                        SetModelPayload::new);
        @Override public CustomPayload.Id<? extends CustomPayload> getId() { return ID; }
    }
    public static void registerServerReceivers() {
        PayloadTypeRegistry.playS2C().register(OpenDialogPayload.ID, OpenDialogPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SendPromptPayload.ID, SendPromptPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetApiKeyPayload.ID, SetApiKeyPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(SetModelPayload.ID, SetModelPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(SendPromptPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerWorld world = (ServerWorld) context.player().getEntityWorld();
                Entity entity = world.getEntityById(payload.entityId());
                if (entity instanceof CopperGolemEntity golem) {
                    golem.receivePrompt(payload.prompt(), context.player());
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SetApiKeyPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerWorld world = (ServerWorld) context.player().getEntityWorld();
                Entity entity = world.getEntityById(payload.entityId());
                if (entity instanceof CopperGolemEntity golem) {
                    golem.storeApiKey(payload.apiKey());
                    context.player().sendMessage(
                            net.minecraft.text.Text.literal("[Copper Golem] API key saved!"), false
                    );
                }
            });
        });
        ServerPlayNetworking.registerGlobalReceiver(SetModelPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                ServerWorld world = (ServerWorld) context.player().getEntityWorld();
                Entity entity = world.getEntityById(payload.entityId());
                if (entity instanceof CopperGolemEntity golem) {
                    golem.getAiBrain().setModelId(payload.modelId());
                    context.player().sendMessage(
                            net.minecraft.text.Text.literal("[Copper Golem] Using model: " + payload.modelId()), false
                    );
                }
            });
        });
    }
    public static void sendOpenDialogPacket(net.minecraft.entity.player.PlayerEntity player, int entityId) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, new OpenDialogPayload(entityId));
        }
    }
}
