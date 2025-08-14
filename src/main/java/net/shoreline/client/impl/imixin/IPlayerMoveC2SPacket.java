package net.shoreline.client.impl.imixin;

@IMixin
public interface IPlayerMoveC2SPacket
{
    void setOnGround(boolean onGround);

    void setX(double x);

    void setY(double y);

    void setZ(double z);

    void setYaw(float yaw);

    void setPitch(float pitch);
}
