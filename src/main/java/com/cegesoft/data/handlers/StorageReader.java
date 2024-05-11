package com.cegesoft.data.handlers;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.Storage;
import com.cegesoft.data.exception.ParseFromFileException;
import lombok.Getter;

import java.io.IOException;

/**
 * Permet de lire des données stockées dans un StorageHandler de manière itérative.
 * @param <T> le type de données à lire.
 */
public class StorageReader<T extends ByteStorable> {

    private final StorageHandler storageHandler;
    private final Class<T> tClass;
    /**
     * Le Storage de travail.
     */
    private Storage currentStorage;
    @Getter
    private int storageIndex = -1;
    @Getter
    private int currentStorageIndex = -1;

    public StorageReader(StorageHandler storageHandler, Class<T> tClass) {
        this.storageHandler = storageHandler;
        this.tClass = tClass;
    }

    /**
     * Lit la prochaine donnée stockée.
     * @return la donnée lue
     * @throws IOException si une erreur d'entrée/sortie survient
     * @throws ParseFromFileException si une erreur de lecture du Storage survient
     */
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

    /**
     * Lit la donnée précédente stockée.
     * @return la donnée lue
     * @throws IOException si une erreur d'entrée/sortie survient
     * @throws ParseFromFileException si une erreur de lecture du Storage survient
     */
    public T previous() throws IOException, ParseFromFileException {
        if (currentStorage == null || currentStorageIndex - 1 < 0) {
            if (storageIndex - 1 < 0)
                throw new IndexOutOfBoundsException("No more storages to read");
            storageIndex--;
            currentStorage = storageHandler.get(storageIndex);
            currentStorageIndex = currentStorage.getGroupsAmount();
        }
        currentStorageIndex--;
        return currentStorage.getDataGroup(tClass, currentStorageIndex);
    }

    /**
     * @return true si une donnée précédente est disponible, false sinon
     */
    public boolean hasPrevious() {
        return storageIndex - 1 >= 0 || currentStorageIndex - 1 >= 0;
    }

    /**
     * @return true si une donnée suivante est disponible, false sinon
     */
    public boolean hasNext() {
        return storageIndex + 1 < storageHandler.size() || currentStorageIndex + 1 < currentStorage.getGroupsAmount();
    }

    /**
     * @return la quantité de données stockées dans le Storage de travail
     */
    public int getStorageSize() {
        return currentStorage.getGroupsAmount();
    }

    /**
     * @return le nombre de Storage stockés
     */
    public int getStorageAmount() {
        return storageHandler.size();
    }

    public Class<T> getStorableClass() {
        return tClass;
    }

}
