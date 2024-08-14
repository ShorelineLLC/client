#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform mat4 ProjMat;
uniform vec2 InSize;
uniform vec2 OutSize;

//
uniform vec2 texelSize;
uniform sampler2D imageTexture;
uniform vec4 color;

uniform float radius;
uniform float glow;

void main()
{
    vec4 centerCol = texture(DiffuseSampler, texCoord);
    if (centerCol.a > 0.0)
    {
        fragColor = texture(imageTexture, texCoord) * vec4(1.0, 1.0, 1.0, color.a);
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