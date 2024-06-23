package net.shoreline.server.route;

import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.Header;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class Route implements Handler
{
    @Override
    public final void handle(@NotNull Context context) throws Exception
    {
        context.header(Header.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
        context.header(Header.CACHE_CONTROL, "no-store, no-cache, must-revalidate, private");
        context.header(Header.PRAGMA, "no-cache");
        context.header(Header.EXPIRES, "0");

        doHandle(context);
    }

    public abstract void doHandle(Context context) throws Exception;

    public final void sendFile(Context context,
                               String path) throws Throwable
    {
        Path file = Paths.get(path);

        String contentType = Files.probeContentType(file);

        if (contentType == null)
        {
            contentType = "application/octet-stream";
        }

        context.contentType(contentType);
        context.header("Content-Length", String.valueOf(Files.size(file)));

        Files.copy(file, context.res().getOutputStream());
    }
}
