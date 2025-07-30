package net.shoreline.client.gui.clickgui;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.gui.clickgui.components.ToggleComponent;
import net.shoreline.client.gui.clickgui.config.ColorPickerComponent;
import net.shoreline.client.gui.clickgui.config.ConfigComponent;
import net.shoreline.client.gui.clickgui.config.GroupComponent;
import net.shoreline.client.impl.render.ColorUtil;
import net.shoreline.client.impl.render.Theme;

public class ToggleModuleComponent extends ModuleComponent
{
    private final ToggleComponent toggleComponent;

    public ToggleModuleComponent(Toggleable module,
                                 Frame frame,
                                 int x,
                                 int y,
                                 int frameWidth,
                                 int frameHeight)
    {
        super(module, frame, x, y, frameWidth, frameHeight);
        this.toggleComponent = new ToggleComponent(frame, x, y, frameWidth, frameHeight, module.isEnabled(), module::toggle);

        module.getEnabled().addListener(this::onModuleToggled);
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {
        Theme theme = ClickGuiScreen.INSTANCE.getTheme();

        toggleComponent.setYOffset(getYOffset());
        toggleComponent.setX(getTx());
        toggleComponent.setY(getTy());
        toggleComponent.drawComponent(context, mouseX, mouseY, delta);

        int textColor = ColorUtil.interpolateColor(1.0f - (float) toggleComponent.getFactor(), 0xffaaaaaa, theme.getTextColor());
        drawText(context, textBuffer, Text.literal(module.getName()).withColor(textColor), getTx() + 3, getTy() + 4);

        if (components.size() > 1)
        {
            Text dotsText = Text.literal("...").withColor(textColor);
            drawText(context, dotsBuffer, dotsText, getTx() + width - getTextWidth(dotsBuffer, dotsText, true) - 1, getTy() + 4, 0.0f, 5.0f, true);
        }

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
        toggleComponent.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void onModuleToggled(boolean enabled)
    {
        toggleComponent.setState(enabled);
    }
}
