package net.shoreline.client.impl.module;

import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.AntiHungerModule;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.impl.module.exploit.PhaseModule;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.SequencedCollection;

public class ModuleManager
{
    private final LinkedHashMap<String, Module> modules = new LinkedHashMap<>();

    public ModuleManager()
    {
        registerModules(
                new AnticheatModule(),
                new ClickGuiModule(),
                new ColorsModule(),
                new FontModule(),
                new HeadlessMCModule(),
                new HudModule(),
                // Combat
                new AuraModule(),
                new AutoBowReleaseModule(),
                new AutoCrystalModule(),
                new AutoMineModule(),
                new AutoTotemModule(),
                new AutoTrapModule(),
                new AutoXPModule(),
                new CriticalsModule(),
                new FeetTrapModule(),
                new OffhandGappleModule(),
                new ReplenishModule(),
                new SelfTrapModule(),
                // Exploit
                new AntiHungerModule(),
                new FastLatencyModule(),
                new PhaseModule(),
                // Misc
                new AntiAimModule(),
                new ChestSwapModule(),
                new FakePlayerModule(),
                new MiddleClickModule(),
                new PacketSnifferModule(),
                // Movement
                new FastFallModule(),
                new FlightModule(),
                new NoJumpDelayModule(),
                new NoSlowModule(),
                new SpeedModule(),
                new SprintModule(),
                new VelocityModule(),
                // Render
                new FreecamModule(),
                new FullbrightModule(),
                new NametagsModule(),
                new NoBobModule(),
                new NoRenderModule(),
                new NoWeatherModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                // World
                new AirPlaceModule(),
                new AutoToolModule(),
                new FastPlaceModule(),
                new NoRotateModule(),
                new ScaffoldModule(),
                new SpeedMineModule(),
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
