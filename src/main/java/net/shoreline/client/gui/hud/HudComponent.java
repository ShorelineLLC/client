package net.shoreline.client.gui.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.impl.module.impl.HudModule;
import net.shoreline.client.gui.DrawableComponent;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;

@Getter
@Setter
public class HudComponent extends DrawableComponent implements Interactable
{
    private final HudModule hudModule;

    private int x, y;

    private int px, py;

    private int width;
    private int height;

    private boolean dragging;

    protected final Animation hoverAnim;

    public HudComponent(HudModule hudModule, int x, int y)
    {
        this.hudModule = hudModule;
        this.x = x;
        this.y = y;
        this.hoverAnim = new Animation(false, 150L, Easing.LINEAR);
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

            int screenWidth = context.getScaledWindowWidth();
            int screenHeight = context.getScaledWindowHeight();

            x = MathHelper.clamp(x, 0, screenWidth - width);
            y = MathHelper.clamp(y, 0, screenHeight - height);
        }

        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, x, y, width, height));

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, x, y, width, height, color);

        hudModule.setX(x);
        hudModule.setY(y);
        hudModule.drawGuiComponent(context, delta);

        width = hudModule.getWidth();
        height = hudModule.getHeight();

        px = mouse.getMouseX();
        py = mouse.getMouseY();
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int mouseButton)
    {

    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {
        if (dragging)
        {
            dragging = false;

            int screenWidth = mc.getWindow().getScaledWidth();
            int screenHeight = mc.getWindow().getScaledHeight();

            int snapThreshold = 15;
            if (Math.abs(x) < snapThreshold)
            {
                x = 0;
            } else if (Math.abs(x + width - screenWidth) < snapThreshold)
            {
                x = screenWidth - width;
            } else if (Math.abs(x + width / 2 - screenWidth / 2) < snapThreshold)
            {
                x = (screenWidth - width) / 2;
            }

            if (Math.abs(y) < snapThreshold)
            {
                y = 0;
            } else if (Math.abs(y + height - screenHeight) < snapThreshold)
            {
                y = screenHeight - height;
            } else if (Math.abs(y + height / 2 - screenHeight / 2) < snapThreshold)
            {
                y = (screenHeight - height) / 2;
            }
        }
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

    @Override
    public void charTyped(char chr, int modifiers) {}

}
