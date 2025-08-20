package net.shoreline.client.impl.render.shader;

import lombok.Getter;
import net.shoreline.client.api.GenericFeature;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ShaderEffect extends GenericFeature
{
    private final List<Uniform<?>> uniforms = new ArrayList<>();

    public ShaderEffect(String name)
    {
        super(name);
    }

    public void addIntUniform(String name, int value)
    {
        uniforms.add(new IntUniform(name, value));
    }

    public void addFltUniform(String name, float value)
    {
        uniforms.add(new FloatUniform(name, value));
    }

    public void addVec2Uniform(String name, float x, float y)
    {
        uniforms.add(new Vec2Uniform(name, x, y));
    }

    public void addVec3Uniform(String name, float x, float y, float z)
    {
        uniforms.add(new Vec3Uniform(name, x, y, z));
    }

    public void addVec4Uniform(String name, float r, float g, float b, float a)
    {
        uniforms.add(new Vec4Uniform(name, r, g, b, a));
    }
}
