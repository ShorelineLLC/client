package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;

public class TextboxComponent extends ConfigComponent<String>
{

    public TextboxComponent(Config<String> config,
                            ModuleComponent moduleComponent,
                            Frame frame,
                            int x,
                            int y,
                            int frameWidth,
                            int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {

    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {

    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {

    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {

    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {

    }
}
