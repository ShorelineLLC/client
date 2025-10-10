package net.shoreline.client.impl.render;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class Vertex
{
    private final float x, y, z;
    private final int color;
}