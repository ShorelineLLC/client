package net.shoreline.client.impl.module.impl.hud;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.Easing;
import net.shoreline.client.impl.render.Smoother;
import net.shoreline.client.impl.render.UnboundAnimation;

import java.util.function.Supplier;

@Getter
@Setter
public class DynamicEntry
{
    private final DynamicHudModule module;
    private final Supplier<String> text;
    private final Supplier<Boolean> drawing;
    private final Smoother smootherX;
    private final Animation yAnimation; // y animation should never go out of bounds.

    public DynamicEntry(DynamicHudModule mod, Supplier<String> text, Supplier<Boolean> drawing)
    {
        this.module = mod;
        this.text = text;
        this.drawing = drawing;
        this.smootherX = new Smoother();
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
        int width = getModule().getTextWidth(current);
        float renderX = (x - (left ? width : 0)) + paddingX;
        float renderY = (int) (y + currentOffset) + paddingY;

        if (drawing.get())
        {
            width = left ? width : -width;
            renderX += (float) smootherX.smooth(width, 0.075f, tickDelta);
            yAnimation.setState(true);
        }
        else
        {
            if (!isDone())
            {
                width = left ? -width + width - 2 : 2;
                renderX += (float) smootherX.smooth(width, 0.03f, tickDelta);
                yAnimation.setState(false);
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
