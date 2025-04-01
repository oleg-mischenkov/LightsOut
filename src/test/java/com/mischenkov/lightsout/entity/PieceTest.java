package com.mischenkov.lightsout.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class PieceTest {

    @Test
    void shouldGetCorrectPieceMatrix_getPieceMatrix() {
        // given
        var pieceVector = "X.,XX,XX";
        var piece = new Piece(pieceVector);
        int[][] expectedMatrix = {
                {1, 0},
                {1, 1},
                {1, 1}
        };

        // when
        int[][] pieceMatrix = piece.getPieceMatrix();

        // then
        for (int i = 0; i <pieceMatrix.length; i++) {
            Assertions.assertArrayEquals(expectedMatrix[i], pieceMatrix[i]);
        }
    }
}
