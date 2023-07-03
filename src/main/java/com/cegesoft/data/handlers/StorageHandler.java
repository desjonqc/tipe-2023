package com.cegesoft.data.handlers;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.log.Logger;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class StorageHandler {

    @Getter
    private final int maxStorableInAFile;
    @Getter
    private final String baseDirectoryPath;
    @Getter
    private final int dataGroupSize;
    @Getter
    private final String extension;
    private final ArrayList<FileStorage> storages = new ArrayList<>();
    @Getter
    protected SimulationInformation information;
    private int currentId;

    public StorageHandler(String baseDirectoryPath, String extension, int maxStorableInAFile, int dataGroupSize) {
        this.maxStorableInAFile = maxStorableInAFile;
        this.baseDirectoryPath = baseDirectoryPath;
        this.dataGroupSize = dataGroupSize;
        this.extension = extension;
        this.information = null;
        this.initStorages();
    }

    private void initStorages() {
        File directory = new File(this.baseDirectoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Path provided is not a directory (" + this.baseDirectoryPath + ")");
        }

        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith("." + this.extension)) {
                try {
                    int id;
                    try {
                        id = Integer.parseInt(file.getName().split("\\.")[0]);
                    } catch (NumberFormatException e) {
                        Logger.getLogger().warn("Skipping " + file.getName() + ".");
                        continue;
                    }
                    FileStorage fileStorage = new FileStorage(file.getPath(), this.dataGroupSize);
                    Storage storage = fileStorage.read();
                    storages.add(id, fileStorage);
                    if (currentId <= id)
                        currentId = id + (storage.getGroupsAmount() < this.maxStorableInAFile ? 0 : 1);
                } catch (IOException e) {
                    Logger.getLogger().warn("Can't load data from " + file.getName() + ". Skipping");
                }
            }
        }

        Logger.getLogger().println("Loaded " + storages.size() + " storages !");
    }

    public void addStorable(ByteStorable storable) {
        if (storages.size() != 0) {
            FileStorage fileStorage = this.storages.get(this.currentId);
            try {
                Storage storage = fileStorage.read();
                if (storage.getGroupsAmount() < this.maxStorableInAFile) {
                    Storage newStorage = storage.addStorable(storable);
                    fileStorage.write(newStorage);
                    Logger.getLogger().println("Stored new data in " + fileStorage.getFile().getName() + " !");
                    return;
                }
                currentId++;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        Logger.getLogger().println("Current file is full, creating a new one...");
        Storage storage = new Storage(storable);
        if (storage.getDataGroupSize() != this.dataGroupSize) {
            throw new IllegalArgumentException("Data group size mismatch");
        }
        File file = new File(this.baseDirectoryPath, currentId + "." + extension);
        FileStorage fileStorage = new FileStorage(file.getPath(), this.dataGroupSize);
        this.storages.add(currentId, fileStorage);
        fileStorage.write(storage);
        Logger.getLogger().println("Stored new data in " + file.getName() + " !");
    }

    public Storage get(int i) {
        try {
            return this.storages.get(i).read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public <T extends ByteStorable> List<T> listStorable(Class<T> tClass) {
        List<T> storableList = new ArrayList<>();
        for (FileStorage fileStorage : this.storages) {
            try {
                Storage storage = fileStorage.read();
                for (int i = 0; i < storage.getGroupsAmount(); i++) {
                    storableList.add(storage.getDataGroup(tClass, i, this.information));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return storableList;
    }


}
