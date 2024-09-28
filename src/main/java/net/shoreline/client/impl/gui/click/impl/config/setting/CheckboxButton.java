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
import net.shoreline.client.util.render.animation.Animation;

import java.awt.*;

/**
 * @author linus
 * @see Config
 * @since 1.0
 */
public class CheckboxButton extends ConfigButton<Boolean>
{

    /**
     * @param frame
     * @param config
     */
    public CheckboxButton(CategoryFrame frame, ModuleButton moduleButton, Config<Boolean> config, float x, float y)
    {
        super(frame, moduleButton, config, x, y);
        config.getAnimation().setState(config.getValue());
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
        Animation checkboxAnimation = config.getAnimation();
        boolean state = isWithin(mouseX, mouseY);
        if (state != hoverAnimation.getState())
        {
            hoverAnimation.setState(state);
        }
        int hoverAlpha = (int) (80 * MathHelper.clamp(hoverAnimation.getFactor(), 0.0f, 1.0f));

        rect(context, checkboxAnimation.getFactor() > 0.01f ? ClickGuiModule.getInstance().getColor(100 + hoverAlpha, (float) checkboxAnimation.getFactor()) : new Color(hoverAlpha, hoverAlpha, hoverAlpha, hoverAlpha).getRGB());
        int whiteText = -1;
        drawStringScaled(context, config.getName(), (ix + (2.0f * ClickGuiModule.CLICK_GUI_SCALE)), (iy + (4.0f * ClickGuiModule.CLICK_GUI_SCALE)), whiteText);
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
            if (button == 0)
            {
                boolean val = config.getValue();
                config.setValue(!val);

                if (ClickGuiModule.getInstance().getSounds())
                {
                    Managers.SOUND.playSound(SoundManager.GUI_CLICK);
                }
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
