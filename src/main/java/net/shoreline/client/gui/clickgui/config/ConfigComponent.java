package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.FrameComponent;
import net.shoreline.client.gui.clickgui.ModuleComponent;

@Getter
public abstract class ConfigComponent<T> extends FrameComponent
{
    private final Config<T> config;
    private final ModuleComponent moduleComponent;

    public ConfigComponent(Config<T> config,
                           ModuleComponent moduleComponent,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(frame, x, y, frameWidth, frameHeight);
        this.config = config;
        this.moduleComponent = moduleComponent;
    }
}
