package com.mischenkov.lightsout.entity;

import java.util.Arrays;
import java.util.List;
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

    public int getDepth() {
        return depth;
    }

    public long[] computeCoverageMasks(List<Piece> pieces) {
        int boardHeight = this.boardMatrix.length;
        int boardWidth = this.boardMatrix[0].length;
        int totalPieces = pieces.size();
        long[] coverageMasks = new long[boardHeight * boardWidth];

        for (int p = 0; p < totalPieces; p++) {
            Piece piece = pieces.get(p);
            int[][] pieceMatrix = piece.getPieceMatrix();
            int h = pieceMatrix.length;
            int w = pieceMatrix[0].length;
            for (int y = 0; y <= boardHeight - h; y++) {
                for (int x = 0; x <= boardWidth - w; x++) {
                    for (int i = 0; i < h; i++) {
                        for (int j = 0; j < w; j++) {
                            if (pieceMatrix[i][j] == 1) {
                                int cellIndex = (y + i) * boardWidth + (x + j);
                                coverageMasks[cellIndex] |= (1L << p);
                            }
                        }
                    }
                }
            }
        }
        return coverageMasks;
    }

    public boolean isEmbedPiece(Piece piece, Position position) {
        int pieceWidth = piece.getPieceMatrix()[0].length;
        int pieceHeight = piece.getPieceMatrix().length;

        return position.x() + pieceWidth <= boardMatrix[0].length && position.y() + pieceHeight <= boardMatrix.length;
    }

    public Board placePiece(Piece piece, Position position) {
        if (!isEmbedPiece(piece, position)) {
            var exceptionMsg = String.format("The piece is outside the board. Incorrect coordinates (%d, %d).",
                    position.x(), position.y());
            throw new IllegalArgumentException(exceptionMsg);
        }
        
        int[][] resultMatrix = getBoardMatrix();
        int[][] pieceMatrix = piece.getPieceMatrix();
        for (int y = 0; y < pieceMatrix.length; y++) {
            for (int x = 0; x < pieceMatrix[y].length; x++) {
                if (pieceMatrix[y][x] == 1) {
                    var currentX = x + position.x();
                    var currentY = y + position.y();

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
