package net.shoreline.client.api.macro;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.shoreline.client.api.Identifiable;
import net.shoreline.client.api.config.Serializable;
import net.shoreline.client.impl.manager.client.MacroManager;
import net.shoreline.client.util.KeyboardUtil;
import org.lwjgl.glfw.GLFW;

/**
 * @author linus
 * @see MacroManager
 * @since 1.0
 */
public class Macro implements Identifiable, Serializable<Macro>
{
    //
    private final String name;
    // Runnable macro which represents the functionality of the keybind. This
    // code block will run when the key is pressed.
    private final Runnable macro;
    // The GLFW keycode which represents the macro keybind.
    private int keycode;

    /**
     * @param name
     * @param keycode
     * @param macro
     */
    public Macro(String name, int keycode, Runnable macro)
    {
        this.name = name;
        this.keycode = keycode;
        this.macro = macro;
    }

    /**
     * @see Runnable#run()
     */
    public void runMacro()
    {
        macro.run();
    }

    /**
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * @return
     */
    public Runnable getRunnable()
    {
        return macro;
    }

    /**
     * Returns the macro keybind represented as {@link GLFW} keycode integer
     * between <b>0 and 348</b>.
     *
     * @return The macro keycode
     * @see #keycode
     */
    public int getKeycode()
    {
        return keycode;
    }

    /**
     * @param keycode
     */
    public void setKeycode(int keycode)
    {
        this.keycode = keycode;
    }

    /**
     * @return
     */
    @Override
    public String getId()
    {
        return String.format("%s-macro", name.toLowerCase());
    }

    /**
     * Returns the name associated with the macro keycode. Equivalent to
     * {@link GLFW#glfwGetKeyName(int, int)} on {@link #keycode}.
     *
     * @return The macro key name
     * @see #keycode
     * @see GLFW#glfwGetKeyScancode(int)
     */
    public String getKeyName()
    {
        if (keycode != GLFW.GLFW_KEY_UNKNOWN)
        {
            final String name = KeyboardUtil.getKeyName(keycode);
            return name != null ? name.toUpperCase() : "NONE";
        }
        return "NONE";
    }

    @Override
    public JsonObject toJson()
    {
        JsonObject obj = new JsonObject();
        obj.addProperty("id", getId());
        // obj.addProperty("key", getKeyName());
        obj.addProperty("value", getKeycode());
        return obj;
    }

    @Override
    public Macro fromJson(JsonObject jsonObj)
    {
        if (jsonObj.has("value"))
        {
            JsonElement element = jsonObj.get("value");
            return new Macro(getId(), element.getAsInt(), getRunnable());
        }
        return null;
    }
}
