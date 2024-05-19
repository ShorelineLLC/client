package net.shoreline.client.mixin.world;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;
import net.shoreline.client.impl.event.handler.EventBus;
import net.shoreline.client.impl.event.world.AddEntityEvent;
import net.shoreline.client.impl.event.world.RemoveEntityEvent;
import net.shoreline.client.impl.event.world.SkyboxEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(ClientWorld.class)
public abstract class MixinClientWorld {

    @Shadow
    @Nullable
    public abstract Entity getEntityById(int id);

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void hookAddEntity(Entity entity, CallbackInfo ci) {
        AddEntityEvent addEntityEvent = new AddEntityEvent(entity);
        EventBus.EVENT_HANDLER.dispatch(addEntityEvent);
    }

    /**
     *
     * @param entityId
     * @param removalReason
     * @param ci
     */
    @Inject(method = "removeEntity", at = @At(value = "HEAD"))
    private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci) {
        Entity entity = getEntityById(entityId);
        if (entity == null) {
            return;
        }
        RemoveEntityEvent addEntityEvent = new RemoveEntityEvent(entity, removalReason);
        EventBus.EVENT_HANDLER.dispatch(addEntityEvent);
    }

    /**
     * @param cameraPos
     * @param tickDelta
     * @param cir
     */
    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(Vec3d cameraPos, float tickDelta,
                                 CallbackInfoReturnable<Vec3d> cir) {
        SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
        EventBus.EVENT_HANDLER.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColorVec());
        }
    }

    /**
     * @param tickDelta
     * @param cir
     */
    @Inject(method = "getCloudsColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetCloudsColor(float tickDelta,
                                    CallbackInfoReturnable<Vec3d> cir) {
        SkyboxEvent.Cloud skyboxEvent = new SkyboxEvent.Cloud();
        EventBus.EVENT_HANDLER.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled()) {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColorVec());
        }
    }
}
