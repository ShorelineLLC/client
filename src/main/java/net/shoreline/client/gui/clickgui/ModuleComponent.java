package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.minecraft.client.gui.DrawContext;
import net.shoreline.client.api.module.Module;

@Getter
public class ModuleComponent extends FrameComponent
{
    private final Module module;

    private final GuiCategoryFrame frame;

    public ModuleComponent(Module module,
                           GuiCategoryFrame frame,
                           int x,
                           int y,
                           int frameWidth,
                           int frameHeight)
    {
        super(x, y, frameWidth, frameHeight);
        this.module = module;
        this.frame = frame;
    }

    @Override
    public void drawComponent(DrawContext context,
                              float mouseX,
                              float mouseY,
                              float delta)
    {

    }

    @Override
    public void mouseClicked(double mouseX,
                             double mouseY,
                             int mouseButton)
    {

    }

    @Override
    public void mouseReleased(double mouseX,
                              double mouseY,
                              int button)
    {

    }

    @Override
    public void mouseScrolled(double mouseX,
                              double mouseY,
                              double horizontalAmount,
                              double verticalAmount)
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
}
