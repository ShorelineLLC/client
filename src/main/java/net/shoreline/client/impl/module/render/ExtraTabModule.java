package net.shoreline.client.impl.module.render;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.eventbus.annotation.EventListener;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.impl.event.gui.hud.PlayerListColumnsEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListNameEvent;
import net.shoreline.client.init.Managers;

/**
 * @author hockeyl8, linus
 * @since 1.0
 */
public class ExtraTabModule extends ToggleModule {

    Config<Integer> sizeConfig = register(new NumberConfig<>("Size", "The number of players to show", 80, 200, 1000));
    Config<Integer> columnsConfig = register(new NumberConfig<>("Columns", "The number columns to show.", 1, 20, 100));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Highlights yourself in the tab list.", false));
    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Highlights friends in the tab list.", true));

    public ExtraTabModule() {
        super("ExtraTab", "Expands the tab list size to allow for more players", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerListName(PlayerListNameEvent event)
    {
        if (selfConfig.getValue() && event.getPlayerName().getString().contains(mc.getGameProfile().getName()))
        {
            event.cancel();
            event.setPlayerName(Text.of(("§s" + event.getPlayerName().getString())));
        }
        else if (friendsConfig.getValue())
        {
            for (String s : Managers.SOCIAL.getFriends()) {
                if (event.getPlayerName().getString().contains(s)) {
                    event.cancel();
                    event.setPlayerName(Text.of(Formatting.AQUA + event.getPlayerName().getString()));
                    break;
                }
            }
        }
    }

    @EventListener
    public void onPlayerList(PlayerListEvent event)
    {
        event.cancel();
        event.setSize(sizeConfig.getValue());
    }

    @EventListener
    public void onPlayerListColumns(PlayerListColumnsEvent event)
    {
        event.cancel();
        event.setTabHeight(columnsConfig.getValue());
    }
}
