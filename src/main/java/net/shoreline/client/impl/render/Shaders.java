package net.shoreline.client.impl.render;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class Shaders
{
    public static final ShaderProgramKey LIGHTMAP = new ShaderProgramKey(
            Identifier.of("shoreline", "lightmap"), VertexFormats.BLIT_SCREEN, Defines.EMPTY);
}
