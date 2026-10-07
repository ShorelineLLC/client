package net.shoreline.client.api.module;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum GuiCategory
{
    COMBAT("Combat"),
    EXPLOIT("Exploit"),
    MISCELLANEOUS("Miscellaneous"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    CLIENT("Client"),
    HUD("HUD");

    private final String name;
}
