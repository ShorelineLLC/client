package net.shoreline.client.mixin.network;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientCommonNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientCommonNetworkHandler.class)
public class MixinClientCommonNetworkHandler
{
    @Shadow
    @Final
    protected MinecraftClient client;
}
