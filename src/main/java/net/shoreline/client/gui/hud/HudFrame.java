package net.shoreline.client.gui.hud;

import net.shoreline.client.api.module.HudModule;
import net.shoreline.client.gui.clickgui.ComponentFactory;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.Managers;

public class HudFrame extends Frame
{
    public HudFrame(String title, int x, int y, int width, int titleHeight)
    {
        super(title, x, y, width, titleHeight);

        int moduleY = getTitleHeight() + 2;
        for (HudModule module : Managers.MODULES.getHudModules())
        {
            final ComponentFactory factory = getComponentFactory();
            final ModuleComponent component = factory.createModuleComponent(
                    module, this, 2, moduleY, width - 4, 15);

            components.add(component);
            allComponents.add(component);
            moduleY += component.getHeight() + 1;
        }
    }
}
