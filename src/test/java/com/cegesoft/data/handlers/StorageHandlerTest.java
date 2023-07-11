package com.cegesoft.data.handlers;

import com.cegesoft.TestApp;
import com.cegesoft.data.exception.StorageInitialisationException;
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
        StorageHandler handler = new StorageHandler("test/", "data", 3, 128);
        handler.addStorable(Board.INITIAL_POSITION);
        handler.addStorable(Board.INITIAL_POSITION);
        handler.addStorable(Board.INITIAL_POSITION);
        handler.addStorable(Board.INITIAL_POSITION);

        StorageHandler handler2 = new StorageHandler("test/", "data", 3, 128);
        List<BoardPosition> positions = handler2.listStorable(BoardPosition.class);
        Assert.assertEquals(positions.size(), 4);
        for (int i = 0; i < 4; i++)
            Assert.assertEquals(positions.get(i), Board.INITIAL_POSITION);

        TestApp.clearDirectory();
    }

}