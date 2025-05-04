package net.shoreline.server.route.loader.classcache;

import net.shoreline.server.ServerMain;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

// need to scan

// classes mentioned in mixins


public final class ClassCache
{
    private final Map<String, Dependency> dependencyMap;

    private final List<String> mixinClasses;
    private final List<String> mixinAccessorClasses;
    private final List<String> iMixinClasses;
    private final List<String> cacheClasses;
    private final List<String> lateLoadingClasses;

    public ClassCache(File jarFile)
    {
        Map<String, byte[]> classes = new HashMap<>();

        try (JarFile jar = new JarFile(jarFile))
        {
            Enumeration<JarEntry> entries = jar.entries();

            while (entries.hasMoreElements())
            {
                JarEntry entry = entries.nextElement();

                if (entry.getName().endsWith(".class"))
                {
                    byte[] content = readBytes(jar.getInputStream(entry));

                    classes.put(entry.getName().substring(0, entry.getName().length() - 6), content);
                }
            }
        } catch (Throwable t)
        {
            throw new IllegalStateException("Failed to load jar file", t);
        }

        this.dependencyMap = generateDependencies(classes);

        this.mixinClasses = findMixinClasses();
        this.mixinAccessorClasses = findMixinAccessorClasses();
        this.iMixinClasses = findIMixinClasses();
        this.cacheClasses = findCacheClasses();
        this.lateLoadingClasses = findLateLoadingClasses();
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

    public boolean isMixinClass(String name)
    {
        return this.mixinClasses.contains(name);
    }

    public boolean isMixinAccessorClass(String name)
    {
        return this.mixinAccessorClasses.contains(name);
    }

    public boolean isIMixinClass(String name)
    {
        return this.iMixinClasses.contains(name);
    }

    public boolean shouldCacheClass(String name)
    {
        return this.cacheClasses.contains(name);
    }

    public boolean isLateLoadingClass(String name)
    {
        return this.lateLoadingClasses.contains(name);
    }

    private List<String> findMixinClasses()
    {
        List<String> mixinClasses = new ArrayList<>();

        for (String className : dependencyMap.keySet())
        {
            if (annotationCheck(className, "Lorg/spongepowered/asm/mixin/Mixin;"))
            {
                mixinClasses.add(className);
            }
        }

        return mixinClasses;
    }

    private List<String> findMixinAccessorClasses()
    {
        List<String> mixinAccessorClasses = new ArrayList<>();

        for (String className : dependencyMap.keySet())
        {
            if (annotationCheck(className, "Lorg/spongepowered/asm/mixin/Mixin;"))
            {
                ClassNode clazz = this.dependencyMap.get(className).representingClass;

                if ((clazz.access & Opcodes.ACC_INTERFACE) != 0)
                {
                    mixinAccessorClasses.add(className);
                }
            }
        }

        return mixinAccessorClasses;
    }

    private List<String> findIMixinClasses()
    {
        List<String> mixinClasses = new ArrayList<>();

        for (String className : dependencyMap.keySet())
        {
            if (annotationCheck(className, "Lnet/shoreline/client/im;"))
            {
                mixinClasses.add(className);
            }
        }

        return mixinClasses;
    }

    /**
     * Finds the names of classes that mixin classes reference, meaning mixin has to find their bytecode
     * and the loader has to cache them
     */
    private List<String> findCacheClasses()
    {
        List<String> cacheClasses = new ArrayList<>();

        List<String> mixinClasses = this.dependencyMap
                .keySet()
                .stream()
                .filter(this::isMixinClass)
                .toList();

        for (String mixinClass : mixinClasses)
        {
            ClassNode clazz = this.dependencyMap.get(mixinClass).representingClass;

            for (MethodNode methodNode : clazz.methods)
            {
                for (AbstractInsnNode insnNode : methodNode.instructions)
                {
                    if (insnNode instanceof TypeInsnNode tin)
                    {
                        if (tin.desc.contains("net/shoreline/client"))
                        {
                            cacheClasses.add(tin.desc);
                            cacheClasses.addAll(allSubClassesOf(tin.desc));
                        }
                    } else if (insnNode instanceof MethodInsnNode min)
                    {
                        if (min.owner.contains("net/shoreline/client"))
                        {
                            cacheClasses.add(min.owner);
                            cacheClasses.addAll(allSubClassesOf(min.owner));
                        }

                        Type methodType = Type.getMethodType(min.desc);
                        String returnTypeName = methodType.getReturnType().getInternalName();
                        if (returnTypeName.contains("net/shoreline/client"))
                        {
                            cacheClasses.add(returnTypeName);
                            cacheClasses.addAll(allSubClassesOf(returnTypeName));
                        }

                        for (Type argumentType : methodType.getArgumentTypes())
                        {
                            if (argumentType.getInternalName().contains("net/shoreline/client"))
                            {
                                cacheClasses.add(argumentType.getInternalName());
                                cacheClasses.addAll(allSubClassesOf(argumentType.getInternalName()));
                            }
                        }
                    } else if (insnNode instanceof FieldInsnNode fin)
                    {
                        if (fin.owner.contains("net/shoreline/client"))
                        {
                            cacheClasses.add(fin.owner);
                            cacheClasses.addAll(allSubClassesOf(fin.owner));
                        }
                    } else if (insnNode instanceof InvokeDynamicInsnNode indy)
                    {
                        if (indy.desc.contains("net/shoreline/client"))
                        {
                            Type methodType = Type.getMethodType(indy.desc);
                            String returnTypeName = methodType.getReturnType().getInternalName();
                            if (returnTypeName.contains("net/shoreline/client"))
                            {
                                cacheClasses.add(returnTypeName);
                                cacheClasses.addAll(allSubClassesOf(returnTypeName));
                            }

                            for (Type argumentType : methodType.getArgumentTypes())
                            {
                                if (argumentType.getInternalName().contains("net/shoreline/client"))
                                {
                                    cacheClasses.add(argumentType.getInternalName());
                                    cacheClasses.addAll(allSubClassesOf(argumentType.getInternalName()));
                                }
                            }
                        }

                        Handle bsm = indy.bsm;

                        if (bsm.getOwner().contains("net/shoreline/client"))
                        {
                            cacheClasses.add(bsm.getOwner());
                            cacheClasses.addAll(allSubClassesOf(bsm.getOwner()));
                        }

                        if (bsm.getDesc().contains("net/shoreline/client"))
                        {
                            Type methodType = Type.getMethodType(bsm.getDesc());
                            String returnTypeName = methodType.getReturnType().getInternalName();
                            if (returnTypeName.contains("net/shoreline/client"))
                            {
                                cacheClasses.add(returnTypeName);
                                cacheClasses.addAll(allSubClassesOf(returnTypeName));
                            }

                            for (Type argumentType : methodType.getArgumentTypes())
                            {
                                if (argumentType.getInternalName().contains("net/shoreline/client"))
                                {
                                    cacheClasses.add(argumentType.getInternalName());
                                    cacheClasses.addAll(allSubClassesOf(argumentType.getInternalName()));
                                }
                            }
                        }
                    } else if (insnNode instanceof LdcInsnNode ldc)
                    {
                        if (ldc.cst instanceof Type ldc_t)
                        {
                            String desc = ldc_t.getClassName().replace(".", "/");
                            if (desc.contains("net/shoreline/client"))
                            {
                                cacheClasses.add(desc);
                                cacheClasses.addAll(allSubClassesOf(desc));
                            }
                        }
                    } else if (insnNode instanceof MultiANewArrayInsnNode min)
                    {
                        if (min.desc.contains("net/shoreline/client"))
                        {
                            Type type = Type.getType(min.desc);
                            cacheClasses.add(type.getInternalName());
                            cacheClasses.addAll(allSubClassesOf(type.getInternalName()));
                        }
                    }
                }
            }
        }

        Set<String> unique = new HashSet<>(cacheClasses);

        return unique.stream().toList();
    }

    private List<String> allSubClassesOf(String className)
    {
        List<String> subClasses = new ArrayList<>();

        if (!this.dependencyMap.containsKey(className))
        {
            throw new IllegalStateException("Class not found");
        }

        Dependency dependency = this.dependencyMap.get(className);

        if (dependency.superName.contains("net/shoreline/client"))
        {
            subClasses.add(dependency.superName);
            subClasses.addAll(allSubClassesOf(dependency.superName));
        }

        for (String interfaceName : dependency.interfaces)
        {
            if (interfaceName.contains("net/shoreline/client"))
            {
                subClasses.add(interfaceName);
                subClasses.addAll(allSubClassesOf(interfaceName));
            }
        }

        return subClasses;
    }

    private List<String> findLateLoadingClasses()
    {
        List<String> lateLoadingClasses = new ArrayList<>();

        for (String name : this.dependencyMap.keySet())
        {
            ClassNode classNode = this.dependencyMap.get(name).representingClass;

            if (checkClass(classNode))
            {
                lateLoadingClasses.add(name);
            }
        }

        return lateLoadingClasses;
    }

    private boolean checkClass(ClassNode classNode)
    {
        if (classNode.superName.startsWith("net/minecraft"))
        {
            return true;
        }

        String currSuperName = classNode.superName;
        while (currSuperName.startsWith("net/shoreline/client"))
        {
            Dependency superClass = this.dependencyMap.get(currSuperName);

            if (checkClass(superClass.representingClass))
            {
                return true;
            }

            currSuperName = superClass.superName;
        }

        for (String interf : classNode.interfaces)
        {
            if (interf.startsWith("net/minecraft"))
            {
                return true;
            }

            Dependency dependency = this.dependencyMap.get(interf);

            if (dependency != null)
            {
                ClassNode interfaceClass = dependency.representingClass;

                if (checkClass(interfaceClass))
                {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean annotationCheck(String name,
                                    String annotationDesc)
    {
        if (!this.dependencyMap.containsKey(name))
        {
            throw new IllegalStateException("Class not found");
        }

        ClassNode clazz = this.dependencyMap.get(name).representingClass;

        if (clazz.invisibleAnnotations == null)
        {
            return false;
        }

        for (AnnotationNode annotation : clazz.invisibleAnnotations)
        {
            if (annotation.desc.equals(annotationDesc))
            {
                return true;
            }
        }

        return false;
    }

    private Map<String, Dependency> generateDependencies(Map<String, byte[]> classes)
    {
        Map<String, Dependency> dependencyMap = new HashMap<>();

        classes.forEach((name, bytes) ->
        {
            ClassNode classNode = new ClassNode();
            ClassReader reader = new ClassReader(bytes);

            reader.accept(classNode, 0);

            Dependency dependency = new Dependency(
                    classNode,
                    classNode.superName,
                    classNode.interfaces
            );

            dependencyMap.put(name, dependency);
        });

        return dependencyMap;
    }

    private static class Dependency
    {
        ClassNode representingClass;
        String superName;
        List<String> interfaces;

        Dependency(ClassNode representingClass,
                   String superName,
                   List<String> interfaces)
        {
            this.representingClass = representingClass;
            this.superName = superName;
            this.interfaces = interfaces;
        }
    }
}
