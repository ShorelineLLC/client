package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Screen.class)
public abstract class MixinScreen
{
    @Shadow
    protected MinecraftClient client;

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    @Inject(method = "keyPressed", at = @At(value = "HEAD"))
    private void hookKeyPressed(int keyCode,
                                int scanCode,
                                int modifiers,
                                CallbackInfoReturnable<Boolean> cir)
    {
        if ((Object) this instanceof TitleScreen
                || (Object) this instanceof MultiplayerScreen
                || (Object) this instanceof SelectWorldScreen)
        {
            if (keyCode == ClickGuiModule.INSTANCE.getKeybindMacro().getKeycode())
            {
                ClickGuiModule.INSTANCE.enable();
            }
        }
    }
}
