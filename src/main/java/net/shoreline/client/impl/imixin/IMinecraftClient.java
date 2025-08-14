package net.shoreline.client.impl.imixin;

@IMixin
public interface IMinecraftClient
{
    int getItemUseCooldown();

    void setItemUseCooldown(int itemUseCooldown);
}
