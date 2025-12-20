package net.shoreline.client.mixin.gl;

import net.minecraft.client.gl.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.impl.render.Programs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShaderLoader.Cache.class)
public abstract class MixinCache
{
    @Shadow
    protected abstract CompiledShader loadShader(Identifier id, CompiledShader.Type type, Defines defines) throws ShaderLoader.LoadException;

    @Inject(method = "loadProgram", at = @At(value = "HEAD"), cancellable = true)
    private void hookLoadProgram(ShaderProgramKey key, CallbackInfoReturnable<ShaderProgram> cir)
    {
        if (key == Programs.LIGHTMAP_KEY)
        {
            cir.cancel();
            ShaderProgramDefinition lightmapDefinition = Programs.LIGHTMAP_PROGRAM;
            Defines defines = lightmapDefinition.defines().withMerged(key.defines());

            try
            {
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
