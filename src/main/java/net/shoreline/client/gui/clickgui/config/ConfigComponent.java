package net.shoreline.client.gui.clickgui.config;

import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.FrameComponent;

public abstract class ConfigComponent<T> extends FrameComponent
{
    public ConfigComponent(Frame frame, int x, int y, int frameWidth, int frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
    }
}
