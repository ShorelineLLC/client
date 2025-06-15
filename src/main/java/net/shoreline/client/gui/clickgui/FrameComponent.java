package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.Setter;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.gui.Mouse;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public abstract class FrameComponent extends DrawableComponent implements Interactable
{
    protected final Frame frame;

    // Positions inside the frame
    protected int x, y;

    protected int width;
    protected int height;

    private final List<FrameComponent> components = new ArrayList<>();

    public FrameComponent(Frame frame,
                          int x,
                          int y,
                          int width,
                          int height)
    {
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    protected boolean isInBounds(Mouse mouse)
    {
        return mouse.isInBounds(x, y, x + width, y + height);
    }

    public int getTx()
    {
        return frame.getX() + this.x;
    }

    public int getTy()
    {
        return frame.getY() + this.y;
    }
}
