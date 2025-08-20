package net.shoreline.client.impl.render.shader;

import net.minecraft.client.gl.ShaderProgram;

public class Vec4Uniform extends Uniform<float[]>
{
    public Vec4Uniform(String name, float r, float g, float b, float a)
    {
        super(name, new float[] {r, g, b, a});
    }

    @Override
    public void applyUniform(ShaderProgram program)
    {
        program.getUniform(getName()).set(getValue()[0], getValue()[1], getValue()[2], getValue()[3]);
    }
}
