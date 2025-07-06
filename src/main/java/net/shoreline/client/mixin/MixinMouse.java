package net.shoreline.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.shoreline.client.impl.event.MouseEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Mouse.class)
public class MixinMouse
{
    @Redirect(method = "updateMouse", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;changeLookDirection(DD)V"))
    public void hookUpdateMouse(ClientPlayerEntity instance, double cursorDeltaX, double cursorDeltaY)
    {
        MouseEvent mouseUpdateEvent = new MouseEvent(cursorDeltaX, cursorDeltaY);
        EventBus.INSTANCE.dispatch(mouseUpdateEvent);

        if (!mouseUpdateEvent.isCanceled())
        {
            instance.changeLookDirection(cursorDeltaX, cursorDeltaY);
        }
    }
}
