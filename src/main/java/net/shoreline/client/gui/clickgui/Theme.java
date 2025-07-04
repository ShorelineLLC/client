package net.shoreline.client.gui.clickgui;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Builder
public class Theme
{
    private final int titleColor;
    private final int backgroundColor;
    private final int outlineColor;
    private final int componentColor;
    private final int textColor;
}
