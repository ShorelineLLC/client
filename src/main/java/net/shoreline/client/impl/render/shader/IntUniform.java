package net.shoreline.client.impl.render.shader;

import net.minecraft.client.gl.ShaderProgram;

public class IntUniform extends Uniform<Integer>
{
    public IntUniform(String name, int value)
    {
        super(name, value);
    }

    @Override
    public void applyUniform(ShaderProgram program)
    {
        program.getUniform(getName()).set(getValue());
    }
}
