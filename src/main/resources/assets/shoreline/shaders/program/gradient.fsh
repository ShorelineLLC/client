#version 150

#define TWO_PI 6.28318530718f

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

uniform vec2 texelSize;
uniform int samples;
uniform int steps;
uniform vec4 color;
uniform vec4 color1;
uniform float factor;
uniform float time;

uniform int fastOutline;
uniform float radius;
uniform int glow;
uniform float glowRadius;

// Computes the distance from a vec2 to the nearest texture edge
float computeEdgeDistance(vec2 coords)
{
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
                    }
                }
            }
        }

        return closest;
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
        float distance = sqrt(gl_FragCoord.x * gl_FragCoord.x + gl_FragCoord.y * gl_FragCoord.y) + time;
        distance = distance / factor;
        distance = ((sin(distance) + 1.0) / 2.0);
        float j = 1.0 - distance;
        float r = color.r * distance + color1.r * j;
        float g = color.g * distance + color1.g * j;
        float b = color.b * distance + color1.b * j;
        float a = color.a * distance + color1.a * j;
        fragColor = vec4(r, g, b, a);
    }
    else
    {
        float edgeDist = computeEdgeDistance(texCoord);

        if (radius > 0.0f && edgeDist <= radius)
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