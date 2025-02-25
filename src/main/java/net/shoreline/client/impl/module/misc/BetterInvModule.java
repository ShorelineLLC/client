package net.shoreline.client.impl.module.misc;

import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.gui.screen.MouseDraggedEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

public class BetterInvModule extends ToggleModule
{

    Config<Boolean> dragItemMoveConfig = register(new BooleanConfig("DragItemMove", "Allows you to hold down shift and drag move items.", true));
    Config<Boolean> fastDropConfig = register(new BooleanConfig("FastDrop", "Drops items from the hotbar faster", false));
    Config<Integer> delayConfig = register(new NumberConfig<>("Delay", "The delay for dropping items", 0, 0, 4, () -> fastDropConfig.getValue()));

    private int dropTicks;

    public BetterInvModule()
    {
        super("BetterInv", "Makes your inventory better", ModuleCategory.MISCELLANEOUS);
    }

    /**
     * @param event
     */
    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE || !fastDropConfig.getValue())
        {
            return;
        }
        if (mc.options.dropKey.isPressed() && dropTicks > delayConfig.getValue())
        {
            Managers.NETWORK.sendPacket(new PlayerActionC2SPacket(PlayerActionC2SPacket.Action.DROP_ITEM,
                    BlockPos.ORIGIN, Direction.DOWN));
            dropTicks = 0;
        }
        ++dropTicks;
    }

    @EventListener
    public void onMouseDragged(MouseDraggedEvent event)
    {
        if (dragItemMoveConfig.getValue())
        {
            event.cancel();
        }
    }
}
