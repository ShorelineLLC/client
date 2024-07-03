package net.shoreline.client.mixin.world;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.entity.EntityLookup;
import net.shoreline.client.impl.event.world.*;
import net.shoreline.eventbus.EventBus;
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
public abstract class MixinClientWorld
{

    @Shadow
    protected abstract EntityLookup<Entity> getEntityLookup();

    /**
     * @param entity
     * @param ci
     */
    @Inject(method = "addEntity", at = @At(value = "HEAD"))
    private void hookAddEntity(Entity entity, CallbackInfo ci)
    {
        AddEntityEvent addEntityEvent = new AddEntityEvent(entity);
        EventBus.INSTANCE.dispatch(addEntityEvent);
    }

    /**
     * @param entityId
     * @param removalReason
     * @param ci
     */
    @Inject(method = "removeEntity", at = @At(value = "HEAD"))
    private void hookRemoveEntity(int entityId, Entity.RemovalReason removalReason, CallbackInfo ci)
    {
        Entity entity = getEntityLookup().get(entityId);
        if (entity == null)
        {
            return;
        }
        RemoveEntityEvent removeEntityEvent = new RemoveEntityEvent(entity, removalReason);
        EventBus.INSTANCE.dispatch(removeEntityEvent);
    }

    /**
     * @param cameraPos
     * @param tickDelta
     * @param cir
     */
    @Inject(method = "getSkyColor", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetSkyColor(Vec3d cameraPos, float tickDelta,
                                 CallbackInfoReturnable<Vec3d> cir)
    {
        SkyboxEvent.Sky skyboxEvent = new SkyboxEvent.Sky();
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
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
                                    CallbackInfoReturnable<Vec3d> cir)
    {
        SkyboxEvent.Cloud skyboxEvent = new SkyboxEvent.Cloud();
        EventBus.INSTANCE.dispatch(skyboxEvent);
        if (skyboxEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(skyboxEvent.getColorVec());
        }
    }

    @Inject(method = "playSound(DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FFZJ)V", at = @At(value = "HEAD"), cancellable = true)
    private void hookPlaySound(double x, double y, double z, SoundEvent event, SoundCategory category,
                               float volume, float pitch, boolean useDistance, long seed, CallbackInfo ci)
    {
        PlaySoundEvent playSoundEvent = new PlaySoundEvent(new Vec3d(x, y, z), event, category);
        EventBus.INSTANCE.dispatch(playSoundEvent);
        if (playSoundEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "setBlockState", at = @At(value = "HEAD"), cancellable = true)
    private void hookSetBlockState(BlockPos pos, BlockState state, int flags, int maxUpdateDepth, CallbackInfoReturnable<Boolean> cir)
    {
        SetBlockStateEvent setBlockStateEvent = new SetBlockStateEvent(flags, pos, state);
        EventBus.INSTANCE.dispatch(setBlockStateEvent);
        if (setBlockStateEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "unloadBlockEntities", at = @At(value = "HEAD"))
    private void hookUnloadBlockEntities(WorldChunk chunk, CallbackInfo ci)
    {
        UnloadChunkBlocksEvent unloadChunkBlocksEvent = new UnloadChunkBlocksEvent(chunk);
        EventBus.INSTANCE.dispatch(unloadChunkBlocksEvent);
    }
}
