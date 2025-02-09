#version 150

#define TWO_PI 6.28318530718f

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

//
uniform vec2 resolution;
uniform vec2 texelSize;
uniform vec4 color;
uniform int samples;
uniform int steps;
uniform float time;

uniform int fastOutline;
uniform float radius;
uniform int glow;
uniform float glowRadius;

// Computes the distance from a vec2 to the nearest texture edge
vec4 computeEdgeDistance(vec2 coords)
{
    vec4 color = vec4(0.0f);

    if (fastOutline != 1)
    {
        float closest = radius * 2.0f + 2.0f;
        for (float x = -radius; x <= radius; x++)
        {
            for (float y = -radius; y <= radius; y++)
            {
                vec4 currentColor = texture(DiffuseSampler, texCoord + vec2(texelSize.x * x, texelSize.y * y));
                if (currentColor.a > 0)
                {
                    float currentDist = sqrt(x * x + y * y);
                    if (currentDist < closest)
                    {
                        closest = currentDist;
                        color = currentColor;
                    }
                }
            }
        }

        return vec4(color.rgb, closest);
    }

    float minDist = radius * 2.0f;
    float stepSize = radius / float(steps);
    for (float r = stepSize; r < radius; r += stepSize)
    {
        for (int i = 0; i < samples; ++i)
        {
            float angle = float(i) * TWO_PI / float(samples);
            vec2 offset = vec2(cos(angle), sin(angle)) * r;
            vec2 offsetCoord = coords + offset * texelSize;

            vec4 offsetTex = texture(DiffuseSampler, offsetCoord);
            if (offsetTex.a > 0.0)
            {
                float dist = length(offset);
                minDist = min(minDist, dist);
                color = offsetTex;

                if (minDist <= radius)
                {
                    return vec4(color.rgb, minDist);
                }
            }
        }
    }

    return vec4(color.rgb, minDist);
}

void main()
{
    vec2 uv = gl_FragCoord.xy / resolution.xy;
    vec3 o = -3.1416 * vec3(0.0, 0.5, 1.0);

    float g = uv.y + time;
    vec3 col = 0.5 + 0.5 * -sin(g) * cos(g + o);

    col.g += 0.25;
    col = 0.5 + (col * 2.0 - 1.0);
    col.gb *= vec2(0.75, 0.9);
    col = 0.125 + 0.75 * col;

    vec4 centerTex = texture(DiffuseSampler, texCoord);

    if (centerTex.a > 0.0)
    {
        fragColor = vec4(col, color.a);
    }
    else
    {
        vec4 edgeDist = computeEdgeDistance(texCoord);
        if (radius > 0.0f && edgeDist.a <= radius)
        {
            if (glow != 0)
            {
                float alpha = edgeDist.a / radius;
                float transform = 1.0f - pow(alpha, glowRadius);
                fragColor = vec4(col, transform);
            }
            else
            {
                fragColor = vec4(col, 1.0f);
            }
        }
        else
        {
            fragColor = vec4(0.0f);
        }
    }
}
