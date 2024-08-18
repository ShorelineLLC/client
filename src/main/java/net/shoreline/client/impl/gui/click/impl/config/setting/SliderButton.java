package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.module.client.ClickGuiModule;

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
    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta)
    {
        x = ix;
        y = iy;
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
            float lower = ix + ClickGuiModule.CLICK_GUI_SCALE;
            float upper = ix + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE;
            // out of bounds
            if (mouseX < lower)
            {
                config.setValue((T) min);
            }
            else if (mouseX > upper)
            {
                config.setValue((T) max);
            }
        }
        // slider fill
        float fill = (config.getValue().floatValue() - min.floatValue())
                / (max.floatValue() - min.floatValue());
        fill(context, ix, iy, (fill * width * ClickGuiModule.CLICK_GUI_SCALE), height * ClickGuiModule.CLICK_GUI_SCALE, 0.0, ClickGuiModule.getInstance().getColor());

        int whiteText = -1;
        drawStringScaled(context, config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        float textLeng = RenderManager.textWidth(config.getName()) * ClickGuiModule.CLICK_GUI_SCALE;

        int grayText = 0xFFAAAAAA;
        drawStringScaled(context, " " + config.getValue(), ix + (2.0F * ClickGuiModule.CLICK_GUI_SCALE) + textLeng, iy + (4.0F * ClickGuiModule.CLICK_GUI_SCALE), grayText);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {

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

    }

    @Override
    public void charTyped(char character, int modifiers)
    {

    }
}
