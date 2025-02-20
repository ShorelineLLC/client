package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.string.EnumFormatter;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * @author linus
 * @since 1.0
 */
public class DropdownButton extends ConfigButton<Enum<?>>
{
    // Current enum value index
    private int index;

    /**
     * @param frame
     * @param config
     */
    public DropdownButton(CategoryFrame frame, ModuleButton moduleButton, Config<Enum<?>> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
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
        setHeight(RenderManager.textHeight() + 4.0f);
        x = ix;
        y = iy;
        String val = EnumFormatter.formatEnum(config.getValue());
        boolean state = isWithin(mouseX, mouseY);
        if (state != hoverAnimation.getState())
        {
            hoverAnimation.setState(state);
        }
        int hoverAlpha = (int) (80 * MathHelper.clamp(hoverAnimation.getFactor(), 0.0f, 1.0f));

        rect(context, ClickGuiModule.getInstance().getColor(100 + hoverAlpha));

        int whiteText = -1;
        drawStringScaled(context, config.getName(), ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE), iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE), whiteText);

        float textLeng = RenderManager.textWidth(config.getName()) * ClickGuiModule.CLICK_GUI_SCALE;

        int grayText = 0xFFAAAAAA;
        drawStringScaled(context, " " + val, ix + (2.0F * ClickGuiModule.CLICK_GUI_SCALE) + textLeng, iy + (4.0F * ClickGuiModule.CLICK_GUI_SCALE), grayText);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button)
    {
        if (isWithin(mouseX, mouseY))
        {
            Enum<?> val = config.getValue();
            String[] values = Arrays.stream(val.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT)
            {
                index = index + 1 > values.length - 1 ? 0 : index + 1;
                config.setValue(Enum.valueOf(val.getClass(), values[index]));
            }
            else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT)
            {
                index = index - 1 < 0 ? values.length - 1 : index - 1;
                config.setValue(Enum.valueOf(val.getClass(), values[index]));
            }

            if (ClickGuiModule.getInstance().getSounds())
            {
                Managers.SOUND.playSound(SoundManager.GUI_CLICK);
            }
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

    }

    @Override
    public void charTyped(char character, int modifiers)
    {

    }
}
