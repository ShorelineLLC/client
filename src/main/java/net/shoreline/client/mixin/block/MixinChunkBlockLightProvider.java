package net.shoreline.client.mixin.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.light.ChunkBlockLightProvider;
import net.shoreline.client.impl.event.render.LuminanceEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChunkBlockLightProvider.class)
public abstract class MixinChunkBlockLightProvider
{
    @WrapMethod(method = "getLightSourceLuminance")
    private int getLightSourceLuminanceHook(long blockPos,
                                            BlockState blockState,
                                            Operation<Integer> original)
    {
        LuminanceEvent event = new LuminanceEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return event.getLuminance();
        }

        return original.call(blockPos, blockState);
    }
}
