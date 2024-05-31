package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.command.Command;

public class PeekCommand extends Command {

    public PeekCommand()
    {
        super("Peek", "Shows the contents of held shulkers", literal("peek"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {

    }

    public void displayShulkerInventory(ItemStack stack)
    {

    }
}
