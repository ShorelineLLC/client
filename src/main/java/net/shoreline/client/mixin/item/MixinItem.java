package net.shoreline.client.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.shoreline.client.impl.event.item.ItemUseEvent;
import net.shoreline.eventbus.EventBus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class MixinItem
{
    @Inject(method = "use", at = @At(value = "HEAD"))
    private void hookUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<ActionResult> cir)
    {
        ItemUseEvent itemUseEvent = new ItemUseEvent((Item) (Object) this);
        EventBus.INSTANCE.dispatch(itemUseEvent);
    }
}
