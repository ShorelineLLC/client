package net.shoreline.client.impl.render.shader;

import net.minecraft.client.gl.ShaderProgram;

public class Vec2Uniform extends Uniform<float[]>
{
    public Vec2Uniform(String name, float x, float y)
    {
        super(name, new float[] {x, y});
    }

    @Override
    public void applyUniform(ShaderProgram program)
    {
        program.getUniform(getName()).set(getValue()[0], getValue()[1]);
    }
}
