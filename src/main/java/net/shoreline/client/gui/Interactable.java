package net.shoreline.client.gui;

public interface Interactable
{

    void mouseClicked(double mouseX,
                      double mouseY,
                      int mouseButton);

    void mouseReleased(double mouseX,
                       double mouseY,
                       int button);

    void mouseScrolled(double mouseX,
                       double mouseY,
                       double horizontalAmount,
                       double verticalAmount);

    void keyPressed(int keyCode,
                    int scanCode,
                    int modifiers);

    void charTyped(char chr,
                   int modifiers);
}
