package com.cegesoft.data;

import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.exception.StorageInitialisationException;
import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public class Storage {

    @Getter
    private final FileMetadata metadata;
    @Getter
    private final byte[] data;

    public Storage(FileMetadata metadata, byte[] data) {
        this.metadata = metadata;
        this.data = data;
    }

    public Storage(FileMetadata metadata, byte[][] groups) {
        this.metadata = metadata;

        this.data = new byte[metadata.getDataGroupSize() * groups.length];
        for (int i = 0; i < groups.length; i++) {
            System.arraycopy(groups[i], 0, data, i * metadata.getDataGroupSize(), metadata.getDataGroupSize());
        }
    }

    public Storage(ByteStorable... groups) throws StorageInitialisationException {
        this(getCommonMetadata(groups), parseBytes(groups));
    }

    public Storage(Storage base, ByteStorable... groups) throws StorageInitialisationException {
        if (!base.getMetadata().equals(getCommonMetadata(groups)))
            throw new StorageInitialisationException("Data group size mismatch");
        this.metadata = base.getMetadata();
        this.data = new byte[metadata.getDataGroupSize() * groups.length + base.data.length];
        System.arraycopy(base.data, 0, data, 0, base.data.length);
        for (int i = 0; i < groups.length; i++) {
            System.arraycopy(groups[i].toBytes(), 0, data, base.data.length + i * metadata.getDataGroupSize(), metadata.getDataGroupSize());
        }
    }

    public Storage addStorable(ByteStorable... groups) throws StorageInitialisationException {
        return new Storage(this, groups);
    }

    private static FileMetadata getCommonMetadata(ByteStorable... groups) throws StorageInitialisationException {
        FileMetadata meta = groups[0].getMetadata();
        for (ByteStorable group : groups) {
            if (!group.getMetadata().equals(meta))
                throw new StorageInitialisationException("Data group size mismatch");
        }
        return meta;
    }

    private static byte[][] parseBytes(ByteStorable... groups) {
        byte[][] result = new byte[groups.length][];
        for (int i = 0; i < groups.length; i++) {
            result[i] = groups[i].toBytes();
        }
        return result;
    }

    public byte[] getDataGroup(int index) {
        byte[] result = new byte[this.metadata.getDataGroupSize()];
        System.arraycopy(data, index * this.metadata.getDataGroupSize(), result, 0, this.metadata.getDataGroupSize());
        return result;
    }

    public <T extends ByteStorable> T getDataGroup(Class<T> tClass, int index) throws ParseFromFileException {
        try {
            Constructor<T> constructor = tClass.getConstructor();
            constructor.setAccessible(true);
            T result = constructor.newInstance();
            result.setMetadata(this.metadata);
            result.fromBytes(getDataGroup(index));
            return result;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException e) {
            throw new ParseFromFileException("Can't import data from file to '" + tClass.getName() + "' model", e);
        } catch (WrongFileMetadataException e) {
            throw new ParseFromFileException("Incompatible metadata. Good luck...", e);
        }
    }

    public int getGroupsAmount() {
        return this.data.length / this.metadata.getDataGroupSize();
    }

}
