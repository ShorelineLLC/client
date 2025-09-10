package net.shoreline.client.mixin.option;

import com.mojang.serialization.Codec;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
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
                Codec.DOUBLE.xmap(value -> (int)(value * 40.0 + 70.0),
                        value -> ((double) value.intValue() - 70.0) / 40.0),
                70,
                value -> MinecraftClient.getInstance().worldRenderer.scheduleTerrainUpdate());
    }
}
