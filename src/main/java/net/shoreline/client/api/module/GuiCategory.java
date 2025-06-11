package net.shoreline.client.api.module;

import lombok.Getter;

public enum GuiCategory
{
    COMBAT("Combat"),
    EXPLOITS("Exploits"),
    MISCELLANEOUS("Miscellaneous"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    WORLD("World"),
    CLIENT("Client");

    @Getter
    private final String name;

    GuiCategory(String name)
    {
        this.name = name;
    }
}
