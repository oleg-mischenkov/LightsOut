package com.mischenkov.lightsout.entity;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

public class Board {
    private final int depth;
    private final int[][] boardMatrix;

    public Board(int depth, int[][] boardMatrix) {
        this.depth = depth;
        this.boardMatrix = boardMatrix;
    }

    public Board(int depth, String line) {
        this.depth = depth;
        this.boardMatrix = Arrays.stream(line.split(","))
                .map(str -> str.chars()
                        .map(ch -> ch - '0')
                        .toArray())
                .toArray(int[][]::new);
    }

    public int[][] getBoardMatrix() {
        int[][] result = new int[boardMatrix.length][boardMatrix[0].length];
        for (int y = 0; y < result.length; y++) {
            System.arraycopy(boardMatrix[y], 0, result[y], 0, boardMatrix[y].length);
        }
        return result;
    }

    public boolean isEmbedPiece(Piece piece, Position position) {
        int pieceWidth = piece.getPieceMatrix()[0].length;
        int pieceHeight = piece.getPieceMatrix().length;

        return position.getX() + pieceWidth <= boardMatrix[0].length && position.getY() + pieceHeight <= boardMatrix.length;
    }

    public Board placePiece(Piece piece, Position position) {
        if (!isEmbedPiece(piece, position)) {
            var exceptionMsg = String.format("The piece is outside the board. Incorrect coordinates (%d, %d).",
                    position.getX(), position.getY());
            throw new IllegalArgumentException(exceptionMsg);
        }
        
        int[][] resultMatrix = getBoardMatrix();
        int[][] pieceMatrix = piece.getPieceMatrix();
        for (int y = 0; y < pieceMatrix.length; y++) {
            for (int x = 0; x < pieceMatrix[y].length; x++) {
                if (pieceMatrix[y][x] == 1) {
                    var currentX = x + position.getX();
                    var currentY = y + position.getY();

                    resultMatrix[currentY][currentX] += 1;
                    resultMatrix[currentY][currentX] %= depth;
                }
            }
        }
        
        return new Board(depth, resultMatrix);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Board board = (Board) o;
        return depth == board.depth && Objects.deepEquals(boardMatrix, board.boardMatrix);
    }

    @Override
    public int hashCode() {
        return Objects.hash(depth, Arrays.deepHashCode(boardMatrix));
    }

    @Override
    public String toString() {
        return "Board{" +
                "depth=" + depth +
                ", boardMatrix=\n" + Arrays.stream(boardMatrix)
                .map(Arrays::toString)
                .collect(Collectors.joining("\n")) +
                '}';
    }
}
