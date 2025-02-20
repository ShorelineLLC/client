package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * @param <T>
 * @author linus
 * @since 1.0
 */
public class SliderButton<T extends Number> extends ConfigButton<T>
{
    // Slider rounding scale
    private final int scale;

    private char[] buffer;
    private boolean typing;
    // Insertion point
    private boolean idling;
    private final Timer idleTimer = new CacheTimer();

    /**
     * @param frame
     * @param config
     */
    public SliderButton(CategoryFrame frame, ModuleButton moduleButton, Config<T> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
        //
        final String sval = String.valueOf(config.getValue());
        scale = sval.substring(sval.indexOf(".") + 1).length();
        if (config.getValue() instanceof Integer)
        {
            buffer = String.valueOf(config.getValue().intValue()).toCharArray();
        }
        else if (config.getValue() instanceof Float)
        {
            buffer = String.valueOf(config.getValue().floatValue()).toCharArray();
        }
        else if (config.getValue() instanceof Double)
        {
            buffer = String.valueOf(config.getValue().doubleValue()).toCharArray();
        }
    }

    /**
     * @param context
     * @param ix
     * @param iy
     * @param mouseX
     * @param mouseY
     * @param delta
     */
    @Override
    public void render(DrawContext context, float ix, float iy, float mouseX, float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 4.0f);
        x = ix;
        y = iy;

        boolean state = isWithin(mouseX, mouseY);
        if (state != hoverAnimation.getState())
        {
            hoverAnimation.setState(state);
        }
        int hoverAlpha = (int) (80 * MathHelper.clamp(hoverAnimation.getFactor(), 0.0f, 1.0f));

        //
        Number min = ((NumberConfig<T>) config).getMin();
        Number max = ((NumberConfig<T>) config).getMax();
        if (isWithin(mouseX, mouseY) && ClickGuiScreen.MOUSE_LEFT_HOLD)
        {
            float fillv = (mouseX - ix) / (width * ClickGuiModule.CLICK_GUI_SCALE);
            if (config.getValue() instanceof Integer)
            {
                float val = min.floatValue() + fillv * (max.intValue() - min.intValue());
                int bval = (int) MathHelper.clamp(val, min.intValue(), max.intValue());
                ((NumberConfig<Integer>) config).setValue(bval);
            }
            else if (config.getValue() instanceof Float)
            {
                float val = min.floatValue() + fillv * (max.floatValue() - min.floatValue());
                float bval = MathHelper.clamp(val, min.floatValue(),
                        max.floatValue());
                BigDecimal bigDecimal = new BigDecimal(bval);
                bval = bigDecimal.setScale(scale, RoundingMode.HALF_UP).floatValue();
                ((NumberConfig<Float>) config).setValue(bval);
            }
            else if (config.getValue() instanceof Double)
            {
                double val = min.doubleValue() + fillv * (max.doubleValue() - min.doubleValue());
                double bval = MathHelper.clamp(val, min.doubleValue(),
                        max.doubleValue());
                BigDecimal bigDecimal = new BigDecimal(bval);
                bval = bigDecimal.setScale(scale, RoundingMode.HALF_UP).doubleValue();
                ((NumberConfig<Double>) config).setValue(bval);
            }
            float lower = MathHelper.ceil(x + ClickGuiModule.CLICK_GUI_SCALE);
            float upper = MathHelper.floor(x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE);
            // out of bounds
            if (mouseX < lower)
            {
                config.setValue((T) min);
            }
            else if (mouseX >= upper)
            {
                config.setValue((T) max);
            }
        }
        // slider fill
        if (!typing)
        {
            if (config.getValue() instanceof Integer)
            {
                buffer = String.valueOf(config.getValue().intValue()).toCharArray();
            }
            else if (config.getValue() instanceof Float)
            {
                buffer = String.valueOf(config.getValue().floatValue()).toCharArray();
            }
            else if (config.getValue() instanceof Double)
            {
                buffer = String.valueOf(config.getValue().doubleValue()).toCharArray();
            }
            float fill = (config.getValue().floatValue() - min.floatValue())
                    / (max.floatValue() - min.floatValue());
            fill(context, ix, iy, (fill * width * ClickGuiModule.CLICK_GUI_SCALE), height * ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.getInstance().getColor(100 + hoverAlpha));
        }

        int whiteText = -1;
        drawStringScaled(context, typing ? new String(buffer) + getInsertionPoint() : config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        float textLeng = RenderManager.textWidth(config.getName()) * ClickGuiModule.CLICK_GUI_SCALE;

        if (!typing)
        {
            int grayText = 0xFFAAAAAA;
            drawStringScaled(context, " " + config.getValue(), ix + (2.0F * ClickGuiModule.CLICK_GUI_SCALE) + textLeng, iy + (4.0F * ClickGuiModule.CLICK_GUI_SCALE), grayText);
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY))
        {
            if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                if (typing)
                {
                    String number = new String(buffer);
                    try
                    {
                        if (config.getValue() instanceof Integer)
                        {
                            ((NumberConfig<Integer>) config).setValue(Integer.parseInt(number));
                        }
                        else if (config.getValue() instanceof Float)
                        {
                            ((NumberConfig<Float>) config).setValue(Float.parseFloat(number));
                        }
                        else if (config.getValue() instanceof Double)
                        {
                            ((NumberConfig<Double>) config).setValue(Double.parseDouble(number));
                        }
                    }
                    catch (NumberFormatException ignored)
                    {

                    }
                    typing = false;
                }
                else
                {
                    typing = true;
                }
            }

            if (ClickGuiModule.getInstance().getSounds())
            {
                Managers.SOUND.playSound(SoundManager.GUI_CLICK);
            }
        }
        else
        {
            typing = false;
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {

    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (typing)
        {
            switch (keyCode)
            {
                case GLFW.GLFW_KEY_ENTER ->
                {
                    String number = new String(buffer);
                    try
                    {
                        if (config.getValue() instanceof Integer)
                        {
                            ((NumberConfig<Integer>) config).setValue(Integer.parseInt(number));
                        }
                        else if (config.getValue() instanceof Float)
                        {
                            ((NumberConfig<Float>) config).setValue(Float.parseFloat(number));
                        }
                        else if (config.getValue() instanceof Double)
                        {
                            ((NumberConfig<Double>) config).setValue(Double.parseDouble(number));
                        }
                    }
                    catch (NumberFormatException ignored)
                    {

                    }
                    if (config.getValue() instanceof Integer)
                    {
                        buffer = String.valueOf(config.getValue().intValue()).toCharArray();
                    }
                    else if (config.getValue() instanceof Float)
                    {
                        buffer = String.valueOf(config.getValue().floatValue()).toCharArray();
                    }
                    else if (config.getValue() instanceof Double)
                    {
                        buffer = String.valueOf(config.getValue().doubleValue()).toCharArray();
                    }
                    typing = false;
                }
                case GLFW.GLFW_KEY_BACKSPACE ->
                {
                    if (buffer.length != 0)
                    {
                        buffer = ArrayUtils.remove(buffer, buffer.length - 1);
                    }
                }
                case GLFW.GLFW_KEY_ESCAPE ->
                {
                    if (config.getValue() instanceof Integer)
                    {
                        buffer = String.valueOf(config.getValue().intValue()).toCharArray();
                    }
                    else if (config.getValue() instanceof Float)
                    {
                        buffer = String.valueOf(config.getValue().floatValue()).toCharArray();
                    }
                    else if (config.getValue() instanceof Double)
                    {
                        buffer = String.valueOf(config.getValue().doubleValue()).toCharArray();
                    }
                    typing = false;
                }
            }
        }
    }

    @Override
    public void charTyped(char character, int modifiers)
    {
        if (typing && (Character.isDigit(character) || character == '.'))
        {
            buffer = ArrayUtils.add(buffer, character);
        }
    }

    public String getInsertionPoint()
    {
        if (idleTimer.passed(250))
        {
            idling = !idling;
            idleTimer.reset();
        }
        if (idling && typing)
        {
            return "_";
        }
        return "";
    }

    public void endTyping()
    {
        typing = false;
    }

    public boolean isTyping()
    {
        return typing;
    }
}
