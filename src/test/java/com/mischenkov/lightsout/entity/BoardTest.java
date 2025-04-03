package com.mischenkov.lightsout.entity;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class BoardTest {

    @Test
    void shouldGetCorrectBoardMatrix_getBoardMatrixTest() {
        // given
        var boardVector = "0100,0110,1010,1110";
        var board = new Board(2, boardVector);
        int[][] expectedMatrix = {
                {0, 1, 0, 0},
                {0, 1, 1, 0},
                {1, 0, 1, 0},
                {1, 1, 1, 0}
        };

        // when
        int[][] boardMatrix = board.getBoardMatrix();

        // then
        for (int i = 0; i < boardMatrix.length; i++) {
            assertArrayEquals(expectedMatrix[i], boardMatrix[i]);
        }
    }

    @ParameterizedTest
    @CsvSource({
            "2, '0100,0110,1010,1110', '.XX,XXX', 0, 0, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 1, 0, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 2, 0, false",
            "2, '0100,0110,1010,1110', '.XX,XXX', 0, 1, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 0, 2, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 0, 3, false",
            "2, '0100,0110,1010,1110', '.XX,XXX', 1, 1, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 1, 2, true",
            "2, '0100,0110,1010,1110', '.XX,XXX', 2, 2, false"
    })
    void isEmbedPieceTest(int depth, String boardVector, String pieceVector, int pieceX, int pieceY, boolean expected) {
        // given
        var board = new Board(depth, boardVector);
        var piece = new Piece(pieceVector);
        var position = new Position(pieceX, pieceY);

        // when
        var result = board.isEmbedPiece(piece, position);

        // then
        assertEquals(expected, result);
    }

    @Test
    void shouldThrowIAException_placePieceTest() {
        // given
        var board = new Board(2, "0100,0110,1010,1110");
        var piece = new Piece(".XX,XXX");
        var position = new Position(2, 2);
        var expected = "The piece is outside the board. Incorrect coordinates (2, 2).";

        // when
        IllegalArgumentException exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> board.placePiece(piece, position));

        // then
        assertEquals(expected, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "4, '3302012,3221112,3121312', '.X,XX,X.', 0, 0, '3002012,0321112,0121312'",
            "2, '0100,0110,1010,1110', '.XX,XXX', 0, 0, '0010,1000,1010,1110'"
    })
    void placePiece(int depth, String boardVector, String pieceVector, int pieceX, int pieceY, String expectedBoardVec) {
        // given
        var board = new Board(depth, boardVector);
        var piece = new Piece(pieceVector);
        var position = new Position(pieceX, pieceY);
        var expectedBoard = new Board(depth, expectedBoardVec);

        // when
        var result = board.placePiece(piece, position);

        // then
        assertEquals(expectedBoard, result);
    }

    @ParameterizedTest
    @CsvSource({
            "2, '00,00', 'X|X.', '3|1|3|1'",
            "2, '000,000', 'XX|.X', '1|3|3|1|3|3'",
            "3, '00', 'X|X', '3|3'"
    })
    void shouldComputeCorrectCoverageMasks_computeCoverageMasksTest(int depth, String boardVector, String pieceVectors, String expectedMasksStr) {
        // given
        var board = new Board(depth, boardVector);
        var pieces = Arrays.stream(pieceVectors.split("\\|"))
                .map(Piece::new)
                .collect(Collectors.toList());
        var expectedMasks = Arrays.stream(expectedMasksStr.split("\\|"))
                .mapToLong(Long::parseLong)
                .toArray();

        // when
        var resultMasks = board.computeCoverageMasks(pieces);

        // then
        assertArrayEquals(expectedMasks, resultMasks);
    }
}
