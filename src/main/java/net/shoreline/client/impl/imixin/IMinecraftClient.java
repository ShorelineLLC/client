package net.shoreline.client.impl.imixin;

@IMixin
public interface IMinecraftClient
{
    void hookDoItemUse();

    int getItemUseCooldown();

    void setItemUseCooldown(int itemUseCooldown);
}
