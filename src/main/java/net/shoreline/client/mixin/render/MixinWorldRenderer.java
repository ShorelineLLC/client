package net.shoreline.client.mixin.render;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.WorldRenderer;
import net.shoreline.client.impl.event.render.RenderPlayerThirdPersonEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public class MixinWorldRenderer
{
    @Redirect(method = "getEntitiesToRender", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/render/Camera;isThirdPerson()Z"))
    public boolean hookGetEntitiesToRender(Camera instance)
    {
        RenderPlayerThirdPersonEvent renderPlayerEvent = new RenderPlayerThirdPersonEvent();
        EventBus.INSTANCE.dispatch(renderPlayerEvent);
        return renderPlayerEvent.isCanceled() || instance.isThirdPerson();
    }
}
