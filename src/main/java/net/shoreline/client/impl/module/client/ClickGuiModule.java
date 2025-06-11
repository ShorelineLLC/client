package net.shoreline.client.impl.module.client;

import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Toggleable
{
    public ClickGuiModule()
    {
        super("ClickGui", "The client mod menu", GuiCategory.CLIENT);
        setKeybind(GLFW.GLFW_KEY_RIGHT_SHIFT);
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
}
