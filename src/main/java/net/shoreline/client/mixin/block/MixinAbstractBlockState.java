package net.shoreline.client.mixin.block;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.block.AbstractBlock;
import net.shoreline.client.impl.event.render.LuminanceEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractBlock.AbstractBlockState.class)
public abstract class MixinAbstractBlockState
{
    @WrapMethod(method = "getLuminance")
    private int getLuminanceHook(Operation<Integer> original)
    {
        LuminanceEvent event = new LuminanceEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return event.getLuminance();
        }

        return original.call();
    }
}
