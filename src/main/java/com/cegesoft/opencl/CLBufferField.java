package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import com.nativelibs4java.opencl.CLQueue;
import org.bridj.Pointer;

public class CLBufferField<T> extends CLField<T> {

    public CLBufferField(CLHandler handler, CLMem.Usage type, Class<T> tClass, long size) {
        super(handler, type, tClass, size);
    }

    @Override
    public CLBuffer<T> getArgument() {
        return (CLBuffer<T>) super.getArgument();
    }

    public CLEvent setValues(CLQueue queue, T[] values) {
        Pointer<T> pointer = getArgument().read(queue);
        for (int i = 0; i < values.length; i++) {
            pointer.set(i, values[i]);
        }
        return getArgument().write(queue, pointer, false);
    }
}
