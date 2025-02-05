package net.shoreline.client.impl.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.command.CommandSource;
import net.shoreline.client.Shoreline;
import net.shoreline.client.api.command.Command;
import net.shoreline.client.init.Fonts;
import net.shoreline.client.util.chat.ChatUtil;

import java.nio.file.Files;
import java.nio.file.Path;

public class CustomFontCommand extends Command
{
    public CustomFontCommand()
    {
        super("CustomFont", "Sets the client font", literal("customfont"));
    }

    @Override
    public void buildCommand(LiteralArgumentBuilder<CommandSource> builder)
    {
        builder.then(argument("font name", StringArgumentType.string()).executes(c ->
        {
            String font = StringArgumentType.getString(c, "font name");
            if (font.equalsIgnoreCase("verdana") || font.equalsIgnoreCase("verdana.ttf"))
            {
                Fonts.FONT_FILE_PATH = Fonts.DEFAULT_FONT_FILE_PATH;
                Fonts.loadFonts();
                Shoreline.CONFIG.saveFonts();
                ChatUtil.clientSendMessage("Set client font to " + font + "!");
                return 0;
            }

            String fileName = font.endsWith(".ttf") ? font : font + ".ttf";
            String filePath = String.format("%s/%s", Shoreline.CONFIG.getClientDirectory().toAbsolutePath(), fileName);
            if (!Files.exists(Path.of(filePath)))
            {
                ChatUtil.error("Could not find font file: " + fileName);
                return 0;
            }

            Fonts.FONT_FILE_PATH = filePath;
            Fonts.loadFonts();
            Shoreline.CONFIG.saveFonts();
            ChatUtil.clientSendMessage("Set client font to " + font + "!");
            return 0;
        }));
    }
}
