package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.Module;

import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public class ModuleManager
{
    private final LinkedHashMap<String, Module> modules = new LinkedHashMap<>();

    public ModuleManager()
    {

    }

    private void registerModule(Module module)
    {
        modules.put(module.getId(), module);
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
