package net.shoreline.client.impl.module;

import lombok.Getter;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.gui.hud.Anchor;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.AntiHungerModule;
import net.shoreline.client.impl.module.exploit.FastLatencyModule;
import net.shoreline.client.impl.module.exploit.PhaseModule;
import net.shoreline.client.impl.module.hud.FPSHudModule;
import net.shoreline.client.impl.module.hud.WatermarkHudModule;
import net.shoreline.client.impl.module.impl.HudModule;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;

import java.util.*;

@Getter
public class ModuleManager
{
    private final LinkedHashMap<String, Module> allModules = new LinkedHashMap<>();

    private final List<Module> modules = new ArrayList<>();
    private final List<HudModule> hudModules = new ArrayList<>();

    public ModuleManager()
    {
        registerModules(
                new AnticheatModule(),
                new ClickGuiModule(),
                new FontModule(),
                new HeadlessMCModule(),
                new HudGuiModule(),
                new ThemeModule(),
                // Combat
                new AuraModule(),
                new AutoBowReleaseModule(),
                new AutoCrystalModule(),
                new AutoDisconnectModule(),
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
                new AutoRespawnModule(),
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
                new BlockHighlightModule(),
                new ChamsModule(),
                new FreecamModule(),
                new FullbrightModule(),
                new HoleESPModule(),
                new MineESPModule(),
                new NametagsModule(),
                new NoBobModule(),
                new NoRenderModule(),
                new NoWeatherModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                new ZoomModule(),
                // World
                new AirPlaceModule(),
                new AutoToolModule(),
                new FastPlaceModule(),
                new NoRotateModule(),
                new ScaffoldModule(),
                new SpeedMineModule(),
                new TimerModule(),

                // HUD
                new FPSHudModule(),
                new WatermarkHudModule()
        );

        for (Module module : getAllModules())
        {
            module.reflectConfigs();
        }
    }

    private void registerModule(Module module)
    {
        if (module instanceof HudModule hudModule)
        {
            hudModules.add(hudModule);
        } else
        {
            modules.add(module);
        }

        allModules.put(module.getId(), module);
    }

    private void registerModules(Module... modules)
    {
        Arrays.stream(modules).forEach(this::registerModule);
    }

    public Module getModule(String id)
    {
        return allModules.get(id);
    }

    public SequencedCollection<Module> getAllModules()
    {
        return allModules.sequencedValues();
    }
}
