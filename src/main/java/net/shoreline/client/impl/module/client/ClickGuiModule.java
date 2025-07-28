package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Toggleable
{
    public static ClickGuiModule INSTANCE;

    Config<Boolean> blurConfig = new BooleanConfig.Builder("Blur")
            .setDescription("Blurs the screen background")
            .setDefaultValue(true).build();
    Config<Boolean> darkenConfig = new BooleanConfig.Builder("Darken")
            .setDescription("Darkens the screen background")
            .setDefaultValue(true).build();
    Config<Integer> scrollSpeedConfig = new NumberConfig.Builder<Integer>("ScrollSpeed")
            .setMin(5).setMax(100).setDefaultValue(30).setFormat("dpi")
            .setDescription("The speed for mouse scrolling").build();

    private final Animation fadeInAnimation;

    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.fadeInAnimation = new Animation(false, 400, Easing.SINE_OUT);
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
