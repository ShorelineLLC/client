package net.shoreline.client.impl.manager.player.rotation;

public class Rotation
{
    private final int priority;
    private float yaw, pitch;
    private boolean snap;

    public Rotation(int priority, float yaw, float pitch, boolean snap)
    {
        this.priority = priority;
        this.yaw = yaw;
        this.pitch = pitch;
        this.snap = snap;
    }

    public Rotation(int priority, float yaw, float pitch)
    {
        this(priority, yaw, pitch, false);
    }

    public int getPriority()
    {
        return priority;
    }

    public void setYaw(float yaw)
    {
        this.yaw = yaw;
    }

    public void setPitch(float pitch)
    {
        this.pitch = pitch;
    }

    public float getYaw()
    {
        return yaw;
    }

    public float getPitch()
    {
        return pitch;
    }

    public void setSnap(boolean snap)
    {
        this.snap = snap;
    }

    public boolean isSnap()
    {
        return snap;
    }
}
