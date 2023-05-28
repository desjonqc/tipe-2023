package com.cegesoft.opencl;

import com.nativelibs4java.opencl.*;
import lombok.Getter;

import java.io.IOException;

public class CLHandler {
    @Getter
    private final CLContext context;
    @Getter
    private final CLFile boardFile;

    public CLHandler() throws IOException {
        this.context = JavaCL.createBestContext(CLPlatform.DeviceFeature.GPU);
        this.boardFile = new CLFile("board.cl", this.context);
    }

    public <T> CLBuffer<T> createBuffer(Class<T> tClass, CLMem.Usage usage, int size) {
        return this.context.createBuffer(usage, tClass, size);
    }

    public CLQueue createQueue() {
        return this.context.createDefaultQueue();
    }

    public void release() {
        this.context.release();
    }

    //4294959104
    //3221061632
}
