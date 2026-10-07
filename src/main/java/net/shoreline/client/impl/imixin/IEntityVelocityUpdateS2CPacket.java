package net.shoreline.client.impl.imixin;

@IMixin
public interface IEntityVelocityUpdateS2CPacket
{
    void setX(int velocityX);

    void setY(int velocityY);

    void setZ(int velocityZ);
}
