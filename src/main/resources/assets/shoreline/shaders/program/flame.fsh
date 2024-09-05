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

float rand(vec2 n)
{
    return fract(sin(dot(n, vec2(12.9898, 12.1414))) * 83758.5453);
}

float noise(vec2 n)
{
    vec2 d = vec2(0.0, 1.0);
    vec2 b = floor(n);
    vec2 f = mix(vec2(0.0), vec2(1.0), fract(n));
    return mix(mix(rand(b), rand(b + d.yx), f.x), mix(rand(b + d.xy), rand(b + d.yy), f.x), f.y);
}

vec4 ramp(float t)
{
    return vec4(color.rgb, 1.0) / t;
}

float fire(vec2 n)
{
    return noise(n) + noise(n * 2.1) * 0.6 + noise(n * 5.4) * 0.42;
}

void main()
{
    vec4 centerTex = texture(DiffuseSampler, texCoord);

    if (centerTex.a > 0.0)
    {
        float t = time;
        vec2 uv = gl_FragCoord.xy / resolution.xy;

        uv.x += -11.0 + t * .3;
        uv.y = abs(uv.y + 1.0);
        uv *= 3.0;

        float q = fire(uv - t * .013) / 2.0;
        vec2 r = vec2(fire(uv + q / 2.0 + t), fire(uv + q - t));
        vec4 color2 = vec4(0.0);

        float grad = (r.y + r.y) * max(0.0, uv.y / 6.0);
        color2 = ramp(grad);
        fragColor = vec4(color2 / 2.0 + color2);
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