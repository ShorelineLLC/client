package net.shoreline.client.api.config.setting;

import net.shoreline.client.api.config.Config;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.init.Managers;

public class MacroConfig extends Config<Macro> {
    public MacroConfig(String name, String desc, Macro val) {
        super(name, desc, val);
    }

    /**
     * Overloaded method {@link Config#setValue(Object)}. Sets value by
     * instantiating new {@link Macro} based on method parameters.
     *
     * @param keycode The macro keycode
     */
    public void setValue(int keycode) {
        getValue().setKeycode(keycode);
        if (Managers.isInitialized()) {
            Managers.MACRO.setMacro(getValue(), keycode);
        }
    }

    public String getMacroId() {
        return value.getName();
    }

    public Runnable getRunnable() {
        return value.getRunnable();
    }

    public int getKeycode() {
        return value.getKeycode();
    }

    public String getKeyName() {
        return value.getKeyName();
    }
}
