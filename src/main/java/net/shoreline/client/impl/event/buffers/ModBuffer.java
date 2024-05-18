package net.shoreline.client.impl.event.buffers;

import net.shoreline.client.ShorelineMod;

public class ModBuffer {

    public static String getShorelineModName() {
        return ShorelineMod.MOD_NAME;
    }

    public static String getShorelineModVersion() {
        return ShorelineMod.MOD_VER;
    }

    public static String getShorelineModBuildNumber() {
        return ShorelineMod.MOD_BUILD_NUMBER;
    }

    public static String getShorelineModMCVersion() {
        return ShorelineMod.MOD_MC_VER;
    }
}
