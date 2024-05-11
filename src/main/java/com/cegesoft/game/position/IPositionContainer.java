package com.cegesoft.game.position;

import com.cegesoft.data.ByteStorable;

/**
 * Interface contenant une position de billard.
 */
public interface IPositionContainer extends ByteStorable {

    BoardPosition getBoardPosition();

}
