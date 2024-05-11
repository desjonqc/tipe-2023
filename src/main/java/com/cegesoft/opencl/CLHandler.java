package com.cegesoft.opencl;

import com.nativelibs4java.opencl.*;
import lombok.Getter;

import java.io.IOException;

/**
 * Classe permettant de gérer la base de JavaCL.
 */
@Getter
public class CLHandler {
    private final CLContext context;
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
}
