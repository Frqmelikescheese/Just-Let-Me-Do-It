package com.frqme.just_let_me_do_it.item;
import com.frqme.just_let_me_do_it.CopperGolemEntity;
import com.frqme.just_let_me_do_it.entity.ModEntities;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import java.util.List;
public class CopperGolemCoreItem extends Item {
    public CopperGolemCoreItem(Settings settings) {
        super(settings);
    }
    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        if (!world.isClient() && world instanceof ServerWorld serverWorld) {
            BlockPos pos = context.getBlockPos().up();
            CopperGolemEntity golem = ModEntities.COPPER_GOLEM.create(serverWorld, SpawnReason.COMMAND);
            if (golem != null) {
                golem.setPosition(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
                if (context.getPlayer() != null) {
                    double dx = context.getPlayer().getX() - golem.getX();
                    double dz = context.getPlayer().getZ() - golem.getZ();
                    golem.setYaw((float)(Math.atan2(-dx, dz) * (180.0 / Math.PI)));
                }
                serverWorld.spawnEntity(golem);
                PlayerEntity player = context.getPlayer();
                if (player != null) {
                    player.sendMessage(Text.literal("[Copper Golem] Summoned! Right-click me to give orders."), false);
                    if (!player.isCreative()) {
                        context.getStack().decrement(1);
                    }
                }
                return ActionResult.SUCCESS;
            }
        }
        return ActionResult.PASS;
    }
    public void appendTooltip(ItemStack stack, TooltipContext context, List<Text> tooltip, TooltipType type) {
        tooltip.add(Text.literal("Right-click a block to summon your AI builder"));
        tooltip.add(Text.literal("Then right-click the golem to give it instructions"));
    }
}
