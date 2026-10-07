package net.shoreline.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.font.FontManager;
import net.shoreline.client.impl.event.LoadingEvent;
import net.shoreline.client.impl.event.OpenScreenEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.WorldEvent;
import net.shoreline.client.impl.imixin.IMinecraftClient;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient
{
    @Unique
    private long startTime;

    @Override
    @Accessor("itemUseCooldown")
    public abstract int getItemUseCooldown();

    @Override
    @Accessor("itemUseCooldown")
    public abstract void setItemUseCooldown(int itemUseCooldown);

    @Override
    @Invoker("doItemUse")
    public abstract void hookDoItemUse();

    @Inject(method = "onInitFinished", at = @At(value = "RETURN"))
    private void hookOnInitFinished(MinecraftClient.LoadingContext loadingContext,
                                    CallbackInfoReturnable<Runnable> cir)
    {
        Shoreline.postInit();

        LoadingEvent.Finished finished = new LoadingEvent.Finished();
        EventBus.INSTANCE.dispatch(finished);
    }

    @Inject(method = "joinWorld", at = @At(value = "TAIL"))
    private void hookJoinWorld(ClientWorld world, DownloadingTerrainScreen.WorldEntryReason worldEntryReason, CallbackInfo ci)
    {
        final WorldEvent.Join worldEvent = new WorldEvent.Join();
        EventBus.INSTANCE.dispatch(worldEvent);
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screen/Screen;Z)V", at = @At(value = "TAIL"))
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

    @Inject(method = "setScreen", at = @At(value = "HEAD"), cancellable = true)
    private void hookSetScreen(Screen screen, CallbackInfo ci)
    {
        OpenScreenEvent screenOpenEvent = new OpenScreenEvent(screen);
        EventBus.INSTANCE.dispatch(screenOpenEvent);
        if (screenOpenEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "render", at = @At("HEAD"))
    public void hookRender(boolean tick, CallbackInfo ci)
    {
        startTime = System.nanoTime();
    }
}
