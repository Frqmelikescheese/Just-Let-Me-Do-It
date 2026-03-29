package com.frqme.just_let_me_do_it;
import com.frqme.just_let_me_do_it.ai.GolemAIBrain;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
public class CopperGolemEntity extends net.minecraft.entity.passive.CopperGolemEntity {
    private static final ConcurrentHashMap<UUID, String> API_KEY_STORE = new ConcurrentHashMap<>();
    private boolean chestOpen = false;
    private int chestOpenTicks = 0;
    private GolemAIBrain aiBrain;
    private List<GolemAIBrain.BuildStep> buildQueue = null;
    private int buildQueueIndex = 0;
    private int buildTickCooldown = 0;
    public CopperGolemEntity(EntityType<? extends net.minecraft.entity.passive.CopperGolemEntity> entityType, World world) {
        super(entityType, world);
        this.aiBrain = new GolemAIBrain(this);
    }
    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.MAX_HEALTH, 100.0)
                .add(EntityAttributes.MOVEMENT_SPEED, 0.25)
                .add(EntityAttributes.KNOCKBACK_RESISTANCE, 0.5);
    }
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        if (!this.getEntityWorld().isClient()) {
            setChestOpen(true);
            if (this.getEntityWorld() instanceof ServerWorld) {
                com.frqme.just_let_me_do_it.network.ModPackets.sendOpenDialogPacket(player, this.getId());
            }
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.getEntityWorld().isClient() && aiBrain.getApiKey() == null) {
            String storedKey = API_KEY_STORE.get(this.getUuid());
            if (storedKey != null) {
                aiBrain.setApiKey(storedKey);
            }
        }
        if (chestOpen) {
            chestOpenTicks++;
            if (chestOpenTicks > 60) {
                setChestOpen(false);
                chestOpenTicks = 0;
            }
        }
        if (!this.getEntityWorld().isClient() && buildQueue != null && buildQueueIndex < buildQueue.size()) {
            buildTickCooldown--;
            if (buildTickCooldown <= 0) {
                for (int i = 0; i < 5 && buildQueue != null && buildQueueIndex < buildQueue.size(); i++) {
                    executeBuildStep();
                }
                buildTickCooldown = 2;
            }
        }
    }
    private void executeBuildStep() {
        if (buildQueue == null || buildQueueIndex >= buildQueue.size()) return;
        GolemAIBrain.BuildStep step = buildQueue.get(buildQueueIndex);
        buildQueueIndex++;
        BlockPos pos = new BlockPos(step.x(), step.y(), step.z());
        String blockId = step.blockId();
        if (!blockId.contains(":")) blockId = "minecraft:" + blockId;
        Block block = Registries.BLOCK.get(Identifier.of(blockId));
        BlockState state = block.getDefaultState();
        if (step.properties() != null && !step.properties().isEmpty()) {
            state = applyProperties(state, step.properties());
        }
        this.getEntityWorld().setBlockState(pos, state, Block.NOTIFY_ALL);
        if (buildQueueIndex == buildQueue.size()) {
            final int total = buildQueue.size();
            this.getEntityWorld().getPlayers().forEach(p -> {
                if (p.squaredDistanceTo(this) < 2500) {
                    p.sendMessage(Text.literal("[Copper Golem] Build complete! Placed " + total + " blocks."), false);
                }
            });
            buildQueue = null;
            buildQueueIndex = 0;
        }
    }
    @SuppressWarnings({"unchecked", "rawtypes"})
    private BlockState applyProperties(BlockState state, Map<String, String> props) {
        for (Map.Entry<String, String> entry : props.entrySet()) {
            String key   = entry.getKey().toLowerCase().trim();
            String value = entry.getValue().toLowerCase().trim();
            try {
                Property<?> property = state.getBlock().getStateManager().getProperty(key);
                if (property == null) continue;
                if (property instanceof BooleanProperty bp) {
                    boolean bval = value.equals("true") || value.equals("1");
                    state = state.with(bp, bval);
                } else if (property instanceof IntProperty ip) {
                    try {
                        int intVal = Integer.parseInt(value);
                        if (ip.getValues().contains(intVal)) {
                            state = state.with(ip, intVal);
                        }
                    } catch (NumberFormatException ignored) {}
                } else if (property instanceof EnumProperty ep) {
                    for (Object val : ep.getValues()) {
                        String valName;
                        if (val instanceof StringIdentifiable si) {
                            valName = si.asString();
                        } else {
                            valName = val.toString().toLowerCase();
                        }
                        if (valName.equalsIgnoreCase(value)) {
                            state = state.with(ep, (Comparable) val);
                            break;
                        }
                    }
                } else {
                    Optional<?> parsed = property.parse(value);
                    if (parsed.isPresent()) {
                        state = state.with((Property) property, (Comparable) parsed.get());
                    }
                }
            } catch (Exception ignored) {
            }
        }
        return state;
    }
    public void receiveBuildPlan(List<GolemAIBrain.BuildStep> steps) {
        this.buildQueue = steps;
        this.buildQueueIndex = 0;
        this.buildTickCooldown = 5;
        setChestOpen(false);
        this.getEntityWorld().getPlayers().forEach(p -> {
            if (p.squaredDistanceTo(this) < 2500) {
                p.sendMessage(Text.literal("[Copper Golem] Starting build: " + steps.size() + " blocks..."), false);
            }
        });
    }
    public void receivePrompt(String prompt, PlayerEntity player) {
        player.sendMessage(Text.literal("[Copper Golem] Thinking..."), false);
        setChestOpen(true);
        World world = this.getEntityWorld();
        BlockPos origin = this.getBlockPos();
        Thread aiThread = new Thread(() -> {
            try {
                List<GolemAIBrain.BuildStep> steps = aiBrain.processPrompt(prompt, origin);
                world.getServer().execute(() -> {
                    if (steps != null && !steps.isEmpty()) {
                        receiveBuildPlan(steps);
                        player.sendMessage(Text.literal("[Copper Golem] Building: " + prompt), false);
                    } else {
                        player.sendMessage(Text.literal("[Copper Golem] Couldn't figure that out. Try again?"), false);
                        setChestOpen(false);
                    }
                });
            } catch (Exception e) {
                world.getServer().execute(() -> {
                    player.sendMessage(Text.literal("[Copper Golem] AI error: " + e.getMessage()), false);
                    setChestOpen(false);
                });
            }
        });
        aiThread.setDaemon(true);
        aiThread.start();
    }
    public boolean isChestOpen() { return chestOpen; }
    public void setChestOpen(boolean open) { this.chestOpen = open; }
    public GolemAIBrain getAiBrain() { return aiBrain; }
    public void storeApiKey(String apiKey) {
        aiBrain.setApiKey(apiKey);
        if (apiKey != null) {
            API_KEY_STORE.put(this.getUuid(), apiKey);
        } else {
            API_KEY_STORE.remove(this.getUuid());
        }
    }
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (reason == RemovalReason.KILLED || reason == RemovalReason.DISCARDED) {
            API_KEY_STORE.remove(this.getUuid());
        }
    }
    @Override
    public ItemStack getPickBlockStack() {
        return new ItemStack(com.frqme.just_let_me_do_it.item.ModItems.COPPER_GOLEM_CORE);
    }
}
