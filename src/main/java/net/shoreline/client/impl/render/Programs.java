package net.shoreline.client.impl.render;

import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;

public class Programs
{
    public static final RenderPhase.ShaderProgram GLINT = new RenderPhase.ShaderProgram(new ShaderProgramKey(
            Identifier.of(ShorelineMod.MOD_ID, "core/glint"), VertexFormats.POSITION_TEXTURE_COLOR, Defines.EMPTY));

}
