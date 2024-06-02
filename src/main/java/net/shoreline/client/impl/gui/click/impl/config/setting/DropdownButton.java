package net.shoreline.client.impl.gui.click.impl.config.setting;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.gui.click.impl.config.ModuleButton;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.util.string.EnumFormatter;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;

/**
 * @author linus
 * @since 1.0
 */
public class DropdownButton extends ConfigButton<Enum<?>> {
    // Current enum value index
    private int index;

    /**
     * @param frame
     * @param config
     */
    public DropdownButton(CategoryFrame frame, ModuleButton moduleButton, Config<Enum<?>> config, float x, float y) {
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
                       float mouseY, float delta) {
        x = ix;
        y = iy;
        String val = EnumFormatter.formatEnum(config.getValue());
        rectGradient(context, ClickGuiModule.getInstance().getColor(), ClickGuiModule.getInstance().getColor());

        int whiteText = ClickGuiModule.getInstance().getColor(-1);
        RenderManager.renderText(context, config.getName(), ix + 2.0f, iy + 4.0f, whiteText);

        int textLeng = RenderManager.textWidth(config.getName());

        int grayText = ClickGuiModule.getInstance().getColor(0xFFAAAAAA);

        RenderManager.renderText(context, " " + val, ix + 2.0F + textLeng, iy + 4.0F, grayText);
    }

    /**
     * @param mouseX
     * @param mouseY
     * @param button
     */
    @SuppressWarnings("unchecked")
    @Override
    public void mouseClicked(double mouseX, double mouseY, int button) {
        if (isWithin(mouseX, mouseY)) {
            Enum<?> val = config.getValue();
            String[] values = Arrays.stream(val.getClass().getEnumConstants()).map(Enum::name).toArray(String[]::new);
            if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                index = index + 1 > values.length - 1 ? 0 : index + 1;
                config.setValue(Enum.valueOf(val.getClass(), values[index]));
            } else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                index = index - 1 < 0 ? values.length - 1 : index - 1;
                config.setValue(Enum.valueOf(val.getClass(), values[index]));
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
