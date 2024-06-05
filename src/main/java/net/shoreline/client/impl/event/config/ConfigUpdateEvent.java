package net.shoreline.client.impl.event.config;

import net.shoreline.client.api.config.Config;
import net.shoreline.eventbus.event.StageEvent;

/**
 * @author linus
 * @since 1.0
 */
public class ConfigUpdateEvent extends StageEvent
{
    //
    private final Config<?> config;

    /**
     * @param config
     */
    public ConfigUpdateEvent(Config<?> config)
    {
        this.config = config;
    }

    /**
     * @return
     */
    public Config<?> getConfig()
    {
        return config;
    }
}
