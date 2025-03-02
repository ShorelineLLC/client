package net.shoreline.client.impl.module.world;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BlockListConfig;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.AttackBlockEvent;
import net.shoreline.client.impl.event.network.InteractBlockEvent;
import net.shoreline.client.impl.event.network.InteractBorderEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.BlastResistantBlocks;
import net.shoreline.eventbus.annotation.EventListener;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
public class AntiInteractModule extends ToggleModule
{
    //
    Config<List<Block>> blacklistConfig = register(new BlockListConfig<>("Blacklist", "Valid block blacklist"));
    Config<Boolean> miningFixConfig = register(new BooleanConfig("MiningFix", "Fixes vanilla mining on GrimV3", false));
    Config<Boolean> borderConfig = register(new BooleanConfig("Border", "Prevents interacting with the world border", true));

    public AntiInteractModule()
    {
        super("AntiInteract", "Prevents player from interacting with certain objects", ModuleCategory.WORLD);
    }

    @EventListener
    public void onInteractBlock(InteractBlockEvent event)
    {
        BlockPos pos = event.getHitResult().getBlockPos();
        BlockState state = mc.world.getBlockState(pos);
        if (((BlockListConfig<?>) blacklistConfig).contains(state.getBlock()))
        {
            event.cancel();
            // Managers.NETWORK.sendSequencedPacket(sequence -> new PlayerInteractBlockC2SPacket(
            //        event.getHand(), event.getHitResult(), sequence));
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || mc.world == null)
        {
            return;
        }
        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet)
        {
            BlockPos pos = packet.getBlockHitResult().getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            if (((BlockListConfig<?>) blacklistConfig).contains(state.getBlock()))
            {
                event.cancel();
            }
        }

        if (miningFixConfig.getValue() && event.getPacket() instanceof PlayerActionC2SPacket packet
                && (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK
                || packet.getAction() == PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK))
        {
            if (BlastResistantBlocks.isUnbreakable(packet.getPos()) || mc.world.isAir(packet.getPos()))
            {
                event.cancel();
                return;
            }

            if (packet.getAction() == PlayerActionC2SPacket.Action.START_DESTROY_BLOCK)
            {
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                Managers.NETWORK.sendQuietPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, packet.getPos(), Direction.UP));
                event.cancel();
            }
        }
    }

    @EventListener
    public void onInteractBorder(InteractBorderEvent event)
    {
        if (!borderConfig.getValue() || mc.player.getMainHandStack().getItem() instanceof BlockItem)
        {
            return;
        }
        event.cancel();
    }
}