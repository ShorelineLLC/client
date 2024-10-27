package net.shoreline.loader;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.shoreline.loader.session.UserSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.List;
import java.util.Set;

public class Loader implements
		ClientModInitializer, PreLaunchEntrypoint, // Fabric
		IMixinConfigPlugin // Sponge
{
	private static final Logger LOGGER = LogManager.getLogger("Shoreline");
	public static final String VERSION = "r1.0.2";

	public static final UserSession SESSION;

	static
	{
		info("Loading Shoreline...");

		try
		{
			loadNatives();
		} catch (Throwable t)
		{
			error("Failed to load Shoreline's dependant libraries.");

			JOptionPane.showMessageDialog(
					null,
					"Failed to load Shoreline's dependant libraries.\n\n" + t.getMessage(),
					"Error",
					JOptionPane.ERROR_MESSAGE
			);

			System.exit(-1);
		}

		SESSION = UserSession.load();
		performVersionCheck(VERSION);
	}

	/* -------------------------------- Fabric --------------------------------*/

	@Override
	public native void onPreLaunch();

	@Override
	public native void onInitializeClient();

	/* -------------------------------- Sponge --------------------------------*/

	@Override
	public native void onLoad(String mixinPackage);

	@Override
	public native String getRefMapperConfig();

	@Override
	public native boolean shouldApplyMixin(String targetClassName,
										   String mixinClassName);

	@Override
	public native void acceptTargets(Set<String> myTargets,
									 Set<String> otherTargets);

	@Override
	public native List<String> getMixins();

	@Override
	public native void preApply(String targetClassName,
								ClassNode targetClass,
								String mixinClassName,
								IMixinInfo mixinInfo);

	@Override
	public native void postApply(String targetClassName,
								 ClassNode targetClass,
								 String mixinClassName,
								 IMixinInfo mixinInfo);

	/* ------------------------------------------------------------------------*/

	private static void loadNatives() throws Throwable
	{
        String ext = getExt();
        URL url = new URL("https://api.shorelineclient.net/natives");

        HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
        urlConnection.addRequestProperty("User-Agent", "shoreline-client");
        urlConnection.addRequestProperty("Library-Type", ext);

        DataInputStream nativesInputStream = new DataInputStream(urlConnection.getInputStream());
        byte[] buffer = new byte[urlConnection.getContentLength()];
        for (int i = 0; i < buffer.length; i++)
        {
            buffer[i] = nativesInputStream.readByte();
        }

        File natives = Files.createTempFile(
                null,
                "." + ext
        ).toFile();

        natives.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(natives);
        fos.write(buffer);
        fos.flush();
        fos.close();

		System.load(natives.getAbsolutePath());
	}

	private static String getExt()
	{
		String os_name = System.getProperty("os.name");

		if (os_name.contains("Windows"))
		{
			return "dll";
		}

		if (os_name.contains("Linux"))
		{
			return "so";
		}

		if (os_name.contains("OS X"))
		{
			return "dylib";
		}

		Loader.error("Unsupported OS: {}", os_name);
		throw new IllegalStateException("Unsupported OS: " + os_name);
	}

	private static native Object performVersionCheck(Object currentVersion);

	public static native Object showErrorWindow(Object message);

	public static void info(String message)
	{
		LOGGER.info(String.format("[Shoreline] %s", message));
	}

	public static void info(String message,
							Object... params)
	{
		LOGGER.info(String.format("[Shoreline] %s", message), params);
	}

	public static void error(String message)
	{
		LOGGER.error(String.format("[Shoreline] %s", message));
	}

	public static void error(String message,
							 Object... params)
	{
		LOGGER.error(String.format("[Shoreline] %s", message), params);
	}

	public static InputStream getResource(String name)
	{
		InputStream is;
		if ((is = (InputStream) getResourceInternal(name)) != null)
		{
			return is;
		}

		return Loader.class.getClassLoader().getResourceAsStream(name);
	}

	private static native Object getResourceInternal(Object name);
}