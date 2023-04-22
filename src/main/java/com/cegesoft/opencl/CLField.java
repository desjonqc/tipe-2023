package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

public class CLField<T> {

    private final CLHandler handler;
    private CLMem.Usage type;
    private long size;
    private final Class<T> tClass;
    private Object argument;
    public CLField(CLHandler handler, CLMem.Usage type, Class<T> tClass, long size) {
        this.type = type;
        this.size = size;
        this.tClass = tClass;
        this.handler = handler;
        if (type != CLMem.Usage.Input || size != 1) {
            argument = handler.getContext().createBuffer(type, tClass, size);
        }
    }

    public CLField(CLHandler handler, Class<T> tClass, T value) {
        this(handler, CLMem.Usage.Input, tClass, 1);
        this.argument = value;
    }

    public CLMem.Usage getType() {
        return type;
    }

    public long getSize() {
        return size;
    }

    public Class<T> gettClass() {
        return tClass;
    }

    public Object getArgument() {
        return argument;
    }

    public CLEvent setValue(CLQueue queue, long index, T value) {
        if (this.argument instanceof CLBuffer) {
            Pointer<T> pointer = ((CLBuffer<T>) this.argument).read(queue);
            pointer.set(index, value);
            return ((CLBuffer<T>) this.argument).write(queue, pointer, false);
        }
        return null;
    }

    public CLField<T> duplicate(CLQueue queue) {
        if (this.argument instanceof CLBuffer) {
            CLField<T> field = new CLField<>(this.handler, this.type, this.tClass, this.size);
            ((CLBuffer<?>) this.argument).copyTo(queue, (CLBuffer<T>) field.argument).waitFor();
            return field;
        }
        return new CLField<>(this.handler, this.tClass, (T) this.argument);
    }
}
