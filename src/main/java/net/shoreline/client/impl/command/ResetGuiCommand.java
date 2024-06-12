package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.gui.click.impl.config.CategoryFrame;
import net.shoreline.client.impl.module.client.ClickGuiModule;
import net.shoreline.client.util.chat.ChatUtil;

public class ResetGuiCommand extends Command
{
    public ResetGuiCommand()
    {
        super("ResetGui", "Resets gui frames", literal("resetgui"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.executes(c ->
        {
            float x = 15.0f;
            for (CategoryFrame frame : ClickGuiModule.CLICK_GUI_SCREEN.getCategoryFrames())
            {
                frame.setPos(x, 15.0f);
                x += frame.getWidth() + 2.0f;
            }
            ChatUtil.clientSendMessage("Reset clickgui screen");
            return 1;
        });
    }
}
