package net.shoreline.client.mixin.render;

import net.caffeinemc.mods.sodium.client.model.light.data.LightDataAccess;
import net.shoreline.client.impl.event.render.LightDataEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = LightDataAccess.class, remap = false)
public abstract class MixinLightDataAccess
{
    @ModifyVariable(method = "compute", at = @At(value = "STORE"), name = "sl")
    private int hookCompute_assignSL(int sl)
    {
        LightDataEvent event = new LightDataEvent();
        EventBus.INSTANCE.dispatch(event);
        if (event.isCanceled())
        {
            return event.getSl();
        }

        return sl;
    }
}
