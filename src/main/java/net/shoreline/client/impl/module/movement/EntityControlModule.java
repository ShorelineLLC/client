package net.shoreline.client.impl.module.movement;

import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.entity.mob.PigAIEvent;
import net.shoreline.client.impl.event.entity.passive.EntitySteerEvent;
import net.shoreline.client.impl.event.network.MountJumpStrengthEvent;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author linus
 * @since 1.0
 */
public class EntityControlModule extends ToggleModule
{
    //
    Config<Float> jumpStrengthConfig = register(new NumberConfig<>("JumpStrength", "The fixed jump strength of the mounted entity", 0.1f, 0.7f, 2.0f));
    Config<Boolean> noPigMoveConfig = register(new BooleanConfig("NoPigAI", "Prevents the pig movement when controlling pigs", false));

    public EntityControlModule()
    {
        super("EntityControl", "Allows you to steer entities without a saddle", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null)
        {
            return;
        }
        vehicle.setYaw(mc.player.getYaw());
        if (vehicle instanceof LlamaEntity llama)
        {
            llama.headYaw = mc.player.getYaw();
        }
    }

    @EventListener
    public void onEntitySteer(EntitySteerEvent event)
    {
        event.cancel();
    }

    @EventListener
    public void onMountJumpStrength(MountJumpStrengthEvent event)
    {
        event.cancel();
        event.setJumpStrength(jumpStrengthConfig.getValue());
    }

    @EventListener
    public void onPigAI(PigAIEvent event)
    {
        if (mc.player == null)
        {
            return;
        }
        if (noPigMoveConfig.getValue() && event.getPigEntity().getPassengerList().contains(mc.player))
        {
            event.cancel();
        }
    }
}
