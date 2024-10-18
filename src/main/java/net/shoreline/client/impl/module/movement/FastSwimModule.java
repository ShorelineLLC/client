package net.shoreline.client.impl.module.movement;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ElytraItem;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.player.PlayerMoveEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.player.EnchantmentUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class FastSwimModule extends ToggleModule
{
    Config<SwimMode> modeConfig = register(new EnumConfig<>("Mode", "The mode for swimming", SwimMode.VANILLA, SwimMode.values()));
    Config<Float> waterSpeedConfig = register(new NumberConfig<>("WaterSpeed", "Base speed for moving through water", 0.1f, 1.0f, 10.0f));
    Config<Float> lavaSpeedConfig = register(new NumberConfig<>("LavaSpeed", "Base speed for moving through lava", 0.1f, 1.0f, 10.0f));
    Config<Boolean> verticalConfig = register(new BooleanConfig("Vertical", "Allows moving vertically", true));
    Config<Boolean> elytraConfig = register(new BooleanConfig("Elytra", "Applies elytra speed when moving through liquids", false));
    Config<Float> elytraSpeedConfig = register(new NumberConfig<>("ElytraSpeed", "Base speed for moving through lava", 0.1f, 1.0f, 10.0f, () -> elytraConfig.getValue()));
    Config<Boolean> depthStriderConfig = register(new BooleanConfig("DepthStrider", "Prefers depth strider", false));

    public FastSwimModule()
    {
        super("FastSwim", "Move faster in liquids", ModuleCategory.MOVEMENT);
    }

    @EventListener
    public void onPlayerMove(PlayerMoveEvent event)
    {
        if (!depthStriderConfig.getValue() && EnchantmentUtil.getLevel(mc.player.getEquippedStack(EquipmentSlot.FEET), Enchantments.DEPTH_STRIDER) > 0)
        {
            return;
        }
        // Must be fully submerged to apply speed
        if (!mc.player.isSubmergedIn(FluidTags.WATER) && !mc.player.isSubmergedIn(FluidTags.LAVA))
        {
            return;
        }
        event.cancel();
        if (mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() instanceof ElytraItem && elytraConfig.getValue())
        {
            event.setX(event.getX() * elytraSpeedConfig.getValue());
            event.setZ(event.getZ() * elytraSpeedConfig.getValue());
            if (verticalConfig.getValue())
            {
                if (mc.options.jumpKey.isPressed())
                {
                    event.setY(event.getY() + 0.16);
                    Managers.MOVEMENT.setMotionY(event.getY() + 0.16);
                }
                else if (mc.options.sneakKey.isPressed())
                {
                    event.setY(event.getY() - 0.12);
                    Managers.MOVEMENT.setMotionY(event.getY() - 0.12);
                }
            }
            return;
        }
        float speed = mc.player.isSubmergedIn(FluidTags.WATER) ? waterSpeedConfig.getValue() : lavaSpeedConfig.getValue();
        switch (modeConfig.getValue())
        {
            case VANILLA ->
            {
                event.setX(event.getX() * speed);
                event.setZ(event.getZ() * speed);
            }
            case NORMAL ->
            {
                Vec2f strafe = SpeedModule.getInstance().handleStrafeMotion(speed / 10.0f);
                event.setX(strafe.x);
                event.setZ(strafe.y);
            }
        }
        if (verticalConfig.getValue())
        {
            if (mc.options.jumpKey.isPressed())
            {
                event.setY(event.getY() + 0.16);
                Managers.MOVEMENT.setMotionY(event.getY() + 0.16);
            }
            else if (mc.options.sneakKey.isPressed())
            {
                event.setY(event.getY() - 0.12);
                Managers.MOVEMENT.setMotionY(event.getY() - 0.12);
            }
        }
    }

    private enum SwimMode
    {
        VANILLA,
        NORMAL
    }
}
