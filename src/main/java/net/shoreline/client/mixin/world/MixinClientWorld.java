package net.shoreline.client.mixin.world;

import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.render.SkyboxEvent;
import net.shoreline.client.impl.imixin.IClientWorld;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements IClientWorld
{
    @Override
    @Accessor("pendingUpdateManager")
    public abstract PendingUpdateManager getUpdateManager();

    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(Vec3d cameraPos,
                                 float tickDelta,
                                 CallbackInfoReturnable<Integer> cir)
    {
        SkyboxEvent.SkyColor skyboxEvent = new SkyboxEvent.SkyColor();
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColor().getRGB());
        }
    }

    @Inject(method = "getCloudsColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetCloudsColor(float tickDelta,
                                    CallbackInfoReturnable<Integer> cir)
    {
        SkyboxEvent.CloudColor skyboxEvent = new SkyboxEvent.CloudColor();
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColor().getRGB());
        }
    }
}
