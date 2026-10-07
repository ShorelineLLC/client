package net.shoreline.client.api.font;

import lombok.Getter;

@Getter
public enum Fonts
{
    VERDANA("Verdana", 9.5f),
    ARIAL("Arial", 10.0f),
    HELVETICA("Helvetica", 10.0f),
    DIALOG("Dialog", 9.5f);

    private final String name;
    private final float size;

    Fonts(String name, float size)
    {
        this.name = name;
        this.size = size;
    }
}
