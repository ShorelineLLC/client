package net.shoreline.server.route.loader.route;

import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;
import org.objectweb.asm.*;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

/**
 * Throws:
 *   NotFoundResponse (404) -> User agent not set properly, session attribute not set
 *   InternalServerErrorResponse (500) -> Some internal error happened
 */
public final class DownloadRoute extends Route
{
    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String token = context.header("Download-Token");

        if (token == null)
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("Usertype");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String hardwareID = context.sessionAttribute("Hardware-ID");

        if (hardwareID == null)
        {
            throw new NotFoundResponse();
        }

        boolean authorized = switch (usertype)
        {
            case "dev" -> LoaderEndpoint.DEV_TOKEN_MANAGER.isAuthorizedToken(token);
            case "beta" -> LoaderEndpoint.BETA_TOKEN_MANAGER.isAuthorizedToken(token);
            case "release" -> LoaderEndpoint.RELEASE_TOKEN_MANAGER.isAuthorizedToken(token);
            default ->
            {
                ServerMain.LOGGER.error("Unrecognized session usertype: {}", usertype);
                throw new InternalServerErrorResponse();
            }
        };

        if (!authorized)
        {
            throw new UnauthorizedResponse();
        }

        String path = String.format(
                "/home/container/assets/%s/client.jar",
                usertype
        );

        try
        {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JarOutputStream tempJarOutputStream = new JarOutputStream(byteArrayOutputStream);

            Path file = Paths.get(path);
            try (JarFile jar = new JarFile(file.toFile()))
            {
                Enumeration<JarEntry> entries = jar.entries();
                byte[] buffer = new byte[8192];
                int bytesRead;

                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    InputStream entryInputStream = jar.getInputStream(entry);

                    // We will store user information in RenderLayersClient.class, a class
                    // with a known obfuscation mapping
                    if (entry.getName().equals("net/shoreline/client/lc.class"))
                    {
                        ClassReader classReader = new ClassReader(entryInputStream);
                        ClassWriter classWriter = new ClassWriter(classReader, 0);

                        classReader.accept(new HWIDWriter(classWriter, hardwareID), 0);

                        byte[] modifiedClass = classWriter.toByteArray();

                        JarEntry newEntry = new JarEntry(entry.getName());
                        tempJarOutputStream.putNextEntry(newEntry);
                        tempJarOutputStream.write(modifiedClass);
                    } else
                    {
                        tempJarOutputStream.putNextEntry(entry);
                        while ((bytesRead = entryInputStream.read(buffer)) != -1)
                        {
                            tempJarOutputStream.write(buffer, 0, bytesRead);
                        }
                    }

                    entryInputStream.close();
                    tempJarOutputStream.closeEntry();
                }
            }

            tempJarOutputStream.finish();
            tempJarOutputStream.close();

            byte[] modifiedJarBytes = byteArrayOutputStream.toByteArray();
            int modifiedJarSize = modifiedJarBytes.length;

            String contentType = Files.probeContentType(file);

            if (contentType == null)
            {
                contentType = "application/octet-stream";
            }

            context.contentType(contentType);
            context.header("Content-Length", String.valueOf(modifiedJarSize));

            OutputStream clientOutputStream = context.res().getOutputStream();
            clientOutputStream.write(modifiedJarBytes);
            clientOutputStream.flush();

            context.req().getSession().invalidate();
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't read or send {} : ", path, t);
            context.req().getSession().invalidate();
            throw new InternalServerErrorResponse();
        }
    }

    private static class HWIDWriter extends ClassVisitor
    {
        private final String hwid;

        HWIDWriter(ClassWriter cw,
                   String hwid)
        {
            super(Opcodes.ASM9, cw);
            this.hwid = hwid;
        }

        @Override
        public void visitEnd()
        {
            Attribute attr = new StringAttribute(this.hwid);
            this.cv.visitAttribute(attr);

            super.visitEnd();
        }
    }

    private static class StringAttribute extends Attribute
    {
        StringAttribute(String value)
        {
            super(value);
        }

        @Override
        protected ByteVector write(ClassWriter classWriter,
                                   byte[] code,
                                   int codeLength,
                                   int maxStack,
                                   int maxLocals)
        {
            ByteVector byteVector = new ByteVector();
            byteVector.putByteArray(this.type.getBytes(), 0, this.type.length());

            return byteVector;
        }

        @Override
        public boolean isUnknown()
        {
            return true;
        }

        @Override
        public boolean isCodeAttribute()
        {
            return false;
        }
    }
}
