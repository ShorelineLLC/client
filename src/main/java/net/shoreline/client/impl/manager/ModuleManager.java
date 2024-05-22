package net.shoreline.client.impl.manager;

import net.shoreline.client.Shoreline;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.*;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;
import net.shoreline.client.init.Managers;

import java.util.*;

/**
 * @author linus
 * @since 1.0
 */
public final class ModuleManager
{
    // The client module register. Keeps a list of modules and their ids for
    // easy retrieval by id.
    private final Map<String, Module> modules =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Initializes the module register.
     */
    public ModuleManager() {
        // MAINTAIN ALPHABETICAL ORDER
        register(
                // Client
                new ServerModule(),
                new CapesModule(),
                new ClickGuiModule(),
                new ColorsModule(),
                new HUDModule(),
                new RotationsModule(),
                // Combat
                new AuraModule(),
                // new AutoAnchorModule(),
                new AutoArmorModule(),
                new AutoBowReleaseModule(),
                new AutoCrystalModule(),
                new AutoLogModule(),
                new AutoTotemModule(),
                new AutoTrapModule(),
                new AutoWebModule(),
                new AutoXPModule(),
                // new BacktrackModule(),
                new BlockLagModule(),
                new BowAimModule(),
                new ClickCrystalModule(),
                new CriticalsModule(),
                new HoleFillModule(),
                new NoHitDelayModule(),
                new ReplenishModule(),
                new SelfBowModule(),
                new SelfTrapModule(),
                new SurroundModule(),
                new TriggerModule(),
                // Exploit
                new AntiHungerModule(),
                new ChorusControlModule(),
                new ClientSpoofModule(),
                new CrasherModule(),
                new DisablerModule(),
                new ExtendedFireworkModule(),
                new FakeLatencyModule(),
                new FastLatencyModule(),
                new FastProjectileModule(),
                new InventorySyncModule(),
                new PacketCancelerModule(),
                new PacketFlyModule(),
                new PhaseModule(),
                new PortalGodModeModule(),
                new ReachModule(),
                // Misc
                // new AntiAFKModule(),
                new AntiAimModule(),
                // new AntiBookBanModule(),
                new AntiSpamModule(),
                new AutoAcceptModule(),
                new AutoEatModule(),
                new AutoFishModule(),
                new AutoReconnectModule(),
                new AutoRespawnModule(),
                new BeaconSelectorModule(),
                new BetterChatModule(),
                new ChatNotifierModule(),
                new ChestSwapModule(),
                // new ChestStealerModule(),
                new FakePlayerModule(),
                new InvCleanerModule(),
                new MiddleClickModule(),
                new NoPacketKickModule(),
                new NoSoundLagModule(),
                new PacketLoggerModule(),
                new TimerModule(),
                new TrueDurabilityModule(),
                new UnfocusedFPSModule(),
                new XCarryModule(),
                // Movement
                new AntiLevitationModule(),
                new AutoWalkModule(),
                new ElytraFlyModule(),
                new EntityControlModule(),
                new EntitySpeedModule(),
                new FakeLagModule(),
                new FastFallModule(),
                new FlightModule(),
                new IceSpeedModule(),
                new JesusModule(),
                new LongJumpModule(),
                new NoFallModule(),
                new NoJumpDelayModule(),
                new NoSlowModule(),
                new ParkourModule(),
                new SpeedModule(),
                new SprintModule(),
                new StepModule(),
                new TickShiftModule(),
                new TridentFlyModule(),
                new VelocityModule(),
                new YawModule(),
                // Render
                new BlockHighlightModule(),
                // new BreadcrumbsModule(),
                new BreakHighlightModule(),
                new ChamsModule(),
                new ESPModule(),
                new ExtraTabModule(),
                new FreecamModule(),
                new FullbrightModule(),
                new HoleESPModule(),
                new NameProtectModule(),
                new NametagsModule(),
                new NoRenderModule(),
                new NoRotateModule(),
                new NoWeatherModule(),
                new ParticlesModule(),
                new PhaseESPModule(),
                new SkeletonModule(),
                new SkyboxModule(),
                new TooltipsModule(),
                new TracersModule(),
                new TrueSightModule(),
                new ViewClipModule(),
                new ViewModelModule(),
                new WaypointsModule(),
                new ZoomModule(),
                // World
                new AntiInteractModule(),
                new AutoMineModule(),
                new AutoToolModule(),
                new AvoidModule(),
                new BlockInteractModule(),
                new FastDropModule(),
                new FastPlaceModule(),
                new MultitaskModule(),
                new NoGlitchBlocksModule(),
                new ScaffoldModule(),
                new SpeedmineModule()
                // new WallhackModule()
        );
        if (ShorelineMod.isBaritonePresent()) {
            register(new BaritoneModule());
        }
        // Register keybinds
        for (Module module : getModules()) {
            if (module instanceof ToggleModule t) {
                Managers.MACRO.register(t.getKeybinding());
            }
        }
        Shoreline.info("Registered {} modules!", modules.size());
    }

    /**
     *
     */
    public void postInit() {
        // TODO
    }

    /**
     * @param modules
     * @see #register(Module)
     */
    private void register(Module... modules) {
        for (Module module : modules) {
            register(module);
        }
    }

    /**
     * @param module
     */
    private void register(Module module) {
        modules.put(module.getId(), module);
    }

    /**
     * @param id
     * @return
     */
    public Module getModuleById(String id) {
        return modules.get(id);
    }

    /**
     * @return
     */
    public List<Module> getModules() {
        return new ArrayList<>(modules.values());
    }
}
