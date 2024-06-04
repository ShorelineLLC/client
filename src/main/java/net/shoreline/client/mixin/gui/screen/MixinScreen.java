package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.List;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(Screen.class)
public abstract class MixinScreen
{
    //
    @Shadow
    public int width;
    //
    @Shadow
    public int height;
    //
    @Shadow
    @Final
    private List<Drawable> drawables;

    @Shadow
    @Final
    protected Text title;

    @Shadow
    @Nullable
    protected MinecraftClient client;

    /**
     * @param drawableElement
     * @param <T>
     * @return
     */
    @Shadow
    protected abstract <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement);

    /**
     *
     */
    @Shadow
    public void tick()
    {

    }

    /**
     * @return
     */
    @Unique
    public List<Drawable> getDrawables()
    {
        return drawables;
    }
}
