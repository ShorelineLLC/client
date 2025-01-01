package net.shoreline.client.impl.module.movement;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.network.PlayerUpdateEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class TickShiftModule extends ToggleModule
{
    // Basically auto timer for NCP
    //
    Config<Integer> ticksConfig = register(new NumberConfig<>("MaxTicks", "Maximum charge ticks", 1, 20, 40));
    Config<Integer> packetsConfig = register(new NumberConfig<>("Packets", "Packets to release from storage every tick", 1, 1, 5));
    Config<Integer> chargeSpeedConfig = register(new NumberConfig<>("ChargeSpeed", "The speed to charge the stored packets", 1, 1, 5));
    //
    private int packets;

    /**
     *
     */
    public TickShiftModule()
    {
        super("TickShift", "Exploits NCP to speed up ticks",
                ModuleCategory.MOVEMENT);
    }

    @Override
    public String getModuleData()
    {
        return String.valueOf(packets);
    }

    @Override
    public void onDisable()
    {
        Managers.TICK.setClientTick(1.0f);
    }

    @EventListener
    public void onPlayerUpdate(PlayerUpdateEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        if (MovementUtil.isMoving() || !mc.player.isOnGround())
        {
            packets -= packetsConfig.getValue();
            if (packets <= 0)
            {
                packets = 0;
                Managers.TICK.setClientTick(1.0f);
                return;
            }
            Managers.TICK.setClientTick(packetsConfig.getValue() + 1.0f);
        }
        else
        {
            packets += chargeSpeedConfig.getValue();
            if (packets > ticksConfig.getValue())
            {
                packets = ticksConfig.getValue();
            }
        }
    }
}
