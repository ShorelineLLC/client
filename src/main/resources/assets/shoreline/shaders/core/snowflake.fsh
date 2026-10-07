#version 150

in vec3 Position;
in vec2 UV0;
in vec4 Color;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;

out vec2 texCoord;

void main() {
    int corner = gl_VertexID % 4;

    float size = Position.z;
    float swayFreq = Color.r / 255.0;
    float phase = Color.g / 255.0;

    float sway = sin(Time * swayFreq + phase) * UV0.y;
    float fall = Time * UV0.x;

    vec2 basePos = vec2(Position.x + sway, Position.y + fall);

    vec2 cornerOffset;
    if (corner == 0) cornerOffset = vec2(0, 0);
    else if (corner == 1) cornerOffset = vec2(size, 0);
    else if (corner == 2) cornerOffset = vec2(size, size);
    else cornerOffset = vec2(0, size);

    texCoord = cornerOffset / size;

    gl_Position = ProjMat * ModelViewMat * vec4(basePos + cornerOffset, 0, 1);
}