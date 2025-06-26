package net.shoreline.client.gui.clickgui.config;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.FrameComponent;
import net.shoreline.client.gui.clickgui.ModuleComponent;

import java.util.ArrayList;
import java.util.List;

@Getter
public abstract class ConfigComponent<T> extends FrameComponent
{
    private final Config<T> config;
    private final ModuleComponent moduleComponent;

    @Setter
    private int moduleOffset;

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

    @Override
    public int getTx()
    {
        return getModuleComponent().getTx();
    }

    @Override
    public int getTy()
    {
        ModuleComponent parent = getModuleComponent();
        return parent.getTy() + parent.getHeight() + this.y + this.yOffset;
    }
}
