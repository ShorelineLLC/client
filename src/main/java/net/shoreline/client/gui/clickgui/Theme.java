package net.shoreline.client.gui.clickgui;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
public class Theme
{
    private int titleColor;
    private int backgroundColor;
    private int outlineColor;
    private int componentColor;
    private int textColor;

    public Theme(int titleColor,
                 int backgroundColor,
                 int outlineColor,
                 int componentColor,
                 int textColor)
    {
        this.titleColor = titleColor;
        this.backgroundColor = backgroundColor;
        this.outlineColor = outlineColor;
        this.componentColor = componentColor;
        this.textColor = textColor;
    }
}
