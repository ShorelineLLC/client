package net.shoreline.client.gui.titlescreen.particle;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.render.VertexConsumer;

@AllArgsConstructor
@Getter
@Setter
public abstract class Particle
{
    protected final int screenWidth;
    protected final int screenHeight;
    protected float x;
    protected float y;
    protected boolean frozen;

    protected float windupTime;
    protected float elapsedTime;

    /** Call this inside update() before doing anything */
    protected boolean isWindingUp(float delta)
    {
        elapsedTime += delta;
        return elapsedTime < windupTime;
    }

    protected boolean isWindingUp()
    {
        return elapsedTime < windupTime;
    }

    /** Reset windup when particle respawns */
    protected void resetWindup()
    {
        this.elapsedTime = 0f;
        this.windupTime = (float) (Math.random() * 5.0);
    }

    public abstract void update(float delta);

    public abstract void render(VertexConsumer consumer);

    public abstract boolean isOutOfBounds();

    public abstract void reset();
}
