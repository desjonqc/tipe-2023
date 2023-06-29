package com.cegesoft.data.handlers;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.game.SimulationInformation;
import com.cegesoft.util.weighting.ScoreWeighting;
import lombok.Getter;
import org.bridj.util.Pair;

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
    private final ArrayList<Pair<FileStorage, Storage>> storages = new ArrayList<>();
    private int currentId;
    @Getter
    protected SimulationInformation information;

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
                        System.out.println("Skipping " + file.getName() + ".");
                        continue;
                    }
                    FileStorage fileStorage = new FileStorage(file.getPath(), this.dataGroupSize);
                    Storage storage = fileStorage.read();
                    storages.add(id, new Pair<>(fileStorage, storage));
                    if (currentId <= id)
                        currentId = id + (storage.getGroupsAmount() < this.maxStorableInAFile ? 0 : 1);
                } catch (IOException e) {
                    System.out.println("Can't load data from " + file.getName() + ". Skipping");
                }
            }
        }

        System.out.println("Loaded " + storages.size() + " storages !");
    }

    public void addStorable(ByteStorable storable, boolean save) {
        if (storages.size() != 0) {
            Pair<FileStorage, Storage> current = this.storages.get(this.currentId);
            if (current.getSecond().getGroupsAmount() < this.maxStorableInAFile) {
                Storage newStorage = current.getSecond().addStorable(storable);
                current.setSecond(newStorage);
                if (save || current.getSecond().getGroupsAmount() == this.maxStorableInAFile - 1) {
                    current.getFirst().write(newStorage);
                }
                System.out.println("Stored new data in " + current.getFirst().getFile().getName() + " !");
                return;
            }
            currentId++;
        }
        System.out.println("Current file is full, creating a new one...");
        Storage storage = new Storage(storable);
        if (storage.getDataGroupSize() != this.dataGroupSize) {
            throw new IllegalArgumentException("Data group size mismatch");
        }
        File file = new File(this.baseDirectoryPath, currentId + "." + extension);
        FileStorage fileStorage = new FileStorage(file.getPath(), this.dataGroupSize);
        this.storages.add(currentId, new Pair<>(fileStorage, storage));
        if (save)
            fileStorage.write(storage);
        System.out.println("Stored new data in " + file.getName() + " !");
    }

    public Storage get(int i) {
        return this.storages.get(i).getSecond();
    }

    public <T extends ByteStorable> List<T> listStorable(Class<T> tClass) {
        List<T> storableList = new ArrayList<>();
        for (Pair<FileStorage, Storage> entry : this.storages) {
            if (entry != null && entry.getSecond() != null) {
                for (int i = 0; i < entry.getSecond().getGroupsAmount(); i++) {
                    storableList.add(entry.getSecond().getDataGroup(tClass, i, this.information));
                }
            }
        }
        return storableList;
    }


}
