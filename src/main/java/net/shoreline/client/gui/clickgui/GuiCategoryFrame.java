package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.client.ClickGuiModule;

public class GuiCategoryFrame extends Frame
{
    @Getter
    private final GuiCategory guiCategory;

    public GuiCategoryFrame(GuiCategory guiCategory, float x, float y, float width, float titleHeight)
    {
        super(guiCategory.getName(), x, y, width, titleHeight);
        this.guiCategory = guiCategory;

        float moduleY = getTitleHeight() + 2.0f;
        for (Module module1 : Managers.MODULES.getAllModules())
        {
            if (module1.getCategory().equals(guiCategory))
            {
                final ComponentFactory factory = getComponentFactory();
                final ModuleComponent component = factory.createModuleComponent(module1, this,
                        2.0f,
                        moduleY,
                        (width - 4.0f),
                        15.0f);

                components.add(component);
                allComponents.add(component);
                moduleY += component.getHeight() + 1;
            }
        }
    }
}
