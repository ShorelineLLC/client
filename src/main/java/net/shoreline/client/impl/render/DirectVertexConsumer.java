package net.shoreline.client.impl.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.mixin.accessor.AccessorBufferBuilder;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class DirectVertexConsumer implements VertexConsumer
{
    private final BufferBuilder original;
    private final VertexFormat format;
    private ByteBuffer into;

    public DirectVertexConsumer(BufferBuilder original, boolean skipFirstAlloc)
    {
        this.original = original;
        format = ((AccessorBufferBuilder) original).getVertexFormat();
        long ptr;
        if (!skipFirstAlloc) {
            ptr = ((AccessorBufferBuilder) original).beginNewVertex();
        } else
        {
            ptr = ((AccessorBufferBuilder) original).getVertexPointer();
        }
        into = MemoryUtil.memByteBuffer(ptr, format.getVertexSize());
        into.order(ByteOrder.nativeOrder());
    }

    private void checkEnd()
    {
        if (!into.hasRemaining())
        {
            newVert();
        }
    }

    private void newVert()
    {
        into = MemoryUtil.memByteBuffer(((AccessorBufferBuilder) original).beginNewVertex(), format.getVertexSize());
        into.order(ByteOrder.nativeOrder());
    }

    public ByteBuffer getCurrentVertexData()
    {
        checkEnd();
        return into;
    }

    @Override
    public VertexConsumer vertex(float x, float y, float z)
    {
        checkEnd();
        into.putFloat(x);
        into.putFloat(y);
        into.putFloat(z);
        return this;
    }

    @Override
    public VertexConsumer color(int red, int green, int blue, int alpha)
    {
        return color(ColorHelper.getArgb(alpha, red, green, blue));
    }

    @Override
    public VertexConsumer color(int argb)
    {
        checkEnd();
        into.putInt(argb);
        return this;
    }

    @Override
    public VertexConsumer texture(float u, float v)
    {
        checkEnd();
        into.putFloat(u);
        into.putFloat(v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int u, int v)
    {
        checkEnd();
        into.putShort((short) u);
        into.putShort((short) v);
        return this;
    }

    @Override
    public VertexConsumer overlay(int uv)
    {
        checkEnd();
        into.putInt(uv);
        return this;
    }

    @Override
    public VertexConsumer light(int u, int v)
    {
        return overlay(u, v);
    }

    @Override
    public VertexConsumer light(int uv)
    {
        return overlay(uv);
    }

    private static byte floatToByte(float f)
    {
        return (byte) ((int) (MathHelper.clamp(f, -1.0f, 1.0f) * 127.0f) & 0xff);
    }

    @Override
    public VertexConsumer normal(float x, float y, float z)
    {
        checkEnd();
        into.put(floatToByte(x));
        into.put(floatToByte(y));
        into.put(floatToByte(z));
        return this;
    }
}