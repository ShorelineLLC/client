package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.LookDirectionEvent;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class YawModule extends ToggleModule
{

    Config<Boolean> lockConfig = register(new BooleanConfig("Lock", "Locks the yaw in cardinal direction", false));

    public YawModule()
    {
        super("Yaw", "Locks player yaw to a cardinal axis", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() == StageEvent.EventStage.PRE)
        {
            float yaw = Math.round(mc.player.getYaw() / 45.0f) * 45.0f;
            Entity vehicle = mc.player.getVehicle();
            if (vehicle != null)
            {
                vehicle.setYaw(yaw);
                if (vehicle instanceof LlamaEntity llama)
                {
                    llama.setHeadYaw(yaw);
                }
                return;
            }
            mc.player.setYaw(yaw);
            mc.player.setHeadYaw(yaw);
        }
    }

    @EventListener
    public void onLookDirection(LookDirectionEvent event)
    {
        if (lockConfig.getValue())
        {
            event.cancel();
            float f = (float) event.getCursorDeltaY() * 0.15f;
            mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + f, -90.0f, 90.0f));
        }
    }
}
