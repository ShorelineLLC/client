package net.shoreline.client.mixin.accessor;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * @author linus
 * @see ClientPlayerInteractionManager
 * @since 1.0
 */
@Mixin(ClientPlayerInteractionManager.class)
public interface AccessorClientPlayerInteractionManager
{
    /**
     *
     */
    @Invoker("syncSelectedSlot")
    void hookSyncSelectedSlot();

    @Invoker("interactBlockInternal")
    ActionResult hookInteractBlockInternal(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult);

    /**
     * @return
     */
    @Accessor("currentBreakingProgress")
    float hookGetCurrentBreakingProgress();

    /**
     * @param currentBreakingProgress
     */
    @Accessor("currentBreakingProgress")
    void hookSetCurrentBreakingProgress(float currentBreakingProgress);
}
