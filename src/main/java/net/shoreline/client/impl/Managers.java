package net.shoreline.client.impl;

import net.shoreline.client.api.macro.MacroManager;
import net.shoreline.client.impl.module.ModuleManager;

public class Managers
{
    public static MacroManager MACROS;
    public static ModuleManager MODULES;

    public static void init()
    {
        MACROS = new MacroManager();
        MODULES = new ModuleManager();
    }
}
