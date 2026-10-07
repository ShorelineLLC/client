package net.shoreline.client.gui.titlescreen.particle.snow;

import lombok.Getter;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.Shoreline;
import net.shoreline.client.gui.titlescreen.particle.Particle;

import java.util.Random;

@Getter
public class SnowParticle extends Particle
{
    private static final Random RANDOM = new Random();
    private final Identifier SNOWFLAKE = Identifier.of("shoreline", "textures/snowflake.png");

    private boolean collisionCheck;
    private final float speed;
    private final float swayAmplitude;
    private final float swayFrequency;
    private final float size;
    private float time;

    public SnowParticle(int screenWidth, int screenHeight)
    {
        super(screenWidth, screenHeight, RANDOM.nextFloat() * screenWidth, RANDOM.nextFloat() * screenHeight, false, 0, 0);
        float r = RANDOM.nextFloat();
        r = Math.min(r * r, 0.85f);
        size = 1.5f + r * 2.5f;

        float baseSpeed = (350f + RANDOM.nextFloat() * 70f) / (size / 2f);
        float randomness = Math.max(0.3f, 1f - (size - 1.5f) / 3f);

        speed = baseSpeed * (1f + (RANDOM.nextFloat() * 2f - 1f) * 0.3f * randomness);
        swayAmplitude = 0.3f + RANDOM.nextFloat() * 0.7f;
        swayFrequency = 1f + RANDOM.nextFloat() * 2f;
        time = RANDOM.nextFloat() * (float) Math.PI * 2f;
        resetWindup();
    }

    @Override
    public void update(float delta)
    {
        if (isWindingUp(delta))
        {
            if (!isOutOfBounds())
            {
                reset();
            }

            return;
        }

        time += delta;
        y += speed * delta;
        x += (float) (speed * 0.33f * delta + Math.sin(time * swayFrequency) * swayAmplitude);
    }

    @Override
    public void render(VertexConsumer consumer)
    {
        consumer.vertex(x, y, 0)
                .texture(0, 0)
                .color(255, 255, 255, 255);

        consumer.vertex(x, y + size, 0)
                .texture(0, 1)
                .color(255, 255, 255, 255);

        consumer.vertex(x + size, y + size, 0)
                .texture(1, 1)
                .color(255, 255, 255, 255);

        consumer.vertex(x + size, y, 0)
                .texture(1, 0)
                .color(255, 255, 255, 255);
    }

    @Override
    public boolean isOutOfBounds()
    {
        return y > screenHeight || x < -size || x > screenWidth;
    }

    @Override
    public void reset()
    {
        collisionCheck = true;
        float spawnSide = RANDOM.nextFloat();
        float rnd       = RANDOM.nextFloat();
        if (spawnSide < 0.5f)
        {
            y = -size;
            x = rnd * screenWidth;

            if (rnd > 0.75f || rnd < 0.05f)
            {
                collisionCheck = false;
            }
        }
        else if (spawnSide < 0.75f)
        {
            x = -size;
            y = rnd * screenHeight;
            collisionCheck = false;
        }
        else
        {
            x = screenWidth + size;
            y = rnd * screenHeight;
            collisionCheck = false;
        }
    }
}
