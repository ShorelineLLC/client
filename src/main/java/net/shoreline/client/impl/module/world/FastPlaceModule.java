package net.shoreline.client.impl.module.world;

import net.minecraft.block.BlockState;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.mixin.accessor.AccessorMinecraftClient;
import net.shoreline.eventbus.annotation.EventListener;

public class FastPlaceModule extends Toggleable
{
    Config<Integer> delayConfig = new NumberConfig.Builder<Integer>("Delay")
            .setMin(0).setMax(4).setDefaultValue(1)
            .setDescription("The click delay of placements").build();
    Config<Boolean> ghostFixConfig = new BooleanConfig.Builder("GhostFix")
            .setDescription("Fixes items ghosting on Paper servers")
            .setDefaultValue(false).build();

    public FastPlaceModule()
    {
        super("FastPlace", "Place blocks and items faster", GuiCategory.WORLD);
    }

    @EventListener
    public void onTick(TickEvent.Pre event)
    {
        if (mc.options.useKey.isPressed() && checkItem(mc.player.getMainHandStack())
                && ((AccessorMinecraftClient) mc).getItemUseCooldown() > delayConfig.getValue())
        {
            if (ghostFixConfig.getValue())
            {
                Managers.NETWORK.sendSequencedPacket(id -> new PlayerInteractItemC2SPacket(
                        mc.player.getActiveHand(), id, mc.player.getYaw(), mc.player.getPitch()));
            }
            ((AccessorMinecraftClient) mc).setItemUseCooldown(delayConfig.getValue());
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull() || Managers.NETWORK.wasSentFromClient(event.getPacket()))
        {
            return;
        }

        if (event.getPacket() instanceof PlayerInteractBlockC2SPacket packet
                && ghostFixConfig.getValue() && checkItem(mc.player.getStackInHand(packet.getHand())))
        {
            event.cancel();
        }
    }

    private boolean checkItem(ItemStack stack)
    {
        return stack.getItem() instanceof ExperienceBottleItem;
    }
}
