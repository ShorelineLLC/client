package net.shoreline.client.mixin.input;

import net.minecraft.client.input.KeyboardInput;
import net.shoreline.client.impl.event.input.PlayerInputEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public abstract class MixinKeyboardInput extends MixinInput
{
    @Inject(method = "tick", at = @At(value = "TAIL"), cancellable = true)
    private void hookTick(CallbackInfo ci)
    {
        PlayerInputEvent inputEvent = new PlayerInputEvent(movementVector);
        EventBus.INSTANCE.dispatch(inputEvent);

        if (inputEvent.isCanceled())
        {
            ci.cancel();
            movementVector = inputEvent.getMovementInput();
        }
    }
}
