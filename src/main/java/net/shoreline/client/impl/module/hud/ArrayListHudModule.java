package net.shoreline.client.impl.module.hud;

import net.minecraft.util.Formatting;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.module.impl.hud.DynamicEntry;
import net.shoreline.client.impl.module.impl.hud.DynamicHudModule;

public class ArrayListHudModule extends DynamicHudModule
{
    public ArrayListHudModule()
    {
        super("ArrayList", "Displays the currently enabled modules", 40, 40);
    }

    @Override
    public void onEnable()
    {
        for (Module module : Managers.MODULES.getModules())
        {
            if (module instanceof Toggleable toggleable)
            {
                getHudEntries().add(new DynamicEntry(this, () -> getFullName(toggleable), () -> toggleable.isEnabled() && !toggleable.isHidden()));
            }
        }
    }

    public String getFullName(Toggleable module)
    {
        return module.getName() +
                (module.getModuleData() == null
                ? ""
                : Formatting.GRAY + " ["
                + Formatting.WHITE + module.getModuleData()
                + Formatting.GRAY + "]");
    }
}
