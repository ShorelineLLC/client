package net.shoreline.client.impl.module.combat;

import net.minecraft.item.Items;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.shoreline.client.api.config.BooleanConfig;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.NumberConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.Managers;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.inventory.InventoryUtil;
import net.shoreline.client.impl.module.combat.util.DamageUtil;
import net.shoreline.eventbus.annotation.EventListener;

import java.time.Instant;
import java.util.BitSet;

public class AutoDisconnectModule extends Toggleable
{
    Config<Float> healthConfig = new NumberConfig.Builder<Float>("Health")
            .setMin(0.0f).setMax(19.0f).setDefaultValue(5.0f)
            .setDescription("Min health before disconnecting").build();
    Config<Integer> totemsConfig = new NumberConfig.Builder<Integer>("Totems")
            .setMin(0).setMax(5).setDefaultValue(1)
            .setDescription("Min totems in inventory before disconnecting").build();
    Config<Float> invincibilityTime = new NumberConfig.Builder<Float>("SpawnInvincibility")
            .setMin(0.0f).setMax(10.0f).setDefaultValue(0.0f).setFormat("s")
            .setDescription("The invincibility time when logging into a server").build();
    Config<Boolean> illegalDisconnect = new BooleanConfig.Builder("IllegalDisconnect")
            .setDescription("Disconnects from the server by kicking you")
            .setDefaultValue(false).build();
    Config<Boolean> autoDisable = new BooleanConfig.Builder("AutoDisable")
            .setDescription("Disables after disconnecting")
            .setDefaultValue(false).build();

    public AutoDisconnectModule()
    {
        super("AutoDisconnect", "Tactical log", GuiCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent.Post event)
    {
        if (checkNull())
        {
            return;
        }

        int totems = InventoryUtil.getItemCount(Items.TOTEM_OF_UNDYING);
        if (totems > totemsConfig.getValue())
        {
            return;
        }

        float health = DamageUtil.getHealth(mc.player);
        if (health > healthConfig.getValue())
        {
            return;
        }

        String logMessage = String.format("[AutoDisconnect] disconnected with %d totems and %d hearts remaining.", totems, (int) health);
        disconnectFromServer(logMessage);
        if (autoDisable.getValue())
        {
            disable();
        }
    }

    private void disconnectFromServer(String disconnectReason)
    {
        if (!illegalDisconnect.getValue())
        {
            Managers.NETWORK.disconnect(disconnectReason);
            return;
        }

        sendPacket(new ChatMessageC2SPacket("§",
                Instant.now(),
                NetworkEncryptionUtils.SecureRandomUtil.nextLong(),
                null,
                new LastSeenMessageList.Acknowledgment(1, new BitSet(2))));
    }
}
