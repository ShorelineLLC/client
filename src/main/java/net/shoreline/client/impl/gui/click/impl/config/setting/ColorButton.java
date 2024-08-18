package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;

import java.awt.*;

/**
 * @author Shoreline
 * @since 1.0
 */
public class ColorButton extends ConfigButton<Color>
{

    private boolean open;
    private final Animation pickerAnimation = new Animation(false, 200, Easing.CUBIC_IN_OUT);
    private float[] selectedColor;

    /**
     * @param frame
     * @param config
     * @param x
     * @param y
     */
    public ColorButton(CategoryFrame frame, ModuleButton moduleButton, Config<Color> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
        float[] hsb = ((ColorConfig) config).getHsb();
        selectedColor = new float[] {hsb[0], hsb[1], 1.0f - hsb[2], hsb[3]};
    }

    @Override
    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta)
    {
        x = ix;
        y = iy;
        int originalColor = ((ColorConfig) config).getRgb();
        int modifiedTransparencyColor = ClickGuiModule.getInstance().fixTransparency(originalColor);
        fill(context, ix + (width * ClickGuiModule.CLICK_GUI_SCALE) - (11.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), (10.0f * ClickGuiModule.CLICK_GUI_SCALE), (10.0f * ClickGuiModule.CLICK_GUI_SCALE), modifiedTransparencyColor);
        int whiteText = -1;
        drawStringScaled(context, config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        if (pickerAnimation.getFactor() > 0.01f)
        {
            ColorConfig colorConfig = (ColorConfig) config;
            if (ClickGuiScreen.MOUSE_LEFT_HOLD)
            {
                if (isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), width * ClickGuiModule.CLICK_GUI_SCALE) && !colorConfig.isGlobal())
                {
                    selectedColor[1] = (mouseX - (x + ClickGuiModule.CLICK_GUI_SCALE)) / ((width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE);
                    selectedColor[2] = (mouseY - (y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE))) / width * ClickGuiModule.CLICK_GUI_SCALE;
                }
                if (isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 10.0f * ClickGuiModule.CLICK_GUI_SCALE) && !colorConfig.isGlobal())
                {
                    selectedColor[0] = (mouseX - (x + ClickGuiModule.CLICK_GUI_SCALE)) / ((width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE);
                }
                if (colorConfig.allowAlpha() && isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (17.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 10.0f * ClickGuiModule.CLICK_GUI_SCALE))
                {
                    selectedColor[3] = (mouseX - (x + ClickGuiModule.CLICK_GUI_SCALE)) / (width - ClickGuiModule.CLICK_GUI_SCALE);
                }
                Color color = Color.getHSBColor(MathHelper.clamp(selectedColor[0], 0.001f, 0.999f), MathHelper.clamp(selectedColor[1], 0.001f, 0.999f), 1.0f - MathHelper.clamp(selectedColor[2], 0.001f, 0.999f));
                color = new Color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, MathHelper.clamp(selectedColor[3], 0.0f, 1.0f));
                colorConfig.setValue(color);
            }
            float[] hsb = colorConfig.getHsb();
            int color = Color.HSBtoRGB(hsb[0], 1.0f, 1.0f);
            boolean canScissor = ClickGuiModule.getInstance().getScaleFactor() == 1.0F;

            if (canScissor)
            {
                enableScissor((int) x, (int) (y + (height * ClickGuiModule.CLICK_GUI_SCALE)), (int) (x + (width * ClickGuiModule.CLICK_GUI_SCALE)), (int) (y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (getPickerHeight() * getScaledTime())));
            }
            for (float i = 0.0f; i < (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE); i += ClickGuiModule.CLICK_GUI_SCALE)
            {
                float hue = i / ((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE));
                fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + i, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 10.0f * ClickGuiModule.CLICK_GUI_SCALE, Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
            fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + (((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE)) * hsb[0]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 10.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);
            fillGradientQuad(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), 0xffffffff, color, true);
            fillGradientQuad(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), 0, 0xff000000, false);
            fill(context, x + (width * ClickGuiModule.CLICK_GUI_SCALE * hsb[1]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + ClickGuiModule.CLICK_GUI_SCALE + (width * ClickGuiModule.CLICK_GUI_SCALE * (1.0f - hsb[2])), 2.0f * ClickGuiModule.CLICK_GUI_SCALE, 2.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);
            if (colorConfig.allowAlpha())
            {
                fillGradient(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (17.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (27.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), color, 0xff000000);
                fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + (((width - 2.0f) * ClickGuiModule.CLICK_GUI_SCALE) * hsb[3]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (17.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 10.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);
            }
            if (!config.getContainer().getName().equalsIgnoreCase("Colors"))
            {
                Animation globalAnimation = colorConfig.getAnimation();
                if (globalAnimation.getFactor() > 0.01)
                {
                    fill(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 29.0f * ClickGuiModule.CLICK_GUI_SCALE : 17.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                            (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 13.0f * ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.getInstance().getColor((float) globalAnimation.getFactor()));
                }
                drawStringScaled(context, "ClientColor", x + (3.0f * ClickGuiModule.CLICK_GUI_SCALE), y + (height  * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 31.0f  * ClickGuiModule.CLICK_GUI_SCALE : 21.0f  * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

            }
            moduleButton.offset((float) (getPickerHeight() * pickerAnimation.getFactor()));
            ((CategoryFrame) frame).offset((float) (getPickerHeight() * pickerAnimation.getFactor() * moduleButton.getScaledTime()));

            if (canScissor)
            {
                disableScissor();
            }
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY) && button == 1)
        {
            open = !open;
            pickerAnimation.setState(open);
        }
        if (!config.getContainer().getName().equalsIgnoreCase("Colors") && isMouseOver(mouseX, mouseY,
                x + 1.0f, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (((ColorConfig) config).allowAlpha() ? 29.0f * ClickGuiModule.CLICK_GUI_SCALE : 17.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 13.0f * ClickGuiModule.CLICK_GUI_SCALE) && button == 0)
        {
            ColorConfig colorConfig = (ColorConfig) config;
            boolean val = !colorConfig.isGlobal();
            colorConfig.setGlobal(val);
            float[] hsb = ((ColorConfig) config).getHsb();
            selectedColor = new float[]{hsb[0], hsb[1], 1.0f - hsb[2], hsb[3]};
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {

    }

    @Override
    public void charTyped(char character, int modifiers)
    {

    }

    public float getPickerHeight()
    {
        float pickerHeight = 16.0f * ClickGuiModule.CLICK_GUI_SCALE;
        if (((ColorConfig) config).allowAlpha())
        {
            pickerHeight += 12.0f * ClickGuiModule.CLICK_GUI_SCALE;
        }
        if (!config.getContainer().getName().equalsIgnoreCase("Colors"))
        {
            pickerHeight += 15.0f * ClickGuiModule.CLICK_GUI_SCALE;
        }
        return pickerHeight + (width * ClickGuiModule.CLICK_GUI_SCALE);
    }

    public float getScaledTime()
    {
        return (float) pickerAnimation.getFactor();
    }
}
