package net.shoreline.client.impl.movement;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.network.packet.s2c.play.OpenScreenS2CPacket;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.math.NanoTimer;
import net.shoreline.client.api.math.Timer;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.InteractSneakEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.module.client.LatencyModule;
import net.shoreline.client.impl.module.client.RotationsModule;
import net.shoreline.client.impl.module.client.RotationsModule.MoveFix;
import net.shoreline.client.impl.network.NetworkHandler;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class MovementManager extends NetworkHandler
{
    private final RotationsModule rotationConfig = RotationsModule.INSTANCE;

    private boolean sneaking;

    public MovementManager()
    {
        super("Movement");
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onInteractSneak(InteractSneakEvent event)
    {
        if (sneaking)
        {
            event.cancel();
        }
    }

    public void setSilentSneaking(boolean sneaking)
    {
        if (sneaking)
        {
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
        }
        else
        {
            sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
        }
        
        /* this.sneaking = sneaking;

        PlayerInput playerInput = InputUtil.inputSneaking(mc.player.input.playerInput, true);
        if (sneaking && rotationConfig.getMoveFixConfig().getValue() != MoveFix.OFF)
        {
            int swiftSneak = EnchantUtil.getLevel(Enchantments.SWIFT_SNEAK, mc.player.getEquippedStack(EquipmentSlot.FEET));
            float modifier = MathHelper.clamp(0.3f + swiftSneak * 0.15f, 0.0f, 1.0f);
            Vec2f modified = mc.player.input.getMovementInput().multiply(modifier);
            mc.player.input.movementForward = modified.x;
            mc.player.input.movementSideways = modified.y;
        }

        sendPacket(new PlayerInputC2SPacket(playerInput)); */
    }
}