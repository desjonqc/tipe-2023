package com.cegesoft.data;

import com.cegesoft.TestApp;
import com.cegesoft.data.exception.ParseFromFileException;
import com.cegesoft.data.exception.StorageInitialisationException;
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
        FileStorage fileStorage = new FileStorage("test/write_read.data", 128);
        Storage storage = new Storage(Board.INITIAL_POSITION, Board.INITIAL_POSITION);
        fileStorage.write(storage);

        FileStorage readFStorage = new FileStorage("test/write_read.data");
        Storage rStorage = readFStorage.read();
        Assert.assertEquals(rStorage.getGroupsAmount(), 2);
        Assert.assertEquals(Board.INITIAL_POSITION, rStorage.getDataGroup(BoardPosition.class, 0));
        Assert.assertEquals(Board.INITIAL_POSITION, rStorage.getDataGroup(BoardPosition.class, 1));
        Assert.assertEquals(readFStorage.readDataGroupSize(), 128);

        TestApp.clearDirectory();
    }
}