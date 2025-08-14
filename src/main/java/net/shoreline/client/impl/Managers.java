package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.impl.combat.KitManager;
import net.shoreline.client.impl.module.combat.crystal.CrystalCalcManager;
import net.shoreline.client.impl.network.NetworkManager;
import net.shoreline.client.impl.ac.AnticheatManager;
import net.shoreline.client.impl.combat.SafeHoleManager;
import net.shoreline.client.impl.combat.TotemManager;
import net.shoreline.client.impl.command.CommandManager;
import net.shoreline.client.impl.mining.MiningManager;
import net.shoreline.client.impl.movement.MovementManager;
import net.shoreline.client.impl.interact.InteractManager;
import net.shoreline.client.impl.rotation.RotationManager;
import net.shoreline.client.impl.module.ModuleManager;
import net.shoreline.client.impl.inventory.InventoryManager;
import net.shoreline.client.impl.render.RenderManager;

public class Managers
{
    public static MacroManager MACROS;
    public static ModuleManager MODULES;
    public static CommandManager COMMANDS;
    public static NetworkManager NETWORK;
    public static AnticheatManager ANTICHEAT;
    public static RotationManager ROTATION;
    public static InventoryManager INVENTORY;
    public static MovementManager MOVEMENT;
    public static InteractManager INTERACT;
    public static MiningManager MINING;
    public static RenderManager RENDER;
    public static SafeHoleManager HOLE;
    public static TotemManager TOTEM;
    public static KitManager KIT;
    public static CrystalCalcManager CRYSTAL;

    public static void init()
    {
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
        COMMANDS = new CommandManager();
        NETWORK = new NetworkManager();
        ANTICHEAT = new AnticheatManager();
        ROTATION = new RotationManager();
        INVENTORY = new InventoryManager();
        MOVEMENT = new MovementManager();
        INTERACT = new InteractManager();
        MINING = new MiningManager();
        RENDER = new RenderManager();
        HOLE = new SafeHoleManager();
        TOTEM = new TotemManager();
        KIT = new KitManager();
        CRYSTAL = new CrystalCalcManager();
    }
}
