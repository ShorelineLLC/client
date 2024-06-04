package net.shoreline.client.impl.gui.click.component;

import net.minecraft.client.gui.ScreenRect;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

public class ScissorStack
{
    //
    private final Deque<ScreenRect> stack = new ArrayDeque<>();

    /**
     * @param rect
     * @return
     */
    public ScreenRect push(ScreenRect rect)
    {
        ScreenRect screenRect = stack.peekLast();
        if (screenRect != null)
        {
            ScreenRect screenRect2 = Objects.requireNonNullElse(rect.intersection(screenRect), ScreenRect.empty());
            stack.addLast(screenRect2);
            return screenRect2;
        }
        else
        {
            stack.addLast(rect);
            return rect;
        }
    }

    /**
     * @return
     */
    public ScreenRect pop()
    {
        if (stack.isEmpty())
        {
            throw new IllegalStateException("Scissor stack underflow");
        }
        else
        {
            stack.removeLast();
            return stack.peekLast();
        }
    }
}
