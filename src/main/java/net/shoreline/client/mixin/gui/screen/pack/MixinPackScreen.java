package net.shoreline.client.mixin.gui.screen.pack;

import net.minecraft.client.gui.screen.pack.PackScreen;
import net.shoreline.client.impl.event.gui.screen.pack.RefreshPacksEvent;
import net.shoreline.eventbus.bus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PackScreen.class)
public class MixinPackScreen {

    @Inject(method = "refresh", at = @At(value = "HEAD"))
    private void hookRefresh(CallbackInfo ci)
    {
        RefreshPacksEvent refreshPacksEvent = new RefreshPacksEvent();
        EventBus.INSTANCE.dispatch(refreshPacksEvent);
    }
}
