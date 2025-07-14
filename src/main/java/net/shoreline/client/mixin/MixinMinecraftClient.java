package net.shoreline.client.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.shoreline.client.api.font.FontScalingRegistry;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.OpenScreenEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MixinMinecraftClient
{
    @Unique
    private long startTime;

    @Inject(method = "joinWorld", at = @At(value = "TAIL"))
    private void hookJoinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        final WorldEvent.Join worldEvent = new WorldEvent.Join();
        EventBus.INSTANCE.dispatch(worldEvent);
    }

    @Inject(method = "disconnect", at = @At(value = "TAIL"))
    private void hookDisconnect(Screen disconnectionScreen, boolean transferring, CallbackInfo ci)
    {
        final WorldEvent.Disconnect worldEvent = new WorldEvent.Disconnect();
        EventBus.INSTANCE.dispatch(worldEvent);
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci)
    {
        final TickEvent.Pre tickEvent = new TickEvent.Pre();
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci)
    {
        final TickEvent.Post tickEvent = new TickEvent.Post();
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    @Inject(method = "setScreen", at = @At(value = "TAIL"))
    private void hookSetScreen(Screen screen, CallbackInfo ci)
    {
        OpenScreenEvent screenOpenEvent = new OpenScreenEvent(screen);
        EventBus.INSTANCE.dispatch(screenOpenEvent);
    }

    @Inject(method = "onResolutionChanged", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/util/Window;setScaleFactor(I)V"))
    private void hookOnResolutionChanged(CallbackInfo ci, @Local(ordinal = 0) int i)
    {
        FontScalingRegistry.resize(i);
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void hookRender(boolean tick, CallbackInfo ci)
    {
        startTime = System.nanoTime();
    }
}
