package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Toggleable
{
    public static ClickGuiModule INSTANCE;

    Config<Float> scaleConfig = new NumberConfig.Builder<Float>("Scale")
            .setMin(0.5f).setMax(1.5f).setDefaultValue(1.0f)
            .setDescription("The global gui scale").build();
    Config<Boolean> blurConfig = new BooleanConfig.Builder("Blur")
            .setDescription("Blurs the screen background")
            .setDefaultValue(true).build();
    Config<Boolean> darkenConfig = new BooleanConfig.Builder("Darken")
            .setDescription("Darkens the screen background")
            .setDefaultValue(true).build();
    Config<Integer> scrollSpeedConfig = new NumberConfig.Builder<Integer>("ScrollSpeed")
            .setMin(5).setMax(100).setDefaultValue(30).setFormat("dpi")
            .setDescription("The speed for mouse scrolling").build();

    @Getter
    private final Theme theme;
    private final Animation fadeInAnimation;

    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.fadeInAnimation = new Animation(false, 400, Easing.SINE_OUT);
        this.theme = new Theme(fadeInAnimation);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (checkNull())
        {
            disable();
            return;
        }

        ThemeModule primaryTheme = ThemeModule.INSTANCE;
        theme.setComponentColor(primaryTheme.getPrimaryColor());
        theme.setTitleColor(primaryTheme.getTitleColor());
        theme.setBackgroundColor(primaryTheme.getBackgroundColor());
        theme.setOutlineColor(primaryTheme.getOutlineColor());
        theme.setTextColor(primaryTheme.getTextColor());

        setFadeState(true);
        mc.setScreen(ClickGuiScreen.INSTANCE);
    }

    @Override
    public void onDisable()
    {
        if (checkNull())
        {
            return;
        }

        mc.player.closeScreen();
        setFadeState(false);
    }

    public void setFadeState(boolean fadeState)
    {
        if (fadeState)
        {
            fadeInAnimation.setState(true);
            fadeInAnimation.reset();
        } else
        {
            fadeInAnimation.setState(false);
        }
    }

    public Animation getFadeAnimation()
    {
        return fadeInAnimation;
    }

    public float getScale()
    {
        return scaleConfig.getValue();
    }

    public boolean shouldBlur()
    {
        return blurConfig.getValue();
    }

    public boolean shouldDarken()
    {
        return darkenConfig.getValue();
    }

    public int getScrollSpeed()
    {
        return scrollSpeedConfig.getValue();
    }
}
