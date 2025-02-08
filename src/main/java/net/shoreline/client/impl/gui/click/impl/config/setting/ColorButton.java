package net.shoreline.client.impl.gui.click.impl.config.setting;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.ColorConfig;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.config.ConfigUpdateEvent;
import net.shoreline.client.impl.gui.click.ClickGuiScreen;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.render.animation.Animation;
import net.shoreline.client.util.render.animation.Easing;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

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

    private boolean ignoreSetColor;

    private char[] buffer;
    private boolean typing;
    // Insertion point
    private boolean idling;
    private final Timer idleTimer = new CacheTimer();

    /**
     * @param frame
     * @param config
     * @param x
     * @param y
     */
    public ColorButton(CategoryFrame frame, ModuleButton moduleButton, Config<Color> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
        ColorConfig colorConfig = ((ColorConfig) config);
        float[] hsb = colorConfig.getHsb();
        selectedColor = new float[] {hsb[0], hsb[1], 1.0f - hsb[2], hsb[3]};
        buffer = Integer.toHexString(colorConfig.getRgb()).toCharArray();
        EventBus.INSTANCE.subscribe(this);
    }

    @Override
    public void render(DrawContext context, float ix, float iy, float mouseX,
                       float mouseY, float delta)
    {
        setHeight(RenderManager.textHeight() + 4.0f);
        x = ix;
        y = iy;
        int originalColor = ((ColorConfig) config).getRgb();
        boolean state = isWithin(mouseX, mouseY);
        if (state != hoverAnimation.getState())
        {
            hoverAnimation.setState(state);
        }
        int hoverAlpha = (int) (80 * MathHelper.clamp(hoverAnimation.getFactor(), 0.0f, 1.0f));

        rect(context, new Color(hoverAlpha, hoverAlpha, hoverAlpha, hoverAlpha).getRGB());

        int modifiedTransparencyColor = ClickGuiModule.getInstance().fixTransparency(originalColor);
        fill(context, ix + (width * ClickGuiModule.CLICK_GUI_SCALE) - (11.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), (10.0f * ClickGuiModule.CLICK_GUI_SCALE), (10.0f * ClickGuiModule.CLICK_GUI_SCALE), modifiedTransparencyColor);
        int whiteText = -1;
        drawStringScaled(context, config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        if (pickerAnimation.getFactor() > 0.01f)
        {
            ColorConfig colorConfig = (ColorConfig) config;
            if (colorConfig.isGlobal())
            {
                Color global = ColorsModule.getInstance().getColor();
                float[] hsb = Color.RGBtoHSB(global.getRed(), global.getGreen(), global.getBlue(), null);
                selectedColor = new float[] {hsb[0], hsb[1], 1.0f - hsb[2], selectedColor[3]};
            }
            if (ClickGuiScreen.MOUSE_LEFT_HOLD)
            {
                if (isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, width * ClickGuiModule.CLICK_GUI_SCALE) && !colorConfig.isGlobal())
                {
                    float left = x + ClickGuiModule.CLICK_GUI_SCALE;
                    float right = x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE;
                    float top = y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE);
                    float bottom = y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE);
                    float width1 = right - left;
                    selectedColor[1] = MathHelper.clamp((mouseX - left) / width1, 0.001f, 0.999f);
                    float height1 = bottom - top;
                    selectedColor[2] =  MathHelper.clamp((mouseY - top) / height1, 0.001f, 0.999f);
                }
                if (isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 8.0f * ClickGuiModule.CLICK_GUI_SCALE) && !colorConfig.isGlobal())
                {
                    selectedColor[0] = MathHelper.clamp((mouseX - (x + ClickGuiModule.CLICK_GUI_SCALE)) / ((width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE), 0.001f, 0.999f);
                }
                if (colorConfig.allowAlpha() && isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (15.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE), 8.0f * ClickGuiModule.CLICK_GUI_SCALE))
                {
                    selectedColor[3] = MathHelper.clamp((mouseX - (x + ClickGuiModule.CLICK_GUI_SCALE)) / ((width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE), 0.001f, 0.999f);
                }
                Color color = Color.getHSBColor(selectedColor[0], selectedColor[1], 1.0f - selectedColor[2]);
                color = new Color(color.getRed() / 255.0f, color.getGreen() / 255.0f, color.getBlue() / 255.0f, MathHelper.clamp(selectedColor[3], 0.0f, 1.0f));
                ignoreSetColor = true;
                colorConfig.setValue(color);
                ignoreSetColor = false;
            }
            float[] hsb = selectedColor;
            int color = Color.HSBtoRGB(hsb[0], 1.0f, 1.0f);
            boolean canScissor = ClickGuiModule.getInstance().getScaleFactor() == 1.0F;

            if (canScissor)
            {
                enableScissor(x, y + (height * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (getPickerHeight() * getScaledTime()));
            }
            for (float i = 0.0f; i < (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE); i += ClickGuiModule.CLICK_GUI_SCALE)
            {
                float hue = i / ((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE));
                fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + i, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 8.0f * ClickGuiModule.CLICK_GUI_SCALE, Color.getHSBColor(hue, 1.0f, 1.0f).getRGB());
            }
            fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + (((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE)) * hsb[0]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (4.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 8.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);

            fillGradientQuad(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), 0xffffffff, color, true);
            fillGradientQuad(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), x + (width * ClickGuiModule.CLICK_GUI_SCALE) - ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (2.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), 0, 0xff000000, false);
            fill(context, x + (width * ClickGuiModule.CLICK_GUI_SCALE * hsb[1]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + ClickGuiModule.CLICK_GUI_SCALE + (width * ClickGuiModule.CLICK_GUI_SCALE * hsb[2]), 2.0f * ClickGuiModule.CLICK_GUI_SCALE, 2.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);

            if (colorConfig.allowAlpha())
            {
                boolean flip = false;
                for (float i = 0.0f; i < (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0 * ClickGuiModule.CLICK_GUI_SCALE); i += 4.0f * ClickGuiModule.CLICK_GUI_SCALE)
                {
                    float x1 = x + ClickGuiModule.CLICK_GUI_SCALE + i;
                    double width1 = 4.0f * ClickGuiModule.CLICK_GUI_SCALE;
                    if (x1 + width1 > (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0 * ClickGuiModule.CLICK_GUI_SCALE))
                    {
                        width1 = ((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0 * ClickGuiModule.CLICK_GUI_SCALE)) - i;
                    }
                    fill(context, x1, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (15.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), width1, 4.0f * ClickGuiModule.CLICK_GUI_SCALE, flip ? 0xffffffff : 0xff909090);
                    fill(context, x1, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (19.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), width1, 4.0f * ClickGuiModule.CLICK_GUI_SCALE, flip ? 0xff909090 : 0xffffffff);
                    flip = !flip;
                }
                fillGradient(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (15.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), (width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0 * ClickGuiModule.CLICK_GUI_SCALE), 8.0f * ClickGuiModule.CLICK_GUI_SCALE, 0x00000000, color);
                fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + (((width * ClickGuiModule.CLICK_GUI_SCALE) - (2.0f * ClickGuiModule.CLICK_GUI_SCALE)) * hsb[3]), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (15.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), ClickGuiModule.CLICK_GUI_SCALE, 8.0f * ClickGuiModule.CLICK_GUI_SCALE, -1);
            }
            String renderText = typing ? new String(buffer) + getInsertionPoint() : new String(buffer);
            drawStringScaled(context, "#" + renderText, x + (3.0f * ClickGuiModule.CLICK_GUI_SCALE), y + (height  * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 28.0f * ClickGuiModule.CLICK_GUI_SCALE : 18.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE), whiteText);
            fill(context, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 26.0f * ClickGuiModule.CLICK_GUI_SCALE : 14.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                    (width * ClickGuiModule.CLICK_GUI_SCALE) - (17.0f * ClickGuiModule.CLICK_GUI_SCALE), 12.0f * ClickGuiModule.CLICK_GUI_SCALE, 0x30909090);
            double globalAnimation = config.getName().equalsIgnoreCase("Global") ? 1.0f : colorConfig.getAnimation().getFactor();
            if (globalAnimation > 0.01)
            {
                fill(context, x + ClickGuiModule.CLICK_GUI_SCALE + (width * ClickGuiModule.CLICK_GUI_SCALE) - (15.0f * ClickGuiModule.CLICK_GUI_SCALE), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 26.0f * ClickGuiModule.CLICK_GUI_SCALE : 14.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                        13.0f * ClickGuiModule.CLICK_GUI_SCALE, 12.0f * ClickGuiModule.CLICK_GUI_SCALE, ClickGuiModule.getInstance().getColor((float) globalAnimation));
            }
            float x2 = x + ClickGuiModule.CLICK_GUI_SCALE + (width * ClickGuiModule.CLICK_GUI_SCALE) - (14.0f * ClickGuiModule.CLICK_GUI_SCALE);
            float y2 = y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 27.0f * ClickGuiModule.CLICK_GUI_SCALE : 15.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE);

            mc.getTextureManager().bindTexture(Identifier.of("shoreline", "icon/sync_clickgui.png"));
            // RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.setShaderTexture(0, Identifier.of("shoreline", "icon/sync_clickgui.png"));
            drawTexture(context, x2, y2, 0, 0, 11 * ClickGuiModule.CLICK_GUI_SCALE,
                    10 * ClickGuiModule.CLICK_GUI_SCALE, 11 * ClickGuiModule.CLICK_GUI_SCALE, 10 * ClickGuiModule.CLICK_GUI_SCALE);

            moduleButton.offset((float) (getPickerHeight() * pickerAnimation.getFactor()));
            ((CategoryFrame) frame).offset((float) (getPickerHeight() * pickerAnimation.getFactor() * moduleButton.getScaledTime()));

            if (canScissor)
            {
                disableScissor();
            }
        }
        else
        {
            endTyping();
        }
    }

    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        ColorConfig colorConfig = (ColorConfig) config;
        boolean m1 = isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE, y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 26.0f * ClickGuiModule.CLICK_GUI_SCALE : 14.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                (width * ClickGuiModule.CLICK_GUI_SCALE) - (17.0f * ClickGuiModule.CLICK_GUI_SCALE), 12.0f * ClickGuiModule.CLICK_GUI_SCALE);
        if (!m1)
        {
            endTyping();
        }
        if (isWithin(mouseX, mouseY) && button == 1)
        {
            open = !open;
            pickerAnimation.setState(open);
            if (ClickGuiModule.getInstance().getSounds())
            {
                Managers.SOUND.playSound(SoundManager.GUI_CLICK);
            }
        }
        if (button == 0)
        {
            if (m1)
            {
                if (typing)
                {
                    try
                    {
                        Color color = colorConfig.parseColor(new String(buffer).trim());
                        config.setValue(color);
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

                if (ClickGuiModule.getInstance().getSounds())
                {
                    Managers.SOUND.playSound(SoundManager.GUI_CLICK);
                }
            }

            if (!config.getName().equalsIgnoreCase("Global") && isMouseOver(mouseX, mouseY, x + ClickGuiModule.CLICK_GUI_SCALE + (width * ClickGuiModule.CLICK_GUI_SCALE) - (15.0f * ClickGuiModule.CLICK_GUI_SCALE), y + (height * ClickGuiModule.CLICK_GUI_SCALE) + (colorConfig.allowAlpha() ? 26.0f * ClickGuiModule.CLICK_GUI_SCALE : 14.0f * ClickGuiModule.CLICK_GUI_SCALE) + (width * ClickGuiModule.CLICK_GUI_SCALE),
                    13.0f * ClickGuiModule.CLICK_GUI_SCALE, 12.0f * ClickGuiModule.CLICK_GUI_SCALE))
            {
                boolean val = !colorConfig.isGlobal();
                colorConfig.setGlobal(val);
                float[] hsb = ((ColorConfig) config).getHsb();
                selectedColor = new float[]{hsb[0], hsb[1], 1.0f - hsb[2], hsb[3]};
                if (ClickGuiModule.getInstance().getSounds())
                {
                    Managers.SOUND.playSound(SoundManager.GUI_CLICK);
                }
            }
        }
    }

    @EventListener
    public void onConfigUpdate(ConfigUpdateEvent event)
    {
        if (event.getConfig() == getConfig() && event.getStage() == StageEvent.EventStage.POST)
        {
            if (!ignoreSetColor)
            {
                float[] hsb = ((ColorConfig) config).getHsb();
                selectedColor = new float[] {hsb[0], hsb[1], 1.0f - hsb[2], hsb[3]};
            }
            buffer = Integer.toHexString(((ColorConfig) config).getRgb()).toCharArray();
        }
    }

    @Override
    public void mouseReleased(double mouseX, double mouseY, int button)
    {

    }

    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers)
    {
        if (typing)
        {
            ColorConfig colorConfig = ((ColorConfig) config);
            switch (keyCode)
            {
                case GLFW.GLFW_KEY_ENTER ->
                {
                    try
                    {
                        Color color = colorConfig.parseColor(new String(buffer).trim());
                        config.setValue(color);
                    }
                    catch (NumberFormatException ignored)
                    {

                    }
                    buffer = Integer.toHexString(colorConfig.getRgb()).toCharArray();
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
                    buffer = Integer.toHexString(colorConfig.getRgb()).toCharArray();
                    typing = false;
                }
            }
        }
    }

    @Override
    public void charTyped(char character, int modifiers)
    {
        if (typing && buffer.length < 8 && isHexDigit(character))
        {
            buffer = ArrayUtils.add(buffer, character);
        }
    }

    public boolean isHexDigit(char ch)
    {
        return (ch >= '0' && ch <= '9') || (ch >= 'A' && ch <= 'F') || (ch >= 'a' && ch <= 'f');
    }

    public float getPickerHeight()
    {
        float pickerHeight = 29.0f * ClickGuiModule.CLICK_GUI_SCALE;
        if (((ColorConfig) config).allowAlpha())
        {
            pickerHeight += 10.0f * ClickGuiModule.CLICK_GUI_SCALE;
        }
        return pickerHeight + (width * ClickGuiModule.CLICK_GUI_SCALE);
    }

    public void endTyping()
    {
        this.typing = false;
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

    public boolean isTyping()
    {
        return typing;
    }

    public float getScaledTime()
    {
        return (float) pickerAnimation.getFactor();
    }
}
