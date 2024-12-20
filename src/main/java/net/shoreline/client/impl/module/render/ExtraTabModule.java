package net.shoreline.client.impl.module.render;

import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.setting.BooleanConfig;
import net.shoreline.client.api.config.setting.NumberConfig;
import net.shoreline.client.api.module.ModuleCategory;
import net.shoreline.client.api.module.ToggleModule;
import net.shoreline.client.api.render.RenderManager;
import net.shoreline.client.impl.event.gui.hud.PlayerListColumnsEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListIconEvent;
import net.shoreline.client.impl.event.gui.hud.PlayerListNameEvent;
import net.shoreline.client.impl.irc.IRCManager;
import net.shoreline.client.impl.irc.user.OnlineUser;
import net.shoreline.client.impl.module.client.SocialsModule;
import net.shoreline.client.init.Managers;
import net.shoreline.eventbus.annotation.EventListener;

/**
 * @author hockeyl8, linus
 * @since 1.0
 */
public class ExtraTabModule extends ToggleModule
{

    Config<Integer> sizeConfig = register(new NumberConfig<>("Size", "The number of players to show", 80, 200, 1000));
    Config<Integer> columnsConfig = register(new NumberConfig<>("Columns", "The number columns to show.", 1, 20, 100));
    Config<Boolean> selfConfig = register(new BooleanConfig("Self", "Highlights yourself in the tab list.", false));
    Config<Boolean> friendsConfig = register(new BooleanConfig("Friends", "Highlights friends in the tab list.", true));

    public ExtraTabModule()
    {
        super("ExtraTab", "Expands the tab list size to allow for more players", ModuleCategory.RENDER);
    }

    @EventListener
    public void onPlayerListName(PlayerListNameEvent event)
    {
        String[] names = event.getPlayerName().getString().split(" ");
        if (selfConfig.getValue())
        {
            for (String s : names)
            {
                String name1 = stripControlCodes(s);
                if (name1.equals(mc.getGameProfile().getName()))
                {
                    event.cancel();
                    event.setPlayerName(Text.of(("§s" + event.getPlayerName().getString())));
                    return;
                }
            }
        }
        if (friendsConfig.getValue() && SocialsModule.getInstance().isFriendsEnabled())
        {
            for (String s : names)
            {
                String name1 = stripControlCodes(s);
                if (Managers.SOCIAL.isFriend(name1))
                {
                    event.cancel();
                    event.setPlayerName(Text.of("§g" + event.getPlayerName().getString()));
                    break;
                }
            }
        }
    }

    @EventListener
    public void onPlayerListIcon(PlayerListIconEvent.Width event)
    {
        String[] names = event.getText().split(" ");
        for (String name : names)
        {
            String name1 = stripControlCodes(name);
            OnlineUser onlineUser = IRCManager.getInstance().findOnlineUser(name1);
            if (onlineUser != null)
            {
                event.cancel();
                break;
            }
        }
    }

    @EventListener
    public void onPlayerListIconRender(PlayerListIconEvent.Render event)
    {
        String[] names = event.getPlayerNameText().getString().split(" ");
        for (String name : names)
        {
            // String name1 = AWTFontRenderer.stripControlCodes(name);
            String name1 = stripControlCodes(name);
            OnlineUser onlineUser = IRCManager.getInstance().findOnlineUser(name1);
            if (onlineUser != null)
            {
                event.cancel();
                Identifier identifier = NametagsModule.getInstance().getNametagLogo(onlineUser.getUsertype());
                event.getMatrixStack().push();
                RenderManager.rectTextured(event.getMatrixStack(), identifier, (float) event.getX() + 1.0f, (float) event.getX() + 9.0f, (float) event.getY(), (float) event.getY() + 8.0f,
                        0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f);
                event.getMatrixStack().pop();
                break;
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

    private String stripControlCodes(String string)
    {
        StringBuilder builder = new StringBuilder();
        boolean skip = false;
        for (char c : string.toCharArray())
        {
            if (c == Formatting.FORMATTING_CODE_PREFIX)
            {
                skip = true;
                continue;
            }
            if (skip)
            {
                skip = false;
                continue;
            }
            builder.append(c);
        }
        return builder.toString();
    }
}
