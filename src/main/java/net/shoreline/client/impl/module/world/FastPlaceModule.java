package net.shoreline.client.impl.module.world;

import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.PacketEvent;
import net.shoreline.client.impl.imixin.IMinecraftClient;
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
        if (checkNull())
        {
            return;
        }

        if (mc.options.useKey.isPressed() && checkItem(mc.player.getMainHandStack())
                && ((IMinecraftClient) mc).getItemUseCooldown() > delayConfig.getValue())
        {
            ((IMinecraftClient) mc).setItemUseCooldown(delayConfig.getValue());
        }
    }

    @EventListener
    public void onPacketOutbound(PacketEvent.Outbound event)
    {
        if (checkNull() || wasSentFromClient(event.getPacket()))
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
