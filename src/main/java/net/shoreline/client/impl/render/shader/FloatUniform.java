package net.shoreline.client.impl.render.shader;

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
        program.getUniform(getName()).set(getValue());
    }
}
