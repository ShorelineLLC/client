package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.AuraModule;
import net.shoreline.client.impl.module.combat.AutoBowReleaseModule;
import net.shoreline.client.impl.module.exploit.AntiHungerModule;
import net.shoreline.client.impl.module.misc.AntiAimModule;
import net.shoreline.client.impl.module.misc.FakePlayerModule;
import net.shoreline.client.impl.module.movement.NoSlowModule;
import net.shoreline.client.impl.module.movement.SpeedModule;
import net.shoreline.client.impl.module.movement.SprintModule;
import net.shoreline.client.impl.module.movement.VelocityModule;
import net.shoreline.client.impl.module.render.FullbrightModule;
import net.shoreline.client.impl.module.render.NoBobModule;
import net.shoreline.client.impl.module.world.FastPlaceModule;
import net.shoreline.client.impl.module.world.NoRotateModule;
import net.shoreline.client.impl.module.render.NoWeatherModule;
import net.shoreline.client.impl.module.world.TimerModule;

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
                new FontModule(),
                new HeadlessMcModule(),
                new HudModule(),
                new RotationsModule(),
                // Combat
                new AuraModule(),
                new AutoBowReleaseModule(),
                // Exploit
                new AntiHungerModule(),
                // Misc
                new AntiAimModule(),
                new FakePlayerModule(),
                // Movement
                new NoSlowModule(),
                new SpeedModule(),
                new SprintModule(),
                new VelocityModule(),
                // Render
                new FullbrightModule(),
                new NoBobModule(),
                new NoWeatherModule(),
                // World
                new FastPlaceModule(),
                new NoRotateModule(),
                new TimerModule()
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
