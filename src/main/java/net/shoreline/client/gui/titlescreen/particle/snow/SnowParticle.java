package net.shoreline.client.gui.titlescreen.particle.snow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.gui.titlescreen.particle.Particle;
import net.shoreline.client.impl.imixin.IDrawContext;

import java.awt.*;
import java.util.Random;

public class SnowParticle extends Particle
{
    private static final Random RANDOM = new Random();
    private final Identifier SNOWFLAKE = Identifier.of("shoreline", "textures/snowflake.png");

    private final float size;
    private final float speed;
    private final float swayAmplitude;
    private final float swayFrequency;
    private final int color;
    private float time;

    private static final int[] COLORS = new int[15];

    public SnowParticle(int screenWidth, int screenHeight)
    {
        super(screenWidth, screenHeight, RANDOM.nextFloat() * screenWidth, RANDOM.nextFloat() * screenHeight, false, 0, 0);
        float r = RANDOM.nextFloat();
        r = Math.min(r * r, 0.85f);
        float size = 2.0f + r * 2.5f;
        float baseSpeed = (350f + RANDOM.nextFloat() * 70f) / (size / 2f);
        float randomnessStrength = 1.0f - (size - 1.5f) / 3.0f;
        randomnessStrength = Math.max(0.3f, randomnessStrength);
        float speedVariance = 1.0f + (RANDOM.nextFloat() * 2f - 1f) * 0.3f * randomnessStrength;

        this.size = size;
        this.speed = baseSpeed * speedVariance;
        this.swayAmplitude = 0.3f + (RANDOM.nextFloat() * 0.7f);
        this.swayFrequency = 1.0f + RANDOM.nextFloat() * 2.0f;
        this.color = COLORS[RANDOM.nextInt(COLORS.length)];
        this.time = RANDOM.nextFloat() * (float) Math.PI * 2;
        resetWindup();
    }

    @Override
    public void update(float delta)
    {
        if (isWindingUp(delta))
        {
            return;
        }

        time += delta;
        y += speed * delta;
        x += (float) (speed * 0.33f * delta + Math.sin(time * swayFrequency) * swayAmplitude);
    }

    @Override
    public void render(DrawContext context, float delta)
    {
        if (isWindingUp())
        {
            return;
        }

        drawTexture(context, x, y, x + size, y + size, SNOWFLAKE, color);
    }

    @Override
    public boolean isOutOfBounds()
    {
        return y > screenHeight || x < -size || x > screenWidth;
    }

    @Override
    public void reset()
    {
        float spawnSide = RANDOM.nextFloat();
        if (spawnSide < 0.5f)
        {
            y = -size;
            x = RANDOM.nextFloat() * screenWidth;
        }
        else if (spawnSide < 0.75f)
        {
            x = -size;
            y = RANDOM.nextFloat() * screenHeight;
        }
        else
        {
            x = screenWidth + size;
            y = RANDOM.nextFloat() * screenHeight;
        }
    }

    private void drawTexture(DrawContext context, float x, float y, float x2, float y2, Identifier identifier, int color)
    {
        VertexConsumerProvider provider = ((IDrawContext) context).getVertexConsumerProvider();
        VertexConsumer consumer = provider.getBuffer(RenderLayer.getGuiTextured(identifier));
        consumer.vertex(x, y, 0).color(color).texture(0, 0);
        consumer.vertex(x, y2, 0).color(color).texture(0, 1);
        consumer.vertex(x2, y2, 0).color(color).texture(1, 1);
        consumer.vertex(x2, y, 0).color(color).texture(1, 0);
    }

    static
    {
        for (int i = 0; i < 15; i++)
        {
            int alpha = 180 + (i * 5);
            COLORS[i] = new Color(250, 250, 255, alpha).getRGB();
        }
    }
}
