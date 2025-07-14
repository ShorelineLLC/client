package net.shoreline.client.gui.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.module.HudModule;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Mouse;

@Getter
@Setter
public class HudComponent extends DrawableComponent
{
    private final HudModule hudModule;

    private int x, y;

    private int px, py;

    private int width;
    private int height;

    private boolean dragging;

    public HudComponent(HudModule hudModule, int x, int y)
    {
        this.hudModule = hudModule;
        this.x = x;
        this.y = y;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Mouse mouse = HudGuiScreen.INSTANCE.getMouse();
        if (isDragging())
        {
            x += mouse.getMouseX() - px;
            y += mouse.getMouseY() - py;
        }

        hudModule.setX(x);
        hudModule.setY(y);
        hudModule.drawGuiComponent(context, delta);

        width = hudModule.getWidth();
        height = hudModule.getHeight();

        px = mouse.getMouseX();
        py = mouse.getMouseY();
    }
}
