package net.shoreline.client.mixin.network;

import net.minecraft.block.BlockState;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkThreadUtils;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
import net.minecraft.network.packet.s2c.play.*;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import net.shoreline.client.impl.event.gui.chat.ChatMessageEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.impl.event.network.InventoryEvent;
import net.shoreline.client.impl.event.network.ServerRotationEvent;
import net.shoreline.client.impl.event.world.LoadChunkBlockEvent;
import net.shoreline.client.impl.event.world.LoadChunkEvent;
import net.shoreline.client.impl.imixin.IClientPlayNetworkHandler;
import net.shoreline.client.mixin.accessor.AccessorClientConnection;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
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
public abstract class MixinClientPlayNetworkHandler implements IClientPlayNetworkHandler, Globals
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
        LoadChunkEvent loadChunkEvent = new LoadChunkEvent(chunk);
        EventBus.INSTANCE.dispatch(loadChunkEvent);
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

    @Inject(method = "onPlayerPositionLook", at = @At(value = "HEAD"), cancellable = true)
    private void onPlayerPositionLook(PlayerPositionLookS2CPacket packet, CallbackInfo ci)
    {
        ServerRotationEvent serverRotationEvent = new ServerRotationEvent();
        EventBus.INSTANCE.dispatch(serverRotationEvent);
        if (serverRotationEvent.isCanceled())
        {
            ci.cancel();
            double i;
            double h;
            double g;
            double f;
            double e;
            double d;
            NetworkThreadUtils.forceMainThread(packet, (ClientPlayNetworkHandler) (Object) this, mc);
            ClientPlayerEntity playerEntity = mc.player;
            Vec3d vec3d = playerEntity.getVelocity();
            boolean bl = packet.getFlags().contains(PositionFlag.X);
            boolean bl2 = packet.getFlags().contains(PositionFlag.Y);
            boolean bl3 = packet.getFlags().contains(PositionFlag.Z);
            if (bl) {
                d = vec3d.getX();
                e = playerEntity.getX() + packet.getX();
                playerEntity.lastRenderX += packet.getX();
                playerEntity.prevX += packet.getX();
            } else {
                d = 0.0;
                playerEntity.lastRenderX = e = packet.getX();
                playerEntity.prevX = e;
            }
            if (bl2) {
                f = vec3d.getY();
                g = playerEntity.getY() + packet.getY();
                playerEntity.lastRenderY += packet.getY();
                playerEntity.prevY += packet.getY();
            } else {
                f = 0.0;
                playerEntity.lastRenderY = g = packet.getY();
                playerEntity.prevY = g;
            }
            if (bl3) {
                h = vec3d.getZ();
                i = playerEntity.getZ() + packet.getZ();
                playerEntity.lastRenderZ += packet.getZ();
                playerEntity.prevZ += packet.getZ();
            } else {
                h = 0.0;
                playerEntity.lastRenderZ = i = packet.getZ();
                playerEntity.prevZ = i;
            }
            float yaw = serverRotationEvent.getYaw();
            float pitch = serverRotationEvent.getPitch();
            playerEntity.setPosition(e, g, i);
            playerEntity.setVelocity(d, f, h);
            getConnection().send(new TeleportConfirmC2SPacket(packet.getTeleportId()));
            getConnection().send(new PlayerMoveC2SPacket.Full(playerEntity.getX(), playerEntity.getY(),
                    playerEntity.getZ(), Float.isNaN(yaw) ? playerEntity.getYaw() : yaw,
                    Float.isNaN(pitch) ? playerEntity.getPitch() : pitch, false));
        }
    }

    @Override
    public void sendQuietPacket(Packet<?> packet)
    {
        ((AccessorClientConnection) getConnection()).hookSendInternal(packet, null, true);
    }
}
