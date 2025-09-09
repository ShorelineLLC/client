package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.MouseEvent;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.eventbus.annotation.EventListener;

public class YawModule extends Toggleable
{
    Config<Boolean> lockYaw = new BooleanConfig.Builder("Lock")
            .setDescription("Locks your yaw")
            .setDefaultValue(false).build();

    public YawModule()
    {
        super("Yaw", "Fixes your yaw to a direction", GuiCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (checkNull())
        {
            return;
        }

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

    @EventListener
    public void onMouseUpdate(MouseEvent event)
    {
        if (lockYaw.getValue())
        {
            event.cancel();
            float f = (float) event.getCursorDeltaY() * 0.15f;
            mc.player.setPitch(MathHelper.clamp(mc.player.getPitch() + f, -90.0f, 90.0f));
        }
    }
}
