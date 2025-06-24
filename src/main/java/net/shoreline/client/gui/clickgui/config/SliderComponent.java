package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.*;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderComponent<T extends Number> extends ConfigComponent<T>
{
    private final TextComponent textComponent;

    public SliderComponent(Config<T> config,
                           ModuleComponent moduleComponent,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        textComponent = new TextComponent(frame, x, y, frameWidth, frameHeight,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                c -> c >= '0' && c <= '9', // Filter numbers only
                () -> String.valueOf(config.getValue()),
                value ->
                {
                    try
                    {
                        if (config.getValue() instanceof Integer)
                        {
                            ((Config<Integer>) config).setValue(Integer.parseInt(value));
                        } else if (config.getValue() instanceof Float)
                        {
                            ((Config<Float>) config).setValue(Float.parseFloat(value));
                        } else if (config.getValue() instanceof Double)
                        {
                            ((Config<Double>) config).setValue(Double.parseDouble(value));
                        }
                    } catch (NumberFormatException ignored)
                    {
                        
                    }
                });

        frame.getAllComponents().add(textComponent);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        textComponent.setYOffset(getYOffset());
        textComponent.setX(getTx());
        textComponent.setY(getTy());

        Theme theme = ClickGuiScreen.INSTANCE.getTheme();
        if (textComponent.isTyping())
        {
            textComponent.drawComponent(context, mouseX, mouseY, delta);
            return;
        }

        Mouse mouse = ClickGuiScreen.INSTANCE.getMouse();
        NumberConfig<T> numberConfig = (NumberConfig<T>) getConfig();
        Number min = numberConfig.getMin();
        Number max = numberConfig.getMax();
        if (mouse.isHovering(getTx(), getTy(), width, height) && mouse.isLeftHeld())
        {
            setSliderValue(mouseX, min, max);
        }

        float fill = (getConfig().getValue().floatValue() - min.floatValue())
                / (max.floatValue() - min.floatValue());
        drawRect(context, getTx(), getTy(), (int) (fill * width), height, theme.getComponentColor());

        String numberText = getConfig().getValue() instanceof Integer || numberConfig.getRoundingPlaces() == 0
                ? String.valueOf(getConfig().getValue().intValue()) : String.valueOf(getConfig().getValue());
        Text formattedText = Text.empty()
                .append(Text.literal(getConfig().getName()).withColor(theme.getTextColor()))
                .append(Text.literal(" " + numberText + numberConfig.getFormat().getUnits()).formatted(Formatting.GRAY));

        drawText(context, formattedText, getTx() + 3, getTy() + 4);
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

        textComponent.mouseClicked(mouseX, mouseY, mouseButton);
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
        textComponent.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
        textComponent.charTyped(chr, modifiers);
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
