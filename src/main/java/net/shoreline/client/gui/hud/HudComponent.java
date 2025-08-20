package net.shoreline.client.gui.hud;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.gui.Interactable;
import net.shoreline.client.impl.module.impl.hud.HudModule;
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
    private Anchor anchor;
    private int index;

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
        this.anchor = hudModule.getAnchor();
        this.index = hudModule.getIndex();
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
            updateAnchor();
        }

        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, x, y, width, height));

        int color = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, x, y, width, height, color);

        hudModule.setX(x);
        hudModule.setY(y);
        hudModule.setAnchor(anchor);
        hudModule.setIndex(index);
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
        dragging = false;
    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {}

    @Override
    public void charTyped(char chr, int modifiers) {}

    private void updateAnchor() {
        Window resolution = mc.getWindow();
        float offset = 10;

        float x = getX();
        float y = getY();
        float width = getWidth();
        float height = getHeight();

        float screenWidth = resolution.getScaledWidth();
        float screenHeight = resolution.getScaledHeight();

        boolean nearLeft = x <= offset;
        boolean nearRight = x >= screenWidth - width - offset;
        boolean nearTop = y <= offset;
        boolean nearBottom = y >= screenHeight - height - offset;

        float centerX = screenWidth / 2.0f;
        boolean nearTopMiddle = nearTop
                && !nearLeft
                && !nearRight
                && Math.abs((x + width / 2.0f) - centerX) <= offset;

        Anchor found;
        if (nearLeft && nearTop) {
            found = Anchor.Top_Left;
        } else if (nearRight && nearTop) {
            found = Anchor.Top_Right;
        } else if (nearLeft && nearBottom) {
            found = Anchor.Bottom_Left;
        } else if (nearRight && nearBottom) {
            found = Anchor.Bottom_Right;
        } else if (nearTopMiddle) {
            found = Anchor.Middle;
        } else {
            index = 0;
            anchor = Anchor.None;
            return;
        }

        index = Integer.MAX_VALUE;
        anchor = found;
    }
}
