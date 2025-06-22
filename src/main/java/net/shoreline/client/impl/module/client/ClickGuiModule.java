package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
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

    private final Animation openCloseAnim;
    private final Animation fadeInAnimation;

    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
        this.openCloseAnim = new Animation(false, 300, Easing.CUBIC_IN_OUT);
        this.fadeInAnimation = new Animation(false, 400, Easing.BACK_OUT);
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

        openCloseAnim.setState(true);
        openCloseAnim.reset();
        fadeInAnimation.setState(true);
        fadeInAnimation.reset();

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
        openCloseAnim.setState(false);
        fadeInAnimation.setState(false);
    }

    public double getFadeFactor()
    {
        return fadeInAnimation.getFactor();
    }

    public double getAnimFactor()
    {
        return openCloseAnim.getFactor();
    }

    public boolean shouldBlur()
    {
        return blurConfig.getValue();
    }

    public boolean shouldDarken()
    {
        return darkenConfig.getValue();
    }
}
