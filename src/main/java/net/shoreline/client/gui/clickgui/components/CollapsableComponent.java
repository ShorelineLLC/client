package net.shoreline.client.gui.clickgui.components;

import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.render.animation.Animation;
import net.shoreline.client.impl.render.Easing;

import java.util.ArrayList;
import java.util.List;

public class CollapsableComponent extends FrameComponent
{
    private final List<FrameComponent> subComponents = new ArrayList<>();

    @Setter
    private boolean frameOpen;
    private final Animation collapseAnim;

    public CollapsableComponent(Frame frame, int x, int y, int width, int height)
    {
        super(frame, x, y, width, height);
        this.collapseAnim = new Animation(false, 200, Easing.CUBIC_IN_OUT);
    }

    @Override
    public void drawComponent(DrawContext context, float mouseX, float mouseY, float delta)
    {
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height));
        if (collapseAnim.getFactor() > 0.001)
        {
            for (FrameComponent component : subComponents)
            {
                if (component instanceof ConfigComponent<?> configComponent && !configComponent.getConfig().isVisible())
                {
                    continue;
                }


            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {

    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {

    }

    @Override
    public void charTyped(char chr, int modifiers)
    {

    }
}
