package com.cegesoft.data;

import lombok.Getter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class FileStorage {

    @Getter
    private final String path;
    private final int dataGroupSize;
    @Getter
    private final File file;

    public FileStorage(String path, int dataGroupSize) throws IOException {
        this.path = path;
        this.file = new File(path);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        if (dataGroupSize == -1) {
            this.dataGroupSize = this.readDataGroupSize();
        } else {
            this.dataGroupSize = dataGroupSize;
        }
    }

    public FileStorage(String path) throws IOException {
        this(path, -1);
    }

    public void write(Storage merge) throws IOException {
        if (merge.getDataGroupSize() != dataGroupSize)
            throw new IOException("Data group size mismatch");
        ByteBuffer sizeBuffer = ByteBuffer.allocate(4);
        sizeBuffer.putInt(dataGroupSize);
        DataOutputStream out = new DataOutputStream(Files.newOutputStream(file.toPath()));
        out.write(sizeBuffer.array());
        out.write(merge.getData());
        out.close();
    }

    public Storage read() throws IOException {
        DataInputStream in = new DataInputStream(Files.newInputStream(file.toPath()));
        byte[] data = new byte[(int) file.length() - 4];
        byte[] dataSize = new byte[4];
        if (in.read(dataSize, 0, 4) != 4)
            throw new IOException("Corrupted file.");
        int size = ByteBuffer.wrap(dataSize).getInt();
        if (size != dataGroupSize)
            throw new IOException("Wrong dataGroupSize");
        in.readFully(data);
        in.close();
        return new Storage(dataGroupSize, data);
    }

    public int readDataGroupSize() throws IOException {
        DataInputStream in = new DataInputStream(Files.newInputStream(file.toPath()));
        byte[] dataSize = new byte[4];
        if (in.read(dataSize, 0, 4) != 4)
            throw new IOException("Corrupted file.");
        return ByteBuffer.wrap(dataSize).getInt();
    }

}
