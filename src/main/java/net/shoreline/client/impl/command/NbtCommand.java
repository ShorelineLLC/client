package net.shoreline.client.impl.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.minecraft.item.ItemStack;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.util.chat.ChatUtil;

public class NbtCommand extends Command {
    /**
     *
     */
    public NbtCommand() {
        super("Nbt", "Displays all nbt tags on the held item", literal("nbt"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder) {
        builder.executes(context -> {
            ItemStack mainhand = mc.player.getMainHandStack();
            if (!mainhand.hasNbt() || mainhand.getNbt() == null) {
                ChatUtil.error("No Nbt tags on this item!");
                return 0;
            }
            ChatUtil.clientSendMessage(mainhand.getNbt().toString());
            return 1;
        });
    }
}
