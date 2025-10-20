#version 150

#define TAU 6.28318530718

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float u_ShaderTime;
uniform vec2 u_Resolution;

uniform float u_Width;

uniform int u_FillMode;
uniform float u_FillAlpha;

uniform vec4 u_GradientColor;
uniform float u_GradientFactor;

uniform float u_FlowSpeed;
uniform float u_FlowFactor;

uniform float u_LiquidIntensity;
uniform float u_LiquidFactor;

uniform float u_OutlineAlpha;

vec4 getFill(vec3 centerColor)
{
    if (u_FillMode == 1)
    {
        float time = u_ShaderTime / 5.0;
        float distance = sqrt(gl_FragCoord.x * gl_FragCoord.x + gl_FragCoord.y * gl_FragCoord.y) + time;
        distance = distance / u_GradientFactor;
        distance = ((sin(distance) + 1.0) / 2.0);
        float j = 1.0 - distance;
        float r = centerColor.r * distance + u_GradientColor.r * j;
        float g = centerColor.g * distance + u_GradientColor.g * j;
        float b = centerColor.b * distance + u_GradientColor.b * j;
        float a = u_FillAlpha * distance + u_GradientColor.a * j;
        return vec4(r, g, b, a);
    }

    if (u_FillMode == 2)
    {
        float time = u_ShaderTime / 500.0;
        vec2 uv = (2.0 * gl_FragCoord.xy - u_Resolution.xy) / min(u_Resolution.x, u_Resolution.y);
        for (float i = 1.0; i < u_FlowSpeed; i++)
        {
            uv.x += u_FlowFactor / i * cos(i * 2.5 * uv.y + time);
            uv.y += u_FlowFactor / i * cos(i * 1.5 * uv.x + time);
        }

        return vec4(centerColor.r / abs(sin(time - uv.y - uv.x)), centerColor.g / abs(sin(time - uv.y - uv.x)), centerColor.b / abs(sin(time - uv.y - uv.x)), u_FillAlpha);
    }

    if (u_FillMode == 3)
    {
        float time = u_ShaderTime / 1000.0;
        vec2 uv = gl_FragCoord.xy / u_Resolution.xy;
        vec2 p = mod(uv * TAU, TAU) - 250.0;

        vec2 i = vec2(p);
        float c = 1.0;
        float inten = u_LiquidIntensity / 1000.0;

        for (int n = 0; n < u_LiquidFactor; n++)
        {
            float t = time * (1.0 - (3.5 / float(n + 1)));
            i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
            c += 1.0 / length(vec2(p.x / (sin(i.x + t) / inten), p.y / (cos(i.y + t) / inten)));
        }

        c /= u_LiquidFactor;
        c = 1.17 - pow(c, 1.4);
        vec3 color = vec3(pow(abs(c), 8.0));
        color = clamp(color + centerColor, 0.0, 1.0);

        return vec4(color, u_FillAlpha);
    }

    return vec4(centerColor, u_FillAlpha);
}

vec3 getSobelColor(vec2 uv)
{
    for (int r = 1; r <= ceil(u_Width); ++r)
     {
        vec2 dx = vec2(oneTexel.x * float(r), 0.0);
        vec2 dy = vec2(0.0, oneTexel.y * float(r));

        vec4 s = texture(DiffuseSampler, uv - dx);
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv + dx);
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv - dy);
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv + dy);
        if (s.a > 0.0) return s.rgb;

        vec2 od = vec2(dx.x, dy.y);
        s = texture(DiffuseSampler, uv + od);
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv + vec2(od.x, -od.y));
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv + vec2(-od.x,  od.y));
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv - od);
        if (s.a > 0.0) return s.rgb;
    }

    return vec3(0.0);
}

void main()
{
    vec2 dx = vec2(oneTexel.x * u_Width, 0.0);
    vec2 dy = vec2(0.0, oneTexel.y * u_Width);

    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 left = texture(DiffuseSampler, texCoord - dx);
    vec4 right = texture(DiffuseSampler, texCoord + dx);
    vec4 up = texture(DiffuseSampler, texCoord - dy);
    vec4 down = texture(DiffuseSampler, texCoord + dy);
    float e = abs(center.a - left.a) + abs(center.a - right.a) + abs(center.a - up.a) + abs(center.a - down.a);
    float edge = clamp(e, 0.0, 1.0);

    if (center.a > 0.0)
    {
        fragColor = getFill(center.rgb);
        return;
    }

    if (edge > 0.0)
     {
        vec3 outlineRGB = getSobelColor(texCoord);
        fragColor = vec4(outlineRGB, edge * u_OutlineAlpha);
    } else
    {
        fragColor = vec4(0.0);
    }
}
