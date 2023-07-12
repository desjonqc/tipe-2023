package com.cegesoft.data.metadata;

import com.cegesoft.data.exception.WrongFileMetadataException;
import lombok.Getter;

import java.nio.ByteBuffer;
import java.util.Objects;

public class DefaultFileMetadata implements FileMetadata {

    @Getter
    protected int dataGroupSize;

    public DefaultFileMetadata(int dataGroupSize) {
        this.dataGroupSize = dataGroupSize;
    }

    protected DefaultFileMetadata() {}

    public static DefaultFileMetadata empty() {
        return new DefaultFileMetadata();
    }

    @Override
    public byte[] toBytes() {
        return ByteBuffer.allocate(4).putInt(this.dataGroupSize).array();
    }

    @Override
    public void fromBytes(byte[] bytes) {
        this.dataGroupSize = ByteBuffer.wrap(bytes).getInt();
    }

    @Override
    public int size() {
        return 4;
    }

    @Override
    public FileMetadata getMetadata() {
        return this;
    }

    @Override
    public void setMetadata(FileMetadata meta) throws WrongFileMetadataException {}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DefaultFileMetadata that = (DefaultFileMetadata) o;
        return dataGroupSize == that.dataGroupSize;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataGroupSize);
    }
}
