package net.shoreline.client.impl.render.shader;

import net.minecraft.client.gl.ShaderProgram;

public class Vec3Uniform extends Uniform<float[]>
{
    public Vec3Uniform(String name, float x, float y, float z)
    {
        super(name, new float[] {x, y, z});
    }

    @Override
    public void applyUniform(ShaderProgram program)
    {
        program.getUniform(getName()).set(getValue()[0], getValue()[1], getValue()[2]);
    }
}
