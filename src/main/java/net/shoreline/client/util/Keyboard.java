package net.shoreline.client.util;

import lombok.experimental.UtilityClass;
import org.lwjgl.glfw.GLFW;

@UtilityClass
public class Keyboard
{
    public String getKeyName(int keycode, int scancode)
    {
        return switch (keycode)
        {
            case GLFW.GLFW_KEY_UNKNOWN -> "NONE";
            case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RSHIFT";
            case GLFW.GLFW_KEY_LEFT_SHIFT -> "LSHIFT";
            case GLFW.GLFW_KEY_SPACE -> "SPACE";
            case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCONTROL";
            case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCONTROL";
            case GLFW.GLFW_KEY_LEFT_ALT -> "LALT";
            case GLFW.GLFW_KEY_RIGHT_ALT -> "RALT";
            case GLFW.GLFW_KEY_ENTER -> "ENTER";
            case GLFW.GLFW_KEY_TAB -> "TAB";
            case GLFW.GLFW_KEY_BACKSPACE -> "BACKSPACE";
            case GLFW.GLFW_KEY_DELETE -> "DELETE";
            case GLFW.GLFW_KEY_INSERT -> "INSERT";
            case GLFW.GLFW_KEY_CAPS_LOCK -> "CAPS_LOCK";
            case GLFW.GLFW_KEY_PAGE_UP -> "PAGE_UP";
            case GLFW.GLFW_KEY_PAGE_DOWN -> "PAGE_DOWN";
            case GLFW.GLFW_KEY_HOME -> "HOME";
            case GLFW.GLFW_KEY_END -> "END";
            case GLFW.GLFW_KEY_LEFT -> "LEFT";
            case GLFW.GLFW_KEY_RIGHT -> "RIGHT";
            case GLFW.GLFW_KEY_UP -> "UP";
            case GLFW.GLFW_KEY_DOWN -> "DOWN";

            // Mouse Buttons
            case GLFW.GLFW_KEY_LAST + 1 -> "MOUSE0"; // GLFW_MOUSE_BUTTON_1
            case GLFW.GLFW_KEY_LAST + 2 -> "MOUSE1"; // GLFW_MOUSE_BUTTON_2
            case GLFW.GLFW_KEY_LAST + 3 -> "MOUSE2"; // GLFW_MOUSE_BUTTON_3
            case GLFW.GLFW_KEY_LAST + 4 -> "MOUSE3"; // GLFW_MOUSE_BUTTON_4
            case GLFW.GLFW_KEY_LAST + 5 -> "MOUSE4"; // GLFW_MOUSE_BUTTON_5
            case GLFW.GLFW_KEY_LAST + 6 -> "MOUSE5"; // GLFW_MOUSE_BUTTON_6
            case GLFW.GLFW_KEY_LAST + 7 -> "MOUSE6"; // GLFW_MOUSE_BUTTON_7
            case GLFW.GLFW_KEY_LAST + 8 -> "MOUSE7"; // GLFW_MOUSE_BUTTON_8
            default -> GLFW.glfwGetKeyName(keycode, scancode);
        };
    }

    public String getKeyName(int keycode)
    {
        return getKeyName(keycode, keycode > 0 && keycode < GLFW.GLFW_KEY_LAST ? GLFW.glfwGetKeyScancode(keycode) : 0);
    }

    /**
     * @param key
     * @return
     */
    public int getKeyCode(String key)
    {
        if (key.equalsIgnoreCase("NONE"))
        {
            return GLFW.GLFW_KEY_UNKNOWN;
        }
        // Keyboard Keys
        for (int i = 32; i < 97; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i))))
            {
                return i;
            }
        }
        for (int i = 256; i < 349; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i))))
            {
                return i;
            }
        }
        // Mouse Buttons
        for (int i = GLFW.GLFW_KEY_LAST; i < GLFW.GLFW_KEY_LAST + 9; i++)
        {
            if (key.equalsIgnoreCase(getKeyName(i, GLFW.glfwGetKeyScancode(i))))
            {
                return i;
            }
        }
        return GLFW.GLFW_KEY_UNKNOWN;
    }
}
