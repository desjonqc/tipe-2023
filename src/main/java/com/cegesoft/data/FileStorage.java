package com.cegesoft.data;

import java.io.*;
import java.nio.file.Files;

public class FileStorage {

    private final String path;
    private final int dataGroupSize;
    private final File file;

    public FileStorage(String path, int dataGroupSize) {
        this.path = path;
        this.dataGroupSize = dataGroupSize;
        this.file = new File(path);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void write(Storage merge) {
        if (merge.getDataGroupSize() != dataGroupSize)
            throw new IllegalArgumentException("Data group size mismatch");
        try {
            DataOutputStream out = new DataOutputStream(Files.newOutputStream(file.toPath()));
            out.write(merge.getData());
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Storage read() {
        try {
            DataInputStream in = new DataInputStream(Files.newInputStream(file.toPath()));
            byte[] data = new byte[(int) file.length()];
            in.readFully(data);
            in.close();
            return new Storage(dataGroupSize, data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
