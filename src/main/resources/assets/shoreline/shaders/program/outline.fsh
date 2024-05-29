#version 150

uniform sampler2D DiffuseSampler;
in vec2 texCoord;
out vec4 fragColor;

uniform vec2 texelSize;
uniform vec4 color;
uniform float radius;

void main() {
    vec4 centerCol = texture(DiffuseSampler, texCoord);
    if (centerCol.a > 0) {
        fragColor = vec4(color.x, color.y, color.z, 0.5f);
    }
    else {
        float closest = radius * 2.0f + 2.0f;
        for (float x = -radius; x <= radius; x++) {
            for (float y = -radius; y <= radius; y++) {
                vec4 currentColor = texture(DiffuseSampler, texCoord + vec2(texelSize.x * x, texelSize.y * y));
                if (currentColor.a > 0) {
                    float currentDist = sqrt(x * x + y * y);
                    if (currentDist < closest) {
                        closest = currentDist;
                    }
                }
            }
        }
        fragColor = vec4(color.x, color.y, color.z, max(0, (radius - (closest - 1)) / radius));;
    }
}