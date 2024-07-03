package net.shoreline.client.mixin.network;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.ChunkDataS2CPacket;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import net.shoreline.client.impl.event.world.LoadChunkBlockEvent;
import net.shoreline.eventbus.EventBus;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.InventoryEvent;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.client.mixin.accessor.AccessorClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientPlayNetworkHandler.class)
public abstract class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler
{
    @Shadow
    public abstract ClientConnection getConnection();

    @Shadow
    private ClientWorld world;

    /**
     * @param content
     * @param ci
     */
    @Inject(method = "sendChatMessage", at = @At(value = "HEAD"),
            cancellable = true)
    private void hookSendChatMessage(String content, CallbackInfo ci)
    {
        ChatMessageEvent.Server chatInputEvent =
                new ChatMessageEvent.Server(content);
        EventBus.INSTANCE.dispatch(chatInputEvent);
        // prevent chat packet from sending
        if (chatInputEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onGameJoin", at = @At(value = "TAIL"))
    private void hookOnGameJoin(GameJoinS2CPacket packet, CallbackInfo ci)
    {
        GameJoinEvent gameJoinEvent = new GameJoinEvent();
        EventBus.INSTANCE.dispatch(gameJoinEvent);
    }

    /**
     * @param packet
     * @param ci
     */
    @Inject(method = "onInventory", at = @At(value = "TAIL"))
    private void hookOnInventory(InventoryS2CPacket packet, CallbackInfo ci)
    {
        InventoryEvent inventoryEvent = new InventoryEvent(packet);
        EventBus.INSTANCE.dispatch(inventoryEvent);
    }

    @Inject(method = "onChunkData", at = @At(value = "RETURN"))
    private void hookOnChunkData(ChunkDataS2CPacket packet, CallbackInfo ci)
    {
        WorldChunk chunk = world.getChunkManager().getWorldChunk(packet.getChunkX(), packet.getChunkZ(), false);
        int startX = chunk.getPos().getStartX();
        int startZ = chunk.getPos().getStartZ();

        for (int y = chunk.getBottomY(); y < chunk.getHeight(); y++)
        {
            for (int x1 = startX; x1 < startX + 16; x1++)
            {
                for (int z1 = startZ; z1 < startZ + 16; z1++)
                {
                    BlockPos pos = new BlockPos(x1, y, z1);
                    BlockState state = chunk.getBlockState(pos);
                    LoadChunkBlockEvent loadChunkBlockEvent = new LoadChunkBlockEvent(pos, state);
                    EventBus.INSTANCE.dispatch(loadChunkBlockEvent);
                }
            }
        }
    }

    @Override
    public void sendQuietPacket(Packet<?> packet)
    {
        ((AccessorClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }
}
