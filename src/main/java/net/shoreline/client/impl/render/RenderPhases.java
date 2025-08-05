package net.shoreline.client.impl.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.util.Util;
import net.shoreline.client.impl.module.render.ChamsModule;
import org.joml.Matrix4f;

public class RenderPhases
{
    public static final RenderPhase.Texturing GLINT =
            new RenderPhase.Texturing("shoreline_glint_texturing",
                    () -> setupGlintTexturing(ChamsModule.getInstance().getSpeed(), ChamsModule.getInstance().getScale()),
                    RenderSystem::resetTextureMatrix);

    private static void setupGlintTexturing(float speed, float scale)
    {
        long l = (long) (Util.getMeasuringTimeMs() * speed * 8.0F);
        float f = (float) (l % 110000L) / 110000.0F;
        float g = (float) (l % 30000L) / 30000.0F;
        Matrix4f matrix4f = (new Matrix4f()).translation(-f, g, 0.0F);
        matrix4f.rotateZ(0.17453292F).scale(scale);
        RenderSystem.setTextureMatrix(matrix4f);
    }
}
