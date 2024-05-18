package net.shoreline.loader.asm;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused") // @see utils.rs
public final class ClassScanner extends ClassVisitor
{
    @SuppressWarnings("FieldCanBeLocal")
    private int access;

    @SuppressWarnings("FieldCanBeLocal")
    private String superName;

    @SuppressWarnings("FieldCanBeLocal")
    private String[] interfaces;

    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection") // @see utils.rs
    private final List<String> descs = new ArrayList<>();

    private ClassScanner()
    {
        super(Opcodes.ASM9);
    }

    @Override
    public void visit(int version,
                      int access,
                      String name,
                      String signature,
                      String superName,
                      String[] interfaces)
    {
        super.visit(version,
                    this.access = access,
                    name,
                    signature,
                    this.superName = superName,
                    this.interfaces = interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String descriptor,
                                             boolean visible)
    {
        this.descs.add(descriptor);
        return super.visitAnnotation(descriptor, visible);
    }
}
