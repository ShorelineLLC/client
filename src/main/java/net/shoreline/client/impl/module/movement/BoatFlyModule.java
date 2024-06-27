package net.shoreline.client.impl.module.movement;

import net.minecraft.block.AbstractBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
import net.minecraft.util.math.MathHelper;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.EnumConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.entity.EntityTravelEvent;
import net.shoreline.client.impl.event.entity.player.TravelEvent;
import net.shoreline.client.impl.event.network.DismountVehicleEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.exploit.DisablerModule;
import net.shoreline.client.mixin.accessor.AccessorPlayerMoveC2SPacket;
import net.shoreline.client.util.player.MovementUtil;
import net.shoreline.eventbus.annotation.EventListener;

public class BoatFlyModule extends ToggleModule
{
    Config<Float> speedConfig = register(new NumberConfig<>("Speed", "The horizontal flight speed", 0.1f, 2.5f, 10.0f));
    Config<Float> vspeedConfig = register(new NumberConfig<>("VerticalSpeed", "The vertical flight speed", 0.1f, 1.0f, 5.0f));
    Config<Boolean> remountConfig = register(new BooleanConfig("Remount", "Automatically remounts", false));
    Config<AntiKick> antiKickConfig = register(new EnumConfig<>("AntiKick", "Prevents vanilla flight detection", AntiKick.NORMAL, AntiKick.values()));
    Config<Boolean> accelerateConfig = register(new BooleanConfig("Accelerate", "Accelerate as you fly", false));
    Config<Float> accelerateSpeedConfig = register(new NumberConfig<>("AccelerateSpeed", "Speed to accelerate as", 0.01f, 0.2f, 1.0f, () -> accelerateConfig.getValue()));
    Config<Float> maxSpeedConfig = register(new NumberConfig<>("MaxSpeed", "Max speed to accelerate to", 1.0f, 5.0f, 10.0f, () -> accelerateConfig.getValue()));

    private double speed;
    // antikick bs
    private double lastY;
    private boolean floating;
    private int floatingTicks;
    private boolean modifyY;

    public BoatFlyModule()
    {
        super("BoatFly", "Allows you fly with entities", ModuleCategory.MOVEMENT);
    }

    @Override
    public void onEnable()
    {
        speed = 0.0;
    }

    @Override
    public void onDisable()
    {
        modifyY = false;
    }

    @EventListener
    public void onTravel(EntityTravelEvent event)
    {
        if (mc.player.getVehicle() != null && event.getEntity().getId() == mc.player.getVehicle().getId())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onDismountVehicle(DismountVehicleEvent event)
    {
        if (mc.options.sneakKey.isPressed())
        {
            event.cancel();
        }
    }

    @EventListener
    public void onPlayerTick(TravelEvent event)
    {
        if (mc.player.getVehicle() == null || DisablerModule.getInstance().grimFireworkCheck())
        {
            return;
        }
        Entity ridingEntity = mc.player.getVehicle();
        if (accelerateConfig.getValue())
        {
            if (!MovementUtil.isInputtingMovement() || ridingEntity.horizontalCollision)
            {
                speed = 0.0f;
            }
            speed += accelerateSpeedConfig.getValue();
            if (speed > maxSpeedConfig.getValue())
            {
                speed = maxSpeedConfig.getValue();
            }
        }
        else
        {
            speed = speedConfig.getValue();
        }
        ridingEntity.setYaw(mc.player.getYaw());
        if (ridingEntity instanceof LlamaEntity llama)
        {
            llama.headYaw = mc.player.getYaw();
        }

        boolean stopVerticalMovement = false;
        if (floating)
        {
            floatingTicks++;
            if (floatingTicks >= 20)
            {
                if (antiKickConfig.getValue() == AntiKick.PACKET)
                {
                    modifyY = true;
                }
                else if (antiKickConfig.getValue() == AntiKick.NORMAL)
                {
                    ridingEntity.setPosition(ridingEntity.getX(), ridingEntity.getY() - 0.0313, ridingEntity.getZ());
                }
                floatingTicks = 0;
                floating = false;
                stopVerticalMovement = true;
            }
        }
        ridingEntity.setVelocity(ridingEntity.getVelocity().x, 0.0, ridingEntity.getVelocity().z);
        if (mc.options.jumpKey.isPressed() && !stopVerticalMovement)
        {
            ridingEntity.setVelocity(ridingEntity.getVelocity().x, vspeedConfig.getValue(), ridingEntity.getVelocity().z);
        }
        else if (mc.options.sneakKey.isPressed())
        {
            ridingEntity.setVelocity(ridingEntity.getVelocity().x, -vspeedConfig.getValue(), ridingEntity.getVelocity().z);
        }
        speed = Math.max(speed, 0.2873f);
        float forward = mc.player.input.movementForward;
        float strafe = mc.player.input.movementSideways;
        float yaw = mc.player.getYaw();
        if (forward == 0.0f && strafe == 0.0f)
        {
            // ridingEntity.setVelocity(0.0f, ridingEntity.getVelocity().y, 0.0f);
            return;
        }
        double rx = Math.cos(Math.toRadians(yaw + 90.0f));
        double rz = Math.sin(Math.toRadians(yaw + 90.0f));
        ridingEntity.setVelocity((forward * speed * rx) + (strafe * speed * rz),
                ridingEntity.getVelocity().y, (forward * speed * rz) - (strafe * speed * rx));
        event.cancel();
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (mc.player == null || mc.player.getVehicle() == null)
        {
            return;
        }
        Entity ridingEntity = mc.player.getVehicle();
        if (event.getPacket() instanceof VehicleMoveC2SPacket packet && antiKickConfig.getValue() != AntiKick.OFF)
        {
            double packetY = packet.getY();
            // Vanilla fly kick checks every 80 ticks
            if (modifyY)
            {
                ((AccessorPlayerMoveC2SPacket) packet).hookSetY(lastY - 0.04);
                modifyY = false;
            }
            else
            {
                floating = floatingCheck(packet, ridingEntity);
                lastY = packetY;
            }
        }
        if (event.getPacket() instanceof ClientCommandC2SPacket packet && packet.getMode() == ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY
                && mc.player.getVehicle() != null && !mc.player.getVehicle().isOnGround())
        {
            event.cancel();
        }
    }

    private boolean floatingCheck(VehicleMoveC2SPacket packet, Entity ridingEntity)
    {
        double e = MathHelper.clamp(packet.getY(), -2.0E7, 2.0E7);
        double s = e - lastY;
        return s >= -0.03125 && !ridingEntity.groundCollision && isEntityOnAir(ridingEntity);
    }

    private boolean isEntityOnAir(Entity entity)
    {
        return entity.getWorld().getStatesInBox(entity.getBoundingBox().expand(0.0625).stretch(0.0, -0.55, 0.0)).allMatch(AbstractBlock.AbstractBlockState::isAir);
    }

    public enum AntiKick
    {
        NORMAL,
        PACKET,
        OFF
    }
}
