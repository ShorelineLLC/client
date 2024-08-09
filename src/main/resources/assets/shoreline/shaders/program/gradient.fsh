#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

uniform vec2 texelSize;
uniform vec4 color;
uniform vec4 color1;
uniform float factor;
uniform float time;

uniform float radius;
uniform float glow;

void main()
{
    vec4 centerCol = texture(DiffuseSampler, texCoord);
    if (centerCol.a > 0.0)
    {
        float distance = sqrt(gl_FragCoord.x * gl_FragCoord.x + gl_FragCoord.y * gl_FragCoord.y) + time;
        distance = distance / factor;
        distance = ((sin(distance) + 1.0) / 2.0);
        float j = 1.0 - distance;
        float r = color.r * distance + color1.r * j;
        float g = color.g * distance + color1.g * j;
        float b = color.b * distance + color1.b * j;
        fragColor = vec4(r, g, b, color.a);
    }
    else
    {
        float dist = radius * radius * 4.0;
        for (float x = -radius; x <= radius; x++)
        {
            for (float y = -radius; y <= radius; y++)
            {
                vec4 offset = texture(DiffuseSampler, texCoord + vec2(texelSize.x * x, texelSize.y * y));
                if (offset.a > 0.0)
                {
                    float ndist = x * x + y * y - 1.0;
                    dist = min(ndist, dist);
                }
            }
        }
        float minDist = radius * radius;
        if (dist > minDist)
        {
            fragColor = vec4(color.x, color.y, color.z, 0.0);
        }
        else
        {
            if (radius <= 0.0)
            {
                fragColor = vec4(color.x, color.y, color.z, 0.0);
            }
            else if (glow < 0.0)
            {
                fragColor = vec4(color.x, color.y, color.z, 1.0);
            }
            else
            {
                fragColor = vec4(color.x, color.y, color.z, min((1.0 - (dist / minDist)) * glow, 1.0));
            }
        }
    }
}