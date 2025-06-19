package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.api.network.NetworkManager;
import net.shoreline.client.api.rotation.RotationManager;
import net.shoreline.client.impl.module.ModuleManager;
import net.shoreline.client.impl.render.RenderManager;

public class Managers
{
    public static MacroManager MACROS;
    public static ModuleManager MODULES;
    public static NetworkManager NETWORK;
    public static RotationManager ROTATION;
    public static RenderManager RENDER;

    public static void init()
    {
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
        NETWORK = new NetworkManager();
        ROTATION = new RotationManager();
        RENDER = new RenderManager();
    }
}
