package net.shoreline.server.route.loader.routes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.javalin.http.Context;
import io.javalin.http.InternalServerErrorResponse;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import net.shoreline.server.ServerMain;
import net.shoreline.server.dynamicobf.DecompilerCrasher;
import net.shoreline.server.encryption.Encryption;
import net.shoreline.server.route.Route;
import net.shoreline.server.route.loader.LoaderEndpoint;
import net.shoreline.server.route.loader.classcache.ClassCache;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

public final class LoadResourcesRoute extends Route
{
    /* Bitmap flags for the file extension, tells the loader how to handle the file */
    static int DEFINE = 1 << 0; // Define the class normally
    static int CACHE = 1 << 1; // Cache the class for mixin bytecode
    static int MIXIN = 1 << 2; // Add the class to a mixin config
    static int LATE_LOADING = 1 << 3; // Load the class late, it extends classes that haven't been loaded yet
    static int RESOURCE = 1 << 4; // It's a resource file
    static int REFMAP = 1 << 5; // The mixin refmap
    static int ACCESS_WIDENER = 1 << 6; // The access widener
    static int KEYS_FILE = 1 << 7; // Decryption keys

    @Override
    public void doHandle(Context context) throws Exception
    {
        String userAgent = context.header("User-Agent");

        if (!"shoreline-client".equals(userAgent))
        {
            throw new NotFoundResponse();
        }

        String usertype = context.sessionAttribute("User-Type");

        if (usertype == null)
        {
            throw new NotFoundResponse();
        }

        String hardwareID = context.sessionAttribute("Hardware-ID");

        if (hardwareID == null)
        {
            throw new NotFoundResponse();
        }

        String token;
        try
        {
            JsonObject request = JsonParser.parseString(context.body()).getAsJsonObject();

            token = request.get("Token").getAsString();
        } catch (Throwable t)
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
                "/home/container/assets/loader/%s/client.jar",
                usertype
        );

        ClassCache cache = switch (usertype)
        {
            case "release" -> {
                ServerMain.LOGGER.error("release class cache not implemented yet");
                throw new InternalServerErrorResponse();
            }
            case "beta" -> LoaderEndpoint.BETA_CLASS_CACHE;
            case "dev" -> LoaderEndpoint.DEV_CLASS_CACHE;
            default -> {
                throw new IllegalStateException(); // unreachable
            }
        };

        try
        {
            Path file = Paths.get(path);

            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JarOutputStream jarOutputStream = new JarOutputStream(byteArrayOutputStream);

            try (JarFile jar = new JarFile(file.toFile()))
            {
                Enumeration<JarEntry> entries = jar.entries();

                while (entries.hasMoreElements())
                {
                    JarEntry entry = entries.nextElement();
                    InputStream entryInputStream = jar.getInputStream(entry);
                    byte[] content = readBytes(entryInputStream);

                    // Write the users HWID to the constant pool of a class with a known mapping
                    if (entry.getName().equals("net/shoreline/client/lc.class"))
                    {
                        ClassReader classReader = new ClassReader(content);
                        ClassWriter classWriter = new ClassWriter(classReader, 0);

                        classReader.accept(new HWIDWriter(classWriter, hardwareID), 0);

                        content = classWriter.toByteArray();
                    }

                    TransformedJarEntry newEntry = transformEntry(entry, content, cache);

                    if (newEntry == null) // we skipped this entry from being sent
                    {
                        continue;
                    }

                    jarOutputStream.putNextEntry(newEntry.newEntry);
                    jarOutputStream.write(newEntry.newContent);

                    entryInputStream.close();
                    jarOutputStream.closeEntry();
                }
            }

            jarOutputStream.finish();
            jarOutputStream.close();

            byte[] modifiedJarBytes = byteArrayOutputStream.toByteArray();
            int modifiedJarSize = modifiedJarBytes.length;

            context.contentType("application/java-archive");
            context.header("Content-Length", String.valueOf(modifiedJarSize));

            OutputStream clientOutputStream = context.res().getOutputStream();
            clientOutputStream.write(modifiedJarBytes);
            clientOutputStream.flush();
        } catch (Throwable t)
        {
            ServerMain.LOGGER.error("Couldn't read or send {} : ", path, t);
            throw new InternalServerErrorResponse();
        } finally {
            context.req().getSession().invalidate();
        }
    }

    private TransformedJarEntry transformEntry(JarEntry entry,
                                               byte[] content,
                                               ClassCache cache) throws Throwable
    {
        int flags;
        String entryName = entry.getName();
        if (entryName.endsWith(".class"))
        {
            // Skip imixin, server already used it and we dont need it
            if (entryName.equals("net/shoreline/client/im.class"))
            {
                return null;
            }

            ClassNode classNode = new ClassNode();
            ClassReader classReader = new ClassReader(content);

            classReader.accept(classNode, 0);

            // Strip @IMixin from the client, we don't need it past determineClassFileName and neither does the loader
            if (classNode.invisibleAnnotations != null)
            {
                classNode.invisibleAnnotations.removeIf(
                        annotation -> annotation.desc.equals("Lnet/shoreline/client/im;")
                );
            }

            ClassWriter classWriter = new ClassWriter(0);
            classNode.accept(classWriter);

            content = classWriter.toByteArray();

            // now determine file extension
            String formattedName = entryName.substring(0, entryName.length() - 6);

            if (cache.isMixinClass(formattedName))
            {
                flags = MIXIN + CACHE;
                if (cache.isMixinAccessorClass(formattedName))
                {
                    flags += DEFINE;
                }
            } else if (cache.isIMixinClass(formattedName))
            {
                flags = DEFINE + CACHE;
            } else
            {
                flags = DEFINE;

                if (cache.shouldCacheClass(formattedName))
                {
                    flags = CACHE + DEFINE;
                }

                if (cache.isLateLoadingClass(formattedName))
                {
                    flags = CACHE + LATE_LOADING;
                }

//                ServerMain.LOGGER.info("Raping {}", formattedName);
//                try
//                {
//                    DecompilerCrasher crasher = new DecompilerCrasher();
//                    content = crasher.rapeDecompilers(content);
//                } catch (Throwable t)
//                {
//                    ServerMain.LOGGER.error("Couldn't rape it");
//                }
            }

            entryName = entryName.replace(".class", "," + flags);
        } else
        {
            flags = switch (entryName)
            {
                case "shoreline-refmap.json" -> REFMAP;
                case "shoreline.accesswidener" -> ACCESS_WIDENER;
                case "keys.txt" -> KEYS_FILE;
                default -> RESOURCE;
            };

            entryName += "," + flags;
        }

        content = Encryption.encryptReversible(content);

        return new TransformedJarEntry(new JarEntry(entryName), content);
    }

    private byte[] readBytes(InputStream is) throws IOException
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buf = new byte[is.available()];
        int len;
        while ((len = is.read(buf, 0, buf.length)) != -1)
        {
            baos.write(buf, 0, len);
        }

        baos.close();

        return baos.toByteArray();
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
    }

    private static class TransformedJarEntry
    {
        JarEntry newEntry;
        byte[] newContent;

        TransformedJarEntry(JarEntry newEntry,
                            byte[] newContent)
        {
            this.newEntry = newEntry;
            this.newContent = newContent;
        }
    }
}
