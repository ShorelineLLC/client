package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class FrameComponent extends DrawableComponent implements Interactable
{
    protected int x, y;

    protected int frameWidth;
    protected int frameHeight;

    private final List<FrameComponent> components = new ArrayList<>();

    public FrameComponent(int x,
                          int y,
                          int frameWidth,
                          int frameHeight)
    {
        this.x = x;
        this.y = y;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
    }

    protected boolean isInBounds(Mouse mouse)
    {
        return mouse.isInBounds(x, y, x + frameWidth, y + frameHeight);
    }
}
