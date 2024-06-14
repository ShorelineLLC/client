package net.shoreline.client.mixin.world;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.EntityView;
import net.shoreline.eventbus.annotation.EventListener;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

//@Mixin(EntityView.class)
public abstract class MixinEntityView
{
//    @Shadow
//    public abstract List<? extends PlayerEntity> getPlayers();
//
//    @Inject(method = "getPlayerByUuid", at = @At(value = "HEAD"), cancellable = true)
//    private void hookGetPlayerByUuid(UUID uuid, CallbackInfoReturnable<EntityView> cir)
//    {
//        cir.cancel();
//        for (int i = 0; i < this.getPlayers().size(); ++i)
//        {
//            PlayerEntity playerEntity = this.getPlayers().get(i);
//            if (playerEntity != null && uuid.equals(playerEntity.getUuid()))
//            {
//                cir.setReturnValue((EntityView) playerEntity);
//            }
//        }
//
//        cir.setReturnValue(null);
//    }
}
