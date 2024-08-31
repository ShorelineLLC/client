#version 150

#define TWO_PI 6.28318530718f

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

uniform vec2 resolution;
uniform vec2 texelSize;
uniform int samples;
uniform int steps;
uniform vec4 color;
uniform float time;

uniform float radius;
uniform int glow;
uniform float glowRadius;

// Computes the distance from a vec2 to the nearest texture edge
float computeEdgeDistance(vec2 coords)
{
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

                if (minDist <= radius)
                {
                    return minDist;
                }
            }
        }
    }

    return minDist;
}

void main()
{
    vec4 centerTex = texture(DiffuseSampler, texCoord);

    if (centerTex.a > 0.0)
    {
        vec2 uv = (2.0 * gl_FragCoord.xy - resolution.xy) / min(resolution.x, resolution.y);
        for (float i = 1.0; i < 10.0; i++)
        {
            uv.x += 0.6 / i * cos(i * 2.5 * uv.y + time);
            uv.y += 0.6 / i * cos(i * 1.5 * uv.x + time);
        }

        fragColor = vec4(color.r / abs(sin(time -uv.y - uv.x)), color.g / abs(sin(time -uv.y - uv.x)), color.b / abs(sin(time -uv.y - uv.x)), color.a);
    }
    else
    {
        float edgeDist = computeEdgeDistance(texCoord);

        if (edgeDist <= radius)
        {
            if (glow != 0)
            {
                float alpha = edgeDist / radius;
                float transform = 1.0f - pow(alpha, glowRadius);
                fragColor = vec4(color.rgb, transform);
            }
            else
            {
                fragColor = vec4(color.rgb, 1.0f);
            }
        }
        else
        {
            fragColor = vec4(0.0f);
        }
    }
}