#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform int u_Width;
uniform float u_GlowMultiplier;
uniform int u_GlowQuality;
uniform vec4 u_FillColor;
uniform vec4 u_OutlineColor;

float blur(vec4 center, bool outline)
{
    if (u_Width == 0.0) return 0.0;

    int w = u_GlowQuality * u_Width;
    float blurred = 0.0;

    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(w, 0)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, 0)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(0, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(0, -w)).a);

    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(w, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(w, -w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, -w)).a);

    if (u_Width > 2 && blurred == 0.0)
    {
        return 0.0;
    }

    for (int x = -w; x <= w; x += u_GlowQuality)
    {
        for (int y = -w; y <= w; y += u_GlowQuality)
        {
            if (x == 0 && y == 0)
            {
                continue;
            }

            if (sign(x) == w && sign(y) == w
                || sign(x) == w && y == 0
                || sign(y) == 0 && x == 0)
            {
                continue;
            }

            blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(x, y)).a);
        }
    }

    return clamp(blurred / (((u_Width * u_Width) + u_Width) * 4), 0.0, 1.0) * u_GlowMultiplier;
}

void main()
{
    vec4 current = texture(DiffuseSampler, texCoord);
    if (current.a != 0)
    {
        current = u_FillColor;
        if (u_Width != 0)
        {
            current = mix(current, u_OutlineColor, u_GlowMultiplier - blur(current, false));
        }
    }
    else
    {
        float alpha = blur(current, true);
        if (alpha == 0.0)
        {
            discard;
        }

        for (int x = -1; x <= 1; x++)
        {
            for (int y = -1; y <= 1; y++)
            {
                if (x == 0 && y == 0)
                {
                    continue;
                }

                if (texture(DiffuseSampler, texCoord + vec2(x, y) * oneTexel).a > 0.0)
                {
                    current = u_OutlineColor;
                    current.a = 1.0;
                }
            }
        }

        if (current.a == 0.0)
        {
            current = u_OutlineColor;
            current.a = alpha;
        }
    }

    fragColor = current;
}
