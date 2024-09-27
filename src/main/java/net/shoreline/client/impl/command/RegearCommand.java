package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.module.combat.AutoRegearModule;
import net.shoreline.client.util.chat.ChatUtil;

public class RegearCommand extends Command
{

    public RegearCommand()
    {
        super("Regear", "Saves config for AutoRegear", literal("regear"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("save/clear", StringArgumentType.string())
                .suggests(suggest("save", "clear"))
                .executes(c ->
                {
                    String string = StringArgumentType.getString(c, "save/clear");
                    if (string.equalsIgnoreCase("save"))
                    {
                        AutoRegearModule.getInstance().savePlayerInventory();
                        ChatUtil.clientSendMessage("Saved current regear inventory!");
                        return 1;
                    }
                    else if (string.equalsIgnoreCase("clear"))
                    {
                        AutoRegearModule.getInstance().clearPlayerInventory();
                        ChatUtil.clientSendMessage("Cleared current regear inventory!");
                        return 1;
                    }
                    return 0;
                })).executes(c ->
        {
            ChatUtil.error("Invalid arguments!");
            return 0;
        });
    }
}
