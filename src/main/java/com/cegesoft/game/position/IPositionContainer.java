package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;

public interface IPositionContainer extends ByteStorable {

    BoardPosition getBoardPosition();

}
