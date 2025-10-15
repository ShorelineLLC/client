#version 150

uniform sampler2D DiffuseSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

uniform float u_Width;
uniform float u_FillAlpha;
uniform float u_OutlineAlpha;

vec3 getSobelColor(vec2 uv) {
    for (int r = 1; r <= ceil(u_Width); ++r) {
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

void main() {
    vec2 dx = vec2(oneTexel.x * u_Width, 0.0);
    vec2 dy = vec2(0.0, oneTexel.y * u_Width);

    vec4 center = texture(DiffuseSampler, texCoord);
    vec4 left = texture(DiffuseSampler, texCoord - dx);
    vec4 right = texture(DiffuseSampler, texCoord + dx);
    vec4 up = texture(DiffuseSampler, texCoord - dy);
    vec4 down = texture(DiffuseSampler, texCoord + dy);
    float e = abs(center.a - left.a) + abs(center.a - right.a) + abs(center.a - up.a) + abs(center.a - down.a);
    float edge = clamp(e, 0.0, 1.0);

    if (center.a > 0.0) {
        fragColor = vec4(center.rgb, u_FillAlpha);
        return;
    }

    if (edge > 0.0) {
        vec3 outlineRGB = getSobelColor(texCoord);
        fragColor = vec4(outlineRGB, edge * u_OutlineAlpha);
    } else {
        fragColor = vec4(0.0);
    }
}
