package net.shoreline.client.mixin.gui.screen;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.shoreline.client.impl.event.gui.RenderTooltipEvent;
import net.shoreline.client.impl.event.gui.screen.MouseDraggedEvent;
import net.shoreline.eventbus.EventBus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static net.minecraft.client.gui.screen.Screen.hasShiftDown;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen
{

    @Shadow
    @Nullable
    protected abstract Slot getSlotAt(double xPosition, double yPosition);

    @Shadow
    @Nullable
    protected Slot focusedSlot;

    @Shadow
    private boolean doubleClicking;

    @Shadow
    protected abstract void onMouseClick(Slot slot, int invSlot, int clickData, SlotActionType actionType);


    @Inject(method = "drawMouseoverTooltip", at = @At(value = "HEAD"), cancellable = true)
    private void hookDrawMouseoverTooltip(DrawContext context, int x, int y, CallbackInfo ci)
    {
        if (focusedSlot == null)
        {
            return;
        }
        RenderTooltipEvent renderTooltipEvent =
                new RenderTooltipEvent(context, focusedSlot.getStack(), x, y);
        EventBus.INSTANCE.dispatch(renderTooltipEvent);
        if (renderTooltipEvent.isCanceled())
        {
            ci.cancel();
        }
    }

    @Inject(method = "mouseDragged", at = @At("TAIL"))
    private void hookMouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY, CallbackInfoReturnable<Boolean> cir)
    {
        MouseDraggedEvent mouseDraggedEvent = new MouseDraggedEvent();
        EventBus.INSTANCE.dispatch(mouseDraggedEvent);
        if (button != GLFW_MOUSE_BUTTON_LEFT
                || doubleClicking
                || !mouseDraggedEvent.isCanceled())
        {
            return;
        }
        Slot slot = getSlotAt(mouseX, mouseY);
        if (slot != null && slot.hasStack() && hasShiftDown())
        {
            onMouseClick(slot, slot.id, button, SlotActionType.QUICK_MOVE);
        }
    }
}
