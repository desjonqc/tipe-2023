package com.cegesoft.opencl;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.util.IOUtils;
import lombok.Getter;

import java.io.IOException;
import java.net.URL;

/**
 * Classe représentant un fichier de code OpenCL
 */
@Getter
public class CLFile {

    private final CLProgram program;

    /**
     * Charge un fichier OpenCL depuis une URL
     * @param fileUrl l'url
     * @param context le contexte OpenCL
     * @throws IOException si le fichier n'est pas lisible
     */
    public CLFile(URL fileUrl, CLContext context) throws IOException {
        this.program = context.createProgram(IOUtils.readText(fileUrl));
    }

    /**
     * Charge un fichier OpenCL depuis le classpath
     * @param fileName le nom du fichier
     * @param context le contexte OpenCL
     * @throws IOException si le fichier n'est pas lisible
     */
    public CLFile(String fileName, CLContext context) throws IOException {
        this(ClassLoader.getSystemResource(fileName), context);
    }
}
