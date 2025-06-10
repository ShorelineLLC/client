package net.shoreline.client.impl;

import net.shoreline.client.impl.module.ModuleManager;

public class Managers
{
    public static ModuleManager MODULES;

    public static void init()
    {
        MODULES = new ModuleManager();
    }
}
