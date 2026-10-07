package net.shoreline.client.mixin;

import net.minecraft.client.Mouse;
import net.minecraft.client.network.ClientPlayerEntity;
import net.shoreline.client.impl.event.InputEvent;
import net.shoreline.client.impl.event.MouseEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class MixinMouse
{
    @Inject(method = "onMouseButton", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBinding;setKeyPressed(Lnet/minecraft/client/util/InputUtil$Key;Z)V"))
    private void hookOnMouseButton(long window, int button, int action, int mods, CallbackInfo ci)
    {
        InputEvent.Mouse inputEvent = new InputEvent.Mouse(button, action, mods);
        EventBus.INSTANCE.dispatch(inputEvent);
    }

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
