package net.shoreline.client.gui.clickgui.config;

import lombok.Setter;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.gui.clickgui.components.TextComponent;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.animation.Smoother;
import net.shoreline.client.impl.render.Theme;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Setter
public class SliderComponent<T extends Number> extends ConfigComponent<T>
{
    private final TextComponent textComponent;
    private final Smoother smoother = new Smoother();
    private boolean dragging;

    public SliderComponent(Config<T> config,
                           ModuleComponent moduleComponent,
                           Frame frame,
                           float x,
                           float y,
                           float frameWidth,
                           float frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        textComponent = new TextComponent(frame, x, y, frameWidth, frameHeight,
                GLFW.GLFW_MOUSE_BUTTON_RIGHT,
                c -> (c >= '0' && c <= '9') || c == '.' || c == '-', // Filter numbers only
                () -> String.valueOf(config.getValue()),
                value ->
                {
                    try
                    {
                        if (config.getValue() instanceof Integer)
                        {
                            ((NumberConfig) config).setValue(Integer.parseInt(value));
                        } else if (config.getValue() instanceof Float)
                        {
                            ((NumberConfig) config).setValue(Float.parseFloat(value));
                        } else if (config.getValue() instanceof Double)
                        {
                            ((NumberConfig) config).setValue(Double.parseDouble(value));
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
        boolean hovering = Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height);
        setHoverState(hovering);
        if (hovering)
        {
            ClickGuiScreen.INSTANCE.setDescriptionText(getConfig().getDescription());
        }

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
        if (mouse.isHovering(getTx(), getTy(), width, height) && mouse.isLeftHeld() && dragging)
        {
            setSliderValue(mouseX, min, max);
        }

        float fill = (getConfig().getValue().floatValue() - min.floatValue())
                / (max.floatValue() - min.floatValue());
        int color = ColorUtil.brighten(theme.getComponentColor(), 70, (float) hoverAnim.getFactor());
        int color1 = ColorUtil.brighten(0x00646464, 70, (float) hoverAnim.getFactor());
        float sliderWidth = (float) smoother.smooth(fill * width, 1.0f, delta);
        drawRect(context, getTx(), getTy(), sliderWidth, height, color);
        drawRect(context, getTx() + sliderWidth, getTy(), width - sliderWidth, height, color1);

        boolean isInt = getConfig().getValue() instanceof Integer || numberConfig.getRoundingPlaces() == 0;
        String numberText = isInt ? String.valueOf(getConfig().getValue().intValue()) : String.valueOf(getConfig().getValue());
        String formattedText = getConfig().getName() + " " + Formatting.GRAY + numberText;
        if (numberConfig.getFormat() != null)
        {
            formattedText += numberConfig.getFormat();
        }

        drawText(context, formattedText, getTx() + 3.0f, getTy() + 4.0f, theme.getTextColor());
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            setDragging(true);
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
        setDragging(false);
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

    @Override
    protected void onConfigUpdate(Number value)
    {
        textComponent.updateBuffer(String.valueOf(value));
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

    @Override
    public void reset()
    {
        smoother.clear();
    }

    private BigDecimal round(int places, double val)
    {
        BigDecimal bigDecimal = new BigDecimal(val);
        return bigDecimal.setScale(places, RoundingMode.HALF_UP);
    }
}
