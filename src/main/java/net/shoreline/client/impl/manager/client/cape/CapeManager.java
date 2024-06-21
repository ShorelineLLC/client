package net.shoreline.client.impl.manager.client.cape;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.shoreline.client.impl.event.network.LoadCapeEvent;
import net.shoreline.client.util.Globals;
import net.shoreline.eventbus.EventBus;
import net.shoreline.eventbus.annotation.EventListener;

import java.io.InputStream;
import java.net.URL;

// Optifine capes
public class CapeManager implements Globals
{
    public CapeManager()
    {
        EventBus.INSTANCE.subscribe(this);
    }

    @EventListener
    public void onLoadCape(LoadCapeEvent event)
    {
        loadPlayerCape(event.getGameProfile(), event.getTexture());
    }

    /**
     * @param profile
     * @param texture
     * @return
     */
    public void loadPlayerCape(GameProfile profile, CapeTexture texture)
    {
        Util.getMainWorkerExecutor().execute(() ->
        {
            String uuid = profile.getId().toString();
            String url = String.format("http://s.optifine.net/capes/%s.png", profile.getName());
            try
            {
                URL optifineUrl = new URL(url);
                InputStream stream = optifineUrl.openStream();
                NativeImage cape = NativeImage.read(stream);
                NativeImage nativeImage = imageFromStream(cape);
                NativeImageBackedTexture t = new NativeImageBackedTexture(nativeImage);
                Identifier identifier = mc.getTextureManager().registerDynamicTexture("of-capes-" + uuid, t);
                texture.callback(identifier);
                stream.close();
            }
            catch (Exception ignored)
            {

            }
        });
    }

    /**
     * @param image
     * @return
     */
    private NativeImage imageFromStream(NativeImage image)
    {
        int imageWidth = 64;
        int imageHeight = 32;
        int imageSrcWidth = image.getWidth();
        int srcHeight = image.getHeight();
        for (int imageSrcHeight = image.getHeight(); imageWidth < imageSrcWidth
                || imageHeight < imageSrcHeight; imageHeight *= 2)
        {
            imageWidth *= 2;
        }
        NativeImage img = new NativeImage(imageWidth, imageHeight, true);
        for (int x = 0; x < imageSrcWidth; x++)
        {
            for (int y = 0; y < srcHeight; y++)
            {
                img.setColor(x, y, image.getColor(x, y));
            }
        }
        image.close();
        return img;
    }

    public interface CapeTexture
    {
        void callback(Identifier id);
    }
}
