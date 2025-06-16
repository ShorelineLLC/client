package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.Theme;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderComponent<T extends Number> extends ConfigComponent<T>
{
    public SliderComponent(Config<T> config,
                           ModuleComponent moduleComponent,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Mouse mouse = ClickGuiScreen.INSTANCE.getMouse();
        NumberConfig<T> numberConfig = (NumberConfig<T>) getConfig();
        Number min = numberConfig.getMin();
        Number max = numberConfig.getMax();
        if (mouse.isHovering(getTx(), getTy(), width, height) && mouse.isLeftHeld())
        {
            setSliderValue(mouseX, min, max);
        }

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        float fill = (getConfig().getValue().floatValue() - min.floatValue())
                / (max.floatValue() - min.floatValue());
        drawRect(context, getTx(), getTy(), (int) (fill * width), height, theme.getComponentColor());

        String numberText = getConfig().getValue() instanceof Integer || numberConfig.getRoundingPlaces() == 0
                ? String.valueOf(getConfig().getValue().intValue()) : String.valueOf(getConfig().getValue());
        drawText(context, getConfig().getName() + " " + Formatting.GRAY + numberText, getTx() + 3, getTy() + 4, theme.getTextColor());
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            setSliderValue(mouseX, ((NumberConfig<T>) getConfig()).getMin(),
                    ((NumberConfig<T>) getConfig()).getMax());
        }
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
    {

    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {

    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {

    }

    private void setSliderValue(double mouseX, Number min, Number max)
    {
        double fill = (mouseX - getTx()) / width;
        int rounding = ((NumberConfig<?>) getConfig()).getRoundingPlaces();

        if (getConfig().getValue() instanceof Integer)
        {
            double val = min.floatValue() + fill * (max.intValue() - min.intValue());
            int bval = (int) MathHelper.clamp(val, min.intValue(), max.intValue());
            ((NumberConfig<Integer>) getConfig()).setValue(bval);
        } else if (getConfig().getValue() instanceof Float)
        {
            float val = MathHelper.clamp(
                    min.floatValue() + (float) fill * (max.floatValue() - min.floatValue()),
                    min.floatValue(), max.floatValue());

            ((NumberConfig<Float>) getConfig()).setValue(round(rounding, val).floatValue());
        } else if (getConfig().getValue() instanceof Double)
        {
            double val = MathHelper.clamp(
                    min.doubleValue() + fill * (max.doubleValue() - min.doubleValue()),
                    min.doubleValue(), max.doubleValue());

            ((NumberConfig<Double>) getConfig()).setValue(round(rounding, val).doubleValue());
        }
    }

    private BigDecimal round(int places, double val)
    {
        BigDecimal bigDecimal = new BigDecimal(val);
        return bigDecimal.setScale(places, RoundingMode.HALF_UP);
    }
}
