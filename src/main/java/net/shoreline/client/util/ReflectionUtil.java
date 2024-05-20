package net.shoreline.client.util;


import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author TerrificTable, xgraza
 * @since 05/04/24
 */
@SuppressWarnings("unchecked")
public final class ReflectionUtil
{

    public static List<Class<?>> reflectInPackage(final String packageName)
            throws IOException, ClassNotFoundException
    {
        final List<Class<?>> classes = new LinkedList<>();
        final String path = packageName.replace('.', '/');

        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (final URL resource : Collections.list(classLoader.getResources(path))) {
            final String fullPath = resource.getFile().replace("%20", " ");
            if (fullPath.endsWith(".jar"))
            {
                classes.addAll(getClassesFromJar(fullPath, packageName));
            } else
            {
                classes.addAll(getClassesFromDirectory(new File(fullPath), packageName));
            }
        }
        return classes;
    }

    private static List<Class<?>> getClassesFromJar(final String jarPath,
                                                    final String packageName)
            throws IOException, ClassNotFoundException
    {
        final List<Class<?>> classes = new LinkedList<>();
        final JarFile jarFile = new JarFile(jarPath);
        final Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements())
        {
            final JarEntry entry = entries.nextElement();
            final String entryName = entry.getName();
            if (entryName.endsWith(".class")
                    && entryName.startsWith(packageName.replace('.', '/')))
            {
                final String className = entryName
                        .replace('/', '.')
                        .substring(0, entryName.length() - 6);
                classes.add(Class.forName(className));
            }
        }
        jarFile.close();
        return classes;
    }

    private static List<Class<?>> getClassesFromDirectory(final File directory,
                                                          final String packageName)
            throws ClassNotFoundException
    {
        final List<Class<?>> classes = new LinkedList<>();
        if (!directory.exists())
        {
            return classes;
        }

        final File[] files = directory.listFiles();
        if (files == null)
        {
            return classes;
        }

        for (final File file : files)
        {
            if (file.isDirectory())
            {
                classes.addAll(getClassesFromDirectory(file,
                        packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class"))
            {
                final String className = packageName + '.'
                        + file.getName().substring(0, file.getName().length() - 6);
                classes.add(Class.forName(className));
            }
        }
        return classes;
    }

    public static Set<Field> reflectFieldsByType(final Object parent, final Class<?> type)
    {
        final Set<Field> reflected = new LinkedHashSet<>();
        final Field[] parentFields = parent.getClass().getDeclaredFields();
        for (final Field field : parentFields)
        {
            if (field.getType().equals(type) && field.trySetAccessible())
            {
                reflected.add(field);
            }
        }
        return reflected;
    }

    public static <T> T getValue(final Object parent, final Field field)
    {
        try
        {
            return (T) field.get(parent);
        } catch (Exception ignored)
        {
        }
        return null;
    }
}