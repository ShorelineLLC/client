package net.shoreline.client.impl.module;

import lombok.Getter;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.*;
import net.shoreline.client.impl.module.client.LatencyModule;
import net.shoreline.client.impl.module.hud.*;
import net.shoreline.client.impl.module.impl.hud.HudModule;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class ModuleManager
{
    private final LinkedHashMap<String, Module> allModules = new LinkedHashMap<>();

    private final List<String> moduleNames = new ArrayList<>();
    private final List<Module> modules = new ArrayList<>();
    private final List<HudModule> hudModules = new ArrayList<>();

    public ModuleManager()
    {
        if (ShorelineMod.isBaritonePresent())
        {
            registerModule(new BaritoneModule());
        }

        registerModules(

                // Client
                new CapesModule(),
                new ClickGuiModule(),
                new FontModule(),
                new HeadlessMCModule(),
                new HudGuiModule(),
                new InteractionsModule(),
                new InventoryModule(),
                new IRCModule(),
                new LatencyModule(),
                new RotationsModule(),
                new SocialsModule(),
                new SoundsModule(),
                new ThemeModule(),
                new TitleScreenModule(),
                // Combat
                new AnchorAuraModule(),
                new AutoArmorModule(),
                new AutoBowReleaseModule(),
                new AutoCrystalModule(),
                new AutoDisconnectModule(),
                new AutoMineModule(),
                new AutoPotModule(),
                new AutoTotemModule(),
                new AutoTrapModule(),
                new AutoXPModule(),
                new CriticalsModule(),
                new FeetTrapModule(),
                new FillerModule(),
                new KeepSprintModule(),
                new KillAuraModule(),
                new OffhandGappleModule(),
                new PearlBlockerModule(),
                new ReplenishModule(),
                new SelfFillerModule(),
                new SelfTrapModule(),
                // Exploit
                new AntiHungerModule(),
                new BacktrackModule(),
                new ChorusControlModule(),
                new FakeLagModule(),
                new MountControlModule(),
                new NoFallModule(),
                new PhaseModule(),
                new ProjectileBoostModule(),
                new ReachModule(),
                // Misc
                new AntiAimModule(),
                new AutoFishModule(),
                new AutoReconnectModule(),
                new AutoRespawnModule(),
                new BetterChatModule(),
                new BetterTabModule(),
                new ChestSwapModule(),
                new ChestStealerModule(),
                new FakePlayerModule(),
                new MiddleClickModule(),
                new NameProtectModule(),
                new NoLagModule(),
                new NotifierModule(),
                new PacketSnifferModule(),
                new ShulkerceptionModule(),
                new SkinBlinkModule(),
                new SpammerModule(),
                new XCarryModule(),
                // Movement
                new AvoidModule(),
                new ElytraBoostModule(),
                new ElytraFlyModule(),
                new FastFallModule(),
                new FastWebModule(),
                new FlightModule(),
                new JesusModule(),
                new LongJumpModule(),
                new NoAccelModule(),
                new NoJumpDelayModule(),
                new NoSlowModule(),
                new ParkourModule(),
                new PatherModule(),
                new PhaseSnapModule(),
                new SafeWalkModule(),
                new SpeedModule(),
                new SprintModule(),
                new StepModule(),
                new VelocityModule(),
                new YawModule(),
                // Render
                new BlockESPModule(),
                new BlockHighlightModule(),
                new ChamsModule(),
                new ESPModule(),
                new FreecamModule(),
                new FullbrightModule(),
                new HoleESPModule(),
                new LogoutPointsModule(),
                new MineESPModule(),
                new ModelsModule(),
                new NametagsModule(),
                new NoBobModule(),
                new NoRenderModule(),
                new NoWeatherModule(),
                new ShadersModule(),
                new SkyboxModule(),
                new SwingModule(),
                new TintModule(),
                new TracersModule(),
                new TrailsModule(),
                new TrajectoriesModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                new ZoomModule(),
                // World
                new AirPlaceModule(),
                new AutoToolModule(),
                new FastPlaceModule(),
                new NukerModule(),
                new ScaffoldModule(),
                new SpeedMineModule(),
                new TimerModule(),
                new XRayModule(),

                // HUD
                new ArmorHudModule(),
                new ArrayListHudModule(),
                new BrandHudModule(),
                new CoordsHudModule(),
                new CountsHudModule(),
                new CrosshairHudModule(),
                new DurabilityHudModule(),
                new FPSHudModule(),
                new NotificationsHudModule(),
                new PacketsHudModule(),
                new PingHudModule(),
                new PotionsHudModule(),
                new ServerStatusHudModule(),
                new SpeedHudModule(),
                new TextRadarHudModule(),
                new TPSHudModule(),
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
            moduleNames.add(module.getName());
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

    public List<Module> getModules(Predicate<Module> tester)
    {
        List<Module> result = new ArrayList<>();
        for (Module module : getAllModules())
        {
            if (tester.test(module))
            {
                result.add(module);
            }
        }

        return result;
    }
}
