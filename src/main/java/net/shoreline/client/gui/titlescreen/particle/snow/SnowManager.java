package net.shoreline.client.gui.titlescreen.particle.snow;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.gui.titlescreen.particle.ParticleManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SnowManager extends ParticleManager<SnowParticle>
{
    private final Identifier SHORELINE = Identifier.of("shoreline", "textures/shoreline.png");
    private List<Vec2f> points;

    public SnowManager(int count)
    {
        super(count);
        points = new ArrayList<>();
        if (mc.getWindow() == null)
        {
            return;
        }

        loadImage();
    }

    public void loadImage()
    {
        Window window = mc.getWindow();
        float targetWidth = window.getScaledWidth();
        float targetHeight = window.getScaledHeight();

        try
        {
            NativeImage image = NativeImage.read(mc.getResourceManager().getResourceOrThrow(SHORELINE).getInputStream());
            points = sampleImage(image, targetWidth, targetHeight, 4.0f, 0.67f);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    protected SnowParticle createParticle(int screenWidth, int screenHeight)
    {
        return new SnowParticle(screenWidth, screenHeight);
    }

    @Override
    public void reset()
    {
        super.reset();
        runAsync(() ->
        {
            loadImage();
            return null;
        });
    }

    @Override
    public void update()
    {
        runAsync(() ->
        {
            if (particles.isEmpty())
            {
                reset();
                return null;
            }

            if (points.isEmpty())
            {
                loadImage();
                return null;
            }

            long currentTime = System.currentTimeMillis();
            float delta = Math.min((currentTime - lastUpdate) / 1000.0f, 1.0f);
            lastUpdate = currentTime;

            int size = particles.size();
            if (size == 0)
            {
                return null;
            }

            for (SnowParticle particle : particles)
            {
                if (!particle.isFrozen())
                {
                    if (!freezeParticle(particle))
                    {
                        particle.update(delta);
                        if (particle.isOutOfBounds())
                        {
                            particle.reset();
                        }
                    }
                }
            }

            return null;
        });
    }

    private boolean freezeParticle(SnowParticle particle)
    {
        float px = particle.getX();
        float py = particle.getY();
        float radius = 2.5f;
        float chance = 0.9f;
    
        for (Vec2f point : points)
        {
            float dx = px - point.x;
            float dy = py - point.y;

            if (dx * dx + dy * dy < radius * radius)
            {
                if (Math.random() < chance)
                {
                    particle.setFrozen(true);
                    points.remove(point);
                    addParticles(1);
                    return true;
                }

                return false;
            }
        }

        return false;
    }

    private CopyOnWriteArrayList<Vec2f> sampleImage(NativeImage image, float targetWidth, float targetHeight, float density, float iScale)
    {
        CopyOnWriteArrayList<Vec2f> result = new CopyOnWriteArrayList<>();
        int width = image.getWidth();
        int height = image.getHeight();

        float scaleX = targetWidth / width;
        float scaleY = targetHeight / height;
        float scale = Math.min(scaleX, scaleY) * iScale;

        float baseX = (targetWidth - width * scale) / 2f;
        float baseY = (targetHeight - height * scale) / 2f;

        for (float x = 0; x < width; x += density)
        {
            for (float y = 0; y < height; y += density)
            {
                int argb = image.getColorArgb((int) x, (int) y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha < 50)
                {
                    continue;
                }

                float scaledX = baseX + x * scale;
                float scaledY = baseY + y * scale;
                result.add(new Vec2f(scaledX, scaledY));
            }
        }

        return result;
    }
}
