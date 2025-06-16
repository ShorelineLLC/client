package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.client.HudModule;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public class ModuleManager
{
    private final LinkedHashMap<String, Module> modules = new LinkedHashMap<>();

    public ModuleManager()
    {
        registerModules(
                new ClickGuiModule(),
                new ColorsModule(),
                new HudModule()
        );

        for (Module module : getModules())
        {
            module.reflectConfigs();
        }
    }

    private void registerModule(Module module)
    {
        modules.put(module.getId(), module);
    }

    private void registerModules(Module... modules)
    {
        Arrays.stream(modules).forEach(this::registerModule);
    }

    public Module getModule(String id)
    {
        return modules.get(id);
    }

    public SequencedCollection<Module> getModules()
    {
        return modules.sequencedValues();
    }
}
