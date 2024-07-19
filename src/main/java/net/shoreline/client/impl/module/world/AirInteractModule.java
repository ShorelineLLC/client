package net.shoreline.client.impl.module.world;

import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberDisplay;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PlayerTickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author xgraza
 * @since 04/13/24
 */
public final class AirInteractModule extends ToggleModule
{
    public static AirInteractModule INSTANCE;

    Config<Boolean> grimConfig = register(new BooleanConfig("Grim", "Place on air on grim", false));
    Config<Float> rangeConfig = register(new NumberConfig<>("Range", "The range to air place", 1.0f, 4.0f, 6.0f, NumberDisplay.DEFAULT));
    Config<Boolean> fluidsConfig = register(new BooleanConfig("Fluids", "Place against fluids", false));
    private int blockPlaceDelay;

    public AirInteractModule()
    {
        super("AirInteract", "Allows you to place blocks in the air", ModuleCategory.WORLD);
        INSTANCE = this;
    }

    public static AirInteractModule getInstance()
    {
        return INSTANCE;
    }

    @EventListener
    public void onPlayerTick(final TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        blockPlaceDelay--;
        if (blockPlaceDelay > 0)
        {
            return;
        }
        final ItemStack stack = mc.player.getMainHandStack();
        if ((stack.isEmpty() || !(stack.getItem() instanceof BlockItem)) || !mc.options.useKey.isPressed())
        {
            return;
        }

        final HitResult result = mc.player.raycast(rangeConfig.getValue(), 1.0f, fluidsConfig.getValue());
        if (result instanceof BlockHitResult blockHitResult)
        {
            if (grimConfig.getValue())
            {
                Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(
                        PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, blockHitResult.getBlockPos(), Direction.DOWN));
            }
            final ActionResult actionResult = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, blockHitResult);
            if (actionResult.isAccepted() && actionResult.shouldSwingHand())
            {
                mc.player.swingHand(Hand.MAIN_HAND);
            }
            blockPlaceDelay = 10;
        }
    }
}
