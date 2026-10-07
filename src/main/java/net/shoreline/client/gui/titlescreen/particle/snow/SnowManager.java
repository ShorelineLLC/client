package net.shoreline.client.gui.titlescreen.particle.snow;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.Window;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.gui.titlescreen.particle.ParticleManager;
import net.shoreline.client.gui.titlescreen.particle.ParticleRenderer;
import net.shoreline.client.impl.render.Layers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SnowManager extends ParticleManager<SnowParticle>
{
    private final Identifier SHORELINE = Identifier.of("shoreline", "textures/shoreline.png");
    private final Map<Long, ArrayList<Vec2f>> points = new HashMap<>();
    private static final float RAD = 0.75f;
    private static final float RAD_SQ = RAD * RAD;
    private static final float SIZE = 4.0f;

    public SnowManager(int count)
    {
        super(count, new ParticleRenderer<>(Layers.SNOW));
        if (mc.getWindow() == null)
        {
            return;
        }

        loadImage();
    }

    public void loadImageAsync()
    {
        runAsync(() ->
        {
            loadImage();
            return null;
        });
    }

    public void loadImage()
    {
        Window window = mc.getWindow();
        float targetWidth = window.getScaledWidth();
        float targetHeight = window.getScaledHeight();

        try
        {
            NativeImage image = NativeImage.read(mc.getResourceManager().getResourceOrThrow(SHORELINE).getInputStream());
            sampleImage(image, targetWidth, targetHeight, 2.5f, 0.67f);
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
        loadImageAsync();
    }

    @Override
    public void update()
    {
        long now = System.currentTimeMillis();
        float delta = Math.min((now - lastUpdate) / 1000f, 1f);
        lastUpdate = now;

        int count = 0;
        for (SnowParticle particle : particles)
        {
            if (particle.isFrozen())
            {
                continue;
            }

            if (particle.isCollisionCheck() && freezeParticle(particle))
            {
                count++;
                continue;
            }

            particle.update(delta);
            if (particle.isOutOfBounds())
            {
                particle.reset();
            }
        }

        addParticles(count);
    }

    @Override
    public void render(DrawContext context)
    {
        if (particles.isEmpty())
        {
            return;
        }

        Window window = mc.getWindow();
        int width  = window.getScaledWidth();
        int height = window.getScaledHeight();

        if (particles.getFirst().getScreenWidth() != width ||
                particles.getFirst().getScreenHeight() != height)
        {
            reset();
            return;
        }

        renderer.render(context, particles);
    }

    private boolean freezeParticle(SnowParticle particle)
    {
        float px = particle.getX();
        float py = particle.getY();

        int gx = (int) (px / SIZE);
        int gy = (int) (py / SIZE);

        long key = cellKey(gx, gy);
        ArrayList<Vec2f> cell = points.get(key);
        if (cell == null || cell.isEmpty())
        {
            return false;
        }

        for (int i = 0; i < cell.size(); i++)
        {
            Vec2f point = cell.get(i);
            float dx = px - point.x;
            float dy = py - point.y;

            if (dx * dx + dy * dy < RAD_SQ)
            {
                particle.setFrozen(true);
                int last = cell.size() - 1;
                cell.set(i, cell.get(last));
                cell.remove(last);
                return true;
            }
        }

        return false;
    }

    private void sampleImage(NativeImage image, float targetWidth, float targetHeight, float density, float iScale)
    {
        points.clear();
        int w = image.getWidth();
        int h = image.getHeight();

        float scale = Math.min(targetWidth / w, targetHeight / h) * iScale;
        float baseX = (targetWidth - w * scale) / 2f;
        float baseY = (targetHeight - h * scale) / 2f;

        for (float x = 0; x < w; x += density)
        {
            for (float y = 0; y < h; y += density)
            {
                int alpha = (image.getColorArgb((int) x, (int) y) >>> 24);
                if (alpha < 50)
                {
                    continue;
                }

                float sx = baseX + x * scale;
                float sy = baseY + y * scale;

                int gx = (int) (sx / SIZE);
                int gy = (int) (sy / SIZE);
                long key = cellKey(gx, gy);

                points.computeIfAbsent(key, k -> new ArrayList<>())
                        .add(new Vec2f(sx, sy));
            }
        }
    }

    private long cellKey(int x, int y)
    {
        return (((long) x) << 32) | (y & 0xffffffffL);
    }
}