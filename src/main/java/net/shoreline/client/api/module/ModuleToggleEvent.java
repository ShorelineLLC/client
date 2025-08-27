package net.shoreline.client.api.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.shoreline.eventbus.Event;

@RequiredArgsConstructor
@Getter
public class ModuleToggleEvent extends Event
{
    private final Toggleable module;
    private final boolean enabled;
}
