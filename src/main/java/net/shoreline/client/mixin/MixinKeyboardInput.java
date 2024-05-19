package net.shoreline.client.mixin;

import net.minecraft.client.input.Input;
import net.minecraft.client.input.KeyboardInput;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.api.event.EventStage;
import net.shoreline.client.impl.event.keyboard.KeyboardTickEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author Shoreline
 * @since 1.0
 */
@Mixin(KeyboardInput.class)
public class MixinKeyboardInput {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void hookTick$Pre(boolean slowDown, float slowDownFactor, CallbackInfo info)
    {
        KeyboardTickEvent event = new KeyboardTickEvent((Input) (Object) this);
        event.setStage(EventStage.PRE);
        EventBus.EVENT_HANDLER.dispatch(event);
        if (event.isCanceled())
        {
            info.cancel();
        }
    }

    /**
     * @param slowDown
     * @param f
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/" +
            "client/input/KeyboardInput;sneaking:Z", shift = At.Shift.BEFORE), cancellable = true)
    private void hookTick$Post(boolean slowDown, float f, CallbackInfo ci) {
        KeyboardTickEvent keyboardTickEvent = new KeyboardTickEvent((Input) (Object) this);
        keyboardTickEvent.setStage(EventStage.POST);
        EventBus.EVENT_HANDLER.dispatch(keyboardTickEvent);
        if (keyboardTickEvent.isCanceled()) {
            ci.cancel();
        }
    }
}
