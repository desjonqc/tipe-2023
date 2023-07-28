package com.cegesoft.data.metadata;

import com.cegesoft.data.ByteStorable;

public interface FileMetadata extends ByteStorable {
    short getDataGroupSize();
}
