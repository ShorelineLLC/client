package net.shoreline.server.dynamicobf;

import org.objectweb.asm.*;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public final class DecompilerCrasher
{
    private static final String FUCKED_ANNOTATION_SIG = "Lgive up;";

    private final List<String> classSignatureExclusions;
    private final List<String> fieldExclusions;
    private final List<String> methodExclusions;

    public DecompilerCrasher()
    {
        this.classSignatureExclusions = new ArrayList<>();
        this.fieldExclusions = new ArrayList<>();
        this.methodExclusions = new ArrayList<>();
    }

    public void addClassSignatureExclusion(String exclusion)
    {
        this.classSignatureExclusions.add(exclusion);
    }

    public void addFieldExclusion(String exclusion)
    {
        this.fieldExclusions.add(exclusion);
    }

    public void addMethodExclusion(String exclusion)
    {
        this.methodExclusions.add(exclusion);
    }

    public byte[] rapeDecompilers(byte[] bytecode)
    {
        ClassNode classNode = new ClassNode();
        new ClassReader(bytecode).accept(classNode, 0);

        classNode.signature = "you wish";

        for (MethodNode methodNode : classNode.methods)
        {
            if (this.methodExclusions.contains(methodNode.name))
            {
                continue;
            }

            methodNode.signature = "give up";

            AnnotationNode annotationNode = new AnnotationNode(FUCKED_ANNOTATION_SIG);
            annotationNode.visit("value", "LOL_GIVE_UP");

            methodNode.invisibleAnnotations = new ArrayList<>();
            methodNode.invisibleAnnotations.add(annotationNode);
        }

        for (FieldNode fieldNode : classNode.fields)
        {
            if (this.fieldExclusions.contains(fieldNode.name))
            {
                continue;
            }

            fieldNode.signature = "give up";

            AnnotationNode annotationNode = new AnnotationNode(FUCKED_ANNOTATION_SIG);
            annotationNode.visit("value", "LOL_GIVE_UP");

            fieldNode.invisibleAnnotations = new ArrayList<>();
            fieldNode.invisibleAnnotations.add(annotationNode);
        }

        ClassWriter cw = new ClassWriter(0);
        classNode.accept(cw);
        bytecode = cw.toByteArray();

        Analyzer analyzer = new Analyzer(bytecode);
        analyzer.replaceFuckedAnnotationIndexes();

        return bytecode;
    }

    private static class Analyzer
    {
        static final int CONSTANT_CLASS_TAG = 7;
        static final int CONSTANT_FIELDREF_TAG = 9;
        static final int CONSTANT_METHODREF_TAG = 10;
        static final int CONSTANT_INTERFACE_METHODREF_TAG = 11;
        static final int CONSTANT_STRING_TAG = 8;
        static final int CONSTANT_INTEGER_TAG = 3;
        static final int CONSTANT_FLOAT_TAG = 4;
        static final int CONSTANT_LONG_TAG = 5;
        static final int CONSTANT_DOUBLE_TAG = 6;
        static final int CONSTANT_NAME_AND_TYPE_TAG = 12;
        static final int CONSTANT_UTF8_TAG = 1;
        static final int CONSTANT_METHOD_HANDLE_TAG = 15;
        static final int CONSTANT_METHOD_TYPE_TAG = 16;
        static final int CONSTANT_DYNAMIC_TAG = 17;
        static final int CONSTANT_INVOKE_DYNAMIC_TAG = 18;
        static final int CONSTANT_MODULE_TAG = 19;
        static final int CONSTANT_PACKAGE_TAG = 20;

        private final byte[] bytecode;

        private final int header;
        private final int maxStringLength;
        private final int[] cpInfoOffsets;

        public Analyzer(byte[] bytecode)
        {
            this.bytecode = bytecode;

            int constantPoolCount = readUnsignedShort(8);
            this.cpInfoOffsets = new int[constantPoolCount];

            int currentCpInfoIndex = 1;
            int currentCpInfoOffset = 10;
            int currentMaxStringLength = 0;
            while (currentCpInfoIndex < constantPoolCount)
            {
                this.cpInfoOffsets[currentCpInfoIndex++] = currentCpInfoOffset + 1;
                int cpInfoSize;
                switch (bytecode[currentCpInfoOffset])
                {
                    case CONSTANT_FIELDREF_TAG:
                    case CONSTANT_METHODREF_TAG:
                    case CONSTANT_INTERFACE_METHODREF_TAG:
                    case CONSTANT_INTEGER_TAG:
                    case CONSTANT_FLOAT_TAG:
                    case CONSTANT_NAME_AND_TYPE_TAG:
                    case CONSTANT_DYNAMIC_TAG:
                    case CONSTANT_INVOKE_DYNAMIC_TAG:
                        cpInfoSize = 5;
                        break;
                    case CONSTANT_LONG_TAG:
                    case CONSTANT_DOUBLE_TAG:
                        cpInfoSize = 9;
                        currentCpInfoIndex++;
                        break;
                    case CONSTANT_UTF8_TAG:
                        cpInfoSize = 3 + readUnsignedShort(currentCpInfoOffset + 1);
                        if (cpInfoSize > currentMaxStringLength)
                        {
                            currentMaxStringLength = cpInfoSize;
                        }
                        break;
                    case CONSTANT_METHOD_HANDLE_TAG:
                        cpInfoSize = 4;
                        break;
                    case CONSTANT_CLASS_TAG:
                    case CONSTANT_STRING_TAG:
                    case CONSTANT_METHOD_TYPE_TAG:
                    case CONSTANT_PACKAGE_TAG:
                    case CONSTANT_MODULE_TAG:
                        cpInfoSize = 3;
                        break;
                    default:
                        throw new IllegalArgumentException();
                }
                currentCpInfoOffset += cpInfoSize;
            }

            this.header = currentCpInfoOffset;
            this.maxStringLength = currentMaxStringLength;
        }

        private void replaceFuckedAnnotationIndexes()
        {
            char[] charBuffer = new char[this.maxStringLength];
            int currentOffset = this.header;
            String[] interfaces = new String[readUnsignedShort(currentOffset + 6)];
            currentOffset += 8;
            for (int i = 0; i < interfaces.length; ++i)
            {
                currentOffset += 2;
            }

            int currentAttributeOffset = getFirstAttributeOffset();
            for (int i = readUnsignedShort(currentAttributeOffset - 2); i > 0; --i)
            {
                currentAttributeOffset += readInt(currentAttributeOffset + 2);
            }

            int fieldsCount = readUnsignedShort(currentOffset);
            currentOffset += 2;
            while (fieldsCount-- > 0)
            {
                int runtimeVisibleAnnotationsOffset = 0;

                currentOffset += 6;

                int attributesCount = readUnsignedShort(currentOffset);
                currentOffset += 2;
                while (attributesCount-- > 0)
                {
                    String attributeName = readUTF8(currentOffset, charBuffer);
                    int attributeLength = readInt(currentOffset + 2);
                    currentOffset += 6;
                    if ("RuntimeInvisibleAnnotations".equals(attributeName))
                    {
                        runtimeVisibleAnnotationsOffset = currentOffset;
                    }
                    currentOffset += attributeLength;
                }

                if (runtimeVisibleAnnotationsOffset != 0)
                {
                    fuckAnnotations(runtimeVisibleAnnotationsOffset, charBuffer);
                }
            }

            int methodsCount = readUnsignedShort(currentOffset);
            currentOffset += 2;
            while (methodsCount-- > 0)
            {
                currentOffset += 6;

                int runtimeVisibleAnnotationsOffset = 0;

                int attributesCount = readUnsignedShort(currentOffset);
                currentOffset += 2;
                while (attributesCount-- > 0) {
                    String attributeName = readUTF8(currentOffset, charBuffer);
                    int attributeLength = readInt(currentOffset + 2);
                    currentOffset += 6;
                    if ("RuntimeInvisibleAnnotations".equals(attributeName))
                    {
                        runtimeVisibleAnnotationsOffset = currentOffset;
                    }
                    currentOffset += attributeLength;
                }

                if (runtimeVisibleAnnotationsOffset != 0)
                {
                    fuckAnnotations(runtimeVisibleAnnotationsOffset, charBuffer);
                }
            }
        }

        private void fuckAnnotations(int runtimeVisibleAnnotationsOffset,
                                     char[] charBuffer)
        {
            int numAnnotations = readUnsignedShort(runtimeVisibleAnnotationsOffset);
            int currentAnnotationOffset = runtimeVisibleAnnotationsOffset + 2;
            while (numAnnotations-- > 0)
            {
                String annotationDescriptor = readUTF8(currentAnnotationOffset, charBuffer);
                currentAnnotationOffset += 2;
                currentAnnotationOffset = readElementValues(annotationDescriptor, currentAnnotationOffset, charBuffer, true);
            }
        }

        private int readElementValues(String annotationDescriptor,
                                      int annotationOffset,
                                      char[] charBuffer,
                                      boolean named)
        {
            int currentOffset = annotationOffset;
            int numElementValuePairs = readUnsignedShort(currentOffset);
            currentOffset += 2;
            if (named)
            {
                while (numElementValuePairs-- > 0)
                {
                    currentOffset = readElementValue(annotationDescriptor, currentOffset + 2, charBuffer);
                }
            } else
            {
                while (numElementValuePairs-- > 0)
                {
                    currentOffset = readElementValue(annotationDescriptor, currentOffset, charBuffer);
                }
            }
            return currentOffset;
        }

        private int readElementValue(String annotationDescriptor,
                                     int elementValueOffset,
                                     char[] charBuffer)
        {
            int currentOffset = elementValueOffset;

            switch (this.bytecode[currentOffset++] & 0xFF)
            {
                case 'B', 'c', 'Z', 'S', 'D', 'F', 'I', 'J', 'C':
                    currentOffset += 2;
                    break;
                case 's':
                    // here's where the magic happens
                    if (annotationDescriptor.equals(FUCKED_ANNOTATION_SIG))
                    {
                        writeUnsignedShort(currentOffset, 65535);
                    }

                    currentOffset += 2;
                    break;
                case 'e':
                    currentOffset += 4;
                    break;
                case '@':
                    currentOffset = readElementValues(annotationDescriptor, currentOffset + 2, charBuffer, true);
                    break;
                case '[':
                    int numValues = readUnsignedShort(currentOffset);
                    currentOffset += 2;
                    if (numValues == 0)
                    {
                        return readElementValues(annotationDescriptor, currentOffset - 2, charBuffer, false);
                    }
                    switch (this.bytecode[currentOffset] & 0xFF)
                    {
                        case 'B', 'D', 'F', 'J', 'I', 'C', 'S', 'Z':
                            for (int i = 0; i < numValues; i++)
                            {
                                currentOffset += 3;
                            }
                            break;
                        default:
                            currentOffset = readElementValues(annotationDescriptor, currentOffset - 2, charBuffer, false);
                            break;
                    }
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            return currentOffset;
        }

        private int getFirstAttributeOffset()
        {
            int currentOffset = header + 8 + readUnsignedShort(this.header + 6) * 2;
            int fieldsCount = readUnsignedShort(currentOffset);
            currentOffset += 2;
            while (fieldsCount-- > 0)
            {
                int attributesCount = readUnsignedShort(currentOffset + 6);
                currentOffset += 8;
                while (attributesCount-- > 0)
                {
                    currentOffset += 6 + readInt(currentOffset + 2);
                }
            }

            int methodsCount = readUnsignedShort(currentOffset);
            currentOffset += 2;
            while (methodsCount-- > 0)
            {
                int attributesCount = readUnsignedShort(currentOffset + 6);
                currentOffset += 8;
                while (attributesCount-- > 0)
                {
                    currentOffset += 6 + readInt(currentOffset + 2);
                }
            }

            return currentOffset + 2;
        }

        private int readInt(int offset)
        {
            return ((this.bytecode[offset] & 0xFF) << 24)
                    | ((this.bytecode[offset + 1] & 0xFF) << 16)
                    | ((this.bytecode[offset + 2] & 0xFF) << 8)
                    | (this.bytecode[offset + 3] & 0xFF);
        }

        private int readUnsignedShort(int offset)
        {
            return ((this.bytecode[offset] & 0xFF) << 8) | (this.bytecode[offset + 1] & 0xFF);
        }

        private void writeUnsignedShort(int offset,
                                        int value)
        {
            this.bytecode[offset] = (byte) ((value >> 8) & 0xFF);
            this.bytecode[offset + 1] = (byte) (value & 0xFF);
        }

        public String readUTF8(final int offset, final char[] charBuffer)
        {
            int constantPoolEntryIndex = readUnsignedShort(offset);
            if (offset == 0 || constantPoolEntryIndex == 0) {
                return null;
            }
            return readUtf(constantPoolEntryIndex, charBuffer);
        }

        private String readUtf(final int constantPoolEntryIndex, final char[] charBuffer)
        {
            int cpInfoOffset = cpInfoOffsets[constantPoolEntryIndex];
            return readUtf(cpInfoOffset + 2, readUnsignedShort(cpInfoOffset), charBuffer);
        }

        private String readUtf(final int utfOffset,
                               final int utfLength,
                               final char[] charBuffer)
        {
            int currentOffset = utfOffset;
            int endOffset = currentOffset + utfLength;
            int strLength = 0;
            byte[] classBuffer = this.bytecode;
            while (currentOffset < endOffset) {
                int currentByte = classBuffer[currentOffset++];
                if ((currentByte & 0x80) == 0) {
                    charBuffer[strLength++] = (char) (currentByte & 0x7F);
                } else if ((currentByte & 0xE0) == 0xC0) {
                    charBuffer[strLength++] =
                            (char) (((currentByte & 0x1F) << 6) + (classBuffer[currentOffset++] & 0x3F));
                } else {
                    charBuffer[strLength++] =
                            (char)
                                    (((currentByte & 0xF) << 12)
                                            + ((classBuffer[currentOffset++] & 0x3F) << 6)
                                            + (classBuffer[currentOffset++] & 0x3F));
                }
            }
            return new String(charBuffer, 0, strLength);
        }
    }
}
