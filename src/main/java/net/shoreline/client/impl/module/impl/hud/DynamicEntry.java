package net.shoreline.client.impl.module.impl.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.UnboundAnimation;

import java.util.function.Supplier;

@Getter
@Setter
public class DynamicEntry
{
    private final DynamicHudModule module;
    private final Supplier<String> text;
    private final Supplier<Boolean> drawing;
    private final UnboundAnimation animation;
    private final Animation yAnimation; // y animation should never go out of bounds.

    private boolean lastState;
    private float lastWidth;

    public DynamicEntry(DynamicHudModule mod, Supplier<String> text, Supplier<Boolean> drawing)
    {
        this.module = mod;
        this.text = text;
        this.drawing = drawing;
        this.lastState = drawing.get();
        this.animation = new UnboundAnimation(300, Easing.EXPO_OUT);
        this.yAnimation = new Animation(false, 150);
    }

    public void draw(DrawContext context, float x, float y, float currentOffset, float tickDelta)
    {
        boolean left = getModule().isLeft();
        boolean top = getModule().isTop();
        float paddingX = left ? 2 : -2;
        float paddingY = top ? 2 : -2;
        getModule().setOffset((int) (currentOffset + (10 * yAnimation.getFactor())));

        String current = text.get();
        float width = getModule().getTextWidth(current);
        float renderX = (int) (x - (left ? width : 0)) + paddingX;
        float renderY = (int) (y + currentOffset) + paddingY;

        boolean drawing = this.drawing.get();
        if (lastState != drawing)
        {
            animation.setPrev(animation.get());
        }

        lastState = drawing;
        if (drawing)
        {
            width = left ? width : -width;
            if (width > lastWidth)
            {
                animation.setEasing(Easing.SMOOTH_STEP);
            }
            else
            {
                animation.setEasing(Easing.EXPO_OUT);
            }

            renderX += animation.get(width);
            lastWidth = width;
            yAnimation.setState(true);
        }
        else
        {
            if (!isDone())
            {
                animation.setEasing(Easing.EXPO_IN);
                renderX += animation.get(left ? -width + width - 2.0f : 2.0f);
                if (animation.getFactor() > 0.1)
                {
                    yAnimation.setState(false);
                }
            }
        }

        drawText(context, current, renderX, renderY);
    }

    /**
     * We make this a separate method so if any hud entries need custom
     * colors (like potion hud). they can just override this.
     */
    public void drawText(DrawContext context, String string, float x, float y)
    {
        getModule().drawTextTransparency(context.getMatrices(), string, x, y, (float) yAnimation.getFactor());
    }

    public boolean isDrawing()
    {
        return drawing.get();
    }

    public boolean isDone()
    {
        return yAnimation.getFactor() < 0.01;
    }
}