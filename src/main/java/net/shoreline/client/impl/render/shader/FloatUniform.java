package net.shoreline.client.impl.render.shader;

import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.gl.ShaderProgram;

public class FloatUniform extends Uniform<Float>
{
    public FloatUniform(String name, float value)
    {
        super(name, value);
    }

    @Override
    public void applyUniform(ShaderProgram program)
    {
        GlUniform uniform = program.getUniform(getName());
        if (uniform != null)
        {
            uniform.set(getValue());
        }
    }
}
