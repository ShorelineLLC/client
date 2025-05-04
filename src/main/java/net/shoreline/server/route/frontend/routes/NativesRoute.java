package net.shoreline.server.route.frontend.routes;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The route that will provide all the natives for the installer, loader, etc
 *
 * Client requests for the installer or loader based on User-Agent, and requests
 * for the type of native based on Library-Type
 *
 * Example request:
 *
 * User-Agent: shoreline-installer
 * Library-Type: win-x86_64
 */
public final class NativesRoute extends Route
{
    private static final Pattern NATIVE_PATTERN = Pattern.compile("^(win|lin|mac)-[a-zA-Z0-9_]+$");

    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        NativeType nativeType = NativeType.fromString(userAgent);

        if (nativeType == null)
        {
            throw new NotFoundResponse();
        }

        String libType = context.header("Library-Type");

        if (libType == null || !NATIVE_PATTERN.matcher(libType).matches())
        {
            throw new NotFoundResponse();
        }

        String os = libType.split("-")[0];
        String osExt = switch (os)
        {
            case "win" -> "dll";
            case "lin" -> "so";
            case "mac" -> "dylib";
            default -> throw new NotFoundResponse();
        };

        String arch = libType.split("-")[1];

        String path = String.format(
                "/home/container/assets/%s/natives/shoreline_%s_%s.%s",
                nativeType.name,
                nativeType.name,
                arch,
                osExt
        );

        try
        {
            sendFile(context, path);
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't send {} : ", path, t);
            throw new InternalServerErrorResponse();
        }
    }

    private enum NativeType
    {
        INSTALLER("installer"),
        CLIENT("client");

        private final String name;

        NativeType(String name)
        {
            this.name = name;
        }

        public static NativeType fromString(String name)
        {
            if (name == null)
            {
                return null;
            }

            return switch (name)
            {
                case "shoreline-installer" -> INSTALLER;
                case "shoreline-client" -> CLIENT;
                default -> null;
            };
        }
    }
}
