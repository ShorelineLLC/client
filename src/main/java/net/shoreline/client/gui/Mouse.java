package net.shoreline.client.gui;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Mouse
{
    private float mouseX, mouseY;
    private boolean rightClicked, rightHeld;
    private boolean leftClicked, leftHeld;

    public boolean isHovering(float x1, float y1, float x2, float y2)
    {
        return isHovering(mouseX, mouseY, x1, y1, x2, y2);
    }

    public static boolean isHovering(double mouseX,
                                     double mouseY,
                                     float x1,
                                     float y1,
                                     float x2,
                                     float y2)
    {
        return mouseX >= x1 && mouseX <= x1 + x2 && mouseY >= y1 && mouseY <= y1 + y2;
    }
}
