#version 150

#define TAU 6.28318530718

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float u_ShaderTime;
uniform vec2 u_Resolution;

uniform float u_Width;

uniform int u_GlowInside;
uniform int u_GlowQuality;
uniform float u_GlowMultiplier;

uniform int u_FillMode;
uniform float u_FillAlpha;

uniform vec4 u_GradientColor;
uniform float u_GradientFactor;

uniform float u_FlowSpeed;
uniform float u_FlowFactor;

uniform float u_LiquidIntensity;
uniform float u_LiquidFactor;

uniform float u_OutlineAlpha;

vec3 hsv2rgb(vec3 c) {
    vec4 K = vec4(1.0, 2.0 / 3.0, 1.0 / 3.0, 3.0);
    vec3 p = abs(fract(c.xxx + K.xyz) * 6.0 - K.www);
    return c.z * mix(K.xxx, clamp(p - K.xxx, 0.0, 1.0), c.y);
}

vec4 getFill(vec3 centerColor, float alpha)
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
        float a = alpha * distance + u_GradientColor.a * j;
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

        return vec4(centerColor.r / abs(sin(time - uv.y - uv.x)), centerColor.g / abs(sin(time - uv.y - uv.x)), centerColor.b / abs(sin(time - uv.y - uv.x)), alpha);
    }

    if (u_FillMode == 3)
    {
        float time = u_ShaderTime / 1000.0;
        vec2 uv = gl_FragCoord.xy / u_Resolution.xy;
        vec2 p = mod(uv * TAU, TAU) - 250.0;

        vec2 i = vec2(p);
        float c = 1.0;
        float inten = u_LiquidIntensity / 1000.0;

        for (int n = 0; n < floor(u_LiquidFactor); n++)
        {
        	float t = time * (1.0 - (3.5 / float(n + 1)));
        	i = p + vec2(cos(t - i.x) + sin(t + i.y), sin(t - i.y) + cos(t + i.x));
        	c += 1.0 / length(vec2(p.x / (sin(i.x + t) / inten), p.y / (cos(i.y + t) / inten)));
        }

        c /= floor(u_LiquidFactor);
        c = 1.17 - pow(c, 1.4);
        vec3 color = vec3(pow(abs(c), 8.0));
        color = clamp(color + centerColor, 0.0, 1.0);

        return vec4(color, alpha);
    }

    if (u_FillMode == 4)
    {
        float zoom = 1; // < 1.0 = zoom out, > 1.0 = zoom in
        vec2 uv = (gl_FragCoord.xy / u_Resolution.xy - 0.5) * zoom + 0.5;
        float theta = uv.x * 3.14159;
        float phi = uv.y * 3.14159 * 0.5;

        vec3 dir = vec3(
                cos(phi) * cos(theta),
                sin(phi),
                cos(phi) * sin(theta)
            );

        float time = u_ShaderTime / 750.0;
        float rot = time * 0.2;
        mat2 rotMat = mat2(cos(rot), -sin(rot), sin(rot), cos(rot));
        dir.xz = rotMat * dir.xz;

        float dist = length(dir.xy);
        float angle = atan(dir.y, dir.x);
        float spiral = sin(dist * 10.0 - angle * 3.0 - time * 2.0);

        float hue = fract(dist * 2.0 - time * 0.3 + angle / 6.28318);
        vec3 rainbowColor = hsv2rgb(vec3(hue, 0.8, 1.0));

        float rings = sin(dist * 20.0 - time * 3.0);
        rings = pow(max(0.0, rings), 3.0);

        vec3 finalColor = rainbowColor * (spiral * 0.3 + 0.7);
        finalColor += vec3(1.0) * rings * 0.5;

        float glow = exp(-dist * 3.0);
        finalColor += vec3(1.0, 0.9, 1.0) * glow * 0.5;
        return vec4(finalColor, alpha);
    }

    return vec4(centerColor, alpha);
}

vec3 getSobelColor(vec2 uv)
{
    for (int r = 1; r <= u_GlowQuality * int(u_Width); r += u_GlowQuality)
    {
        float rf = float(r);
        vec2 dx = vec2(oneTexel.x * rf, 0.0);
        vec2 dy = vec2(0.0, oneTexel.y * rf);

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
        s = texture(DiffuseSampler, uv + vec2(-od.x, od.y));
        if (s.a > 0.0) return s.rgb;
        s = texture(DiffuseSampler, uv - od);
        if (s.a > 0.0) return s.rgb;
    }

    return vec3(0.0);
}

float blur(vec4 center, bool outline)
{
    if (u_Width == 0)
    {
        return 0.0;
    }

    int w = u_GlowQuality * int(u_Width);
    float blurred = 0.0;

    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2( w, 0)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, 0)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2( 0, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2( 0, -w)).a);

    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(w, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(w, -w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, w)).a);
    blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(-w, -w)).a);

    if (int(u_Width) > 2 && blurred == 0.0)
    {
        return 0.0;
    }

    for (int x = -w; x <= w; x += u_GlowQuality)
    {
        for (int y = -w; y <= w; y += u_GlowQuality)
        {
            if (x == 0 && y == 0) continue;

            if ((abs(x) == w && abs(y) == w) ||
                (abs(x) == w && y == 0) ||
                (abs(y) == w && x == 0))
            {
                continue;
            }

            blurred += sign(texture(DiffuseSampler, texCoord + oneTexel * vec2(float(x), float(y))).a);
        }
    }

    return clamp(blurred / max(float(((int(u_Width) * int(u_Width)) + int(u_Width)) * 4), 1.0), 0.0, 1.0) * u_GlowMultiplier;
}

void main()
{
    vec4 center = texture(DiffuseSampler, texCoord);

    if (center.a > 0.0)
    {
        vec4 fill = getFill(center.rgb, u_FillAlpha);
        if (u_GlowInside == 1)
        {
            vec4 outline = vec4(center.rgb, u_OutlineAlpha);
            fragColor = mix(fill, outline, u_GlowMultiplier - blur(fill, false));
        } else
        {
            fragColor = fill;
        }

        return;
    }

    float glow = blur(center, true);
    if (glow == 0.0)
    {
        discard;
    }

    bool edge = false;
    for (int x = -1; x <= 1 && !edge; ++x)
    {
        for (int y = -1; y <= 1 && !edge; ++y)
        {
            if (x == 0 && y == 0) continue;

            if (texture(DiffuseSampler, texCoord + vec2(float(x), float(y)) * oneTexel).a > 0.0)
            {
                edge = true;
            }
        }
    }

    vec3 outlineRGB;
    if (u_FillMode == 4)
    {
        outlineRGB = vec3(getFill(center.rgb, u_OutlineAlpha));
    }
    else
    {
       outlineRGB = getSobelColor(texCoord);
    }

    if (outlineRGB.r + outlineRGB.g + outlineRGB.b == 0.0)
    {
        int w = max(1, u_GlowQuality * int(u_Width));
        vec2 dx = vec2(oneTexel.x * float(w), 0.0);
        vec2 dy = vec2(0.0, oneTexel.y * float(w));

        vec4 s = texture(DiffuseSampler, texCoord + dx);
        if (s.a > 0.0) outlineRGB = s.rgb;
        s = texture(DiffuseSampler, texCoord - dx);
        if (s.a > 0.0) outlineRGB = s.rgb;
        s = texture(DiffuseSampler, texCoord + dy);
        if (s.a > 0.0) outlineRGB = s.rgb;
        s = texture(DiffuseSampler, texCoord - dy);
        if (s.a > 0.0) outlineRGB = s.rgb;
    }

    if (edge)
    {
        fragColor = vec4(outlineRGB, u_OutlineAlpha);
    }
    else
    {
        fragColor = vec4(outlineRGB, glow * u_OutlineAlpha);
    }
}
