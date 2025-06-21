package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.impl.module.client.ColorsModule;
import net.shoreline.client.impl.module.client.HudModule;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.client.impl.module.combat.AuraModule;
import net.shoreline.client.impl.module.misc.FakePlayerModule;
import net.shoreline.client.impl.module.movement.NoSlowModule;
import net.shoreline.client.impl.module.movement.SprintModule;
import net.shoreline.client.impl.module.movement.VelocityModule;
import net.shoreline.client.impl.module.render.FullbrightModule;
import net.shoreline.client.impl.module.render.NoWeatherModule;

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
                new HudModule(),
                new RotationsModule(),
                // Combat
                new AuraModule(),
                // Misc
                new FakePlayerModule(),
                // Movement
                new NoSlowModule(),
                new SprintModule(),
                new VelocityModule(),
                // Render
                new FullbrightModule(),
                new NoWeatherModule()
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
