package com.cegesoft.comparator;

import java.io.IOException;

public class PythonBridge {


    public static String callPythonScript(String path, String... args) {
        try {
            Process process = Runtime.getRuntime().exec("python " + path + " " + String.join(" ", args));
            process.waitFor();
            return new String(process.getInputStream().readAllBytes());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
