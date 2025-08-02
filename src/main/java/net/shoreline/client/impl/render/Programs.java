package net.shoreline.client.impl.render;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class Programs
{
    public static final RenderPhase.ShaderProgram GLINT = new RenderPhase.ShaderProgram(new ShaderProgramKey(
            Identifier.of("shoreline", "core/glint"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));

}
