package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import lombok.Getter;
import org.bridj.Pointer;

public class CLField<T> {

    @Getter
    protected final CLHandler handler;
    @Getter
    protected final CLMem.Usage type;
    @Getter
    protected final long size;
    @Getter
    protected final Class<T> tClass;
    @Getter
    protected Object argument;
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
