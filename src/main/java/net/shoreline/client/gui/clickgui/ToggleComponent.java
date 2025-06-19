package net.shoreline.client.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.impl.render.Animation;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Easing;
import org.lwjgl.glfw.GLFW;

public class ToggleComponent extends ModuleComponent
{
    private final Animation toggleAnim;

    public ToggleComponent(Toggleable module,
                           Frame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(module, frame, x, y, frameWidth, frameHeight);
        this.toggleAnim = new Animation(module.isEnabled(), 200L, Easing.CUBIC_IN_OUT);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        drawRect(context, getTx(), getTy(), width, height,
                ColorUtil.withTransparency(theme.getComponentColor(), (float) toggleAnim.getFactor()));
        int textColor = ColorUtil.interpolateColor(1.0f - (float) toggleAnim.getFactor(), 0xffaaaaaa, theme.getTextColor());
        drawText(context, module.getName(), getTx() + 3, getTy() + 4, textColor);

        if (getCollapseAnim().getFactor() > 0.0)
        {
            context.enableScissor(getTx(), getTy() + height, getTx() + width, getTy() + height + getComponentHeight());

            int configY = 2;
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.setYOffset(configY);
                    component.drawComponent(context, mouseX, mouseY, delta);
                    configY += component.getHeight() + 1;
                }
            }

            context.disableScissor();
        }
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        if (Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height)
                && mouseButton == GLFW.GLFW_MOUSE_BUTTON_LEFT)
        {
            final Toggleable module1 = (Toggleable) module;
            module1.toggle();
            toggleAnim.setState(module1.isEnabled());
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
