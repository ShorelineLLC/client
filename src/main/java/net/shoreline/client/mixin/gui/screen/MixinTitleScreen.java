package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.gui.titlescreen.ShorelineMenuScreen;
import net.shoreline.client.gui.titlescreen.particle.snow.SnowManager;
import net.shoreline.client.impl.module.client.TitleScreenModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public abstract class MixinTitleScreen extends MixinScreen
{
    @Shadow
    private long backgroundFadeStart;

    @Shadow
    private boolean doBackgroundFade;

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void initHook(CallbackInfo info)
    {
        if (TitleScreenModule.INSTANCE.isEnabled())
        {
            client.setScreen(new ShorelineMenuScreen());
        }
    }

    @Inject(method = "render", at = @At("TAIL"))
    public void hookRender(DrawContext context,
                           int mouseX,
                           int mouseY,
                           float delta,
                           CallbackInfo ci)
    {
        float f = doBackgroundFade ? (float) (Util.getMeasuringTimeMs() - backgroundFadeStart) / 1000.0f : 1.0f;
        float g = doBackgroundFade ? MathHelper.clamp(f - 1.0f, 0.0f, 1.0f) : 1.0f;
        int i = MathHelper.ceil(g * 255.0f) << 24;
        if ((i & 0xFC000000) == 0)
        {
            return;
        }

        context.drawTextWithShadow(client.textRenderer, ShorelineMod.getFormattedVersion(),
                2, height - (client.textRenderer.fontHeight * 2) - 2, 0xffffff | i);
    }
}
