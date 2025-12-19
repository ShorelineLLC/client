package net.shoreline.client.gui.titlescreen.particle;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.shoreline.client.api.async.AsyncFeature;

import java.util.ArrayList;
import java.util.List;

public abstract class ParticleManager<T extends Particle> extends AsyncFeature<Void>
{
    protected List<T> particles = new ArrayList<>();
    protected final ParticleRenderer<T> renderer;
    protected final int count;

    protected Long lastUpdate;

    public ParticleManager(int count, ParticleRenderer<T> renderer)
    {
        super("Particles");
        this.count = count;
        this.renderer = renderer;
        this.lastUpdate = System.currentTimeMillis();
    }

    protected abstract T createParticle(int screenWidth, int screenHeight);

    public void addParticles(int amount)
    {
        if (mc == null || mc.getWindow() == null)
        {
            return;
        }

        Window window = mc.getWindow();
        int width = window.getScaledWidth();
        int height = window.getScaledHeight();

        for (int i = 0; i < amount; i++)
        {
            particles.add(createParticle(width, height));
        }
    }

    public void reset()
    {
        particles.clear();
        List<T> newList = new ArrayList<>();
        if (mc == null || mc.getWindow() == null)
        {
            return;
        }

        Window window = mc.getWindow();
        int width = window.getScaledWidth();
        int height = window.getScaledHeight();

        for (int i = 0; i < count; i++)
        {
            T particle = createParticle(width, height);
            newList.add(particle);
        }

        particles = newList;
    }

    public void update()
    {
        runAsync(() ->
        {
            if (particles.isEmpty())
            {
                reset();
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

            for (T particle : particles)
            {
                particle.update(delta);
                if (particle.isOutOfBounds())
                {
                    particle.reset();
                }
            }

            return null;
        });
    }

    public void render(DrawContext context)
    {
        if (particles.isEmpty())
        {
            return;
        }

        Window window = mc.getWindow();
        int width  = window.getScaledWidth();
        int height = window.getScaledHeight();

        if (particles.getFirst().screenWidth != width ||
            particles.getFirst().screenHeight != height)
        {
            reset();
            return;
        }

        renderer.render(context, particles);
    }
}
