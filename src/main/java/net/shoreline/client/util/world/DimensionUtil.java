package net.shoreline.client.util.world;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;
import net.shoreline.client.util.Globals;

public class DimensionUtil implements Globals
{
    public static int getDimension()
    {
        RegistryKey<World> world = mc.world.getRegistryKey();
        if (world == World.OVERWORLD)
        {
            return 1;
        }
        if (world == World.NETHER)
        {
            return 0;
        }
        if (world == World.END)
        {
            return -1;
        }
        return 1;
    }
}
