package net.shoreline.client.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.Mouse;
import net.shoreline.client.gui.clickgui.config.ColorPickerComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.gui.clickgui.config.GroupComponent;
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

        module.getEnabled().addListener(this::onModuleToggled);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        hoverAnim.setState(Mouse.isHovering(mouseX, mouseY, getTx(), getTy(), width, height));
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        int color = ColorUtil.withTransparency(theme.getComponentColor(), (float) toggleAnim.getFactor());
        color = ColorUtil.brighten(toggleAnim.getFactor() > 0.0 ? color : 0x00646464, 70, (float) hoverAnim.getFactor());
        drawRect(context, getTx(), getTy(), width, height, color);
        int textColor = ColorUtil.interpolateColor(1.0f - (float) toggleAnim.getFactor(), 0xffaaaaaa, theme.getTextColor());
        drawText(context, textBuffer, Text.literal(module.getName()).withColor(textColor), getTx() + 3, getTy() + 4);

        if (getCollapseAnim().getFactor() > 0.0)
        {
            enableScissor(context, getTx(), getTy() + height, getTx() + width, getTy() + height + getScaledHeight());

            int configY = 2;
            for (ConfigComponent<?> component : components)
            {
                if (component.getConfig().isVisible())
                {
                    component.setYOffset(configY);
                    component.drawComponent(context, mouseX, mouseY, delta);
                    configY += component.getHeight() + 1;
                    if (component instanceof GroupComponent c)
                    {
                        configY += c.getScaledHeight();
                    } else if (component instanceof ColorPickerComponent c1)
                    {
                        configY += c1.getComponentHeight();
                    }
                }
            }

            disableScissor(context);
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

    private void onModuleToggled(boolean enabled)
    {
        toggleAnim.setState(enabled);
    }
}
