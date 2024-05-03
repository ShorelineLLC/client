package net.shoreline.client.init;

import net.shoreline.client.Shoreline;
import net.shoreline.client.ShorelineMod;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.manager.ModuleManager;
import net.shoreline.client.api.module.Module;
import net.shoreline.client.impl.module.client.*;
import net.shoreline.client.impl.module.combat.*;
import net.shoreline.client.impl.module.exploit.*;
import net.shoreline.client.impl.module.misc.*;
import net.shoreline.client.impl.module.movement.*;
import net.shoreline.client.impl.module.render.*;
import net.shoreline.client.impl.module.world.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author linus
 * @see Module
 * @see ModuleManager
 * @since 1.0
 */
public class Modules {
    // Module instances.
    public static ServerModule SERVER;
    public static ClickGuiModule CLICK_GUI;
    public static ColorsModule COLORS;
    public static HUDModule HUD;
    public static RotationsModule ROTATIONS;
    public static BaritoneModule BARITONE;
    public static CapesModule CAPES;
    // Combat
    public static AuraModule AURA;
    public static AutoArmorModule AUTO_ARMOR;
    public static AutoBowReleaseModule AUTO_BOW_RELEASE;
    public static AutoCrystalModule AUTO_CRYSTAL;
    public static AutoLogModule AUTO_LOG;
    public static AutoTotemModule AUTO_TOTEM;
    public static AutoTrapModule AUTO_TRAP;
    public static AutoXPModule AUTO_XP;
    public static BackTrackModule BACK_TRACK;
    public static BlockLagModule BLOCK_LAG;
    public static BowAimModule BOW_AIM;
    public static CriticalsModule CRITICALS;
    public static HoleFillModule HOLE_FILL;
    public static NoHitDelayModule NO_HIT_DELAY;
    public static ReplenishModule REPLENISH;
    public static SelfBowModule SELF_BOW;
    public static SelfTrapModule SELF_TRAP;
    public static SurroundModule SURROUND;
    public static TriggerModule TRIGGER;
    // Exploit
    public static AntiHungerModule ANTI_HUNGER;
    public static ChorusControlModule CHORUS_CONTROL;
    public static ClientSpoofModule CLIENT_SPOOFER;
    public static CrasherModule CRASHER;
    public static DisablerModule DISABLER;
    public static ExtendedFireworkModule EXTENDED_FIREWORK;
    public static FakeLatencyModule FAKE_LATENCY;
    public static FastProjectileModule FAST_PROJECTILE;
    public static PacketCancelerModule PACKET_CANCELER;
    public static PacketFlyModule PACKET_FLY;
    public static PhaseModule PHASE;
    public static PortalGodModeModule PORTAL_GOD_MODE;
    public static ReachModule REACH;
    // Misc
    public static AntiAimModule ANTI_AIM;
    public static NoPacketKickModule ANTI_BOOK_BAN;
    public static AntiSpamModule ANTI_SPAM;
    public static AutoAcceptModule AUTO_ACCEPT;
    public static AutoEatModule AUTO_EAT;
    public static AutoFishModule AUTO_FISH;
    public static AutoReconnectModule AUTO_RECONNECT;
    public static AutoRespawnModule AUTO_RESPAWN;
    public static BeaconSelectorModule BEACON_SELECTOR;
    // public static BetterChatModule BETTER_CHAT;
    public static ChatNotifierModule CHAT_NOTIFIER;
    public static ChestSwapModule CHEST_SWAP;
    public static FakePlayerModule FAKE_PLAYER;
    public static InvCleanerModule INV_CLEANER;
    public static MiddleClickModule MIDDLE_CLICK;
    public static NoPacketKickModule NO_PACKET_KICK;
    public static NoSoundLagModule NO_SOUND_LAG;
    public static TimerModule TIMER;
    public static TrueDurabilityModule TRUE_DURABILITY;
    public static UnfocusedFPSModule UNFOCUSED_FPS;
    public static XCarryModule XCARRY;
    // Movement
    public static AntiLevitationModule ANTI_LEVITATION;
    public static AutoWalkModule AUTO_WALK;
    public static ElytraFlyModule ELYTRA_FLY;
    public static EntityControlModule ENTITY_CONTROL;
    public static EntitySpeedModule ENTITY_SPEED;
    public static FakeLagModule FAKE_LAG;
    public static FastFallModule FAST_FALL;
    public static FlightModule FLIGHT;
    public static IceSpeedModule ICE_SPEED;
    public static JesusModule JESUS;
    public static LongJumpModule LONG_JUMP;
    public static NoFallModule NO_FALL;
    public static NoJumpDelayModule NO_JUMP_DELAY;
    public static NoSlowModule NO_SLOW;
    public static ParkourModule PARKOUR;
    public static SpeedModule SPEED;
    public static SprintModule SPRINT;
    public static StepModule STEP;
    public static TickShiftModule TICK_SHIFT;
    public static TridentFlyModule TRIDENT_FLY;
    public static VelocityModule VELOCITY;
    public static YawModule YAW;
    // Render
    public static BlockHighlightModule BLOCK_HIGHLIGHT;
    public static BreakHighlightModule BREAK_HIGHLIGHT;
    public static ChamsModule CHAMS;
    public static ESPModule ESP;
    public static ExtraTabModule EXTRA_TAB;
    public static FreecamModule FREECAM;
    public static FullbrightModule FULLBRIGHT;
    public static NameProtectModule NAME_PROTECT;
    public static HoleESPModule HOLE_ESP;
    public static NametagsModule NAMETAGS;
    public static NoRenderModule NO_RENDER;
    public static NoRotateModule NO_ROTATE;
    public static NoWeatherModule NO_WEATHER;
    public static ParticlesModule PARTICLES;
    public static SkeletonModule SKELETON;
    public static SkyboxModule SKYBOX;
    public static TooltipsModule TOOLTIPS;
    public static TracersModule TRACERS;
    public static TrueSightModule TRUE_SIGHT;
    public static ViewClipModule VIEW_CLIP;
    public static ViewModelModule VIEW_MODEL;
    // public static WaypointsModule WAYPOINTS;
    // World
    public static AntiInteractModule ANTI_INTERACT;
    public static AutoMineModule AUTO_MINE;
    public static AutoToolModule AUTO_TOOL;
    public static AvoidModule AVOID;
    public static BlockInteractModule BLOCK_INTERACT;
    public static FastDropModule FAST_DROP;
    public static FastPlaceModule FAST_PLACE;
    public static MultitaskModule MULTITASK;
    public static NoGlitchBlocksModule NO_GLITCH_BLOCKS;
    public static ScaffoldModule SCAFFOLD;
    public static SpeedmineModule SPEEDMINE;
    // The initialized state of the modules. Once this is true, all modules
    // have been initialized and the init process is complete. As a general
    // rule, it is good practice to check this state before accessing instances.
    private static boolean initialized;
    // The module initialization cache. This prevents modules from being
    // initialized more than once.
    private static Set<Module> CACHE;

    /**
     * Returns the registered {@link Module} with the param name in the
     * {@link ModuleManager}. The same module
     * cannot be retrieved more than once using this method.
     *
     * @param id The module name
     * @return The retrieved module
     * @throws IllegalStateException If the module was not registered
     * @see ModuleManager
     */
    private static Module getRegisteredModule(final String id) {
        Module registered = Managers.MODULE.getModule(id);
        if (CACHE.add(registered)) {
            return registered;
        }
        // already cached!!
        else {
            throw new IllegalStateException("Invalid module requested: " + id);
        }
    }

    /**
     * Initializes the modules instances. Should not be used if the
     * modules are already initialized. Cannot function unless the
     * {@link ModuleManager} is initialized.
     *
     * @see #getRegisteredModule(String)
     * @see Managers#isInitialized()
     */
    public static void init() {
        if (Managers.isInitialized()) {
            CACHE = new HashSet<>();
            CLICK_GUI = (ClickGuiModule) getRegisteredModule("clickgui-module");
            COLORS = (ColorsModule) getRegisteredModule("colors-module");
            HUD = (HUDModule) getRegisteredModule("hud-module");
            ROTATIONS = (RotationsModule) getRegisteredModule("rotations-module");
            SERVER = (ServerModule) getRegisteredModule("server-module");
            if (ShorelineMod.isBaritonePresent()) {
                BARITONE = (BaritoneModule) getRegisteredModule("baritone-module");
            }
            CAPES = (CapesModule) getRegisteredModule("capes-module");
            AURA = (AuraModule) getRegisteredModule("aura-module");
            AUTO_ARMOR = (AutoArmorModule) getRegisteredModule("autoarmor-module");
            AUTO_BOW_RELEASE = (AutoBowReleaseModule) getRegisteredModule("autobowrelease-module");
            AUTO_CRYSTAL = (AutoCrystalModule) getRegisteredModule("autocrystal-module");
            AUTO_LOG = (AutoLogModule) getRegisteredModule("autolog-module");
            AUTO_TOTEM = (AutoTotemModule) getRegisteredModule("autototem-module");
            AUTO_TRAP = (AutoTrapModule) getRegisteredModule("autotrap-module");
            AUTO_XP = (AutoXPModule) getRegisteredModule("autoxp-module");
            // BACK_TRACK = (BackTrackModule) getRegisteredModule("backtrack-module");
            BLOCK_LAG = (BlockLagModule) getRegisteredModule("blocklag-module");
            BOW_AIM = (BowAimModule) getRegisteredModule("bowaim-module");
            CRITICALS = (CriticalsModule) getRegisteredModule("criticals-module");
            HOLE_FILL = (HoleFillModule) getRegisteredModule("holefill-module");
            NO_HIT_DELAY = (NoHitDelayModule) getRegisteredModule("nohitdelay-module");
            REPLENISH = (ReplenishModule) getRegisteredModule("replenish-module");
            SELF_BOW = (SelfBowModule) getRegisteredModule("selfbow-module");
            SELF_TRAP = (SelfTrapModule) getRegisteredModule("selftrap-module");
            SURROUND = (SurroundModule) getRegisteredModule("surround-module");
            TRIGGER = (TriggerModule) getRegisteredModule("trigger-module");
            ANTI_HUNGER = (AntiHungerModule) getRegisteredModule("antihunger-module");
            CHORUS_CONTROL = (ChorusControlModule) getRegisteredModule("choruscontrol-module");
            CLIENT_SPOOFER = (ClientSpoofModule) getRegisteredModule("clientspoof-module");
            CRASHER = (CrasherModule) getRegisteredModule("crasher-module");
            DISABLER = (DisablerModule) getRegisteredModule("disabler-module");
            EXTENDED_FIREWORK = (ExtendedFireworkModule) getRegisteredModule("extendedfirework-module");
            FAKE_LATENCY = (FakeLatencyModule) getRegisteredModule("fakelatency-module");
            FAST_PROJECTILE = (FastProjectileModule) getRegisteredModule("fastprojectile-module");
            PACKET_CANCELER = (PacketCancelerModule) getRegisteredModule("packetcanceler-module");
            PACKET_FLY = (PacketFlyModule) getRegisteredModule("packetfly-module");
            PHASE = (PhaseModule) getRegisteredModule("phase-module");
            PORTAL_GOD_MODE = (PortalGodModeModule) getRegisteredModule("portalgodmode-module");
            REACH = (ReachModule) getRegisteredModule("reach-module");
            ANTI_AIM = (AntiAimModule) getRegisteredModule("antiaim-module");
            // ANTI_BOOK_BAN = (AntiBookBanModule) getRegisteredModule("antibookban-module");
            ANTI_SPAM = (AntiSpamModule) getRegisteredModule("antispam-module");
            AUTO_ACCEPT = (AutoAcceptModule) getRegisteredModule("autoaccept-module");
            AUTO_EAT = (AutoEatModule) getRegisteredModule("autoeat-module");
            AUTO_FISH = (AutoFishModule) getRegisteredModule("autofish-module");
            AUTO_RECONNECT = (AutoReconnectModule) getRegisteredModule("autoreconnect-module");
            AUTO_RESPAWN = (AutoRespawnModule) getRegisteredModule("autorespawn-module");
            BEACON_SELECTOR = (BeaconSelectorModule) getRegisteredModule("beaconselector-module");
            // BETTER_CHAT = (BetterChatModule) getRegisteredModule("betterchat-module");
            CHAT_NOTIFIER = (ChatNotifierModule) getRegisteredModule("chatnotifier-module");
            CHEST_SWAP = (ChestSwapModule) getRegisteredModule("chestswap-module");
            FAKE_PLAYER = (FakePlayerModule) getRegisteredModule("fakeplayer-module");
            INV_CLEANER = (InvCleanerModule) getRegisteredModule("invcleaner-module");
            MIDDLE_CLICK = (MiddleClickModule) getRegisteredModule("middleclick-module");
            NO_PACKET_KICK = (NoPacketKickModule) getRegisteredModule("nopacketkick-module");
            NO_SOUND_LAG = (NoSoundLagModule) getRegisteredModule("nosoundlag-module");
            TIMER = (TimerModule) getRegisteredModule("timer-module");
            TRUE_DURABILITY = (TrueDurabilityModule) getRegisteredModule("truedurability-module");
            UNFOCUSED_FPS = (UnfocusedFPSModule) getRegisteredModule("unfocusedfps-module");
            XCARRY = (XCarryModule) getRegisteredModule("xcarry-module");
            ANTI_LEVITATION = (AntiLevitationModule) getRegisteredModule("antilevitation-module");
            AUTO_WALK = (AutoWalkModule) getRegisteredModule("autowalk-module");
            ELYTRA_FLY = (ElytraFlyModule) getRegisteredModule("elytrafly-module");
            ENTITY_CONTROL = (EntityControlModule) getRegisteredModule("entitycontrol-module");
            ENTITY_SPEED = (EntitySpeedModule) getRegisteredModule("entityspeed-module");
            FAKE_LAG = (FakeLagModule) getRegisteredModule("fakelag-module");
            FAST_FALL = (FastFallModule) getRegisteredModule("fastfall-module");
            FLIGHT = (FlightModule) getRegisteredModule("flight-module");
            ICE_SPEED = (IceSpeedModule) getRegisteredModule("icespeed-module");
            JESUS = (JesusModule) getRegisteredModule("jesus-module");
            LONG_JUMP = (LongJumpModule) getRegisteredModule("longjump-module");
            NO_FALL = (NoFallModule) getRegisteredModule("nofall-module");
            NO_JUMP_DELAY = (NoJumpDelayModule) getRegisteredModule("nojumpdelay-module");
            NO_SLOW = (NoSlowModule) getRegisteredModule("noslow-module");
            PARKOUR = (ParkourModule) getRegisteredModule("parkour-module");
            SPEED = (SpeedModule) getRegisteredModule("speed-module");
            SPRINT = (SprintModule) getRegisteredModule("sprint-module");
            STEP = (StepModule) getRegisteredModule("step-module");
            TICK_SHIFT = (TickShiftModule) getRegisteredModule("tickshift-module");
            TRIDENT_FLY = (TridentFlyModule) getRegisteredModule("tridentfly-module");
            VELOCITY = (VelocityModule) getRegisteredModule("velocity-module");
            YAW = (YawModule) getRegisteredModule("yaw-module");
            BLOCK_HIGHLIGHT = (BlockHighlightModule) getRegisteredModule("blockhighlight-module");
            BREAK_HIGHLIGHT = (BreakHighlightModule) getRegisteredModule("breakhighlight-module");
            CHAMS = (ChamsModule) getRegisteredModule("chams-module");
            ESP = (ESPModule) getRegisteredModule("esp-module");
            EXTRA_TAB = (ExtraTabModule) getRegisteredModule("extratab-module");
            FREECAM = (FreecamModule) getRegisteredModule("freecam-module");
            FULLBRIGHT = (FullbrightModule) getRegisteredModule("fullbright-module");
            HOLE_ESP = (HoleESPModule) getRegisteredModule("holeesp-module");
            NAME_PROTECT = (NameProtectModule) getRegisteredModule("nameprotect-module");
            NAMETAGS = (NametagsModule) getRegisteredModule("nametags-module");
            NO_RENDER = (NoRenderModule) getRegisteredModule("norender-module");
            NO_ROTATE = (NoRotateModule) getRegisteredModule("norotate-module");
            NO_WEATHER = (NoWeatherModule) getRegisteredModule("noweather-module");
            PARTICLES = (ParticlesModule) getRegisteredModule("particles-module");
            SKELETON = (SkeletonModule) getRegisteredModule("skeleton-module");
            SKYBOX = (SkyboxModule) getRegisteredModule("skybox-module");
            TOOLTIPS = (TooltipsModule) getRegisteredModule("tooltips-module");
            TRACERS = (TracersModule) getRegisteredModule("tracers-module");
            TRUE_SIGHT = (TrueSightModule) getRegisteredModule("truesight-module");
            VIEW_CLIP = (ViewClipModule) getRegisteredModule("viewclip-module");
            VIEW_MODEL = (ViewModelModule) getRegisteredModule("viewmodel-module");
            // WAYPOINTS = (WaypointsModule) getRegisteredModule("waypoints-module");
            ANTI_INTERACT = (AntiInteractModule) getRegisteredModule("antiinteract-module");
            AUTO_MINE = (AutoMineModule) getRegisteredModule("automine-module");
            AUTO_TOOL = (AutoToolModule) getRegisteredModule("autotool-module");
            AVOID = (AvoidModule) getRegisteredModule("avoid-module");
            BLOCK_INTERACT = (BlockInteractModule) getRegisteredModule("blockinteract-module");
            FAST_DROP = (FastDropModule) getRegisteredModule("fastdrop-module");
            FAST_PLACE = (FastPlaceModule) getRegisteredModule("fastplace-module");
            MULTITASK = (MultitaskModule) getRegisteredModule("multitask-module");
            NO_GLITCH_BLOCKS = (NoGlitchBlocksModule) getRegisteredModule("noglitchblocks-module");
            SCAFFOLD = (ScaffoldModule) getRegisteredModule("scaffold-module");
            SPEEDMINE = (SpeedmineModule) getRegisteredModule("speedmine-module");
            initialized = true;
            // reflect configuration properties for each cached module
            for (Module module : CACHE) {
                if (module == null) {
                    continue;
                }
                module.reflectConfigs();
                if (module instanceof ToggleModule t) {
                    Managers.MACRO.register(t.getKeybinding());
                }
            }
            CACHE.clear();
        } else {
            throw new RuntimeException("Accessed modules before managers " +
                    "finished initializing!");
        }
    }

    /**
     * Returns <tt>true</tt> if the {@link Module} instances have been
     * initialized. This should always return <tt>true</tt> if
     * {@link Shoreline#init()} has finished running.
     *
     * @return <tt>true</tt> if the module instances have been initialized
     * @see #init()
     * @see #initialized
     */
    public static boolean isInitialized() {
        return initialized;
    }
}
