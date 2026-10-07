package net.shoreline.client.impl.render;

import com.google.common.collect.Lists;
import net.minecraft.client.gl.Defines;
import net.minecraft.client.gl.ShaderProgramDefinition;
import net.minecraft.client.gl.ShaderProgramKey;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;
import net.shoreline.client.ShorelineMod;

import java.util.Arrays;
import java.util.List;

public class Programs
{
    public static final RenderPhase.ShaderProgram GLINT_PROGRAM = new RenderPhase.ShaderProgram(new ShaderProgramKey(
            Identifier.of(ShorelineMod.MOD_ID, "core/glint"),
            VertexFormats.POSITION_TEXTURE_COLOR,
            Defines.EMPTY));

    public static final ShaderProgramKey LIGHTMAP_KEY = new ShaderProgramKey(
            Identifier.of(ShorelineMod.MOD_ID, "lightmap"),
            VertexFormats.BLIT_SCREEN,
            Defines.EMPTY);

    public static final ShaderProgramDefinition LIGHTMAP_PROGRAM = new ShaderProgramDefinition(
            Identifier.ofVanilla("core/blit_screen"),
            Identifier.of(ShorelineMod.MOD_ID, "core/lightmap"),
            Lists.newArrayList(),
            List.of(
                    uniform("AmbientLightFactor", "float", 1, 1.0f),
                    uniform("SkyFactor", "float", 1, 1.0f),
                    uniform("BlockFactor", "float", 1, 1.0f),
                    uniform("UseBrightLightmap", "int", 1, 0.0f),
                    uniform("SkyLightColor", "float", 3, 1.0f, 1.0f, 1.0f),
                    uniform("NightVisionFactor", "float", 1, 0.0f),
                    uniform("DarknessScale", "float", 1, 0.0f),
                    uniform("DarkenWorldFactor", "float", 1, 0.0f),
                    uniform("BrightnessFactor", "float", 1, 1.0f),
                    uniform("CustomLightColor", "float", 3, 1.0f, 1.0f, 1.0f),
                    uniform("CustomLightStrength", "float", 1, 1.0f)
            ),
            Defines.EMPTY
    );

    private static ShaderProgramDefinition.Uniform uniform(String name, String type, int count, Float... values)
    {
        return new ShaderProgramDefinition.Uniform(name, type, count, Arrays.asList(values));
    }
}
