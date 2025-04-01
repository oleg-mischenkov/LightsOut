package com.mischenkov.lightsout.entity;

import java.util.Arrays;
import java.util.stream.Collectors;

public class Piece {
    private final int[][] pieceMatrix;

    public Piece(int[][] pieceMatrix) {
        this.pieceMatrix = pieceMatrix;
    }

    public Piece(String line) {
        this.pieceMatrix = Arrays.stream(line.split(","))
                .map(str -> str.chars()
                        .map(ch -> ch == 88 ? 1 : 0)
                        .toArray())
                .toArray(int[][]::new);
    }

    public int[][] getPieceMatrix() {
        return pieceMatrix;
    }

    @Override
    public String toString() {
        return "Piece{" +
                "pieceMatrix=\n" + Arrays.stream(pieceMatrix)
                .map(Arrays::toString).collect(Collectors.joining("\n")) +
                '}';
    }
}
