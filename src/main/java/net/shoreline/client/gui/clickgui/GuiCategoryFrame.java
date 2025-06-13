package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;

public class GuiCategoryFrame extends Frame
{
    @Getter
    private final GuiCategory guiCategory;

    public GuiCategoryFrame(GuiCategory guiCategory, int x, int y, int width, int titleHeight)
    {
        super(guiCategory.getName(), x, y, width, titleHeight);
        this.guiCategory = guiCategory;

        for (Module module1 : Managers.MODULES.getModules())
        {
            if (module1.getCategory().equals(guiCategory))
            {
                addComponent(new ModuleComponent(module1, this, x, y, width, titleHeight));
            }
        }
    }
}
