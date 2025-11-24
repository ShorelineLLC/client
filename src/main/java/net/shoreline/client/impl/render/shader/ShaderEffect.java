package net.shoreline.client.impl.render.shader;

import lombok.Getter;
import net.shoreline.client.api.GenericFeature;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ShaderEffect extends GenericFeature
{
    private final Map<String, Uniform<?>> uniforms = new HashMap<>();

    public ShaderEffect(String name)
    {
        super(name);
    }

    public void addIntUniform(String name, int value)
    {
        Uniform<?> u = uniforms.get(name);
        if (u instanceof IntUniform intUniform)
        {
            intUniform.setValue(value);
            return;
        }

        addUniform(new IntUniform(name, value));
    }

    public void addFltUniform(String name, float value)
    {
        Uniform<?> u = uniforms.get(name);
        if (u instanceof FloatUniform floatUniform)
        {
            floatUniform.setValue(value);
            return;
        }

        addUniform(new FloatUniform(name, value));
    }

    public void addVec2Uniform(String name, float x, float y)
    {
        Uniform<?> u = uniforms.get(name);
        if (u instanceof Vec2Uniform vec2Uniform)
        {
            vec2Uniform.setValue(new float[] { x, y });
            return;
        }

        addUniform(new Vec2Uniform(name, x, y));
    }

    public void addVec3Uniform(String name, float x, float y, float z)
    {
        Uniform<?> u = uniforms.get(name);
        if (u instanceof Vec3Uniform vec2Uniform)
        {
            vec2Uniform.setValue(new float[] { x, y, z });
            return;
        }

        addUniform(new Vec3Uniform(name, x, y, z));
    }

    public void addVec4Uniform(String name, float r, float g, float b, float a)
    {
        Uniform<?> u = uniforms.get(name);
        if (u instanceof Vec4Uniform vec2Uniform)
        {
            vec2Uniform.setValue(new float[] { r, g, b, a });
            return;
        }

        addUniform(new Vec4Uniform(name, r, g, b, a));
    }

    private void addUniform(Uniform<?> uniform)
    {
        uniforms.put(uniform.getName(), uniform);
    }
}
