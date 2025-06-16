package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
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

    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
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
