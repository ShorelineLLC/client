package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.shoreline.client.impl.module.misc.RekitModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends MixinScreen
{
    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    public abstract T getScreenHandler();

    @Inject(method = "init", at = @At(value = "RETURN"))
    private void hookInit(CallbackInfo info)
    {
        RekitModule rekit = RekitModule.getInstance();
        if (rekit.isEnabled() && rekit.isValidHandler(getScreenHandler()))
        {
            addDrawableChild(
                    new ButtonWidget.Builder(Text.literal("Rekit"), button -> client.execute(rekit::updateMismatch))
                            .position(x - 45, y)
                            .size(40, 20)
                            .build()
            );
        }
    }
}
