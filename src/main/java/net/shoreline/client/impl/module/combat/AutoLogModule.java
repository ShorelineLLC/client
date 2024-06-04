package net.shoreline.client.impl.module.combat;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.text.Text;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.TickEvent;
import net.shoreline.client.init.Managers;
import net.shoreline.client.util.world.FakePlayerEntity;
import net.shoreline.eventbus.StageEvent;
import net.shoreline.eventbus.annotation.EventListener;

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
    Config<Boolean> illegalDisconnectConfig = register(new BooleanConfig("IllegalDisconnect", "Disconnects from the server using invalid packets", false));

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
        int totems = Managers.INVENTORY.count(Items.TOTEM_OF_UNDYING);
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

    /**
     * @param disconnectReason
     * @param args
     */
    private void playerDisconnect(String disconnectReason, Object... args)
    {
        if (illegalDisconnectConfig.getValue())
        {
            Managers.NETWORK.sendPacket(PlayerInteractEntityC2SPacket.attack(mc.player, false)); // Illegal packet
            disable();
            return;
        }
        if (mc.getNetworkHandler() == null)
        {
            mc.world.disconnect();
            disable();
            return;
        }
        disconnectReason = String.format(disconnectReason, args);
        mc.getNetworkHandler().getConnection().disconnect(Text.of(disconnectReason));
        disable();
    }

    private boolean checkEnemy(AbstractClientPlayerEntity player)
    {
        return player != mc.player && !Managers.SOCIAL.isFriend(player.getName()) && !(player instanceof FakePlayerEntity);
    }
}
