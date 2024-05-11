package com.cegesoft.data.handlers;

import com.cegesoft.data.ByteStorable;
import com.cegesoft.data.FileStorage;
import com.cegesoft.data.Storage;
import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.exception.StorageInitialisationException;
import com.cegesoft.data.metadata.FileMetadata;
import com.cegesoft.log.Logger;
import lombok.Getter;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Gère le stockage de données dans des fichiers.
 *
 * <p>Pour éviter de corrompre les données, on enregistre un nombre fini de Storage par fichier.
 * Cette classe s'occupe de créer de nouveaux fichiers si le nombre maximal de Storage est atteint ou d'ajouter les données
 * dans un fichier existant.</p>
 *
 * <p>
 *     Pour ajouter des données, il suffit d'appeler la méthode {@link #addStorable(ByteStorable)}.
 *     Les données seront ajoutées de manière asynchrone pour éviter les problèmes de concurrence.
 * </p>
 *
 * @see FileStorage
 */
public class StorageHandler implements Closeable {

    @Getter
    private final int maxStorableInAFile;
    @Getter
    private final String baseDirectoryPath;
    @Getter
    private final String extension;
    private final ArrayList<FileStorage<?>> storages = new ArrayList<>();
    private int currentId;
    private final AsyncStorageRegisterer registerer;

    public StorageHandler(String baseDirectoryPath, String extension, int maxStorableInAFile, Class<? extends FileMetadata> metaClass) throws StorageInitialisationException {
        this.registerer = new AsyncStorageRegisterer();
        this.maxStorableInAFile = maxStorableInAFile;
        this.baseDirectoryPath = baseDirectoryPath;
        this.extension = extension;
        this.initStorages(metaClass);
    }

    /**
     * Initialise les fichiers de stockage et lit les fichiers existants.
     * @param metaClass la classe des métadonnées des fichiers
     * @throws StorageInitialisationException si une erreur survient lors de l'initialisation des fichiers
     */
    private void initStorages(Class<? extends FileMetadata> metaClass) throws StorageInitialisationException {
        File directory = new File(this.baseDirectoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        } else if (!directory.isDirectory()) {
            throw new StorageInitialisationException("Path provided is not a directory (" + this.baseDirectoryPath + ")");
        }

        for (File file : directory.listFiles()) {
            if (file.isFile() && file.getName().endsWith("." + this.extension)) {
                try {
                    int id;
                    try {
                        id = Integer.parseInt(file.getName().split("\\.")[0]);
                    } catch (NumberFormatException e) {
                        Logger.warn("Skipping " + file.getName());
                        continue;
                    }
                    FileStorage<?> fileStorage = new FileStorage<>(file.getPath(), metaClass);
                    Storage storage = fileStorage.read();
                    storages.add(id, fileStorage);
                    if (currentId <= id)
                        currentId = id + (storage.getGroupsAmount() < this.maxStorableInAFile ? 0 : 1);
                } catch (IOException e) {
                    Logger.warn("Can't load data from " + file.getName() + ". Skipping", e);
                }
            }
        }

        Logger.info("Loaded " + storages.size() + " storages !");
    }

    /**
     * Ajoute un ByteStorable à stocker.
     * @param storable le ByteStorable à stocker
     */
    public void addStorable(ByteStorable storable) {
        this.registerer.addStorable(storable);
    }

    /**
     * Ajoute un ByteStorable à stocker de manière synchrone.
     * @param storable le ByteStorable à stocker
     */
    private void addStorableSync(ByteStorable storable) {
        if (storages.size() != 0) {
            FileStorage<?> fileStorage = this.storages.get(this.currentId);
            try {
                Storage storage = fileStorage.read();
                if (storage.getGroupsAmount() < this.maxStorableInAFile) {
                    Storage newStorage = storage.addStorable(storable);
                    fileStorage.write(newStorage);
                    Logger.info("Stored new data in " + fileStorage.getFile().getName() + " !");
                    return;
                }
            } catch (IOException e) {
                Logger.error("Can't edit current file :", e);
                return;
            } catch (StorageInitialisationException e) {
                Logger.error("Can't merge storages :", e);
                return;
            }
            currentId++;
        }
        Logger.info("Current file is full, creating a new one...");
        try {
            Storage storage = new Storage(storable);
            if (storages.size() > 0 && !storage.getMetadata().equals(storages.get(0).getMetadata())) {
                Logger.error("Can't add new Storable, metadata mismatch");
                return;
            }

            try {
                File file = new File(this.baseDirectoryPath, currentId + "." + extension);
                FileStorage<?> fileStorage = new FileStorage<>(file.getPath(), storable.getMetadata());
                this.storages.add(currentId, fileStorage);
                fileStorage.write(storage);
                Logger.info("Stored new data in " + file.getName() + " !");
            } catch (IOException e) {
                Logger.error("Can't create new storage", e);
            }
        } catch (StorageInitialisationException e) {
            Logger.error(e);
        }
    }

    /**
     * Récupère un Storage à partir de son indice.
     * @param i l'indice du Storage
     * @return le Storage
     * @throws IOException si une erreur d'entrée/sortie survient
     */
    public Storage get(int i) throws IOException {
        return this.storages.get(i).read();
    }

    /**
     * Attend que tous les ByteStorable en attente soient stockés.
     */
    public void waitForStore() {
        this.registerer.waitForStore();
    }

    /**
     * Récupère tous les ByteStorable stockés. <br>
     * Cette méthode est dépréciée, utilisez plutôt {@link #getReader(Class)}.
     *
     * @param tClass la classe des ByteStorable
     * @param <T> le type des ByteStorable
     * @return la liste des ByteStorable
     */
    @Deprecated
    public <T extends ByteStorable> List<T> listStorable(Class<T> tClass) {
        List<T> storableList = new ArrayList<>();
        for (FileStorage<?> fileStorage : this.storages) {
            try {
                Storage storage = fileStorage.read();
                for (int i = 0; i < storage.getGroupsAmount(); i++) {
                    storableList.add(storage.getDataGroup(tClass, i));
                }
            } catch (IOException e) {
                Logger.error("Unable to read data file :", e);
            } catch (ParseFromFileException e) {
                Logger.error(e);
            }
        }
        return storableList;
    }

    /**
     * Récupère un StorageReader pour lire les données stockées.
     * @param tClass la classe des données à lire
     * @param <T> le type des données à lire
     * @return le StorageReader
     *
     * @see StorageReader
     */
    public <T extends ByteStorable> StorageReader<T> getReader(Class<T> tClass) {
        return new StorageReader<>(this, tClass);
    }

    /**
     * @return le nombre de Storage stockés
     */
    public int size() {
        return this.storages.size();
    }

    /**
     * Ferme le StorageHandler : attend que tous les ByteStorable en attente soient stockés et arrête le thread de stockage.
     */
    @Override
    public void close() {
        this.waitForStore();
        this.registerer.interrupt();
    }

    /**
     * Thread permettant de stocker les données de manière asynchrone.
     */
    private class AsyncStorageRegisterer extends Thread {

        private final Object lock = new Object();
        /**
         * File d'attente des ByteStorable à stocker.
         */
        private final Queue<ByteStorable> storableQueue = new ConcurrentLinkedQueue<>();

        public AsyncStorageRegisterer() {
            this.start();
        }

        /**
         * Ajoute un ByteStorable à la file d'attente.
         * @param storable le ByteStorable à ajouter
         */
        public void addStorable(ByteStorable storable) {
            this.storableQueue.add(storable);
        }

        /**
         * Attend que tous les ByteStorable en attente soient stockés.
         */
        public void waitForStore() {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    Logger.error(e);
                }
            }
        }

        @Override
        public void run() {
            ByteStorable storable;
            while (!this.isInterrupted()) {
                while ((storable = storableQueue.poll()) != null) {
                    try {
                        StorageHandler.this.addStorableSync(storable);
                    } catch (Exception e) {
                        Logger.error("Unable to store storable :", e);
                    }
                }
                synchronized (lock) {
                    lock.notifyAll();
                }
            }
        }
    }
}
