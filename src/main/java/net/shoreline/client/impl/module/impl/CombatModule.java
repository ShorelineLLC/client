package net.shoreline.client.impl.module.impl;

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.ListeningToggleable;
import net.shoreline.client.mixin.network.packet.c2s.AccessorPlayerInteractC2SPacket;

public class CombatModule extends TargetingModule
{
    public CombatModule(String name, String description, GuiCategory category) {
        super(name, description, category);
    }

    public CombatModule(final String name,
                        final String[] nameAliases,
                        final String description,
                        final GuiCategory category)
    {
        super(name, nameAliases, description, category);
    }

    public void sendAttackPackets(final Entity entity,
                                  final boolean swing)
    {
        sendAttackPacketsInternal(entity.getId(), swing, Hand.MAIN_HAND);
    }

    public void sendAttackPacketsInternal(final int entityId,
                                          final boolean swing,
                                          final Hand hand)
    {
        boolean sneaking = mc.player.isSneaking();
        sendPacket(AccessorPlayerInteractC2SPacket.invokeInit(
                        entityId,
                        sneaking,
                        PlayerInteractEntityC2SPacket.ATTACK)
        );

        if (swing)
        {
            mc.player.swingHand(hand);
        } else
        {
            sendPacket(new HandSwingC2SPacket(hand));
        }
    }
}
