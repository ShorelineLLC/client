package net.shoreline.client.impl.command.util.impl;

import net.shoreline.client.api.config.MacroConfig;
import net.shoreline.client.api.macro.Macro;
import net.shoreline.client.impl.command.util.IConfigParser;
import net.shoreline.client.util.Keyboard;

public class MacroConfigParser implements IConfigParser<Macro, MacroConfig>
{
    @Override
    public boolean parseString(MacroConfig config, String string)
    {
        int key = Keyboard.getKeyCode(string);
        Macro macro = config.getValue();
        macro.setKeycode(key);
        return true;
    }
}