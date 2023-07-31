package com.cegesoft.data.handlers;

import com.cegesoft.TestApp;
import com.cegesoft.data.exception.StorageInitialisationException;
import com.cegesoft.data.metadata.DefaultFileMetadata;
import com.cegesoft.game.Board;
import com.cegesoft.game.position.BoardPosition;
import junit.framework.Assert;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StorageHandlerTest extends TestCase {

    public StorageHandlerTest() throws IOException {
        new TestApp();
    }

    public void testHandler() throws StorageInitialisationException {
        StorageHandler handler = new StorageHandler("test/", "data", 15, DefaultFileMetadata.class);
        for (int i = 0; i < 16; i++)
            handler.addStorable(Board.INITIAL_POSITION);

        handler.waitForStore();
        handler.close();
        StorageHandler handler2 = new StorageHandler("test/", "data", 15, DefaultFileMetadata.class);
        List<BoardPosition> positions = handler2.listStorable(BoardPosition.class);
        Assert.assertEquals(16, positions.size());
        for (int i = 0; i < 16; i++)
            Assert.assertEquals(positions.get(i), Board.INITIAL_POSITION);
        handler2.close();

        TestApp.clearDirectory();
    }

}