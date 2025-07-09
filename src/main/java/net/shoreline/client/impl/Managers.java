package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.api.network.NetworkManager;
import net.shoreline.client.impl.ac.AnticheatManager;
import net.shoreline.client.impl.manager.MovementManager;
import net.shoreline.client.impl.manager.interact.InteractManager;
import net.shoreline.client.impl.manager.rotation.RotationManager;
import net.shoreline.client.impl.module.ModuleManager;
import net.shoreline.client.impl.manager.inventory.InventoryManager;
import net.shoreline.client.impl.render.RenderManager;

public class Managers
{
    public static MacroManager MACROS;
    public static ModuleManager MODULES;
    public static NetworkManager NETWORK;
    public static AnticheatManager ANTICHEAT;
    public static RotationManager ROTATION;
    public static InventoryManager INVENTORY;
    public static MovementManager MOVEMENT;
    public static InteractManager INTERACT;
    public static RenderManager RENDER;

    public static void init()
    {
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
        NETWORK = new NetworkManager();
        ANTICHEAT = new AnticheatManager();
        ROTATION = new RotationManager();
        INVENTORY = new InventoryManager();
        MOVEMENT = new MovementManager();
        INTERACT = new InteractManager();
        RENDER = new RenderManager();
    }
}
