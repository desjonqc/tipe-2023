package com.cegesoft.data;

import lombok.Getter;

import java.lang.reflect.InvocationTargetException;

public class Storage {

    @Getter
    private final int dataGroupSize;
    @Getter
    private final byte[] data;

    public Storage(int dataGroupSize, byte[]... groups) {
        this.dataGroupSize = dataGroupSize;

        this.data = new byte[dataGroupSize * groups.length];
        for (int i = 0; i < groups.length; i++) {
            System.arraycopy(groups[i], 0, data, i * dataGroupSize, dataGroupSize);
        }
    }

    public Storage(ByteStorable... groups) {
        this(getGroupSize(groups), parseBytes(groups));
    }

    public Storage(Storage base, ByteStorable... groups) {
        if (base.dataGroupSize != getGroupSize(groups))
            throw new IllegalArgumentException("Data group size mismatch");
        this.dataGroupSize = base.dataGroupSize + getGroupSize(groups);
        this.data = new byte[dataGroupSize * groups.length + base.data.length];
        System.arraycopy(base.data, 0, data, 0, base.data.length);
        for (int i = 0; i < groups.length; i++) {
            System.arraycopy(groups[i].toBytes(), 0, data, base.data.length + i * dataGroupSize, dataGroupSize);
        }
    }

    private static int getGroupSize(ByteStorable... groups) {
        int size = groups[0].size();
        for (ByteStorable group : groups) {
            if (group.size() != size)
                throw new IllegalArgumentException("Data group size mismatch");
        }
        return size;
    }

    private static byte[][] parseBytes(ByteStorable... groups) {
        byte[][] result = new byte[groups.length][];
        for (int i = 0; i < groups.length; i++) {
            result[i] = groups[i].toBytes();
        }
        return result;
    }

    public byte[] getDataGroup(int index) {
        byte[] result = new byte[dataGroupSize];
        System.arraycopy(data, index * dataGroupSize, result, 0, dataGroupSize);
        return result;
    }

    public <T extends ByteStorable> T getDataGroup(Class<T> tClass, int index) {
        try {
            T result = (T) tClass.getDeclaredMethod("empty").invoke(null);
            result.fromBytes(getDataGroup(index));
            return result;
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
