package com.cegesoft.data;

public interface ByteStorable {

    byte[] toBytes();

    void fromBytes(byte[] bytes);

    int size();

    static ByteStorable empty() {
        return null;
    }
}
