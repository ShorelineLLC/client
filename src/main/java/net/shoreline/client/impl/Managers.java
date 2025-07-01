package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.api.network.NetworkManager;
import net.shoreline.client.impl.ac.AnticheatManager;
import net.shoreline.client.impl.player.rotation.RotationManager;
import net.shoreline.client.impl.module.ModuleManager;
import net.shoreline.client.impl.player.inventory.InventoryManager;
import net.shoreline.client.impl.render.RenderManager;

public class Managers
{
    public static MacroManager MACROS;
    public static ModuleManager MODULES;
    public static NetworkManager NETWORK;
    public static RotationManager ROTATION;
    public static InventoryManager INVENTORY;
    public static AnticheatManager ANTICHEAT;
    public static RenderManager RENDER;

    public static void init()
    {
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
        NETWORK = new NetworkManager();
        ROTATION = new RotationManager();
        INVENTORY = new InventoryManager();
        ANTICHEAT = new AnticheatManager();
        RENDER = new RenderManager();
    }
}
