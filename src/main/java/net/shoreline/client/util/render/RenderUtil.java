package net.shoreline.client.util.render;

import net.shoreline.client.util.Globals;

/**
 * @see net.minecraft.client.render.WorldRenderer
 */
public class RenderUtil implements Globals
{
    /**
     * @param softReload
     */
    public static void reloadRenders(boolean softReload)
    {
        if (softReload)
        {
            int x = (int) mc.player.getX();
            int y = (int) mc.player.getY();
            int z = (int) mc.player.getZ();
            int d = mc.options.getViewDistance().getValue() * 16;
            mc.worldRenderer.scheduleBlockRenders(
                    x - d, y - d, z - d, x + d, y + d, z + d);
        }
        else
        {
            mc.worldRenderer.reload();
        }
    }
}
