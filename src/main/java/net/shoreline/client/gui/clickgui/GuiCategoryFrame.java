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

        int moduleY = getTitleHeight() + 2;
        for (Module module1 : Managers.MODULES.getModules())
        {
            if (module1.getCategory().equals(guiCategory))
            {
                final ComponentFactory factory = getComponentFactory();
                final ModuleComponent component = factory.createModuleComponent(
                        module1, this, 2, moduleY, width - 4, 15);

                components.add(component);
                moduleY += component.getHeight() + 1;
            }
        }
    }
}
