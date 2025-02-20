package net.shoreline.client.impl.module.combat;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.encryption.NetworkEncryptionUtils;
import net.minecraft.network.message.LastSeenMessageList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.impl.event.network.GameJoinEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.entity.FakePlayerEntity;
import net.shoreline.client.util.math.timer.CacheTimer;
import net.shoreline.client.util.math.timer.Timer;
import net.shoreline.client.util.player.InventoryUtil;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.eventbus.event.StageEvent;

import java.time.Instant;
import java.util.BitSet;

/**
 * @author linus
 * @since 1.0
 */
public class AutoLogModule extends ToggleModule
{
    //
    Config<Float> healthConfig = register(new NumberConfig<>("Health", "Disconnects when player reaches this health", 0.1f, 5.0f, 19.0f));
    Config<Boolean> healthTotemConfig = register(new BooleanConfig("HealthTotems", "Totem check for health config", true));
    Config<Boolean> onRenderConfig = register(new BooleanConfig("OnRender", "Disconnects when a player enters render distance", false));
    Config<Boolean> noTotemConfig = register(new BooleanConfig("NoTotems", "Disconnects when player has no totems in the inventory", false));
    Config<Integer> totemsConfig = register(new NumberConfig<>("Totems", "The number of totems before disconnecting", 0, 1, 5));
    Config<Boolean> invincibilityConfig = register(new BooleanConfig("SpawnInvincibility", "Accounts for spawn invincibility for logout", false));
    Config<Float> invincibilityTimeConfig = register(new NumberConfig<>("InvincibilityTime", "The spawn invincibility time ", 0.1f, 2.9f, 5.0f, () -> invincibilityConfig.getValue()));
    Config<Boolean> illegalDisconnectConfig = register(new BooleanConfig("IllegalDisconnect", "Disconnects from the server using invalid packets", false));
    Config<Boolean> autoDisableConfig = register(new BooleanConfig("AutoDisable", "Automatically disables", true));

    private final Timer invincibilityTimer = new CacheTimer();

    /**
     *
     */
    public AutoLogModule()
    {
        super("AutoLog", "Automatically disconnects from server during combat", ModuleCategory.COMBAT);
    }

    @EventListener
    public void onTick(TickEvent event)
    {
        if (event.getStage() != StageEvent.EventStage.PRE)
        {
            return;
        }
        if (invincibilityConfig.getValue() && !invincibilityTimer.passed(invincibilityTimeConfig.getValue() * 1000))
        {
            return;
        }
        if (onRenderConfig.getValue())
        {
            AbstractClientPlayerEntity player = mc.world.getPlayers().stream()
                    .filter(p -> checkEnemy(p)).findFirst().orElse(null);
            if (player != null)
            {
                playerDisconnect("[AutoLog] %s came into render distance.", player.getName().getString());
                return;
            }
        }
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        int totems = InventoryUtil.count(Items.TOTEM_OF_UNDYING);
        boolean b2 = totems <= totemsConfig.getValue();
        if (health <= healthConfig.getValue())
        {
            if (!healthTotemConfig.getValue())
            {
                playerDisconnect("[AutoLog] logged out with %d hearts remaining.", (int) health);
                return;
            }
            else if (b2)
            {
                playerDisconnect("[AutoLog] logged out with %d totems and %d hearts remaining.", totems, (int) health);
                return;
            }
        }
        if (b2 && noTotemConfig.getValue())
        {
            playerDisconnect("[AutoLog] logged out with %d totems remaining.", totems);
        }
    }

    @EventListener
    public void onGameJoin(GameJoinEvent event)
    {
        invincibilityTimer.reset();
    }

    /**
     * @param disconnectReason
     * @param args
     */
    private void playerDisconnect(String disconnectReason, Object... args)
    {
        if (illegalDisconnectConfig.getValue())
        {
            Managers.NETWORK.sendPacket(new ChatMessageC2SPacket(
                    "§",
                    Instant.now(),
                    NetworkEncryptionUtils.SecureRandomUtil.nextLong(),
                    null,
                    new LastSeenMessageList.Acknowledgment(1, new BitSet(2)))); // Illegal packet
            if (autoDisableConfig.getValue())
            {
                disable();
            }
            return;
        }
        if (mc.getNetworkHandler() == null)
        {
            mc.world.disconnect();
            if (autoDisableConfig.getValue())
            {
                disable();
            }

            return;
        }
        disconnectReason = String.format(disconnectReason, args);
        mc.getNetworkHandler().getConnection().disconnect(Text.of(disconnectReason));
        if (autoDisableConfig.getValue())
        {
            disable();
        }
    }

    private boolean checkEnemy(AbstractClientPlayerEntity player)
    {
        return player != mc.player && !Managers.SOCIAL.isFriend(player.getName()) && !(player instanceof FakePlayerEntity);
    }
}
