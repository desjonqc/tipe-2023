package com.cegesoft.data;

import com.cegesoft.TestApp;
import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.exception.StorageInitialisationException;
import com.cegesoft.data.metadata.DefaultFileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.position.BoardPosition;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.IOException;

public class FileStorageTest extends TestCase {

    public FileStorageTest() throws IOException {
        new TestApp();
    }

    public void testWriteRead() throws IOException, ParseFromFileException, StorageInitialisationException {
        FileStorage<DefaultFileMetadata> fileStorage = new FileStorage<>("test/write_read.data", new DefaultFileMetadata(128));
        Storage storage = new Storage(Board.INITIAL_POSITION, Board.INITIAL_POSITION);
        fileStorage.write(storage);

        FileStorage<DefaultFileMetadata> readFStorage = new FileStorage<>("test/write_read.data", DefaultFileMetadata.class);
        Storage rStorage = readFStorage.read();
        Assert.assertEquals(2, rStorage.getGroupsAmount());
        Assert.assertEquals(Board.INITIAL_POSITION, rStorage.getDataGroup(BoardPosition.class, 0));
        Assert.assertEquals(Board.INITIAL_POSITION, rStorage.getDataGroup(BoardPosition.class, 1));

        TestApp.clearDirectory();
    }
}