package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.util.IOUtils;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;

public class CLFile {

    @Getter
    private final CLProgram program;
    public CLFile(URL file, CLContext context) throws IOException {
        this.program = context.createProgram(IOUtils.readText(file));
    }

    public CLFile(String fileName, CLContext context) throws IOException {
        this(ClassLoader.getSystemResource(fileName), context);
    }
}
