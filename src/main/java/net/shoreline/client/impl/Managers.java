package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.impl.ac.AnticheatManager;
import net.shoreline.client.impl.combat.KitManager;
import net.shoreline.client.impl.combat.SafeHoleManager;
import net.shoreline.client.impl.combat.TargetManager;
import net.shoreline.client.impl.combat.TotemManager;
import net.shoreline.client.impl.command.CommandManager;
import net.shoreline.client.impl.file.ResourceManager;
import net.shoreline.client.impl.interact.InteractManager;
import net.shoreline.client.impl.inventory.InventoryManager;
import net.shoreline.client.impl.mining.MiningManager;
import net.shoreline.client.impl.module.ModuleManager;
import net.shoreline.client.impl.movement.MovementManager;
import net.shoreline.client.impl.network.NetworkManager;
import net.shoreline.client.impl.render.manager.RenderManager;
import net.shoreline.client.impl.render.manager.ShaderManager;
import net.shoreline.client.impl.rotation.RotationManager;
import net.shoreline.client.impl.social.SocialManager;
import net.shoreline.client.impl.world.FallDistManager;
import net.shoreline.client.impl.world.TickManager;

public class Managers
{
    public static NetworkManager NETWORK;
    public static MacroManager MACROS;
    public static ModuleManager MODULES;
    public static CommandManager COMMANDS;
    public static AnticheatManager ANTICHEAT;
    public static RotationManager ROTATION;
    public static InventoryManager INVENTORY;
    public static MovementManager MOVEMENT;
    public static FallDistManager FALL_DIST;
    public static InteractManager INTERACT;
    public static TickManager TICK;
    public static MiningManager MINING;
    public static SafeHoleManager HOLE;
    public static TotemManager TOTEM;
    public static KitManager KIT;
    public static TargetManager TARGETING;
    public static RenderManager RENDER;
    public static SocialManager SOCIAL;
    public static ResourceManager RESOURCE_PACK;

    public static void init()
    {
        NETWORK = new NetworkManager();
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
        COMMANDS = new CommandManager();
        ANTICHEAT = new AnticheatManager();
        ROTATION = new RotationManager();
        INVENTORY = new InventoryManager();
        MOVEMENT = new MovementManager();
        FALL_DIST = new FallDistManager();
        INTERACT = new InteractManager();
        TICK = new TickManager();
        MINING = new MiningManager();
        HOLE = new SafeHoleManager();
        TOTEM = new TotemManager();
        KIT = new KitManager();
        TARGETING = new TargetManager();
        RENDER = new RenderManager();
        SOCIAL = new SocialManager();
        RESOURCE_PACK = new ResourceManager();
    }
}
