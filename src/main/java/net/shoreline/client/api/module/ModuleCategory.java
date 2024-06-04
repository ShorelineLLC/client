package net.shoreline.client.api.module;

/**
 * @author linus
 * @see Module
 * @since 1.0
 */
public enum ModuleCategory
{
    /**
     * Modules used for combat (Ex: Aura, AutoCrystal, Surround, etc.)
     */
    COMBAT,

    /**
     * Modules that exploit certain anticheats to allow for "non-vanilla"
     * behavior (Ex: AntiHunger, PacketFlight, Reach, etc.)
     */
    EXPLOITS,

    /**
     * Modules that don't fit into the other categories
     */
    MISCELLANEOUS,

    /**
     * Modules that allow the player to move in unnatural ways (Ex: Flight,
     * Speed, FastFall, etc.)
     */
    MOVEMENT,

    /**
     * Modules that are visual modifications (Ex: Esp, Nametags, HoleEsp, etc.)
     */
    RENDER,

    /**
     * Modules that are modifications to world (Ex: Wallhack, Speedmine,
     * FastPlace, etc.)
     */
    WORLD,

    /**
     * Modules associated with client processes
     */
    CLIENT
}
