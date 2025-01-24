package net.shoreline.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.shoreline.client.impl.event.*;
import net.shoreline.client.impl.event.entity.EntityDeathEvent;
import net.shoreline.client.impl.event.gui.screen.pack.RefreshPacksEvent;
import net.shoreline.client.impl.imixin.IMinecraftClient;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.event.StageEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author linus
 * @since 1.0
 */
@Mixin(MinecraftClient.class)
public abstract class MixinMinecraftClient implements IMinecraftClient
{
    //
    @Shadow
    public ClientWorld world;
    //
    @Shadow
    public ClientPlayerEntity player;
    //
    @Shadow
    @Nullable
    public ClientPlayerInteractionManager interactionManager;
    //
    @Shadow
    protected int attackCooldown;
    @Unique
    private boolean leftClick;
    // https://github.com/MeteorDevelopment/meteor-client/blob/master/src/main/java/meteordevelopment/meteorclient/mixin/MinecraftClientMixin.java#L54
    @Unique
    private boolean rightClick;
    @Unique
    private boolean doAttackCalled;
    @Unique
    private boolean doItemUseCalled;

    /**
     *
     */
    @Shadow
    protected abstract void doItemUse();

    /**
     * @return
     */
    @Shadow
    protected abstract boolean doAttack();

    @Shadow
    @Final
    private Window window;

    /**
     *
     */
    @Override
    public void leftClick()
    {
        leftClick = true;
    }

    /**
     *
     */
    @Override
    public void rightClick()
    {
        rightClick = true;
    }

    /**
     * @param ci
     */
    @Inject(method = "run", at = @At(value = "INVOKE", target =
            "Lnet/minecraft/client/MinecraftClient;render(Z)V", shift = At.Shift.BEFORE))
    private void hookRun(CallbackInfo ci)
    {
        final RunTickEvent runTickEvent = new RunTickEvent();
        EventBus.INSTANCE.dispatch(runTickEvent);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;overlay:Lnet/minecraft/client/gui/screen/Overlay;"))
    private void hookTick(CallbackInfo ci)
    {
        ClientTickEvent tickEvent = new ClientTickEvent();
        EventBus.INSTANCE.dispatch(tickEvent);
    }

    /**
     * @param loadingContext
     * @param cir
     */
    @Inject(method = "onInitFinished", at = @At(value = "RETURN"))
    private void hookOnInitFinished(MinecraftClient.LoadingContext loadingContext, CallbackInfoReturnable<Runnable> cir)
    {
        FinishLoadingEvent finishLoadingEvent = new FinishLoadingEvent();
        EventBus.INSTANCE.dispatch(finishLoadingEvent);
        // Managers.CAPES.init();
    }

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void hookTickPre(CallbackInfo ci)
    {
        doAttackCalled = false;
        doItemUseCalled = false;
        if (player != null && world != null)
        {
            TickEvent tickPreEvent = new TickEvent();
            tickPreEvent.setStage(StageEvent.EventStage.PRE);
            EventBus.INSTANCE.dispatch(tickPreEvent);
        }
        if (interactionManager == null)
        {
            return;
        }
        if (leftClick && !doAttackCalled)
        {
            doAttack();
        }
        if (rightClick && !doItemUseCalled)
        {
            doItemUse();
        }
        leftClick = false;
        rightClick = false;
    }

    @Unique
    private final List<Integer> deadList = new ArrayList<>();

    /**
     * @param ci
     */
    @Inject(method = "tick", at = @At(value = "TAIL"))
    private void hookTickPost(CallbackInfo ci)
    {
        if (player != null && world != null)
        {
            TickEvent tickPostEvent = new TickEvent();
            tickPostEvent.setStage(StageEvent.EventStage.POST);
            EventBus.INSTANCE.dispatch(tickPostEvent);
            for (Entity entity : world.getEntities())
            {
                if (entity instanceof LivingEntity e)
                {
                    if (e.isDead() && !deadList.contains(e.getId()))
                    {
                        EntityDeathEvent entityDeathEvent = new EntityDeathEvent(e);
                        EventBus.INSTANCE.dispatch(entityDeathEvent);
                        deadList.add(e.getId());
                    }
                    else if (!e.isDead())
                    {
                        deadList.remove((Integer) e.getId());
                    }
                }
            }
        }
    }

    /**
     * @param screen
     * @param ci
     */
    @Inject(method = "setScreen", at = @At(value = "TAIL"))
    private void hookSetScreen(Screen screen, CallbackInfo ci)
    {
        ScreenOpenEvent screenOpenEvent = new ScreenOpenEvent(screen);
        EventBus.INSTANCE.dispatch(screenOpenEvent);
    }

    /**
     * @param ci
     */
    @Inject(method = "doItemUse", at = @At(value = "HEAD"), cancellable = true)
    private void hookDoItemUse(CallbackInfo ci)
    {
        doItemUseCalled = true;
        ItemUseEvent itemUseEvent = new ItemUseEvent();
        EventBus.INSTANCE.dispatch(itemUseEvent);
        if (itemUseEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    /**
     * @param cir
     */
    @Inject(method = "doAttack", at = @At(value = "HEAD"))
    private void hookDoAttack(CallbackInfoReturnable<Boolean> cir)
    {
        doAttackCalled = true;
        AttackCooldownEvent attackCooldownEvent = new AttackCooldownEvent();
        EventBus.INSTANCE.dispatch(attackCooldownEvent);
        if (attackCooldownEvent.isCanceled())
        {
            attackCooldown = 0;
        }
    }

    /**
     * @param instance
     * @return
     */
    @Redirect(method = "handleBlockBreaking", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;isUsingItem()Z"))
    private boolean hookIsUsingItem(ClientPlayerEntity instance)
    {
        ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
        EventBus.INSTANCE.dispatch(itemMultitaskEvent);
        return !itemMultitaskEvent.isCanceled() && instance.isUsingItem();
    }

    /**
     * @param instance
     * @return
     */
    @Redirect(method = "doItemUse", at = @At(value = "INVOKE", target = "Lnet" +
            "/minecraft/client/network/ClientPlayerInteractionManager;isBreakingBlock()Z"))
    private boolean hookIsBreakingBlock(ClientPlayerInteractionManager instance)
    {
        ItemMultitaskEvent itemMultitaskEvent = new ItemMultitaskEvent();
        EventBus.INSTANCE.dispatch(itemMultitaskEvent);
        return !itemMultitaskEvent.isCanceled() && instance.isBreakingBlock();
    }

    /**
     * @param cir
     */
    @Inject(method = "getFramerateLimit", at = @At(value = "HEAD"), cancellable = true)
    private void hookGetFramerateLimit(CallbackInfoReturnable<Integer> cir)
    {
        FramerateLimitEvent framerateLimitEvent = new FramerateLimitEvent();
        EventBus.INSTANCE.dispatch(framerateLimitEvent);
        if (framerateLimitEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(framerateLimitEvent.getFramerateLimit());
        }
    }

    /**
     * @param entity
     * @param cir
     */
    @Inject(method = "hasOutline", at = @At(value = "HEAD"), cancellable = true)
    private void hookHasOutline(Entity entity, CallbackInfoReturnable<Boolean> cir)
    {
        EntityOutlineEvent entityOutlineEvent = new EntityOutlineEvent(entity);
        EventBus.INSTANCE.dispatch(entityOutlineEvent);
        if (entityOutlineEvent.isCanceled())
        {
            cir.cancel();
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "reloadResources(ZLnet/minecraft/client/MinecraftClient$LoadingContext;)Ljava/util/concurrent/CompletableFuture;", at = @At(value = "RETURN"))
    private void hookReloadResources(boolean force, MinecraftClient.LoadingContext loadingContext, CallbackInfoReturnable<CompletableFuture<Void>> cir)
    {
        RefreshPacksEvent refreshPacksEvent = new RefreshPacksEvent();
        EventBus.INSTANCE.dispatch(refreshPacksEvent);
    }

    @Inject(method = "onResolutionChanged", at = @At(value = "TAIL"))
    private void hookOnResolutionChanged(CallbackInfo ci)
    {
        ResolutionEvent resolutionEvent = new ResolutionEvent(window);
        EventBus.INSTANCE.dispatch(resolutionEvent);
    }
}
