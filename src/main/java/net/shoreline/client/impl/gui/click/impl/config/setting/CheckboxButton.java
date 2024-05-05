package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.init.Modules;
import net.shoreline.client.util.render.animation.Animation;

/**
 * @author linus
 * @see Config
 * @since 1.0
 */
public class CheckboxButton extends ConfigButton<Boolean> {

    /**
     * @param frame
     * @param config
     */
    public CheckboxButton(CategoryFrame frame, ModuleButton moduleButton, Config<Boolean> config, float x, float y) {
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
                       float mouseY, float delta) {
        x = ix;
        y = iy;
        Animation checkboxAnimation = config.getAnimation();
        rectGradient(context, checkboxAnimation.getFactor() > 0.01f ?  Modules.CLICK_GUI.getColor((float) checkboxAnimation.getFactor()) : 0x00000000,
                checkboxAnimation.getFactor() > 0.01f ?  Modules.CLICK_GUI.getColor1((float) checkboxAnimation.getFactor()) : 0x00000000);
        RenderManager.renderText(context, config.getName(), ix + 2.0f, iy + 4.0f, -1);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isWithin(mouseX, mouseY)) {
            if (button == 0) {
                boolean val = config.getValue();
                config.setValue(!val);
            }
        }
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @Override
    public void mouseReleased(double mouseX, double mouseY, int button) {

    }

    /**
     * @param keyCode
     * @param scanCode
     * @param modifiers
     */
    @Override
    public void keyPressed(int keyCode, int scanCode, int modifiers) {

    }

}
