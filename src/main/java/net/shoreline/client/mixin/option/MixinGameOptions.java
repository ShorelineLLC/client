package net.shoreline.client.mixin.option;

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import net.shoreline.client.impl.event.option.FovEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;

@Mixin(GameOptions.class)
public class MixinGameOptions
{
    @Mutable
    @Shadow
    @Final
    private SimpleOption<Integer> fov;

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/GameOptions;load()V", shift = At.Shift.BEFORE))
    private void hookInit(MinecraftClient client, File optionsFile, CallbackInfo info)
    {
        this.fov = new SimpleOption<>("options.fov", SimpleOption.emptyTooltip(), (optionText, value) -> switch (value) {
            case 70 -> GameOptions.getGenericValueText(optionText, Text.translatable("options.fov.min"));
            case 150 -> GameOptions.getGenericValueText(optionText, Text.translatable("options.fov.max"));
            default -> GameOptions.getGenericValueText(optionText, value);
        }, new SimpleOption.ValidatingIntSliderCallbacks(30, 150),
                Codec.DOUBLE.xmap(value -> {
                            FovEvent fovEvent = new FovEvent();
                            EventBus.INSTANCE.dispatch(fovEvent);
                            return (int) ((fovEvent.isCanceled() ? fovEvent.getFov() : value) * 40.0 + 70.0);
                        },
                        value ->
                        {
                            FovEvent fovEvent = new FovEvent();
                            EventBus.INSTANCE.dispatch(fovEvent);
                            return ((fovEvent.isCanceled() ? fovEvent.getFov() : (double) value.intValue()) - 70.0) / 40.0;
                        }),
                70,
                value -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate());
    }
}
