package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.util.Formatting;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.impl.Managers;

import java.util.Set;

public class KitCommand extends Command
{
    public KitCommand()
    {
        super("kit", "Manage client pvp kits");
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> argumentBuilder)
    {
        argumentBuilder.then(buildArgument("save/delete", StringArgumentType.string())
                .suggests(buildSuggestions("save", "del", "delete", "remove", "list"))
                .then(buildArgument("kit name", StringArgumentType.string())
                        .executes(context ->
                        {
                            String action = StringArgumentType.getString(context, "save/delete");
                            String kitName = StringArgumentType.getString(context, "kit name");
                            if (action.equalsIgnoreCase("save"))
                            {
                                Managers.KIT.saveKit(kitName, mc.player.getInventory());
                                sendClientChatMessage("Saved kit: " + kitName);
                            } else if (action.equalsIgnoreCase("del") || action.equalsIgnoreCase("delete") || action.equalsIgnoreCase("remove"))
                            {
                                Managers.KIT.deleteKit(kitName);
                                sendClientChatMessage("Deleted kit: " + kitName);
                            }

                            return 1;
                        }))

                .executes(context ->
                {
                    String action = StringArgumentType.getString(context, "save/delete");
                    if (action.equalsIgnoreCase("list"))
                    {
                        Set<String> kitNames = Managers.KIT.getKitNames();
                        if (kitNames.isEmpty())
                        {
                            sendErrorChatMessage("You have no saved kits!");
                            return 0;
                        }

                        sendClientChatMessage(Formatting.GRAY + "Kits: " + Formatting.WHITE + String.join(", ", kitNames));
                        return 1;
                    }

                    sendErrorChatMessage("Must provide a kit name!");
                    return 1;
                }))

                .executes(context ->
                {
                    sendErrorChatMessage("Invalid command usage! Usage: kit <save/delete/list> *<kit_name>");
                    return 1;
                });
    }
}
