package net.shoreline.client.gui.hud;

import net.shoreline.client.gui.clickgui.ComponentFactory;
import net.shoreline.client.gui.clickgui.Frame;
import net.shoreline.client.gui.clickgui.ModuleComponent;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.impl.hud.HudModule;

public class HudFrame extends Frame
{
    public HudFrame(String title, int x, int y, int width, int titleHeight)
    {
        super(title, x, y, width, titleHeight);

        float moduleY = getTitleHeight() + 2.0f;
        for (HudModule module : Managers.MODULES.getHudModules())
        {
            final ComponentFactory factory = getComponentFactory();
            final ModuleComponent component = factory.createModuleComponent(module, this,
                    2.0f,
                    moduleY,
                    width - 4.0f,
                    15.0f);

            components.add(component);
            allComponents.add(component);
            moduleY += component.getHeight() + 1.0f;
        }
    }
}
