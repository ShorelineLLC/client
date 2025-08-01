package net.shoreline.client.impl.movement;

import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec2f;
import net.shoreline.client.api.GenericFeature;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.network.InteractSneakEvent;
import net.shoreline.client.impl.module.client.AnticheatModule;
import net.shoreline.client.mixin.accessor.AccessorInput;
import net.shoreline.client.util.input.InputUtil;
import net.shoreline.client.util.item.EnchantUtil;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

public class MovementManager extends GenericFeature
{
    private final AnticheatModule anticheat = AnticheatModule.INSTANCE;

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

    public void setSilentSneaking(PlayerInput playerInput, boolean sneaking)
    {
        this.sneaking = sneaking;

        if (sneaking)
        {
            playerInput = InputUtil.inputSneaking(playerInput, true);

            if (anticheat.shouldApplyMoveFix())
            {
                float modifier = MathHelper.clamp(0.3f + (EnchantUtil.getLevel(Enchantments.SWIFT_SNEAK,
                        mc.player.getEquippedStack(EquipmentSlot.FEET)) * 0.15F), 0.0f, 1.0f);
                Vec2f modified = mc.player.input.getMovementInput().multiply(modifier);
                mc.player.input.movementForward = modified.x;
                mc.player.input.movementSideways = modified.y;
            }
        }

        Managers.NETWORK.sendPacket(new PlayerInputC2SPacket(playerInput));
    }
}
