package net.shoreline.client.impl.font;

import com.google.common.collect.Lists;
import net.minecraft.client.font.*;
import net.minecraft.util.Identifier;
import net.shoreline.client.util.Globals;

import java.io.IOException;
import java.util.List;

public class CFontRenderer implements Globals
{
    public static TextRenderer getTextRender(String fontName) throws IOException
    {
        List<Font> list = Lists.newArrayList();
        TrueTypeFontLoader loader = new TrueTypeFontLoader(
                new Identifier(String.format("shoreline:%s.ttf", fontName)),
                11,
                20,
                TrueTypeFontLoader.Shift.NONE,
                ""
        );
        FontLoader.Loadable loadable = loader.build().orThrow();
        Font font = loadable.load(mc.getResourceManager());
        list.add(font);
        FontStorage storage = new FontStorage(mc.getTextureManager(), new Identifier("shoreline:tr"));
        storage.setFonts(list);
        return new TextRenderer(id -> storage, true);
    }
}
