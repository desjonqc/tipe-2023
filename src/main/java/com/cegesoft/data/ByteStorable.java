package com.cegesoft.data;

import com.cegesoft.data.exception.WrongFileMetadataException;
import com.cegesoft.data.metadata.FileMetadata;

public interface ByteStorable {

    byte[] toBytes();

    void fromBytes(byte[] bytes);

    int size();

    FileMetadata getMetadata();

    void setMetadata(FileMetadata meta) throws WrongFileMetadataException;

}
