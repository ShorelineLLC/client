package net.shoreline.client.init;

import net.shoreline.client.Shoreline;
import net.shoreline.client.api.render.layers.LightmapManager;
import net.shoreline.client.api.render.shader.ShaderManager;
import net.shoreline.client.impl.manager.EventManager;
import net.shoreline.client.impl.manager.ModuleManager;
import net.shoreline.client.impl.manager.anticheat.AntiCheatManager;
import net.shoreline.client.impl.manager.client.AccountManager;
import net.shoreline.client.impl.manager.client.CommandManager;
import net.shoreline.client.impl.manager.client.MacroManager;
import net.shoreline.client.impl.manager.client.SocialManager;
import net.shoreline.client.impl.manager.client.cape.CapeManager;
import net.shoreline.client.impl.manager.combat.HitboxManager;
import net.shoreline.client.impl.manager.combat.PearlManager;
import net.shoreline.client.impl.manager.combat.TotemManager;
import net.shoreline.client.impl.manager.combat.hole.HoleManager;
import net.shoreline.client.impl.manager.mojang.LookupManager;
import net.shoreline.client.impl.manager.network.NetworkManager;
import net.shoreline.client.impl.manager.player.InventoryManager;
import net.shoreline.client.impl.manager.player.MovementManager;
import net.shoreline.client.impl.manager.player.PositionManager;
import net.shoreline.client.impl.manager.player.interaction.InteractionManager;
import net.shoreline.client.impl.manager.player.rotation.RotationManager;
import net.shoreline.client.impl.manager.world.BlockManager;
import net.shoreline.client.impl.manager.world.WaypointManager;
import net.shoreline.client.impl.manager.world.sound.SoundManager;
import net.shoreline.client.impl.manager.world.tick.TickManager;

/**
 * @author linus
 * @since 1.0
 */
public class Managers
{
    // Manager instances. Managers can be statically referenced after
    // initialized. Managers will be initialized in this order.
    public static NetworkManager NETWORK;
    public static MacroManager MACRO;
    public static ModuleManager MODULE;
    public static EventManager EVENT;
    public static CommandManager COMMAND;
    public static SocialManager SOCIAL;
    public static WaypointManager WAYPOINT;
    public static AccountManager ACCOUNT;
    public static TickManager TICK;
    public static InventoryManager INVENTORY;
    public static PositionManager POSITION;
    public static RotationManager ROTATION;
    //public static NCPManager NCP;
    public static AntiCheatManager ANTICHEAT;
    public static MovementManager MOVEMENT;
    public static HoleManager HOLE;
    public static TotemManager TOTEM;
    public static InteractionManager INTERACT;
    public static SoundManager SOUND;
    public static CapeManager CAPES;
    public static ShaderManager SHADER;
    public static LookupManager LOOKUP;
    public static LightmapManager LIGHT_MAP;
    public static BlockManager BLOCK;
    public static HitboxManager HITBOX;
    public static PearlManager PEARL;
    // The initialized state of the managers. If this is true, all managers
    // have been initialized and the init process is complete. As a general
    // rule, it is good practice to check this state before accessing instances.
    private static boolean initialized;

    /**
     * Initializes the manager instances. Should not be used if the
     * managers are already initialized.
     *
     * @see #isInitialized()
     */
    public static void init()
    {
        if (!isInitialized())
        {
            NETWORK = new NetworkManager();
            MACRO = new MacroManager();
            MODULE = new ModuleManager();
            EVENT = new EventManager();
            SOCIAL = new SocialManager();
            WAYPOINT = new WaypointManager();
            ACCOUNT = new AccountManager();
            TICK = new TickManager();
            INVENTORY = new InventoryManager();
            POSITION = new PositionManager();
            ROTATION = new RotationManager();
            BLOCK = new BlockManager();
            HITBOX = new HitboxManager();
            PEARL = new PearlManager();
            ANTICHEAT = new AntiCheatManager();
            MOVEMENT = new MovementManager();
            HOLE = new HoleManager();
            TOTEM = new TotemManager();
            INTERACT = new InteractionManager();
            COMMAND = new CommandManager();
            SOUND = new SoundManager();
            SHADER = new ShaderManager();
            LOOKUP = new LookupManager();
            initialized = true;
        }
    }

    /**
     * Initializes final manager properties. Only runs if the Manager
     * instances have been initialized.
     *
     * @see #init()
     * @see #isInitialized()
     */
    public static void postInit()
    {
        if (isInitialized())
        {
            MACRO.postInit();
            ACCOUNT.postInit();
            CAPES = new CapeManager();
            LIGHT_MAP = new LightmapManager();
        }
    }

    /**
     * Returns <tt>true</tt> if the Manager instances have been initialized.
     * This should always return <tt>true</tt> if {@link Shoreline#preInit()} has
     * finished running.
     *
     * @return <tt>true</tt> if the Manager instances have been initialized
     * @see Shoreline#preInit()
     * @see #init()
     * @see #initialized
     */
    public static boolean isInitialized()
    {
        return initialized;
    }
}
