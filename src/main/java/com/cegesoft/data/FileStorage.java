package com.cegesoft.data;

import com.cegesoft.data.metadata.FileMetadata;
import lombok.Getter;
import lombok.NonNull;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.file.Files;

public class FileStorage<T extends FileMetadata> {

    @Getter
    private final String path;
    @Getter
    private final T metadata;
    private final Class<T> metaClass;
    @Getter
    private final File file;

    private FileStorage(String path, T metadata, Class<T> metaClass) throws IOException {
        this.path = path;
        this.metaClass = metaClass;
        this.file = new File(path);
        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        if (!file.exists()) {
            file.createNewFile();
        }
        if (metadata == null) {
            try {
                this.metadata = this.readMetadata();
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                throw new IOException("Can't create Metadata object with class '" + metaClass.getName() + "'");
            }
        } else {
            this.metadata = metadata;
        }
    }

    public FileStorage(String path, Class<T> metaClass) throws IOException {
        this(path, null, metaClass);
    }

    public FileStorage(String path, @NonNull T metadata) throws IOException {
        this(path, metadata, (Class<T>) metadata.getClass());
    }

    public void write(Storage merge) throws IOException {
        if (!this.metadata.equals(merge.getMetadata()))
            throw new IOException("Data group size mismatch");
        DataOutputStream out = new DataOutputStream(Files.newOutputStream(file.toPath()));
        out.write(ByteBuffer.allocate(4).putInt(this.metadata.size()).array());
        out.write(this.metadata.toBytes());
        out.write(merge.getData());
        out.close();
    }

    public Storage read() throws IOException {
        DataInputStream in = new DataInputStream(Files.newInputStream(file.toPath()));
        byte[] data = new byte[(int) file.length() - this.metadata.size() - 4];
        byte[] dataSize = new byte[this.metadata.size() + 4];
        if (in.read(dataSize, 0, this.metadata.size() + 4) != this.metadata.size() + 4)
            throw new IOException("Corrupted file.");
        in.readFully(data);
        in.close();
        return new Storage(this.metadata, data);
    }

    private T readMetadata() throws IOException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DataInputStream in = new DataInputStream(Files.newInputStream(file.toPath()));
        byte[] dataSize = new byte[4];
        if (in.read(dataSize, 0, 4) != 4)
            throw new IOException("Corrupted file.");
        int metaSize = ByteBuffer.wrap(dataSize).getInt();
        byte[] metaBytes = new byte[metaSize];
        if (in.read(metaBytes, 0, metaSize) != metaSize)
            throw new IOException("Corrupted file.");
        T metadata = (T) metaClass.getDeclaredMethod("empty").invoke(null);
        metadata.fromBytes(metaBytes);
        return metadata;
    }

}
