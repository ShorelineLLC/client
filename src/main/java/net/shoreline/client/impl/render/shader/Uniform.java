package net.shoreline.client.impl.render.shader;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.gl.ShaderProgram;

@Getter
@Setter
public abstract class Uniform<T>
{
    private final String name;
    private T value;

    protected Uniform(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    public abstract void applyUniform(ShaderProgram program);
}
