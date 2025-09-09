package net.shoreline.client.mixin.gl;

import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.render.Shaders;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ShaderLoader.Cache.class)
public abstract class MixinCache
{
    @Unique
    private final ShaderProgramDefinition lightmapDefinition = new ShaderProgramDefinition(
            Identifier.ofVanilla("core/blit_screen"),
            Identifier.of("shoreline", "core/lightmap"),
            List.of(),
            List.of(
                    new ShaderProgramDefinition.Uniform("AmbientLightFactor", "float", 1, List.of(1.0f)),
                    new ShaderProgramDefinition.Uniform("SkyFactor", "float", 1, List.of(1.0f)),
                    new ShaderProgramDefinition.Uniform("BlockFactor", "float", 1, List.of(1.0f)),
                    new ShaderProgramDefinition.Uniform("UseBrightLightmap", "int", 1, List.of(0.0f)),
                    new ShaderProgramDefinition.Uniform("SkyLightColor", "float", 3, List.of(1.0f, 1.0f, 1.0f)),
                    new ShaderProgramDefinition.Uniform("NightVisionFactor", "float", 1, List.of(0.0f)),
                    new ShaderProgramDefinition.Uniform("DarknessScale", "float", 1, List.of(0.0f)),
                    new ShaderProgramDefinition.Uniform("DarkenWorldFactor", "float", 1, List.of(0.0f)),
                    new ShaderProgramDefinition.Uniform("BrightnessFactor", "float", 1, List.of(1.0f)),
                    new ShaderProgramDefinition.Uniform("CustomLightColor", "float", 3, List.of(1.0f, 1.0f, 1.0f)),
                    new ShaderProgramDefinition.Uniform("CustomLightStrength", "float", 1, List.of(1.0f))
            ),

            Defines.EMPTY
    );

    @Shadow
    protected abstract CompiledShader loadShader(Identifier id, CompiledShader.Type type, Defines defines) throws ShaderLoader.LoadException;

    @Inject(method = "loadProgram", at = @At(value = "HEAD"), cancellable = true)
    private void hookLoadProgram(ShaderProgramKey key, CallbackInfoReturnable<ShaderProgram> cir)
    {
        if (key == Shaders.LIGHTMAP)
        {
            cir.cancel();
            try
            {
                Defines defines = lightmapDefinition.defines().withMerged(key.defines());
                CompiledShader compiledShader = loadShader(lightmapDefinition.vertex(), CompiledShader.Type.VERTEX, defines);
                CompiledShader compiledShader2 = loadShader(lightmapDefinition.fragment(), CompiledShader.Type.FRAGMENT, defines);
                cir.setReturnValue(ShaderLoader.createProgram(key, lightmapDefinition, compiledShader, compiledShader2));

            } catch (ShaderLoader.LoadException e)
            {
                e.printStackTrace();
            }
        }
    }
}
