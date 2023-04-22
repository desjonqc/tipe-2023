package com.cegesoft.opencl;

import com.nativelibs4java.opencl.*;

public class CLHandler {
    private final CLContext context;

    public CLHandler() {
        this.context = JavaCL.createBestContext(CLPlatform.DeviceFeature.GPU);
    }

    public <T> CLBuffer<T> createBuffer(Class<T> tClass, CLMem.Usage usage, int size) {
        return this.context.createBuffer(usage, tClass, size);
    }

    public CLQueue createQueue() {
        return this.context.createDefaultQueue();
    }

    public CLContext getContext() {
        return context;
    }

    public void release() {
        this.context.release();
    }

    //4294959104
    //3221061632
}
