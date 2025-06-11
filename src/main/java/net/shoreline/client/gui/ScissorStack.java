package net.shoreline.client.gui;

import net.minecraft.client.gui.ScreenRect;

import java.util.ArrayDeque;
import java.util.Deque;

public class ScissorStack
{
    private final Deque<ScreenRect> stack = new ArrayDeque<>();

    public ScreenRect push(ScreenRect rect)
    {
        ScreenRect screenRect = stack.peekLast();
        if (screenRect != null)
        {
            ScreenRect screenRect2 = ScreenRect.empty();
            ScreenRect rect1 = rect.intersection(screenRect);
            if (rect1 != null)
            {
                screenRect2 = rect1;
            }

            stack.addLast(screenRect2);
            return screenRect2;
        }

        stack.addLast(rect);
        return rect;
    }

    public ScreenRect pop()
    {
        if (stack.isEmpty())
        {
            throw new IllegalStateException("Scissor stack underflow");
        }

        stack.removeLast();
        return stack.peekLast();
    }

    public boolean contains(int x, int y)
    {
        if (stack.isEmpty())
        {
            return true;
        }
        return stack.peek().contains(x, y);
    }
}
