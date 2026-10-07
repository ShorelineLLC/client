package net.shoreline.client.impl.module.client;

import lombok.Getter;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.macro.ModuleKeybind;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.eventbus.annotation.EventListener;
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
    Config<Boolean> categoryCount = new BooleanConfig.Builder("ShowCount")
            .setDescription("Shows the number of modules in each category")
            .setDefaultValue(true).build();

    @Getter
    private final Theme theme;
    private final Animation fadeInAnimation;

    @Getter
    private float scale = 1.0f;

    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);

        setKeybind(new ModuleKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT, this));

        this.fadeInAnimation = new Animation(false, 150, Easing.LINEAR);
        this.theme = new Theme(fadeInAnimation);
        INSTANCE = this;
    }

    @Override
    public void onEnable()
    {
        if (mc.isFinishedLoading())
        {
            if (scale != scaleConfig.getValue())
            {
                scale = scaleConfig.getValue();
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
    }

    @Override
    public void onDisable()
    {
        setFadeState(false);
        if (checkNull())
        {
            return;
        }

        mc.player.closeScreen();
    }

    @EventListener
    public void onFinishedLoading(LoadingEvent.Finished event)
    {
        scale = scaleConfig.getValue();
    }

    public void setFadeState(boolean fadeState)
    {
        fadeInAnimation.setState(fadeState);
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

    public boolean shouldShowCount()
    {
        return categoryCount.getValue();
    }
}
