package net.shoreline.client.gui.clickgui.config;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.gui.clickgui.ClickGuiScreen;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.render.Theme;
import net.shoreline.client.gui.clickgui.components.ToggleComponent;
import net.shoreline.client.impl.render.ColorUtil;

public class CheckboxComponent extends ConfigComponent<Boolean>
{
    private final ToggleComponent toggleComponent;

    public CheckboxComponent(Config<Boolean> config,
                             ModuleComponent moduleComponent,
                             Frame frame,
                             int x,
                             int y,
                             int frameWidth,
                             int frameHeight)
    {
        super(config, moduleComponent, frame, x, y, frameWidth, frameHeight);
        this.toggleComponent = new ToggleComponent(frame, x, y, frameWidth, frameHeight, config.getValue(), () ->
        {
            boolean val = !config.getValue();
            config.setValue(val);
            return val;
        });
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
        drawText(context, Text.literal(getConfig().getName()).withColor(textColor), getTx() + 3, getTy() + 4);
    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {
        toggleComponent.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {
    }

    @Override
    public void keyPressed(int keyCode,
                           int scanCode,
                           int modifiers)
    {
    }

    @Override
    public void charTyped(char chr,
                          int modifiers)
    {
    }

    @Override
    protected void onConfigUpdate(Boolean value)
    {
        toggleComponent.setState(value);
    }
}
