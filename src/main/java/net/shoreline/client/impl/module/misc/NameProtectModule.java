package net.shoreline.client.impl.module.misc;

import lombok.Getter;
import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.config.StringConfig;
import net.shoreline.client.api.module.GuiCategory;
import net.shoreline.client.api.module.Toggleable;
import net.shoreline.client.impl.event.text.TextVisitedEvent;
import net.shoreline.eventbus.annotation.EventListener;

@Getter
public class NameProtectModule extends Toggleable
{
    public static NameProtectModule INSTANCE;

    Config<String> aliasConfig = new StringConfig.Builder("Alias")
            .setDescription("The alias to replace your username with")
            .setDefaultValue("Player").build();

    public NameProtectModule()
    {
        super("NameProtect", "Censors your name", GuiCategory.MISCELLANEOUS);
        INSTANCE = this;
    }

    @EventListener
    public void onTextVisited(TextVisitedEvent event)
    {
        if (checkNull())
        {
            return;
        }

        String username = mc.getSession().getUsername();
        if (event.getText().contains(username))
        {
            String replaced = event.getText().replace(username, aliasConfig.getValue());
            event.cancel();
            event.setText(replaced);
        }
    }
}
