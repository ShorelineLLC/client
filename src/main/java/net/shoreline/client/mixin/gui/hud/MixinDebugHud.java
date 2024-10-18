package net.shoreline.client.mixin.gui.hud;

import net.minecraft.client.gui.hud.DebugHud;
import net.shoreline.client.BuildConfig;
import net.shoreline.client.ShorelineMod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * @author hockeyl8
 * @since 1.0
 */
@Mixin(DebugHud.class)
public abstract class MixinDebugHud
{
    @Shadow
    protected abstract List<String> getLeftText();

    @Redirect(method = "drawLeftText", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/DebugHud;getLeftText()Ljava/util/List;"))
    private List<String> redirectRightTextEarly(DebugHud instance)
    {
        List<String> list = getLeftText();
        String modName = String.format("%s %s (%s%s%s)",
                ShorelineMod.MOD_NAME, ShorelineMod.MOD_VER, BuildConfig.BUILD_IDENTIFIER,
                !BuildConfig.BUILD_IDENTIFIER.equals("dev") ? "-" + BuildConfig.BUILD_NUMBER : "",
                !BuildConfig.HASH.equals("null") ? "-" + BuildConfig.HASH : "");

        list.add(1, modName);
        return list;
    }
}
