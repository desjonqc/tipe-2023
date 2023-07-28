package com.cegesoft.data.handlers;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.Storage;
import com.cegesoft.data.exception.ParseFromFileException;
import lombok.Getter;

import java.io.IOException;

public class StorageReader<T extends ByteStorable> {

    private final StorageHandler storageHandler;
    private final Class<T> tClass;
    private Storage currentStorage;
    @Getter
    private int storageIndex = -1;
    @Getter
    private int currentStorageIndex = -1;

    public StorageReader(StorageHandler storageHandler, Class<T> tClass) {
        this.storageHandler = storageHandler;
        this.tClass = tClass;
    }

    public T next() throws IOException, ParseFromFileException {
        if (currentStorage == null || currentStorageIndex + 1 >= currentStorage.getGroupsAmount()) {
            if (storageIndex + 1 >= storageHandler.size())
                throw new IndexOutOfBoundsException("No more storages to read");
            storageIndex++;
            currentStorage = storageHandler.get(storageIndex);
            currentStorageIndex = -1;
        }
        currentStorageIndex++;
        return currentStorage.getDataGroup(tClass, currentStorageIndex);
    }

    public T previous() throws IOException, ParseFromFileException {
        if (currentStorage == null || currentStorageIndex - 1 < 0) {
            if (storageIndex - 1 < 0)
                throw new IndexOutOfBoundsException("No more storages to read");
            storageIndex--;
            currentStorage = storageHandler.get(storageIndex);
            currentStorageIndex = storageHandler.getMaxStorableInAFile();
        }
        currentStorageIndex--;
        return currentStorage.getDataGroup(tClass, currentStorageIndex);
    }

    public boolean hasPrevious() {
        return storageIndex - 1 >= 0 || currentStorageIndex - 1 >= 0;
    }

    public boolean hasNext() {
        return storageIndex + 1 < storageHandler.size() || currentStorageIndex + 1 < currentStorage.getGroupsAmount();
    }

    public int getStorageSize() {
        return currentStorage.getGroupsAmount();
    }

    public int getStorageAmount() {
        return storageHandler.size();
    }

    public Class<T> getStorableClass() {
        return tClass;
    }

}
