package net.shoreline.client.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.shoreline.client.impl.event.network.RotationUpdateEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class MixinClientPlayNetworkHandler
{
    @Inject(method = "onPlayerPositionLook", at = @At(value = "TAIL"))
    public void hookPlayerPositionLook(PlayerPositionLookS2CPacket packet,
                                       CallbackInfo ci)
    {
        RotationUpdateEvent event = new RotationUpdateEvent(MinecraftClient.getInstance().player.getYaw(),
                MinecraftClient.getInstance().player.getPitch());
        EventBus.INSTANCE.dispatch(event);
    }
}
